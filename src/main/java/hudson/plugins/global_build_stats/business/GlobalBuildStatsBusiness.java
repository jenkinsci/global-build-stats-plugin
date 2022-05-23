package hudson.plugins.global_build_stats.business;

import hudson.model.TopLevelItem;
import hudson.model.Hudson;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.Run;
import hudson.plugins.global_build_stats.GlobalBuildStatsPlugin;
import hudson.plugins.global_build_stats.JobBuildResultFactory;
import hudson.plugins.global_build_stats.model.*;
import hudson.plugins.global_build_stats.model.AbstractBuildStatChartDimension.LegendItemData;
import hudson.plugins.global_build_stats.rententionstrategies.RetentionStrategy;
import hudson.plugins.global_build_stats.util.CollectionsUtil;
import hudson.util.DataSetBuilder;
import hudson.util.ShiftedCategoryAxis;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.LegendTitle;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

import com.cloudbees.hudson.plugins.folder.Folder;

public class GlobalBuildStatsBusiness {

    private static final Logger LOGGER = Logger.getLogger(GlobalBuildStatsBusiness.class.getName());

    /* package */ final GlobalBuildStatsPluginSaver pluginSaver;
    /* package */ final GlobalBuildStatsPlugin plugin;

	public GlobalBuildStatsBusiness(GlobalBuildStatsPlugin _plugin){
        this.plugin = _plugin;
        this.pluginSaver = new GlobalBuildStatsPluginSaver(_plugin);
	}

    /**
     * Records the result of a build.
     */
	public void onJobCompleted(final Run build) {
        for(RetentionStrategy s : plugin.getRetentionStrategies()){
            s.onBuildCompleted(build, pluginSaver);
        }
        this.pluginSaver.updatePlugin(new GlobalBuildStatsPluginSaver.BeforeSavePluginCallback(){
        	@Override
            public void changePluginStateBeforeSavingIt(GlobalBuildStatsPlugin plugin) {
                plugin.getJobBuildResultsSharder().queueResultToAdd(JobBuildResultFactory.INSTANCE.createJobBuildResult(build));
            }
        });
	}
	
	public BuildStatConfiguration searchBuildStatConfigById(String buildStatId){
		int index = searchBuildStatConfigIndexById(buildStatId);
		if(index != -1){
            return this.plugin.getBuildStatConfigs().get(index);
		} else {
			return null;
		}
	}
	
	private int searchBuildStatConfigIndexById(String id){
		int idx = 0;
		for(BuildStatConfiguration c : plugin.getBuildStatConfigs()){
			if(id.equals(c.getId())){
				break;
			}
			idx++;
		}
		
		if(idx == this.plugin.getBuildStatConfigs().size()){
			idx = -1;
		}
		
		return idx;
	}
	
	public void recordBuildInfos() throws IOException {

        this.pluginSaver.updatePlugin(new GlobalBuildStatsPluginSaver.BeforeSavePluginCallback(){
        	@Override
            public void changePluginStateBeforeSavingIt(GlobalBuildStatsPlugin plugin) {

                List<JobBuildResult> jobBuildResultsRead = new ArrayList<JobBuildResult>();
                for (TopLevelItem item : Hudson.getInstance().getItems()) {
                	if (item instanceof Folder){
                		Folder f = (Folder)item;
                		for (TopLevelItem i : f.getItems()){
                			handleItem(jobBuildResultsRead,i);
                		}
                	}
                    if (item instanceof Job) {
                    	handleItem(jobBuildResultsRead, item);
                    }
                }

                plugin.getJobBuildResultsSharder().queueResultsToAdd(
                        CollectionsUtil.<JobBuildResult>minus(jobBuildResultsRead, plugin.getJobBuildResults()));
            }
        });
	}
	
	public void handleItem(List<JobBuildResult> results, TopLevelItem item){
		if (item instanceof Job){
			addBuildsFrom(results, (Job)item);
		}
	}
	
	public JFreeChart createChart(BuildStatConfiguration config){
		List<AbstractBuildStatChartDimension> dimensions = createDataSetBuilder(config);
        return createChart(dimensions, config.getBuildStatTitle());
	}
	
	public List<JobBuildSearchResult> searchBuilds(BuildHistorySearchCriteria searchCriteria){
    	List<JobBuildSearchResult> filteredJobBuildResults = new ArrayList<JobBuildSearchResult>();
    	
        for(JobBuildResult r : this.plugin.getJobBuildResults()){
        	if(searchCriteria.isJobResultEligible(r)){
                filteredJobBuildResults.add(JobBuildResultFactory.INSTANCE.createJobBuildSearchResult(r));
        	}
        }
        
        // Sorting on job results dates
        Collections.sort(filteredJobBuildResults, new JobBuildResult.AntiChronologicalComparator());

        return filteredJobBuildResults;
	}

    // TODO: remove ioexception ???
	public void updateBuildStatConfiguration(final String oldBuildStatId,
                                             final BuildStatConfiguration config,
                                             final boolean regenerateId) throws IOException {

        this.pluginSaver.updatePlugin(new GlobalBuildStatsPluginSaver.BeforeSavePluginCallback(){
        	@Override
            public void changePluginStateBeforeSavingIt(GlobalBuildStatsPlugin plugin) {
                if(regenerateId){
                    String newBuildStatId = ModelIdGenerator.INSTANCE.generateIdForClass(BuildStatConfiguration.class);
                    config.setId(newBuildStatId);
                }

                int buildStatIndex = searchBuildStatConfigIndexById(oldBuildStatId);

                plugin.getBuildStatConfigs().set(buildStatIndex, config);
            }

            @Override
            public void afterPluginSaved(){
                if(regenerateId){
                    ModelIdGenerator.INSTANCE.unregisterIdForClass(BuildStatConfiguration.class, oldBuildStatId);
                }
            }
        });
	}

	public void addBuildStatConfiguration(final BuildStatConfiguration config) throws IOException {
        this.pluginSaver.updatePlugin(new GlobalBuildStatsPluginSaver.BeforeSavePluginCallback(){

            @Override
            public void changePluginStateBeforeSavingIt(GlobalBuildStatsPlugin plugin) {
                plugin.getBuildStatConfigs().add(config);
            }
        });
	}
	
	public void deleteBuildStatConfiguration(final String buildStatId) throws IOException {
        this.pluginSaver.updatePlugin(new GlobalBuildStatsPluginSaver.BeforeSavePluginCallback(){

            @Override
            public void changePluginStateBeforeSavingIt(GlobalBuildStatsPlugin plugin) {

                int index = searchBuildStatConfigIndexById(buildStatId);
                plugin.getBuildStatConfigs().remove(index);
            }
        });
	}
	
	public void moveUpConf(final String buildStatId) throws IOException {
        this.pluginSaver.updatePlugin(new GlobalBuildStatsPluginSaver.BeforeSavePluginCallback(){

            @Override
            public void changePluginStateBeforeSavingIt(GlobalBuildStatsPlugin plugin) {

                int index = searchBuildStatConfigIndexById(buildStatId);
                if(index <= 0){
                    throw new IllegalArgumentException("Can't move up first build stat configuration !");
                }

                BuildStatConfiguration b = plugin.getBuildStatConfigs().get(index);
                // Swapping build confs
                plugin.getBuildStatConfigs().set(index, plugin.getBuildStatConfigs().get(index-1));
                plugin.getBuildStatConfigs().set(index-1, b);
            }
        });
	}
	
	public void moveDownConf(final String buildStatId) throws IOException {
        this.pluginSaver.updatePlugin(new GlobalBuildStatsPluginSaver.BeforeSavePluginCallback(){

            @Override
            public void changePluginStateBeforeSavingIt(GlobalBuildStatsPlugin plugin) {

                int index = searchBuildStatConfigIndexById(buildStatId);
                if(index >= plugin.getBuildStatConfigs().size()-1){
                    throw new IllegalArgumentException("Can't move down last build stat configuration !");
                }

                BuildStatConfiguration b = plugin.getBuildStatConfigs().get(index);
                // Swapping build confs
                plugin.getBuildStatConfigs().set(index, plugin.getBuildStatConfigs().get(index+1));
                plugin.getBuildStatConfigs().set(index+1, b);
            }
        });
	}
	
	public static String escapeAntiSlashes(String value){
		if(value != null){
			return value.replaceAll("\\\\", "\\\\\\\\");
		} else {
			return null;
		}
	}
    
    private JFreeChart createChart(List<AbstractBuildStatChartDimension> dimensions, String title) {

    	final JFreeChart chart = ChartFactory.createStackedAreaChart(title, null, "", 
    			new DataSetBuilder<String, DateRange>().build(), PlotOrientation.VERTICAL, true, true, false);
        chart.setBackgroundPaint(Color.white);
        
        final LegendTitle legend = chart.getLegend();
        legend.setPosition(RectangleEdge.RIGHT);

        final CategoryPlot plot = chart.getCategoryPlot();
        
        plot.setBackgroundPaint(Color.lightGray);
        plot.setForegroundAlpha(0.85F);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.darkGray);

        CategoryAxis domainAxis = new ShiftedCategoryAxis(null);
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        domainAxis.setLowerMargin(0.0);
        domainAxis.setUpperMargin(0.0);
        domainAxis.setCategoryMargin(0.0);
        plot.setDomainAxis(domainAxis);

        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        for(int i=0; i<dimensions.size(); i++){
        	AbstractBuildStatChartDimension dimension = dimensions.get(dimensions.size()-1-i);
        	plot.setRangeAxis(i, dimension.getRangeAxis());
        	plot.setRenderer(i, dimension.getRenderer());
        	plot.setDataset(i, dimension.getDatasetBuilder().build());
        	plot.mapDatasetToRangeAxis(i,i);
        }
        
        //plot.setFixedLegendItems(sortLegendItems(plot.getLegendItems()));
        plot.setInsets(new RectangleInsets(5.0, 0, 0, 5.0));

        return chart;
    }
    
    // Useless... for the moment...
    private static LegendItemCollection sortLegendItems(LegendItemCollection legendItems){
    	LegendItemCollection sortedLegendItems = new LegendItemCollection();

    	List<LegendItemData> sortedLegendItemsLabels = AbstractBuildStatChartDimension.getSortedLegendItemsLabels();
    	for(LegendItemData legendItemData : sortedLegendItemsLabels){
    		// Looking for item legend label matching with current label
    		Iterator<LegendItem> legendItemsIter = legendItems.iterator();
    		LegendItem legendItemMatchingCurrentLabel = null;
    		while(legendItemMatchingCurrentLabel == null && legendItemsIter.hasNext()){
    			LegendItem currentLegendItem = legendItemsIter.next();
    			if(legendItemData.label.equals(currentLegendItem.getLabel())){
    				legendItemMatchingCurrentLabel = new LegendItem(legendItemData.label, currentLegendItem.getDescription(), 
    						currentLegendItem.getToolTipText(), "", new Rectangle2D.Double(-4.0, -4.0, 8.0, 8.0), legendItemData.color); 
    			}
    		}
    		
    		if(legendItemMatchingCurrentLabel != null){
    			sortedLegendItems.add(legendItemMatchingCurrentLabel);
    		}
    	}
    	
    	return sortedLegendItems;
    }

    public void updateRetentionStrategies(final List<RetentionStrategy> selectedStrategies) {
        this.pluginSaver.updatePlugin(new GlobalBuildStatsPluginSaver.BeforeSavePluginCallback(){
            @Override
            public void changePluginStateBeforeSavingIt(GlobalBuildStatsPlugin plugin) {
                plugin.setRetentionStrategies(selectedStrategies);
                for(RetentionStrategy s : selectedStrategies){
                    s.strategyActivated(pluginSaver);
                }
            }
        });
    }

    public List<AbstractBuildStatChartDimension> createDataSetBuilder(BuildStatConfiguration config) {
    	List<AbstractBuildStatChartDimension> dimensions = new ArrayList<AbstractBuildStatChartDimension>();
    	for(YAxisChartDimension dimensionShown : config.getDimensionsShown()){
    		dimensions.add(dimensionShown.createBuildStatChartDimension(config, new DataSetBuilder<String, DateRange>()));
    	}
    	
    	List<JobBuildResult> sortedJobResults = new ArrayList<JobBuildResult>(this.plugin.getJobBuildResults());
        Collections.sort(sortedJobResults, new JobBuildResult.AntiChronologicalComparator());

		Calendar d2 = new GregorianCalendar();
		Calendar d1 = config.getHistoricScale().getPreviousStep(d2);
		
		int tickCount = 0;
		Iterator<JobBuildResult> buildsIter = sortedJobResults.iterator();
        JobBuildResult currentBuild = null;
        Calendar buildDate = new GregorianCalendar(); buildDate.setTimeInMillis(1);
        if(buildsIter.hasNext()){
            currentBuild = buildsIter.next();
            buildDate = currentBuild.getBuildDate();
        }
		while(tickCount != config.getHistoricLength()){
	    	// Finding range where the build resides
	    	while(tickCount < config.getHistoricLength() && d1.after(buildDate)){
	    		DateRange range = new DateRange(d1, d2, config.getHistoricScale().getDateRangeFormatter());
	    		for(AbstractBuildStatChartDimension dimension : dimensions){
	    			dimension.provideDataInDataSet(range);
	    		}
	    		
				d2 = (Calendar)d1.clone();
				d1 = config.getHistoricScale().getPreviousStep(d2);
				tickCount++;
	    	}
	    	
	    	// If no range found : stop the iteration !
	    	if(tickCount != config.getHistoricLength() && currentBuild != null){
	    		if(config.getBuildFilters().isJobResultEligible(currentBuild)){
		    		for(AbstractBuildStatChartDimension dimension : dimensions){
		    			dimension.saveDataForBuild(currentBuild);
		    		}
	    		}
	    		
	    		if(buildsIter.hasNext()){
	    			currentBuild = buildsIter.next();
	    			buildDate = currentBuild.getBuildDate();
	    		} else {
	    			currentBuild = null;
	    			buildDate = new GregorianCalendar(); buildDate.setTimeInMillis(1);
	    		}
	    	}
		}
		
	    return dimensions;
	}
	
	private static void addBuild(List<JobBuildResult> jobBuildResultsRead, Run build){
		jobBuildResultsRead.add(JobBuildResultFactory.INSTANCE.createJobBuildResult(build));
	}
	
	private static void addBuildsFrom(List<JobBuildResult> jobBuildResultsRead, Job project){
        List<Run> builds = (List<Run>)project.getBuilds();
        Iterator<Run> buildIterator = builds.iterator();

        while (buildIterator.hasNext()) {
        	addBuild(jobBuildResultsRead, buildIterator.next());
        }
	}
	
	protected static List<JobBuildResult> mergeJobBuildResults(List<JobBuildResult> existingJobResults, List<JobBuildResult> jobResultsToMerge){
		List<JobBuildResult> mergedJobResultsList = new ArrayList<JobBuildResult>(existingJobResults);
		
		for(JobBuildResult jbrToMerge : jobResultsToMerge){
			if(!mergedJobResultsList.contains(jbrToMerge)){
				mergedJobResultsList.add(jbrToMerge);
			}
		}
		
		return mergedJobResultsList;
	}

    public void reloadPlugin() {
        this.pluginSaver.reloadPlugin();

        // If job results are empty, let's perform an initialization !
        if(this.plugin.getJobBuildResults()==null || this.plugin.getJobBuildResults().isEmpty()){
            try {
                this.recordBuildInfos();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    public void onBuildDeleted(Run build) {
        for(RetentionStrategy s : plugin.getRetentionStrategies()){
            s.onBuildDeleted(build, pluginSaver);
        }
    }
}

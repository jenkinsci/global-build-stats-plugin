package hudson.plugins.global_build_stats.business;

import com.google.common.collect.Lists;
import hudson.model.TopLevelItem;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.model.Job;
import hudson.plugins.global_build_stats.GlobalBuildStatsPlugin;
import hudson.plugins.global_build_stats.JobBuildResultFactory;
import hudson.plugins.global_build_stats.model.AbstractBuildStatChartDimension;
import hudson.plugins.global_build_stats.model.AbstractBuildStatChartDimension.LegendItemData;
import hudson.plugins.global_build_stats.model.BuildHistorySearchCriteria;
import hudson.plugins.global_build_stats.model.BuildStatConfiguration;
import hudson.plugins.global_build_stats.model.DateRange;
import hudson.plugins.global_build_stats.model.JobBuildResult;
import hudson.plugins.global_build_stats.model.JobBuildSearchResult;
import hudson.plugins.global_build_stats.model.ModelIdGenerator;
import hudson.plugins.global_build_stats.model.YAxisChartDimension;
import hudson.plugins.global_build_stats.util.CollectionsUtil;
import hudson.util.DaemonThreadFactory;
import hudson.util.DataSetBuilder;
import hudson.util.ShiftedCategoryAxis;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

public class GlobalBuildStatsBusiness {

    /* package */ final GlobalBuildStatsPluginSaver pluginSaver;

	public GlobalBuildStatsBusiness(GlobalBuildStatsPlugin _plugin){
        this.pluginSaver = new GlobalBuildStatsPluginSaver(_plugin);
	}

    /**
     * Records the result of a build.
     */
	public void onJobCompleted(final AbstractBuild build) {
        this.pluginSaver.updatePlugin(new GlobalBuildStatsPluginSaver.BeforeSavePluginCallback(){
            public void changePluginStateBeforeSavingIt(List<JobBuildResult> resultsToAdd,
                                                        List<JobBuildResult> resultsToRemove,
                                                        List<BuildStatConfiguration> buildStatConfigs) {

                resultsToAdd.add(JobBuildResultFactory.INSTANCE.createJobBuildResult(build));
            }
        });
	}
	
	public BuildStatConfiguration searchBuildStatConfigById(String buildStatId){
		int index = searchBuildStatConfigIndexById(buildStatId);
		if(index != -1){
            return this.pluginSaver.getBuildStatConfigs().get(index);
		} else {
			return null;
		}
	}
	
	private int searchBuildStatConfigIndexById(String id){
		int idx = 0;
		for(BuildStatConfiguration c : pluginSaver.getBuildStatConfigs()){
			if(id.equals(c.getId())){
				break;
			}
			idx++;
		}
		
		if(idx == this.pluginSaver.getBuildStatConfigs().size()){
			idx = -1;
		}
		
		return idx;
	}
	
	public void recordBuildInfos() throws IOException {

        this.pluginSaver.updatePlugin(new GlobalBuildStatsPluginSaver.BeforeSavePluginCallback(){
            public void changePluginStateBeforeSavingIt(List<JobBuildResult> resultsToAdd,
                                                        List<JobBuildResult> resultsToRemove,
                                                        List<BuildStatConfiguration> buildStatConfigs) {

                List<JobBuildResult> jobBuildResultsRead = new ArrayList<JobBuildResult>();

                //TODO fix MatrixProject and use getAllJobs()
                for (TopLevelItem item : Hudson.getInstance().getItems()) {
                    if (item instanceof AbstractProject) {
                        addBuildsFrom(jobBuildResultsRead, (AbstractProject) item);
                    }
                }

                resultsToAdd = CollectionsUtil.<JobBuildResult>minus(jobBuildResultsRead, pluginSaver.getJobBuildResults());
            }
        });
	}
	
	public JFreeChart createChart(BuildStatConfiguration config){
		List<AbstractBuildStatChartDimension> dimensions = createDataSetBuilder(config);
        return createChart(config, dimensions, config.getBuildStatTitle());
	}
	
	public List<JobBuildSearchResult> searchBuilds(BuildHistorySearchCriteria searchCriteria){
    	List<JobBuildSearchResult> filteredJobBuildResults = new ArrayList<JobBuildSearchResult>();
    	
        for(JobBuildResult r : this.pluginSaver.getJobBuildResults()){
        	if(searchCriteria.isJobResultEligible(r)){
        		boolean isJobAccessible = false;
        		boolean isBuildAccessible = false;
        		
        		Job targetJob = ((Job) Hudson.getInstance().getItem(r.getJobName()));
        		// Link to job will be provided only if job has not been deleted/renamed
        		if(targetJob != null){
        			isJobAccessible = true;
        			if(targetJob.getBuildByNumber(r.getBuildNumber()) != null){
        				// Link to build infos will be provided only if build result has not been purged
        				// @see issue #7240
        				isBuildAccessible = true;
        			}
        		}
        		
        		filteredJobBuildResults.add(new JobBuildSearchResult(r, isJobAccessible, isBuildAccessible));
        	}
        }
        
        // Sorting on job results dates
        sortJobBuildResultsByBuildDate(filteredJobBuildResults);
        
        return filteredJobBuildResults;
	}

    // TODO: remove ioexception ???
	public void updateBuildStatConfiguration(final String oldBuildStatId,
                                             final BuildStatConfiguration config,
                                             final boolean regenerateId) throws IOException {

        this.pluginSaver.updatePlugin(new GlobalBuildStatsPluginSaver.BeforeSavePluginCallback(){
            public void changePluginStateBeforeSavingIt(List<JobBuildResult> resultsToAdd,
                                                        List<JobBuildResult> resultsToRemove,
                                                        List<BuildStatConfiguration> buildStatConfigs) {

                if(regenerateId){
                    String newBuildStatId = ModelIdGenerator.INSTANCE.generateIdForClass(BuildStatConfiguration.class);
                    config.setId(newBuildStatId);
                }

                int buildStatIndex = searchBuildStatConfigIndexById(oldBuildStatId);

                buildStatConfigs.addAll(pluginSaver.getBuildStatConfigs());
                buildStatConfigs.set(buildStatIndex, config);
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
            public void changePluginStateBeforeSavingIt(List<JobBuildResult> resultsToAdd,
                                                        List<JobBuildResult> resultsToRemove,
                                                        List<BuildStatConfiguration> buildStatConfigs) {
                buildStatConfigs.add(config);
            }
        });
	}
	
	public void deleteBuildStatConfiguration(final String buildStatId) throws IOException {
        this.pluginSaver.updatePlugin(new GlobalBuildStatsPluginSaver.BeforeSavePluginCallback(){

            @Override
            public void changePluginStateBeforeSavingIt(List<JobBuildResult> resultsToAdd,
                                                        List<JobBuildResult> resultsToRemove,
                                                        List<BuildStatConfiguration> buildStatConfigs) {

                int index = searchBuildStatConfigIndexById(buildStatId);
                buildStatConfigs.remove(index);
            }
        });
	}
	
	public void moveUpConf(final String buildStatId) throws IOException {
        this.pluginSaver.updatePlugin(new GlobalBuildStatsPluginSaver.BeforeSavePluginCallback(){

            @Override
            public void changePluginStateBeforeSavingIt(List<JobBuildResult> resultsToAdd,
                                                        List<JobBuildResult> resultsToRemove,
                                                        List<BuildStatConfiguration> buildStatConfigs) {

                int index = searchBuildStatConfigIndexById(buildStatId);
                if(index <= 0){
                    throw new IllegalArgumentException("Can't move up first build stat configuration !");
                }

                BuildStatConfiguration b = buildStatConfigs.get(index);
                // Swapping build confs
                buildStatConfigs.set(index, buildStatConfigs.get(index-1));
                buildStatConfigs.set(index-1, b);
            }
        });
	}
	
	public void moveDownConf(final String buildStatId) throws IOException {
        this.pluginSaver.updatePlugin(new GlobalBuildStatsPluginSaver.BeforeSavePluginCallback(){

            @Override
            public void changePluginStateBeforeSavingIt(List<JobBuildResult> resultsToAdd,
                                                        List<JobBuildResult> resultsToRemove,
                                                        List<BuildStatConfiguration> buildStatConfigs) {

                int index = searchBuildStatConfigIndexById(buildStatId);
                if(index >= buildStatConfigs.size()-1){
                    throw new IllegalArgumentException("Can't move down last build stat configuration !");
                }

                BuildStatConfiguration b = buildStatConfigs.get(index);
                // Swapping build confs
                buildStatConfigs.set(index, buildStatConfigs.get(index+1));
                buildStatConfigs.set(index+1, b);
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
    
    private JFreeChart createChart(final BuildStatConfiguration config, List<AbstractBuildStatChartDimension> dimensions, String title) {

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
    
    public List<AbstractBuildStatChartDimension> createDataSetBuilder(BuildStatConfiguration config) {
    	List<AbstractBuildStatChartDimension> dimensions = new ArrayList<AbstractBuildStatChartDimension>();
    	for(YAxisChartDimension dimensionShown : config.getDimensionsShown()){
    		dimensions.add(dimensionShown.createBuildStatChartDimension(config, new DataSetBuilder<String, DateRange>()));
    	}
    	
    	List<JobBuildResult> sortedJobResults = new ArrayList<JobBuildResult>(this.pluginSaver.getJobBuildResults());
    	sortJobBuildResultsByBuildDate(sortedJobResults);
	    
		Calendar d2 = new GregorianCalendar();
		Calendar d1 = config.getHistoricScale().getPreviousStep(d2);
		
		int tickCount = 0;
		Iterator<JobBuildResult> buildsIter = sortedJobResults.iterator();
		JobBuildResult currentBuild = buildsIter.next();
		Calendar buildDate = currentBuild.getBuildDate();
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
	
    private static void sortJobBuildResultsByBuildDate(List<? extends JobBuildResult> c){
        Collections.sort(c, Collections.reverseOrder(new Comparator<JobBuildResult>() {
        	public int compare(JobBuildResult o1, JobBuildResult o2) {
        		return o1.getBuildDate().compareTo(o2.getBuildDate());
        	}
		}));
    }
    
	private static void addBuild(List<JobBuildResult> jobBuildResultsRead, AbstractBuild build){
		jobBuildResultsRead.add(JobBuildResultFactory.INSTANCE.createJobBuildResult(build));
	}
	
	private static void addBuildsFrom(List<JobBuildResult> jobBuildResultsRead, AbstractProject project){
        List<AbstractBuild> builds = project.getBuilds();
        Iterator<AbstractBuild> buildIterator = builds.iterator();

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

    private static final Logger LOGGER = Logger.getLogger(GlobalBuildStatsBusiness.class.getName());

    public void synchronizePluginSaver() {
        this.pluginSaver.synchronizeWithPlugin();
    }
}

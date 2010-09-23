package hudson.plugins.global_build_stats.business;

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
import hudson.util.DataSetBuilder;
import hudson.util.ShiftedCategoryAxis;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

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
	
	private GlobalBuildStatsPlugin plugin;
	
	public GlobalBuildStatsBusiness(GlobalBuildStatsPlugin _plugin){
		this.plugin = _plugin;
	}

	public void onJobCompleted(AbstractBuild job){
		// Synchronizing plugin instance every time we modify persisted informations on it
		synchronized (plugin) {
    		GlobalBuildStatsBusiness.addBuild(plugin.getJobBuildResults(), job);
    		
    		try {
				plugin.save();
			} catch (IOException e) {
				
			}
		}
	}
	
	public BuildStatConfiguration searchBuildStatConfigById(String buildStatId){
		int index = searchBuildStatConfigIndexById(buildStatId);
		if(index != -1){
			return plugin.getBuildStatConfigs().get(index);
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
		
		if(idx == plugin.getBuildStatConfigs().size()){
			idx = -1;
		}
		
		return idx;
	}
	
	public void recordBuildInfos() throws IOException {
        List<JobBuildResult> jobBuildResultsRead = new ArrayList<JobBuildResult>();
        
		// Synchronizing plugin instance every time we modify persisted informations on it
        synchronized (plugin) {
            //TODO fix MatrixProject and use getAllJobs()
            for (TopLevelItem item : Hudson.getInstance().getItems()) {
                if (item instanceof AbstractProject) {
                	addBuildsFrom(jobBuildResultsRead, (AbstractProject) item);
                }
            }
            
            plugin.setJobBuildResults(mergeJobBuildResults(plugin.getJobBuildResults(), jobBuildResultsRead));

        	plugin.save();
		}
	}
	
	public JFreeChart createChart(BuildStatConfiguration config){
		List<AbstractBuildStatChartDimension> dimensions = createDataSetBuilder(config);
        return createChart(config, dimensions, config.getBuildStatTitle());
	}
	
	public List<JobBuildSearchResult> searchBuilds(BuildHistorySearchCriteria searchCriteria){
    	List<JobBuildSearchResult> filteredJobBuildResults = new ArrayList<JobBuildSearchResult>();
    	
        for(JobBuildResult r : plugin.getJobBuildResults()){
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

	public void updateBuildStatConfiguration(String oldBuildStatId, BuildStatConfiguration config, boolean regenerateId) throws IOException {
		// Synchronizing plugin instance every time we modify persisted informations on it
    	synchronized(plugin){
        	if(regenerateId){
        		String newBuildStatId = ModelIdGenerator.INSTANCE.generateIdForClass(BuildStatConfiguration.class);
        		config.setId(newBuildStatId);
        	}
        	
    		int buildStatIndex = searchBuildStatConfigIndexById(oldBuildStatId);
	    	plugin.getBuildStatConfigs().set(buildStatIndex, config);
	    	plugin.save();
	    	
	    	if(regenerateId){
	    		ModelIdGenerator.INSTANCE.unregisterIdForClass(BuildStatConfiguration.class, oldBuildStatId);
	    	}
    	}
	}
	
	public void addBuildStatConfiguration(BuildStatConfiguration config) throws IOException {
		// Synchronizing plugin instance every time we modify persisted informations on it
    	synchronized(plugin){
	    	plugin.getBuildStatConfigs().add(config);
	    	plugin.save();
    	}
	}
	
	public void deleteBuildStatConfiguration(String buildStatId) throws IOException {
    	synchronized(plugin){
    		int index = searchBuildStatConfigIndexById(buildStatId);
    		plugin.getBuildStatConfigs().remove(index);
    		plugin.save();
    	}
	}
	
	public void moveUpConf(String buildStatId) throws IOException {
		// Synchronizing plugin instance every time we modify persisted informations on it
		int index = searchBuildStatConfigIndexById(buildStatId);
		if(index <= 0){
			throw new IllegalArgumentException("Can't move up first build stat configuration !");
		}
    	synchronized(plugin){
	    	BuildStatConfiguration b = plugin.getBuildStatConfigs().get(index);
	    	// Swapping build confs
	    	plugin.getBuildStatConfigs().set(index, plugin.getBuildStatConfigs().get(index-1));
	    	plugin.getBuildStatConfigs().set(index-1, b);
	    	plugin.save();
    	}
	}
	
	public void moveDownConf(String buildStatId) throws IOException {
		// Synchronizing plugin instance every time we modify persisted informations on it
		int index = searchBuildStatConfigIndexById(buildStatId);
		if(index >= plugin.getBuildStatConfigs().size()-1){
			throw new IllegalArgumentException("Can't move down last build stat configuration !");
		}
    	synchronized(plugin){
	    	BuildStatConfiguration b = plugin.getBuildStatConfigs().get(index);
	    	// Swapping build confs
	    	plugin.getBuildStatConfigs().set(index, plugin.getBuildStatConfigs().get(index+1));
	    	plugin.getBuildStatConfigs().set(index+1, b);
	    	plugin.save();
    	}
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
    	
    	List<JobBuildResult> sortedJobResults = new ArrayList<JobBuildResult>(plugin.getJobBuildResults());
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
}

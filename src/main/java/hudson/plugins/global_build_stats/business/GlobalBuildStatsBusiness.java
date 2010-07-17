package hudson.plugins.global_build_stats.business;

import hudson.model.TopLevelItem;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.plugins.global_build_stats.BuildResultStatusesConstants;
import hudson.plugins.global_build_stats.JobBuildResultFactory;
import hudson.plugins.global_build_stats.JobFilter;
import hudson.plugins.global_build_stats.JobFilterFactory;
import hudson.plugins.global_build_stats.GlobalBuildStatsPlugin;
import hudson.plugins.global_build_stats.model.BuildHistorySearchCriteria;
import hudson.plugins.global_build_stats.model.BuildResult;
import hudson.plugins.global_build_stats.model.BuildStatConfiguration;
import hudson.plugins.global_build_stats.model.DateRange;
import hudson.plugins.global_build_stats.model.JobBuildResult;
import hudson.util.DataSetBuilder;
import hudson.util.ShiftedCategoryAxis;
import hudson.util.StackedAreaRenderer2;

import java.awt.Color;
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
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.CategoryDataset;
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
	
	private int searchBuildStatConfigIndexById(String id){
		int idx = 0;
		for(BuildStatConfiguration c : plugin.getBuildStatConfigs()){
			if(id.equals(c.getId())){
				break;
			}
			idx++;
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
    	List<JobBuildResult> filteredJobBuildResults = createFilteredAndSortedBuildResults(config);
        DataSetBuilder<String, DateRange> dsb = createDataSetBuilder(filteredJobBuildResults, config);
    	
        return createChart(config, dsb.build(), config.getBuildStatTitle());
	}
	
	public List<JobBuildResult> searchBuilds(BuildHistorySearchCriteria searchCriteria){
    	List<JobBuildResult> filteredJobBuildResults = new ArrayList<JobBuildResult>();
    	
    	JobFilter jobFilter = JobFilterFactory.createJobFilter(searchCriteria.jobFilter);
        for(JobBuildResult r : plugin.getJobBuildResults()){
        	if(r.getBuildDate().getTimeInMillis() >= searchCriteria.start
        			&& r.getBuildDate().getTimeInMillis() < searchCriteria.end
        			&& jobResultStatusMatchesWith(r.getResult(), searchCriteria)
        			&& jobFilter.isJobApplicable(r.getJobName())){
        		filteredJobBuildResults.add(r);
        	}
        }
        
        // Sorting on job results dates
        sortJobBuildResultsByBuildDate(filteredJobBuildResults);
        
        return filteredJobBuildResults;
	}

	public void updateBuildStatConfiguration(String buildStatId, BuildStatConfiguration config) throws IOException {
		// Synchronizing plugin instance every time we modify persisted informations on it
    	synchronized(plugin){
    		int buildStatIndex = searchBuildStatConfigIndexById(buildStatId);
	    	plugin.getBuildStatConfigs().set(buildStatIndex, config);
	    	plugin.save();
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
    	synchronized(plugin){
    		int index = searchBuildStatConfigIndexById(buildStatId);
	    	BuildStatConfiguration b = plugin.getBuildStatConfigs().get(index);
	    	// Swapping build confs
	    	plugin.getBuildStatConfigs().set(index, plugin.getBuildStatConfigs().get(index-1));
	    	plugin.getBuildStatConfigs().set(index-1, b);
	    	plugin.save();
    	}
	}
	
	public void moveDownConf(String buildStatId) throws IOException {
		// Synchronizing plugin instance every time we modify persisted informations on it
    	synchronized(plugin){
    		int index = searchBuildStatConfigIndexById(buildStatId);
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
    
    protected static boolean jobResultStatusMatchesWith(BuildResult r, BuildHistorySearchCriteria c){
    	return (BuildResult.ABORTED.equals(r) && c.abortedShown)
					|| (BuildResult.FAILURE.equals(r) && c.failuresShown)
					|| (BuildResult.NOT_BUILD.equals(r) && c.notBuildShown)
					|| (BuildResult.SUCCESS.equals(r) && c.successShown)
					|| (BuildResult.UNSTABLE.equals(r) && c.unstablesShown);
    }
    
    private JFreeChart createChart(final BuildStatConfiguration config, CategoryDataset dataset, String title) {

    	final JFreeChart chart = ChartFactory.createStackedAreaChart(title, null, "Count", dataset, PlotOrientation.VERTICAL, true, true, false);
        chart.setBackgroundPaint(Color.white);
        
        final LegendTitle legend = chart.getLegend();
        legend.setPosition(RectangleEdge.RIGHT);

        final CategoryPlot plot = chart.getCategoryPlot();
        
        plot.setForegroundAlpha(0.85F);
        plot.setRangeGridlinesVisible(true);

        CategoryAxis domainAxis = new ShiftedCategoryAxis(null);
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        domainAxis.setLowerMargin(0.0);
        domainAxis.setUpperMargin(0.0);
        domainAxis.setCategoryMargin(0.0);
        plot.setDomainAxis(domainAxis);

        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        // This renderer allows to map area for clicks
        // + it fixes some rendering bug (0 is displayed on "demi" tick instead of "plain" tick)
        final StackedAreaRenderer2 renderer = new StackedAreaRenderer2(){
            @Override
            public String generateURL(CategoryDataset dataset, int row, int column) {
                DateRange range = (DateRange) dataset.getColumnKey(column);
                String status = (String) dataset.getRowKey(row);
                
                boolean successShown=BuildResultStatusesConstants.SUCCESS.equals(status);
                boolean failuresShown=BuildResultStatusesConstants.FAILURES.equals(status);
                boolean unstablesShown=BuildResultStatusesConstants.UNSTABLES.equals(status);
                boolean abortedShown=BuildResultStatusesConstants.ABORTED.equals(status);
                boolean notBuildShown=BuildResultStatusesConstants.NOT_BUILD.equals(status);
                
                return new StringBuilder()
                	.append("buildHistory?jobFilter=").append(config.getJobFilter())
                	.append("&start=").append(range.getStart().getTimeInMillis())
                	.append("&end=").append(range.getEnd().getTimeInMillis())
                	.append("&successShown=").append(successShown)
                	.append("&failuresShown=").append(failuresShown)
                	.append("&unstablesShown=").append(unstablesShown)
                	.append("&abortedShown=").append(abortedShown)
                	.append("&notBuildShown=").append(notBuildShown).toString();
            }

/*          TODO: add tooltip  
			@Override
            public String generateToolTip(CategoryDataset dataset, int row, int column) {
                NumberOnlyBuildLabel label = (NumberOnlyBuildLabel) dataset.getColumnKey(column);
                AbstractTestResultAction a = label.build.getAction(AbstractTestResultAction.class);
                switch (row) {
                    case 0:
                        return String.valueOf(Messages.AbstractTestResultAction_fail(a.getFailCount()));
                    case 1:
                        return String.valueOf(Messages.AbstractTestResultAction_skip(a.getSkipCount()));
                    default:
                        return String.valueOf(Messages.AbstractTestResultAction_test(a.getTotalCount()));
                }
            }*/
        };
        plot.setRenderer(renderer);
        renderer.setSeriesPaint(0, new Color(255, 255, 85));
        renderer.setSeriesPaint(1, new Color(255, 85, 85));
        renderer.setSeriesPaint(2, new Color(85, 85, 85));
        renderer.setSeriesPaint(3, new Color(85, 85, 255));
        renderer.setSeriesPaint(4, new Color(255, 85, 255));

        plot.setRenderer(renderer);
        plot.setInsets(new RectangleInsets(5.0, 0, 0, 5.0));

        return chart;
    }
    
    private DataSetBuilder<String, DateRange> createDataSetBuilder(List<JobBuildResult> filteredJobBuildResults, 
																	BuildStatConfiguration config){
	    DataSetBuilder<String, DateRange> dsb = new DataSetBuilder<String, DateRange>();
	    
	    if(filteredJobBuildResults.size() == 0){
	    	return dsb;
	    }
	    
		Calendar d2 = new GregorianCalendar();
		Calendar d1 = config.getHistoricScale().getPreviousStep(d2);
		
		int nbSuccess=0, nbFailures=0, nbUnstables=0, nbAborted=0, nbNotBuild=0;
		int nbSteps = 0;
		Iterator<JobBuildResult> buildsIter = filteredJobBuildResults.iterator();
		JobBuildResult currentBuild = buildsIter.next();
		Calendar buildDate = currentBuild.getBuildDate();
		while(nbSteps != config.getHistoricLength()){
	    	// Finding range where the build resides
	    	while(nbSteps < config.getHistoricLength() && d1.after(buildDate)){
	    		DateRange range = new DateRange(d1, d2, config.getHistoricScale().getDateRangeFormatter());
	    		dsb.add(nbSuccess, BuildResultStatusesConstants.SUCCESS, range);
				dsb.add(nbFailures, BuildResultStatusesConstants.FAILURES, range);
				dsb.add(nbUnstables, BuildResultStatusesConstants.UNSTABLES, range);
				dsb.add(nbAborted, BuildResultStatusesConstants.ABORTED, range);
				dsb.add(nbNotBuild, BuildResultStatusesConstants.NOT_BUILD, range);
	    		
				d2 = (Calendar)d1.clone();
				d1 = config.getHistoricScale().getPreviousStep(d2);
				nbSuccess=0; nbFailures=0; nbUnstables=0; nbAborted=0; nbNotBuild=0;
				nbSteps++;
	    	}
	    	
	    	// If no range found : stop the iteration !
	    	if(nbSteps != config.getHistoricLength() && currentBuild != null){
	    		nbSuccess += config.isSuccessShown()?currentBuild.getResult().getSuccessCount():0;
	    		nbFailures += config.isFailuresShown()?currentBuild.getResult().getFailureCount():0;
	    		nbUnstables += config.isUnstablesShown()?currentBuild.getResult().getUnstableCount():0;
	    		nbAborted += config.isAbortedShown()?currentBuild.getResult().getAbortedCount():0;
	    		nbNotBuild += config.isNotBuildShown()?currentBuild.getResult().getNotBuildCount():0;
	    		
	    		if(buildsIter.hasNext()){
	    			currentBuild = buildsIter.next();
	    			buildDate = currentBuild.getBuildDate();
	    		} else {
	    			currentBuild = null;
	    			buildDate = new GregorianCalendar(); buildDate.setTimeInMillis(1);
	    		}
	    	}
		}
		
	    return dsb;
	}
	
    private static void sortJobBuildResultsByBuildDate(List<JobBuildResult> c){
        Collections.sort(c, Collections.reverseOrder(new Comparator<JobBuildResult>() {
        	public int compare(JobBuildResult o1, JobBuildResult o2) {
        		return o1.getBuildDate().compareTo(o2.getBuildDate());
        	}
		}));
    }
    
    private List<JobBuildResult> createFilteredAndSortedBuildResults(BuildStatConfiguration config){
    	List<JobBuildResult> filteredJobBuildResults = new ArrayList<JobBuildResult>();
        for(JobBuildResult r : plugin.getJobBuildResults()){
        	if(JobFilterFactory.createJobFilter(config.getJobFilter()).isJobApplicable(r.getJobName())){
        		filteredJobBuildResults.add(r);
        	}
        }
        
        // Sorting on job results dates
        sortJobBuildResultsByBuildDate(filteredJobBuildResults);
        
        return filteredJobBuildResults;
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

package hudson.plugins.global_build_stats;

import hudson.Extension;
import hudson.Plugin;
import hudson.model.ManagementLink;
import hudson.model.TaskListener;
import hudson.model.TopLevelItem;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.model.listeners.RunListener;
import hudson.plugins.global_build_stats.model.BuildHistorySearchCriteria;
import hudson.plugins.global_build_stats.model.BuildResult;
import hudson.plugins.global_build_stats.model.BuildStatConfiguration;
import hudson.plugins.global_build_stats.model.DateRange;
import hudson.plugins.global_build_stats.model.HistoricScale;
import hudson.plugins.global_build_stats.model.JobBuildResult;
import hudson.security.Permission;
import hudson.util.ChartUtil;
import hudson.util.DataSetBuilder;
import hudson.util.FormValidation;
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

import javax.servlet.ServletException;

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
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Entry point of the global build stats plugin
 * 
 * @author fcamblor
 * @plugin
 */
public class GlobalBuildStatsPlugin extends Plugin {

	private List<JobBuildResult> jobBuildResults = new ArrayList<JobBuildResult>();
	private List<BuildStatConfiguration> buildStatConfigs = new ArrayList<BuildStatConfiguration>();

	@Override
	public void postInitialize() throws Exception {
		super.postInitialize();
		
		// Reload plugin informations
		this.load();
	}
	
	/**
	 * Let's add a link in the administration panel linking to the global build stats page
	 */
    @Extension
    public static class GlobalBuildStatsManagementLink extends ManagementLink {

        public String getIconFileName() {
            return "/plugin/global-build-stats/icons/global-build-stats.png";
        }

        public String getDisplayName() {
            return "Global Builds Stats";
        }

        public String getUrlName() {
            return "plugin/global-build-stats/";
        }
        
        @Override public String getDescription() {
            return "Displays stats about daily build results";
        }
    }
    
    /**
     * At the end of every jobs, let's gather job result informations into global build stats
     * persisted data
     */
    @Extension
    public static class GlobalBuildStatsRunListener extends RunListener<AbstractBuild>{
    	public GlobalBuildStatsRunListener() {
    		super(AbstractBuild.class);
		}
    	
    	@Override
    	public void onCompleted(AbstractBuild r, TaskListener listener) {
    		super.onCompleted(r, listener);
    		
    		GlobalBuildStatsPlugin plugin = Hudson.getInstance().getPlugin(GlobalBuildStatsPlugin.class);
    		synchronized (plugin) {
        		GlobalBuildStatsPlugin.addBuild(plugin.jobBuildResults, r);
        		
        		try {
    				plugin.save();
    			} catch (IOException e) {
    				
    			}
			}
    	}
    }
    
    public FormValidation doCheckJobFilter(@QueryParameter String value){
    	try{ JobFilterFactory.createJobFilter(value); return FormValidation.ok(); }
    	catch(Throwable t){ return FormValidation.error("JobFilter is invalid"); }
    }
    
    public FormValidation doCheckFailuresShown(@QueryParameter String value){
    	if(!isBool(value)){ return FormValidation.error("FailuresShown must be a boolean"); }
    	else { return FormValidation.ok(); }
    }
    
    public FormValidation doCheckUnstablesShown(@QueryParameter String value){
    	if(!isBool(value)){ return FormValidation.error("UnstablesShown must be a boolean"); }
    	else { return FormValidation.ok(); }
    }
    
    public FormValidation doCheckAbortedShown(@QueryParameter String value){
    	if(!isBool(value)){ return FormValidation.error("AbortedShown must be a boolean"); }
    	else { return FormValidation.ok(); }
    }
    
    public FormValidation doCheckNotBuildsShown(@QueryParameter String value){
    	if(!isBool(value)){ return FormValidation.error("NotBuildsShown must be a boolean"); }
    	else { return FormValidation.ok(); }
    }
    
    public FormValidation doCheckSuccessShown(@QueryParameter String value){
    	if(!isBool(value)){ return FormValidation.error("SuccessShown must be a boolean"); }
    	else { return FormValidation.ok(); }
    }
    
	public FormValidation doCheckHistoricScale(@QueryParameter String value){
    	if(!isMandatory(value)){ return FormValidation.error("Historic scale is mandatory"); }
    	else {
    		try{ HistoricScale.valueOf(value); return FormValidation.ok(); }
    		catch(Throwable t){ return FormValidation.error("HistoricScale is invalid"); }
    	}
    }

	public FormValidation doCheckHistoricLength(@QueryParameter String value){
    	if(!isMandatory(value)){ return FormValidation.error("Historic length is mandatory"); }
    	else if(!isInt(value)){ return FormValidation.error("Historic length should be an integer"); }
    	else { return FormValidation.ok(); }
    }

    public FormValidation doCheckBuildStatHeight(@QueryParameter String value){
    	if(!isMandatory(value)){ return FormValidation.error("Build stats height is mandatory"); }
    	else if(!isInt(value)){ return FormValidation.error("Build stats height should be an integer"); }
    	else { return FormValidation.ok(); }
    }

    public FormValidation doCheckBuildStatWidth(@QueryParameter String value){
    	if(!isMandatory(value)){ return FormValidation.error("Build stats width is mandatory"); }
    	else if(!isInt(value)){ return FormValidation.error("Build stats width should be an integer"); }
    	else { return FormValidation.ok(); }
    }

    public FormValidation doCheckTitle(@QueryParameter String value){
    	if(!isMandatory(value)){ return FormValidation.error("Title is mandatory"); }
    	else { return FormValidation.ok(); }
    }

    public HttpResponse doRecordBuildInfos() throws IOException {
    	Hudson.getInstance().checkPermission(getRequiredPermission());
    	
        List<JobBuildResult> jobBuildResultsRead = new ArrayList<JobBuildResult>();
        
        synchronized (this) {
            //TODO fix MatrixProject and use getAllJobs()
            for (TopLevelItem item : Hudson.getInstance().getItems()) {
                if (item instanceof AbstractProject) {
                	addBuildsFrom(jobBuildResultsRead, (AbstractProject) item);
                }
            }
            
            this.jobBuildResults = mergeJobBuildResults(jobBuildResults, jobBuildResultsRead);

        	save();
		}
    	
        return new HttpResponse() {
			public void generateResponse(StaplerRequest req, StaplerResponse rsp,
					Object node) throws IOException, ServletException {
			}
		};
    }
    
    public void doCreateChart(StaplerRequest req, StaplerResponse res) throws ServletException, IOException {
    	Hudson.getInstance().checkPermission(getRequiredPermission());
    	
    	BuildStatConfiguration config = createBuildStatConfig(req);
    	List<JobBuildResult> filteredJobBuildResults = createFilteredAndSortedBuildResults(config);
        DataSetBuilder<String, DateRange> dsb = createDataSetBuilder(filteredJobBuildResults, config);
    	
        ChartUtil.generateGraph(req, res, createChart(req, dsb.build(), config.getBuildStatTitle()), 
        		config.getBuildStatWidth(), config.getBuildStatHeight());
    }
    
    public void doCreateChartMap(StaplerRequest req, StaplerResponse res) throws ServletException, IOException {
    	Hudson.getInstance().checkPermission(getRequiredPermission());
    	BuildStatConfiguration config = createBuildStatConfig(req);
    	List<JobBuildResult> filteredJobBuildResults = createFilteredAndSortedBuildResults(config);
        DataSetBuilder<String, DateRange> dsb = createDataSetBuilder(filteredJobBuildResults, config);
    	
        ChartUtil.generateClickableMap(req, res, createChart(req, dsb.build(), config.getBuildStatTitle()), 
        		config.getBuildStatWidth(), config.getBuildStatHeight());
    }
    
    public void doBuildHistory(StaplerRequest req, StaplerResponse res) throws ServletException, IOException {
    	Hudson.getInstance().checkPermission(getRequiredPermission());
    	
    	BuildHistorySearchCriteria searchCriteria = new BuildHistorySearchCriteria();
    	req.bindParameters(searchCriteria);
    	
    	JobFilter jobFilter = JobFilterFactory.createJobFilter(searchCriteria.jobFilter);
    	List<JobBuildResult> filteredJobBuildResults = new ArrayList<JobBuildResult>();
        for(JobBuildResult r : jobBuildResults){
        	if(r.getBuildDate().getTimeInMillis() >= searchCriteria.start
        			&& r.getBuildDate().getTimeInMillis() < searchCriteria.end
        			&& jobResultStatusMatchesWith(r.getResult(), searchCriteria)
        			&& jobFilter.isJobApplicable(r.getJobName())){
        		filteredJobBuildResults.add(r);
        	}
        }
        
        // Sorting on job results dates
        sortJobBuildResultsByBuildDate(filteredJobBuildResults);
    	
        req.setAttribute("jobResults", filteredJobBuildResults);
        req.setAttribute("searchCriteria", searchCriteria);
    	req.getView(this, "/hudson/plugins/global_build_stats/GlobalBuildStatsPlugin/buildHistory.jelly").forward(req, res);
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
    
	protected static boolean isInt(String value){
		try{
			Integer.parseInt(value);
			return true;
		}catch(NumberFormatException e){
			return false;
		}
	}
	
	protected static boolean isMandatory(String value){
		return value != null && !"".equals(value);
	}
	
	protected static boolean isBool(String value){
		try{
			Boolean.valueOf(value);
			return true;
		}catch(Throwable t){
			return false;
		}
	}
    
    public void doUpdateBuildStatConfiguration(StaplerRequest req, StaplerResponse res) throws ServletException, IOException {
    	Hudson.getInstance().checkPermission(getRequiredPermission());
    	
    	synchronized(this){
	    	this.buildStatConfigs.set(Integer.parseInt(req.getParameter("buildStatId")), createBuildStatConfig(req));
	    	save();
    	}
    	
    	res.forwardToPreviousPage(req);
    }
    
    public void doAddBuildStatConfiguration(StaplerRequest req, StaplerResponse res) throws ServletException, IOException {
    	Hudson.getInstance().checkPermission(getRequiredPermission());
    	
    	synchronized(this){
	    	this.buildStatConfigs.add(createBuildStatConfig(req));
	    	save();
    	}
    	
    	res.forwardToPreviousPage(req);
    }
    
    public void doDeleteConfiguration(StaplerRequest req, StaplerResponse res) throws ServletException, IOException {
    	Hudson.getInstance().checkPermission(getRequiredPermission());
    	
    	synchronized(this){
    		this.buildStatConfigs.remove(Integer.parseInt(req.getParameter("buildStatId")));
    		save();
    	}
    	
        res.forwardToPreviousPage(req);
    }
    
    public void doMoveUpConf(StaplerRequest req, StaplerResponse res) throws ServletException, IOException {
    	Hudson.getInstance().checkPermission(getRequiredPermission());
    	
    	// Swapping build confs
    	int index = Integer.parseInt(req.getParameter("buildStatId"));

    	synchronized(this){
	    	BuildStatConfiguration b = this.buildStatConfigs.get(index);
	    	this.buildStatConfigs.set(index, this.buildStatConfigs.get(index-1));
	    	this.buildStatConfigs.set(index-1, b);
	    	save();
    	}
    	
        res.forwardToPreviousPage(req);
    }
    
    public void doMoveDownConf(StaplerRequest req, StaplerResponse res) throws ServletException, IOException {
    	Hudson.getInstance().checkPermission(getRequiredPermission());
    	
    	// Swapping build confs
    	int index = Integer.parseInt(req.getParameter("buildStatId"));

    	synchronized(this){
	    	BuildStatConfiguration b = this.buildStatConfigs.get(index);
	    	this.buildStatConfigs.set(index, this.buildStatConfigs.get(index+1));
	    	this.buildStatConfigs.set(index+1, b);
	    	save();
    	}
    	
        res.forwardToPreviousPage(req);
    }
    
    private BuildStatConfiguration createBuildStatConfig(StaplerRequest req){
    	// TODO: refactor this using StaplerRequest.bindParameters() with introspection !
    	return new BuildStatConfiguration(
    			req.getParameter("title"), 
    			Integer.parseInt(req.getParameter("buildStatWidth")),
    			Integer.parseInt(req.getParameter("buildStatHeight")),
    			Integer.parseInt(req.getParameter("historicLength")), 
    			HistoricScale.valueOf(req.getParameter("historicScale")),
    			req.getParameter("jobFilter"),
    			Boolean.parseBoolean(req.getParameter("successShown")),
    			Boolean.parseBoolean(req.getParameter("failuresShown")),
    			Boolean.parseBoolean(req.getParameter("unstablesShown")),
    			Boolean.parseBoolean(req.getParameter("abortedShown")),
    			Boolean.parseBoolean(req.getParameter("notBuildsShown")));
    }
    
    private JFreeChart createChart(final StaplerRequest req, CategoryDataset dataset, String title) {

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
                
                boolean successShown=BuildResultConstants.SUCCESS.equals(status);
                boolean failuresShown=BuildResultConstants.FAILURES.equals(status);
                boolean unstablesShown=BuildResultConstants.UNSTABLES.equals(status);
                boolean abortedShown=BuildResultConstants.ABORTED.equals(status);
                boolean notBuildShown=BuildResultConstants.NOT_BUILD.equals(status);
                
                return new StringBuilder()
                	.append("buildHistory?jobFilter=").append(req.getParameter("jobFilter"))
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
        		dsb.add(nbSuccess, BuildResultConstants.SUCCESS, range);
    			dsb.add(nbFailures, BuildResultConstants.FAILURES, range);
    			dsb.add(nbUnstables, BuildResultConstants.UNSTABLES, range);
    			dsb.add(nbAborted, BuildResultConstants.ABORTED, range);
    			dsb.add(nbNotBuild, BuildResultConstants.NOT_BUILD, range);
        		
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
    
    private List<JobBuildResult> createFilteredAndSortedBuildResults(BuildStatConfiguration config){
    	List<JobBuildResult> filteredJobBuildResults = new ArrayList<JobBuildResult>();
        for(JobBuildResult r : jobBuildResults){
        	if(JobFilterFactory.createJobFilter(config.getJobFilter()).isJobApplicable(r.getJobName())){
        		filteredJobBuildResults.add(r);
        	}
        }
        
        // Sorting on job results dates
        sortJobBuildResultsByBuildDate(filteredJobBuildResults);
        
        return filteredJobBuildResults;
    }
    	
    private static void sortJobBuildResultsByBuildDate(List<JobBuildResult> c){
        Collections.sort(c, Collections.reverseOrder(new Comparator<JobBuildResult>() {
        	public int compare(JobBuildResult o1, JobBuildResult o2) {
        		return o1.getBuildDate().compareTo(o2.getBuildDate());
        	}
		}));
    }
    
	private static void addBuildsFrom(List<JobBuildResult> jobBuildResultsRead, AbstractProject project){
        List<AbstractBuild> builds = project.getBuilds();
        Iterator<AbstractBuild> buildIterator = builds.iterator();

        while (buildIterator.hasNext()) {
        	addBuild(jobBuildResultsRead, buildIterator.next());
        }
	}
	
	private static void addBuild(List<JobBuildResult> jobBuildResultsRead, AbstractBuild build){
		jobBuildResultsRead.add(JobBuildResultFactory.INSTANCE.createJobBuildResult(build));
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

	public BuildStatConfiguration[] getBuildStatConfigs() {
		return buildStatConfigs.toArray(new BuildStatConfiguration[]{});
	}
	
	public Permission getRequiredPermission(){
		return Hudson.ADMINISTER;
	}
	
	public HistoricScale[] getHistoricScales(){
		return HistoricScale.values();
	}
}

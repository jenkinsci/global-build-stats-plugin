package hudson.plugins.global_build_stats;

import hudson.Extension;
import hudson.Plugin;
import hudson.model.ManagementLink;
import hudson.model.TaskListener;
import hudson.model.AbstractBuild;
import hudson.model.Hudson;
import hudson.model.listeners.RunListener;
import hudson.plugins.global_build_stats.business.GlobalBuildStatsBusiness;
import hudson.plugins.global_build_stats.model.BuildHistorySearchCriteria;
import hudson.plugins.global_build_stats.model.BuildStatConfiguration;
import hudson.plugins.global_build_stats.model.HistoricScale;
import hudson.plugins.global_build_stats.model.JobBuildResult;
import hudson.plugins.global_build_stats.validation.GlobalBuildStatsValidator;
import hudson.plugins.global_build_stats.xstream.GlobalBuildStatsXStreamConverter;
import hudson.security.Permission;
import hudson.util.ChartUtil;
import hudson.util.FormValidation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;

import org.jfree.chart.JFreeChart;
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

	/**
	 * List of aggregated job build results
	 * This list will grow over time
	 */
	private List<JobBuildResult> jobBuildResults = new ArrayList<JobBuildResult>();
	
	/**
	 * List of persisted build statistics configurations used on the
	 * global build stats screen
	 */
	private List<BuildStatConfiguration> buildStatConfigs = new ArrayList<BuildStatConfiguration>();

	@Override
	public void start() throws Exception {
		super.start();
		
		Hudson.XSTREAM.registerConverter(new GlobalBuildStatsXStreamConverter());
	}
	
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
    		
    		getPluginBusiness().onJobCompleted(r);
    	}
    }
    
    // TODO: use a singleton ?
    private static GlobalBuildStatsBusiness getPluginBusiness(){
		// Retrieving global build stats plugin & adding build result to the registered build
		// result
    	return new GlobalBuildStatsBusiness(Hudson.getInstance().getPlugin(GlobalBuildStatsPlugin.class));
    }
    
    // TODO: use a singleton ?
    private GlobalBuildStatsBusiness getBusiness(){
    	return new GlobalBuildStatsBusiness(this);
    }
    
    // TODO: use a singleton ?
    private GlobalBuildStatsValidator getValidator(){
    	return new GlobalBuildStatsValidator();
    }
    
    // Form validations
    
    public FormValidation doCheckJobFilter(@QueryParameter String value){
    	return getValidator().checkJobFilter(value);
    }
    
    public FormValidation doCheckFailuresShown(@QueryParameter String value){
    	return getValidator().checkFailuresShown(value);
    }
    
    public FormValidation doCheckUnstablesShown(@QueryParameter String value){
    	return getValidator().checkUnstablesShown(value);
    }
    
    public FormValidation doCheckAbortedShown(@QueryParameter String value){
    	return getValidator().checkAbortedShown(value);
    }
    
    public FormValidation doCheckNotBuildsShown(@QueryParameter String value){
    	return getValidator().checkNotBuildsShown(value);
    }
    
    public FormValidation doCheckSuccessShown(@QueryParameter String value){
    	return getValidator().checkSuccessShown(value);
    }
    
	public FormValidation doCheckHistoricScale(@QueryParameter String value){
    	return getValidator().checkHistoricScale(value);
    }

	public FormValidation doCheckHistoricLength(@QueryParameter String value){
    	return getValidator().checkHistoricLength(value);
    }

    public FormValidation doCheckBuildStatHeight(@QueryParameter String value){
    	return getValidator().checkBuildStatHeight(value);
    }

    public FormValidation doCheckBuildStatWidth(@QueryParameter String value){
    	return getValidator().checkBuildStatWidth(value);
    }

    public FormValidation doCheckTitle(@QueryParameter String value){
    	return getValidator().checkTitle(value);
    }

    public HttpResponse doRecordBuildInfos() throws IOException {
    	Hudson.getInstance().checkPermission(getRequiredPermission());
    	
    	getBusiness().recordBuildInfos();
    	
        return new HttpResponse() {
			public void generateResponse(StaplerRequest req, StaplerResponse rsp,
					Object node) throws IOException, ServletException {
			}
		};
    }
    
    public void doCreateChart(StaplerRequest req, StaplerResponse res) throws ServletException, IOException {
    	Hudson.getInstance().checkPermission(getRequiredPermission());
    	
    	BuildStatConfiguration config = createBuildStatConfig(req);
    	JFreeChart chart = getBusiness().createChart(config);
    	
        ChartUtil.generateGraph(req, res, chart, config.getBuildStatWidth(), config.getBuildStatHeight());
    }
    
    public void doCreateChartMap(StaplerRequest req, StaplerResponse res) throws ServletException, IOException {
    	Hudson.getInstance().checkPermission(getRequiredPermission());

    	BuildStatConfiguration config = createBuildStatConfig(req);
    	JFreeChart chart = getBusiness().createChart(config);
    	
        ChartUtil.generateClickableMap(req, res, chart, config.getBuildStatWidth(), config.getBuildStatHeight());
    }
    
    public void doBuildHistory(StaplerRequest req, StaplerResponse res) throws ServletException, IOException {
    	Hudson.getInstance().checkPermission(getRequiredPermission());
    	
    	BuildHistorySearchCriteria searchCriteria = new BuildHistorySearchCriteria();
    	req.bindParameters(searchCriteria);
    	
    	List<JobBuildResult> filteredJobBuildResults = getBusiness().searchBuilds(searchCriteria);
    	
        req.setAttribute("jobResults", filteredJobBuildResults);
        req.setAttribute("searchCriteria", searchCriteria);
    	req.getView(this, "/hudson/plugins/global_build_stats/GlobalBuildStatsPlugin/buildHistory.jelly").forward(req, res);
    }
    
    public void doUpdateBuildStatConfiguration(StaplerRequest req, StaplerResponse res) throws ServletException, IOException {
    	Hudson.getInstance().checkPermission(getRequiredPermission());
    	
    	getBusiness().updateBuildStatConfiguration(Integer.parseInt(req.getParameter("buildStatId")), createBuildStatConfig(req));
    	
    	res.forwardToPreviousPage(req);
    }
    
    public void doAddBuildStatConfiguration(StaplerRequest req, StaplerResponse res) throws ServletException, IOException {
    	Hudson.getInstance().checkPermission(getRequiredPermission());
    	
    	getBusiness().addBuildStatConfiguration(createBuildStatConfig(req));
    	
    	res.forwardToPreviousPage(req);
    }
    
    public void doDeleteConfiguration(StaplerRequest req, StaplerResponse res) throws ServletException, IOException {
    	Hudson.getInstance().checkPermission(getRequiredPermission());
    	
    	getBusiness().deleteBuildStatConfiguration(Integer.parseInt(req.getParameter("buildStatId")));
    	
        res.forwardToPreviousPage(req);
    }
    
    public void doMoveUpConf(StaplerRequest req, StaplerResponse res) throws ServletException, IOException {
    	Hudson.getInstance().checkPermission(getRequiredPermission());
    	
    	int buildStatId = Integer.parseInt(req.getParameter("buildStatId"));
    	getBusiness().moveUpConf(buildStatId);
    	
        res.forwardToPreviousPage(req);
    }
    
    public void doMoveDownConf(StaplerRequest req, StaplerResponse res) throws ServletException, IOException {
    	Hudson.getInstance().checkPermission(getRequiredPermission());
    	
    	int buildStatId = Integer.parseInt(req.getParameter("buildStatId"));
    	getBusiness().moveDownConf(buildStatId);

        res.forwardToPreviousPage(req);
    }
    
    /**
     * Method must stay here since, for an unknown reason, in buildHistory.jelly,
     * call to <j:invokeStatic> doesn't work (and <j:invoke> work fine !)
     * @param value Parameter which should be escaped
     * @return value where "\" are escaped
     */
	public static String escapeAntiSlashes(String value){
		return GlobalBuildStatsBusiness.escapeAntiSlashes(value);
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
    
	public BuildStatConfiguration[] getBuildStatConfigsArrayed() {
		return buildStatConfigs.toArray(new BuildStatConfiguration[]{});
	}
	
	public List<BuildStatConfiguration> getBuildStatConfigs() {
		return buildStatConfigs;
	}
	
	public Permission getRequiredPermission(){
		return Hudson.ADMINISTER;
	}
	
	public HistoricScale[] getHistoricScales(){
		return HistoricScale.values();
	}

	public List<JobBuildResult> getJobBuildResults() {
		return jobBuildResults;
	}

	public void setJobBuildResults(List<JobBuildResult> jobBuildResults) {
		this.jobBuildResults = jobBuildResults;
	}
}

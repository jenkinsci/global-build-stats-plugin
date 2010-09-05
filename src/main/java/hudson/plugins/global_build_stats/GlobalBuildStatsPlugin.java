package hudson.plugins.global_build_stats;

import hudson.Extension;
import hudson.Plugin;
import hudson.model.ManagementLink;
import hudson.model.TaskListener;
import hudson.model.AbstractBuild;
import hudson.model.Api;
import hudson.model.Hudson;
import hudson.model.listeners.ItemListener;
import hudson.model.listeners.RunListener;
import hudson.plugins.global_build_stats.business.GlobalBuildStatsBusiness;
import hudson.plugins.global_build_stats.model.BuildHistorySearchCriteria;
import hudson.plugins.global_build_stats.model.BuildStatChartData;
import hudson.plugins.global_build_stats.model.BuildStatConfiguration;
import hudson.plugins.global_build_stats.model.DateRange;
import hudson.plugins.global_build_stats.model.HistoricScale;
import hudson.plugins.global_build_stats.model.JobBuildResult;
import hudson.plugins.global_build_stats.model.JobBuildSearchResult;
import hudson.plugins.global_build_stats.model.ModelIdGenerator;
import hudson.plugins.global_build_stats.model.YAxisChartType;
import hudson.plugins.global_build_stats.validation.GlobalBuildStatsValidator;
import hudson.plugins.global_build_stats.xstream.GlobalBuildStatsXStreamConverter;
import hudson.security.Permission;
import hudson.util.ChartUtil;
import hudson.util.DataSetBuilder;
import hudson.util.FormValidation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import net.sf.json.JSONObject;

import org.jfree.chart.JFreeChart;
import org.jfree.data.category.CategoryDataset;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.kohsuke.stapler.export.Flavor;

/**
 * Entry point of the global build stats plugin
 * 
 * @author fcamblor
 * @plugin
 */
@ExportedBean
public class GlobalBuildStatsPlugin extends Plugin {

    private static final Logger LOGGER = Logger.getLogger(GlobalBuildStatsPlugin.class.getName());
    
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
	
	/**
	 * Business layer for global build stats
	 */
	transient private final GlobalBuildStatsBusiness business = new GlobalBuildStatsBusiness(this);
	
	/**
	 * Validator layer for global build stats
	 */
	transient private final GlobalBuildStatsValidator validator = new GlobalBuildStatsValidator();
	
	@Override
	public void start() throws Exception {
		super.start();
		
		Hudson.XSTREAM.registerConverter(new GlobalBuildStatsXStreamConverter());
		
		// XStream compacting aliases...
		Hudson.XSTREAM.alias(GlobalBuildStatsXStreamConverter.JOB_BUILD_RESULT_CLASS_ALIAS, JobBuildResult.class);
		Hudson.XSTREAM.alias(GlobalBuildStatsXStreamConverter.BUILD_STAT_CONFIG_CLASS_ALIAS, BuildStatConfiguration.class);
		
		Hudson.XSTREAM.aliasField("t", BuildStatConfiguration.class, "buildStatTitle");
		Hudson.XSTREAM.aliasField("w", BuildStatConfiguration.class, "buildStatWidth");
		Hudson.XSTREAM.aliasField("h", BuildStatConfiguration.class, "buildStatHeight");
		Hudson.XSTREAM.aliasField("l", BuildStatConfiguration.class, "historicLength");
		Hudson.XSTREAM.aliasField("s", BuildStatConfiguration.class, "historicScale");
		Hudson.XSTREAM.aliasField("jf", BuildStatConfiguration.class, "jobFilter");
		Hudson.XSTREAM.aliasField("sbr", BuildStatConfiguration.class, "shownBuildResults");
		Hudson.XSTREAM.aliasField("yact", BuildStatConfiguration.class, "yAxisChartType");

		Hudson.XSTREAM.aliasField("r", JobBuildResult.class, "result");
		Hudson.XSTREAM.aliasField("n", JobBuildResult.class, "jobName");
		Hudson.XSTREAM.aliasField("nb", JobBuildResult.class, "buildNumber");
		Hudson.XSTREAM.aliasField("d", JobBuildResult.class, "buildDate");
		Hudson.XSTREAM.aliasField("du", JobBuildResult.class, "duration");
		Hudson.XSTREAM.aliasField("nn", JobBuildResult.class, "nodeName");
	}
	
    /**
     * Expose {@link GlobalBuildStats} to the remote API :
     * - Either all build stat configuration data
     * - OR (if buildStatConfigId http parameter is given) chart data
     */
    public Api getApi() {
    	return new GlobalBuildStatsApi(this);
    }
    
    /**
     * Hack allowing to either generate plugin informations (build stat configurations) OR
     * generate chart data for a given buildStatConfigId request parameter
     * @author fcamblor
     */
    public static class GlobalBuildStatsApi extends Api{
    	public GlobalBuildStatsApi(Object bean) {
    		super(bean);
		}
    	@Override
    	public void doJson(StaplerRequest req, StaplerResponse rsp)
    			throws IOException, ServletException {
    		if(!exposeChartData(req, rsp, Flavor.JSON)){
    			super.doJson(req, rsp);
    		}
    	}
    	@Override
    	public void doPython(StaplerRequest req, StaplerResponse rsp)
    			throws IOException, ServletException {
    		if(!exposeChartData(req, rsp, Flavor.PYTHON)){
        		super.doPython(req, rsp);
    		}
    	}
    	
    	private boolean exposeChartData(StaplerRequest req, StaplerResponse rsp, Flavor flavor) throws ServletException, IOException{
    		boolean chartDataHasBeenExposed = false;
    		String buildStatConfigId = req.getParameter("buildStatConfigId");
    		if(buildStatConfigId != null){
    	    	BuildStatConfiguration config = GlobalBuildStatsPlugin.getPluginBusiness().searchBuildStatConfigById(buildStatConfigId);
    	    	if(config != null){
    	    		DataSetBuilder<String, DateRange> dsb = GlobalBuildStatsPlugin.getPluginBusiness().createDataSetBuilder(config);
    	    		rsp.serveExposedBean(req, new BuildStatChartData(dsb), flavor);
    	    		chartDataHasBeenExposed = true;
    	    	}
    		}
    		return chartDataHasBeenExposed;
    	}
    }
	
    @Extension
    public static class GlobalBuildStatsItemListener extends ItemListener {
    	/**
    	 * After all items are loaded, plugin is loaded
    	 */
    	@Override
    	public void onLoaded() {
    		super.onLoaded();
    		
    		try {
				Hudson.getInstance().getPlugin(GlobalBuildStatsPlugin.class).load();
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
			}
    	}
    	
    	// TODO: check if a node has been renamed and, if so, replace old name by new name in
    	// every job results
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
    
    private static GlobalBuildStatsBusiness getPluginBusiness(){
		// Retrieving global build stats plugin & adding build result to the registered build
		// result
    	return Hudson.getInstance().getPlugin(GlobalBuildStatsPlugin.class).business;
    }
    
    // Form validations
    
    public FormValidation doCheckJobFilter(@QueryParameter String value){
    	return validator.checkJobFilter(value);
    }
    
    public FormValidation doCheckFailuresShown(@QueryParameter String value){
    	return validator.checkFailuresShown(value);
    }
    
    public FormValidation doCheckUnstablesShown(@QueryParameter String value){
    	return validator.checkUnstablesShown(value);
    }
    
    public FormValidation doCheckAbortedShown(@QueryParameter String value){
    	return validator.checkAbortedShown(value);
    }
    
    public FormValidation doCheckNotBuildsShown(@QueryParameter String value){
    	return validator.checkNotBuildsShown(value);
    }
    
    public FormValidation doCheckSuccessShown(@QueryParameter String value){
    	return validator.checkSuccessShown(value);
    }
    
	public FormValidation doCheckHistoricScale(@QueryParameter String value){
    	return validator.checkHistoricScale(value);
    }

	public FormValidation doCheckHistoricLength(@QueryParameter String value){
    	return validator.checkHistoricLength(value);
    }

    public FormValidation doCheckBuildStatHeight(@QueryParameter String value){
    	return validator.checkBuildStatHeight(value);
    }

    public FormValidation doCheckBuildStatWidth(@QueryParameter String value){
    	return validator.checkBuildStatWidth(value);
    }

    public FormValidation doCheckTitle(@QueryParameter String value){
    	return validator.checkTitle(value);
    }

    public FormValidation doCheckYAxisChartType(@QueryParameter String value){
    	return validator.checkYAxisChartType(value);
    }

    public HttpResponse doRecordBuildInfos() throws IOException {
    	Hudson.getInstance().checkPermission(getRequiredPermission());
    	
    	business.recordBuildInfos();
    	
        return new HttpResponse() {
			public void generateResponse(StaplerRequest req, StaplerResponse rsp,
					Object node) throws IOException, ServletException {
			}
		};
    }
    
    public void doShowChart(StaplerRequest req, StaplerResponse res) throws ServletException, IOException {
    	// Don't check any role : this url is public and should provide a BuildStatConfiguration public id
    	BuildStatConfiguration config = business.searchBuildStatConfigById(req.getParameter("buildStatId"));
    	if(config == null){
    		throw new IllegalArgumentException("Unknown buildStatId parameter !");
    	}
    	JFreeChart chart = business.createChart(config);
    	
        ChartUtil.generateGraph(req, res, chart, config.getBuildStatWidth(), config.getBuildStatHeight());
    }
    
    public void doCreateChart(StaplerRequest req, StaplerResponse res) throws ServletException, IOException {
    	Hudson.getInstance().checkPermission(getRequiredPermission());
    	
    	// Passing null id since this is a not persisted BuildStatConfiguration
    	BuildStatConfiguration config = createBuildStatConfig(null, req);
    	JFreeChart chart = business.createChart(config);
    	
        ChartUtil.generateGraph(req, res, chart, config.getBuildStatWidth(), config.getBuildStatHeight());
    }
    
    public void doCreateChartMap(StaplerRequest req, StaplerResponse res) throws ServletException, IOException {
    	Hudson.getInstance().checkPermission(getRequiredPermission());

    	String buildStatId = req.getParameter("buildStatId");
    	BuildStatConfiguration config = null;
    	if(buildStatId != null){
    		config = business.searchBuildStatConfigById(buildStatId);
    	} else {
        	// Passing null id since this is a not persisted BuildStatConfiguration
        	config = createBuildStatConfig(null, req);
    	}
    	JFreeChart chart = business.createChart(config);
    	
        ChartUtil.generateClickableMap(req, res, chart, config.getBuildStatWidth(), config.getBuildStatHeight());
    }
    
    public void doBuildHistory(StaplerRequest req, StaplerResponse res) throws ServletException, IOException {
    	Hudson.getInstance().checkPermission(getRequiredPermission());
    	
    	BuildHistorySearchCriteria searchCriteria = new BuildHistorySearchCriteria();
    	req.bindParameters(searchCriteria);
    	
    	List<JobBuildSearchResult> filteredJobBuildResults = business.searchBuilds(searchCriteria);
    	
        req.setAttribute("jobResults", filteredJobBuildResults);
        req.setAttribute("searchCriteria", searchCriteria);
    	req.getView(this, "/hudson/plugins/global_build_stats/GlobalBuildStatsPlugin/buildHistory.jelly").forward(req, res);
    }
    
    public void doUpdateBuildStatConfiguration(StaplerRequest req, StaplerResponse res) throws ServletException, IOException {
    	Hudson.getInstance().checkPermission(getRequiredPermission());
    	
    	BuildStatConfiguration config = createBuildStatConfig(req.getParameter("buildStatId"), req);
    	business.updateBuildStatConfiguration(req.getParameter("buildStatId"), config);
    	
    	String json = JSONObject.fromObject(config).toString();
    	res.getWriter().write(json);
    }
    
    public void doAddBuildStatConfiguration(StaplerRequest req, StaplerResponse res) throws ServletException, IOException {
    	Hudson.getInstance().checkPermission(getRequiredPermission());
    	
    	BuildStatConfiguration config = createBuildStatConfig(ModelIdGenerator.INSTANCE.generateIdForClass(BuildStatConfiguration.class), req);
    	business.addBuildStatConfiguration(config);
    	
    	String json = JSONObject.fromObject(config).toString();
    	res.getWriter().write(json);
    }
    
    public void doDeleteConfiguration(StaplerRequest req, StaplerResponse res) throws ServletException, IOException {
    	Hudson.getInstance().checkPermission(getRequiredPermission());
    	
    	business.deleteBuildStatConfiguration(req.getParameter("buildStatId"));
    	
    	res.getWriter().write("{ status : 'ok' }");
    }
    
    public void doMoveUpConf(StaplerRequest req, StaplerResponse res) throws ServletException, IOException {
    	Hudson.getInstance().checkPermission(getRequiredPermission());
    	
    	business.moveUpConf(req.getParameter("buildStatId"));
    	
    	res.getWriter().write("{ status : 'ok' }");
    }
    
    public void doMoveDownConf(StaplerRequest req, StaplerResponse res) throws ServletException, IOException {
    	Hudson.getInstance().checkPermission(getRequiredPermission());
    	
    	business.moveDownConf(req.getParameter("buildStatId"));

    	res.getWriter().write("{ status : 'ok' }");
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
    
    private BuildStatConfiguration createBuildStatConfig(String id, StaplerRequest req){
    	// TODO: refactor this using StaplerRequest.bindParameters() with introspection !
    	return new BuildStatConfiguration(
    			id,
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
    			Boolean.parseBoolean(req.getParameter("notBuildsShown")),
    			YAxisChartType.valueOf(req.getParameter("yAxisChartType")));
    }
    
	public BuildStatConfiguration[] getBuildStatConfigsArrayed() {
		return buildStatConfigs.toArray(new BuildStatConfiguration[]{});
	}
	
	@Exported
	public List<BuildStatConfiguration> getBuildStatConfigs() {
		return buildStatConfigs;
	}
	
	public Permission getRequiredPermission(){
		return Hudson.ADMINISTER;
	}
	
	public HistoricScale[] getHistoricScales(){
		return HistoricScale.values();
	}
	
	public YAxisChartType[] getYAxisChartTypes(){
		return YAxisChartType.values();
	}

	public List<JobBuildResult> getJobBuildResults() {
		return jobBuildResults;
	}

	public void setJobBuildResults(List<JobBuildResult> jobBuildResults) {
		this.jobBuildResults = jobBuildResults;
	}
}

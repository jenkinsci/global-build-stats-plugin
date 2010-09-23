package hudson.plugins.global_build_stats;

import hudson.plugins.global_build_stats.model.BuildHistorySearchCriteria;
import hudson.plugins.global_build_stats.model.BuildSearchCriteria;
import hudson.plugins.global_build_stats.model.BuildStatConfiguration;
import hudson.plugins.global_build_stats.model.HistoricScale;
import hudson.plugins.global_build_stats.model.YAxisChartType;

import org.kohsuke.stapler.StaplerRequest;

public class FromRequestObjectFactory {

	public static BuildHistorySearchCriteria createBuildHistorySearchCriteria(StaplerRequest req){
		BuildSearchCriteria criteria = createBuildSearchCriteria(req);
		return new BuildHistorySearchCriteria(
				Long.parseLong(req.getParameter("start")), 
				Long.parseLong(req.getParameter("end")), 
				criteria);
	}
	
	public static BuildStatConfiguration createBuildStatConfiguration(String id, StaplerRequest req){
		BuildSearchCriteria criteria = createBuildSearchCriteria(req);
    	return new BuildStatConfiguration(
    			id,
    			req.getParameter("title"), 
    			Integer.parseInt(req.getParameter("buildStatWidth")),
    			Integer.parseInt(req.getParameter("buildStatHeight")),
    			Integer.parseInt(req.getParameter("historicLength")), 
    			HistoricScale.valueOf(req.getParameter("historicScale")),
    			YAxisChartType.valueOf(req.getParameter("yAxisChartType")),
    			Boolean.parseBoolean(req.getParameter("buildStatusesShown")),
    			Boolean.parseBoolean(req.getParameter("totalBuildTimeShown")),
    			Boolean.parseBoolean(req.getParameter("averageBuildTimeShown")),
    			criteria);
	}
	
	public static BuildSearchCriteria createBuildSearchCriteria(StaplerRequest req){
		BuildSearchCriteria criteria = new BuildSearchCriteria(req.getParameter("jobFilter"), 
				req.getParameter("nodeFilter"), 
				Boolean.parseBoolean(req.getParameter("successShown")),
    			Boolean.parseBoolean(req.getParameter("failuresShown")),
    			Boolean.parseBoolean(req.getParameter("unstablesShown")),
    			Boolean.parseBoolean(req.getParameter("abortedShown")),
    			Boolean.parseBoolean(req.getParameter("notBuildsShown")));
		return criteria;
	}
}

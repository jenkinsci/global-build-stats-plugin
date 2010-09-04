package hudson.plugins.global_build_stats.xstream.migration.v3;

import java.util.List;

import hudson.plugins.global_build_stats.model.BuildStatConfiguration;
import hudson.plugins.global_build_stats.model.JobBuildResult;
import hudson.plugins.global_build_stats.xstream.migration.GlobalBuildStatsPOJO;

public class V3GlobalBuildStatsPOJO implements GlobalBuildStatsPOJO {
	
	/**
	 * List of aggregated job build results
	 * This list will grow over time
	 */
	public List<JobBuildResult> jobBuildResults;
	
	/**
	 * List of persisted build statistics configurations used on the
	 * global build stats screen
	 */
	public List<BuildStatConfiguration> buildStatConfigs;

}

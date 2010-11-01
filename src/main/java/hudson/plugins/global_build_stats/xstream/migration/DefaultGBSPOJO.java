package hudson.plugins.global_build_stats.xstream.migration;

import hudson.plugins.global_build_stats.model.BuildStatConfiguration;
import hudson.plugins.global_build_stats.model.JobBuildResult;

import java.util.List;

public class DefaultGBSPOJO implements GlobalBuildStatsPOJO {

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

	public List<JobBuildResult> getJobBuildResults() {
		return jobBuildResults;
	}

	public void setJobBuildResults(List<JobBuildResult> jobBuildResults) {
		this.jobBuildResults = jobBuildResults;
	}

	public List<BuildStatConfiguration> getBuildStatConfigs() {
		return buildStatConfigs;
	}

	public void setBuildStatConfigs(List<BuildStatConfiguration> buildStatConfigs) {
		this.buildStatConfigs = buildStatConfigs;
	}


}

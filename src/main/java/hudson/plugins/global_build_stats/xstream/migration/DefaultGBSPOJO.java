package hudson.plugins.global_build_stats.xstream.migration;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.plugins.global_build_stats.model.BuildStatConfiguration;
import hudson.plugins.global_build_stats.model.JobBuildResult;
import hudson.plugins.global_build_stats.rententionstrategies.RetentionStrategy;

import java.util.List;

public class DefaultGBSPOJO implements GlobalBuildStatsPOJO {

	/**
	 * List of aggregated job build results
	 * This list will grow over time
	 */
	@SuppressFBWarnings("PA_PUBLIC_PRIMITIVE_ATTRIBUTE")
	public List<JobBuildResult> jobBuildResults;
	
	/**
	 * List of persisted build statistics configurations used on the
	 * global build stats screen
	 */
	@SuppressFBWarnings("PA_PUBLIC_PRIMITIVE_ATTRIBUTE")
	public List<BuildStatConfiguration> buildStatConfigs;

    /**
     * List of retention strategies used for job build results cleaning
     */
    @SuppressFBWarnings("PA_PUBLIC_PRIMITIVE_ATTRIBUTE")
    public List<RetentionStrategy> retentionStrategies;

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

    public List<RetentionStrategy> getRetentionStrategies() {
        return retentionStrategies;
    }

    public void setRetentionStrategies(List<RetentionStrategy> retentionStrategies) {
        this.retentionStrategies = retentionStrategies;
    }
}

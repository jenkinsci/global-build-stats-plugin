package hudson.plugins.global_build_stats.model;

import hudson.Util;

/**
 * Implementation used in search result view
 * @author fcamblor
 */
public class JobBuildSearchResult extends JobBuildResult {

	// Will be true if targetted job hasn't be deleted/renamed
	private boolean jobAccessible;
	// Will be true if isJobAccessible is true AND build result has not been deleted
	private boolean buildAccessible;
	
	public JobBuildSearchResult(JobBuildResult decoree, boolean isJobAccessible, boolean isBuildAccessible){
		super(decoree.getResult(), decoree.getJobName(), decoree.getBuildNumber(), decoree.getBuildDate(), decoree.getDuration(), decoree.getNodeName());
		this.jobAccessible = isJobAccessible;
		this.buildAccessible = isBuildAccessible;
	}

	public boolean isJobAccessible() {
		return jobAccessible;
	}

	public boolean isBuildAccessible() {
		return buildAccessible;
	}
	
	public String getDurationString(){
		return Util.getTimeSpanString(getDuration());
	}
}

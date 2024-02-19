package hudson.plugins.global_build_stats.model;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Util;

/**
 * Implementation used in search result view
 * @author fcamblor
 */
@SuppressFBWarnings("EQ_DOESNT_OVERRIDE_EQUALS")
public class JobBuildSearchResult extends JobBuildResult {

	// Will be true if targetted job hasn't be deleted/renamed
	private boolean jobAccessible;
	// Will be true if isJobAccessible is true AND build result has not been deleted
	private boolean buildAccessible;
	private String jobUrl;
	
	public JobBuildSearchResult(JobBuildResult decoree, boolean isJobAccessible, boolean isBuildAccessible, String jobUrl) {
		super(decoree.getResult(), decoree.getJobName(), decoree.getBuildNumber(), decoree.getBuildDate(), 
			  decoree.getDuration(), decoree.getNodeName(), decoree.getUserName());
		this.jobAccessible = isJobAccessible;
		this.buildAccessible = isBuildAccessible;
		this.jobUrl = jobUrl;
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

	public String getJobUrl() {
		return jobUrl;
	}
}

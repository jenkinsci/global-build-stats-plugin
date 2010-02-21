package hudson.plugins.global_build_stats;

import hudson.model.AbstractProject;

public interface JobFilter {

	boolean isJobApplicable(String projectName);
	
	
	public static final JobFilter ALL = new JobFilter() {
		@Override
		public boolean isJobApplicable(String projectName) {
			return true;
		}
	};
}

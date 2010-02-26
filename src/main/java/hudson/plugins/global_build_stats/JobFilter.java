package hudson.plugins.global_build_stats;


public interface JobFilter {

	boolean isJobApplicable(String projectName);
	
	
	public static final JobFilter ALL = new JobFilter() {
		public boolean isJobApplicable(String projectName) {
			return true;
		}
	};
}

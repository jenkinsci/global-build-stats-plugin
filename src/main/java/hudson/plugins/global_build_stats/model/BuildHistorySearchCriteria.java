package hudson.plugins.global_build_stats.model;

public class BuildHistorySearchCriteria {

	public long start, end;
	public BuildSearchCriteria buildFilters;
	
	public BuildHistorySearchCriteria(long _start, long _end, BuildSearchCriteria _buildFilters){
		this.start = _start;
		this.end = _end;
		this.buildFilters = _buildFilters;
	}
	
	public boolean isJobResultEligible(JobBuildResult result){
		return buildFilters.isJobResultEligible(result)
				&& (result.getBuildDate().getTimeInMillis() >= this.start)
        		&& (result.getBuildDate().getTimeInMillis() < this.end);
	}
}

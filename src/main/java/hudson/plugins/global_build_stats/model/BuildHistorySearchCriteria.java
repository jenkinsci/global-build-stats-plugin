package hudson.plugins.global_build_stats.model;

public class BuildHistorySearchCriteria {

	public String jobFilter;
	public long start, end;
	public boolean successShown, failuresShown, unstablesShown, abortedShown, notBuildShown;
	
}

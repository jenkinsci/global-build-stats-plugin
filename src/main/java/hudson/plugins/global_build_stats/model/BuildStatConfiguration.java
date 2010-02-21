package hudson.plugins.global_build_stats.model;

import hudson.plugins.global_build_stats.JobFilter;

import java.io.Serializable;

public class BuildStatConfiguration implements Serializable {

	private String buildStatTitle;
	private int historicLength;
	private HistoricScale historicScale;
	private JobFilter targetJobs;
	private short shownBuildResults;
	
	public BuildStatConfiguration(String _buildStatTitle, int _historicLength, HistoricScale _historicScale,
			JobFilter _targetJobs, boolean successShown, boolean failuresShown, 
			boolean unstablesShown, boolean abortedShown, boolean notBuildsShown){
		
		this.buildStatTitle = _buildStatTitle;
		this.historicLength = _historicLength;
		this.historicScale = _historicScale;
		this.targetJobs = _targetJobs;
		
		this.shownBuildResults = 0;
		this.shownBuildResults |= successShown?BuildResult.SUCCESS.code:0;
		this.shownBuildResults |= failuresShown?BuildResult.FAILURE.code:0;
		this.shownBuildResults |= unstablesShown?BuildResult.UNSTABLE.code:0;
		this.shownBuildResults |= abortedShown?BuildResult.ABORTED.code:0;
		this.shownBuildResults |= notBuildsShown?BuildResult.NOT_BUILD.code:0;
	}

	public boolean isSuccessShown(){
		return (shownBuildResults & BuildResult.SUCCESS.code) != 0;
	}
	
	public boolean isFailuresShown(){
		return (shownBuildResults & BuildResult.FAILURE.code) != 0;
	}
	
	public boolean isUnstablesShown(){
		return (shownBuildResults & BuildResult.UNSTABLE.code) != 0;
	}
	
	public boolean isAbortedShown(){
		return (shownBuildResults & BuildResult.ABORTED.code) != 0;
	}
	
	public boolean isNotBuildShown(){
		return (shownBuildResults & BuildResult.NOT_BUILD.code) != 0;
	}
	
	public String getBuildStatTitle() {
		return buildStatTitle;
	}

	public int getHistoricLength() {
		return historicLength;
	}

	public HistoricScale getHistoricScale() {
		return historicScale;
	}

	public short getShownBuildResults() {
		return shownBuildResults;
	}

	public JobFilter getTargetJobs() {
		return targetJobs;
	}
}

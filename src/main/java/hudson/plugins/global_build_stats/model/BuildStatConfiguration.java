package hudson.plugins.global_build_stats.model;

import hudson.plugins.global_build_stats.JobFilterFactory;

import java.io.Serializable;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean
public class BuildStatConfiguration implements Serializable {

	private static final long serialVersionUID = -2962124739645932894L;
	
	private String id;
	private String buildStatTitle;
	private int buildStatWidth=400, buildStatHeight=300;
	private int historicLength;
	private HistoricScale historicScale;
	private String jobFilter = JobFilterFactory.ALL_JOBS_FILTER_PATTERN;
	private short shownBuildResults;
	
	public BuildStatConfiguration(){
	}
	
	public BuildStatConfiguration(String _id, String _buildStatTitle, int _buildStatWidth, int _buildStatHeight, 
			int _historicLength, HistoricScale _historicScale, String _jobFilter, 
			boolean successShown, boolean failuresShown, boolean unstablesShown, 
			boolean abortedShown, boolean notBuildsShown){
		
		this.id = _id;
		this.buildStatTitle = _buildStatTitle;
		this.buildStatHeight = _buildStatHeight;
		this.buildStatWidth = _buildStatWidth;
		this.historicLength = _historicLength;
		this.historicScale = _historicScale;
		this.jobFilter = _jobFilter;
		
		this.shownBuildResults = 0;
		this.shownBuildResults |= successShown?BuildResult.SUCCESS.code:0;
		this.shownBuildResults |= failuresShown?BuildResult.FAILURE.code:0;
		this.shownBuildResults |= unstablesShown?BuildResult.UNSTABLE.code:0;
		this.shownBuildResults |= abortedShown?BuildResult.ABORTED.code:0;
		this.shownBuildResults |= notBuildsShown?BuildResult.NOT_BUILD.code:0;
	}

	@Exported
	public boolean isSuccessShown(){
		return (shownBuildResults & BuildResult.SUCCESS.code) != 0;
	}
	
	@Exported
	public boolean isFailuresShown(){
		return (shownBuildResults & BuildResult.FAILURE.code) != 0;
	}
	
	@Exported
	public boolean isUnstablesShown(){
		return (shownBuildResults & BuildResult.UNSTABLE.code) != 0;
	}
	
	@Exported
	public boolean isAbortedShown(){
		return (shownBuildResults & BuildResult.ABORTED.code) != 0;
	}
	
	@Exported
	public boolean isNotBuildShown(){
		return (shownBuildResults & BuildResult.NOT_BUILD.code) != 0;
	}
	
	@Exported
	public String getBuildStatTitle() {
		return buildStatTitle;
	}

	@Exported
	public int getHistoricLength() {
		return historicLength;
	}

	@Exported
	public HistoricScale getHistoricScale() {
		return historicScale;
	}

	public short getShownBuildResults() {
		return shownBuildResults;
	}

	@Exported
	public int getBuildStatWidth() {
		return buildStatWidth;
	}

	@Exported
	public int getBuildStatHeight() {
		return buildStatHeight;
	}

	@Exported
	public String getJobFilter() {
		return jobFilter;
	}

	public void setBuildStatTitle(String buildStatTitle) {
		this.buildStatTitle = buildStatTitle;
	}

	public void setBuildStatWidth(int buildStatWidth) {
		this.buildStatWidth = buildStatWidth;
	}

	public void setBuildStatHeight(int buildStatHeight) {
		this.buildStatHeight = buildStatHeight;
	}

	public void setHistoricLength(int historicLength) {
		this.historicLength = historicLength;
	}

	public void setHistoricScale(String historicScale) {
		this.historicScale = HistoricScale.valueOf(historicScale);
	}

	public void setJobFilter(String jobFilter) {
		this.jobFilter = jobFilter;
	}

	@Exported
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}

package hudson.plugins.global_build_stats.model;

import hudson.plugins.global_build_stats.JobFilter;
import hudson.plugins.global_build_stats.JobFilterFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Data persisted for every build stat configuration allowing to create charts
 * on build results
 * WARNING : if any change is made to this class, don't miss to create a new
 * data migrator in the hudson.plugins.global_build_stats.xstream.migration package ! 
 * @author fcamblor
 */
@ExportedBean
public class BuildStatConfiguration implements Serializable {

	private static final long serialVersionUID = -2962124739645932894L;
	
	private String id;
	
	// Chart configuration
	private String buildStatTitle;
	private int buildStatWidth=400, buildStatHeight=300;
	private int historicLength;
	private HistoricScale historicScale;
	private YAxisChartType yAxisChartType = YAxisChartType.COUNT;
	private YAxisChartDimension[] dimensionsShown;
	
	// Filters on jobs
	private String jobFilter = JobFilterFactory.ALL_JOBS_FILTER_PATTERN;
	transient JobFilter calculatedJobFilter = null; // For calcul optimizations only
	private short shownBuildResults;
	
	public BuildStatConfiguration(){
	}
	
	public BuildStatConfiguration(String _id, String _buildStatTitle, int _buildStatWidth, int _buildStatHeight, 
			int _historicLength, HistoricScale _historicScale, String _jobFilter, 
			boolean successShown, boolean failuresShown, boolean unstablesShown, 
			boolean abortedShown, boolean notBuildsShown, YAxisChartType yAxisChartType,
			boolean buildCountsShown, boolean totalBuildTimeShown, boolean averageBuildTimeShown){
		
		this.id = _id;
		this.buildStatTitle = _buildStatTitle;
		this.buildStatHeight = _buildStatHeight;
		this.buildStatWidth = _buildStatWidth;
		this.historicLength = _historicLength;
		this.historicScale = _historicScale;
		
		this.setJobFilter(_jobFilter);
		
		this.shownBuildResults = 0;
		this.shownBuildResults |= successShown?BuildResult.SUCCESS.code:0;
		this.shownBuildResults |= failuresShown?BuildResult.FAILURE.code:0;
		this.shownBuildResults |= unstablesShown?BuildResult.UNSTABLE.code:0;
		this.shownBuildResults |= abortedShown?BuildResult.ABORTED.code:0;
		this.shownBuildResults |= notBuildsShown?BuildResult.NOT_BUILD.code:0;
		
		this.yAxisChartType = yAxisChartType;
		
		List<YAxisChartDimension> dimensionsList = new ArrayList<YAxisChartDimension>();
		if(buildCountsShown){ dimensionsList.add(YAxisChartDimension.BUILD_COUNTER); }
		if(totalBuildTimeShown){ dimensionsList.add(YAxisChartDimension.BUILD_TOTAL_DURATION); }
		if(averageBuildTimeShown){ dimensionsList.add(YAxisChartDimension.BUILD_AVERAGE_DURATION); }
		this.dimensionsShown = dimensionsList.toArray(new YAxisChartDimension[]{});
	}

	public boolean isJobResultEligible(JobBuildResult result){
		boolean jobBuildEligible = true;

		jobBuildEligible &= getCalculatedJobFilter().isJobApplicable(result.getJobName());
		jobBuildEligible &= isAbortedShown() || result.getResult().getAbortedCount()!=1;
		jobBuildEligible &= isFailuresShown() || result.getResult().getFailureCount()!=1;
		jobBuildEligible &= isNotBuildShown() || result.getResult().getNotBuildCount()!=1;
		jobBuildEligible &= isSuccessShown() || result.getResult().getSuccessCount()!=1;
		jobBuildEligible &= isUnstablesShown() || result.getResult().getUnstableCount()!=1;
		
		return jobBuildEligible;
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

	@Exported
	public String getId() {
		return id;
	}

	@Exported
	public YAxisChartType getyAxisChartType() {
		return yAxisChartType;
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
		this.calculatedJobFilter = JobFilterFactory.createJobFilter(jobFilter);
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setyAxisChartType(YAxisChartType yAxisChartType) {
		this.yAxisChartType = yAxisChartType;
	}

	public void setShownBuildResults(short shownBuildResults) {
		this.shownBuildResults = shownBuildResults;
	}

	@Exported
	public YAxisChartDimension[] getDimensionsShown() {
		return dimensionsShown;
	}

	public void setDimensionsShown(YAxisChartDimension[] dimensionsShown) {
		this.dimensionsShown = dimensionsShown;
	}
	
	protected JobFilter getCalculatedJobFilter(){
		// When BuildStatConfiguration is XStream deserialized, the transient calculatedJobFilter field
		// will be null !
		if(calculatedJobFilter == null){ calculatedJobFilter = JobFilterFactory.createJobFilter(jobFilter); }
		return this.calculatedJobFilter;
	}
	
	@Exported
	public boolean isBuildStatusesShown(){
		return Arrays.binarySearch(this.dimensionsShown, YAxisChartDimension.BUILD_COUNTER)>=0;
	}
	
	@Exported
	public boolean isTotalBuildTimeShown (){
		return Arrays.binarySearch(this.dimensionsShown, YAxisChartDimension.BUILD_TOTAL_DURATION)>=0;
	}
	
	@Exported
	public boolean isAverageBuildTimeShown(){
		return Arrays.binarySearch(this.dimensionsShown, YAxisChartDimension.BUILD_AVERAGE_DURATION)>=0;
	}
}

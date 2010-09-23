package hudson.plugins.global_build_stats.model;

import hudson.plugins.global_build_stats.FieldFilter;
import hudson.plugins.global_build_stats.FieldFilterFactory;

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
	private BuildSearchCriteria buildFilters;
	/**
	 * @deprecated Use buildFilters.jobFilter instead !
	 */
	@Deprecated
	transient private String jobFilter = FieldFilterFactory.ALL_VALUES_FILTER_LABEL;
	transient FieldFilter calculatedJobFilter = null; // For calcul optimizations only
	/**
	 * @deprecated Use buildFilters.nodeFilter instead !
	 */
	@Deprecated
	transient private String nodeFilter = FieldFilterFactory.ALL_VALUES_FILTER_LABEL;
	transient FieldFilter calculatedNodeFilter = null; // For calcul optimizations only
	/**
	 * @deprecated Use buildFilters.shownBuildResults instead !
	 */
	@Deprecated
	transient private short shownBuildResults;
	
	public BuildStatConfiguration(){
	}
	
	public BuildStatConfiguration(String _id, String _buildStatTitle, int _buildStatWidth, int _buildStatHeight, 
			int _historicLength, HistoricScale _historicScale, YAxisChartType _yAxisChartType,
			boolean _buildCountsShown, boolean _totalBuildTimeShown, boolean _averageBuildTimeShown,
			BuildSearchCriteria _buildFilters){
		
		this.id = _id;
		this.buildStatTitle = _buildStatTitle;
		this.buildStatHeight = _buildStatHeight;
		this.buildStatWidth = _buildStatWidth;
		this.historicLength = _historicLength;
		this.historicScale = _historicScale;
		
		this.yAxisChartType = _yAxisChartType;
		this.buildFilters = _buildFilters;
		
		List<YAxisChartDimension> dimensionsList = new ArrayList<YAxisChartDimension>();
		if(_buildCountsShown){ dimensionsList.add(YAxisChartDimension.BUILD_COUNTER); }
		if(_totalBuildTimeShown){ dimensionsList.add(YAxisChartDimension.BUILD_TOTAL_DURATION); }
		if(_averageBuildTimeShown){ dimensionsList.add(YAxisChartDimension.BUILD_AVERAGE_DURATION); }
		this.dimensionsShown = dimensionsList.toArray(new YAxisChartDimension[]{});
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

	@Exported
	public int getBuildStatWidth() {
		return buildStatWidth;
	}

	@Exported
	public int getBuildStatHeight() {
		return buildStatHeight;
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

	public void setId(String id) {
		this.id = id;
	}

	public void setyAxisChartType(YAxisChartType yAxisChartType) {
		this.yAxisChartType = yAxisChartType;
	}

	@Exported
	public YAxisChartDimension[] getDimensionsShown() {
		return dimensionsShown;
	}

	public void setDimensionsShown(YAxisChartDimension[] dimensionsShown) {
		this.dimensionsShown = dimensionsShown;
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

	@Exported
	public BuildSearchCriteria getBuildFilters() {
		return buildFilters;
	}
	
	public void setBuildFilters(BuildSearchCriteria buildFilters) {
		this.buildFilters = buildFilters;
	}
	
	/**
	 * Use getBuildFilters().isSuccessShown() instead
	 */
	@Deprecated
	public boolean isSuccessShown(){
		return (shownBuildResults & BuildResult.SUCCESS.code) != 0;
	}
	
	/**
	 * Use getBuildFilters().isFailuresShown() instead
	 */
	@Deprecated
	public boolean isFailuresShown(){
		return (shownBuildResults & BuildResult.FAILURE.code) != 0;
	}
	
	/**
	 * Use getBuildFilters().isUnstablesShown() instead
	 */
	@Deprecated
	public boolean isUnstablesShown(){
		return (shownBuildResults & BuildResult.UNSTABLE.code) != 0;
	}
	
	/**
	 * Use getBuildFilters().isAbortedShown() instead
	 */
	@Deprecated
	public boolean isAbortedShown(){
		return (shownBuildResults & BuildResult.ABORTED.code) != 0;
	}
	
	/**
	 * Use getBuildFilters().isNotBuildShown() instead
	 */
	@Deprecated
	public boolean isNotBuildShown(){
		return (shownBuildResults & BuildResult.NOT_BUILD.code) != 0;
	}

	/**
	 * Use getBuildFilters().getJobFilter() instead
	 */
	@Deprecated
	public String getJobFilter() {
		return jobFilter;
	}

	/**
	 * Use getBuildFilters().getNodeFilter() instead
	 */
	@Deprecated
	public String getNodeFilter() {
		return nodeFilter;
	}
	
	/**
	 * Use getBuildFilters().setJobFilter(jobFilter) instead
	 */
	@Deprecated
	public void setJobFilter(String jobFilter) {
		this.jobFilter = jobFilter;
		this.calculatedJobFilter = FieldFilterFactory.createFieldFilter(jobFilter);
	}
	
	/**
	 * Use getBuildFilters().setNodeFilter(nodeFilter) instead
	 */
	@Deprecated
	public void setNodeFilter(String nodeFilter) {
		this.nodeFilter = nodeFilter;
		this.calculatedNodeFilter = FieldFilterFactory.createFieldFilter(nodeFilter);
	}

	/**
	 * Use getBuildFilters().setShownBuildResults(shownBuildResults) instead
	 */
	@Deprecated
	public void setShownBuildResults(short shownBuildResults) {
		this.shownBuildResults = shownBuildResults;
	}
}

package hudson.plugins.global_build_stats.model;

import hudson.plugins.global_build_stats.FieldFilter;
import hudson.plugins.global_build_stats.FieldFilterFactory;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean
public class BuildSearchCriteria {

	private String jobFilter = FieldFilterFactory.ALL_VALUES_FILTER_LABEL;
	transient FieldFilter calculatedJobFilter = null; // For calcul optimizations only
	private String nodeFilter = FieldFilterFactory.ALL_VALUES_FILTER_LABEL;
	transient FieldFilter calculatedNodeFilter = null; // For calcul optimizations only
	private short shownBuildResults;

	public BuildSearchCriteria(String _jobFilter, String _nodeFilter,
			boolean _successShown, boolean _failuresShown, boolean _unstablesShown, 
			boolean _abortedShown, boolean _notBuildsShown){
		
		this.setJobFilter(_jobFilter);
		this.setNodeFilter(_nodeFilter);
		
		this.shownBuildResults = 0;
		this.shownBuildResults |= _successShown?BuildResult.SUCCESS.code:0;
		this.shownBuildResults |= _failuresShown?BuildResult.FAILURE.code:0;
		this.shownBuildResults |= _unstablesShown?BuildResult.UNSTABLE.code:0;
		this.shownBuildResults |= _abortedShown?BuildResult.ABORTED.code:0;
		this.shownBuildResults |= _notBuildsShown?BuildResult.NOT_BUILD.code:0;
	}

	public boolean isJobResultEligible(JobBuildResult result){
		boolean jobBuildEligible = true;

		jobBuildEligible &= getCalculatedJobFilter().isFieldValueValid(result.getJobName());
		jobBuildEligible &= getCalculatedNodeFilter().isFieldValueValid(result.getNodeName());
		jobBuildEligible &= isAbortedShown() || result.getResult().getAbortedCount()!=1;
		jobBuildEligible &= isFailuresShown() || result.getResult().getFailureCount()!=1;
		jobBuildEligible &= isNotBuildShown() || result.getResult().getNotBuildCount()!=1;
		jobBuildEligible &= isSuccessShown() || result.getResult().getSuccessCount()!=1;
		jobBuildEligible &= isUnstablesShown() || result.getResult().getUnstableCount()!=1;
		
		return jobBuildEligible;
	}
	
	public void setJobFilter(String jobFilter) {
		this.jobFilter = jobFilter;
		this.calculatedJobFilter = FieldFilterFactory.createFieldFilter(jobFilter);
	}

	public void setNodeFilter(String nodeFilter) {
		this.nodeFilter = nodeFilter;
		this.calculatedNodeFilter = FieldFilterFactory.createFieldFilter(nodeFilter);
	}

	protected FieldFilter getCalculatedJobFilter(){
		// When BuildStatConfiguration is XStream deserialized, the transient calculatedJobFilter field
		// will be null !
		if(calculatedJobFilter == null){ calculatedJobFilter = FieldFilterFactory.createFieldFilter(jobFilter); }
		return this.calculatedJobFilter;
	}
	
	protected FieldFilter getCalculatedNodeFilter(){
		// When BuildStatConfiguration is XStream deserialized, the transient calculatedNodeFilter field
		// will be null !
		if(calculatedNodeFilter == null){ calculatedNodeFilter = FieldFilterFactory.createFieldFilter(nodeFilter); }
		return this.calculatedNodeFilter;
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
	public String getJobFilter() {
		return jobFilter;
	}

	@Exported
	public String getNodeFilter() {
		return nodeFilter;
	}

	public void setShownBuildResults(short shownBuildResults) {
		this.shownBuildResults = shownBuildResults;
	}
}

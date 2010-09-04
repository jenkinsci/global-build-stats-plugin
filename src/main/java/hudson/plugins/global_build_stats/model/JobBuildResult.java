package hudson.plugins.global_build_stats.model;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Data persisted in the end of every build
 * WARNING : if any change is made to this class, don't miss to create a new
 * data migrator in the hudson.plugins.global_build_stats.xstream.migration package ! 
 * @author fcamblor
 */
public class JobBuildResult implements Serializable {

	private static final long serialVersionUID = -4697202185011561179L;
	public static long EMPTY_DURATION = -1;
	public static final String EMPTY_NODE_NAME = null;
	public static final String MASTER_NODE_NAME = "master";
	
	private BuildResult result;
	private String jobName;
	private int buildNumber;
	private Calendar buildDate;
	private long duration = -1;
	private String nodeName;
	
	public JobBuildResult(BuildResult _result, String _jobName, int _buildNumber, Calendar _buildDate, long duration, String nodeName){
		this.result = _result;
		this.jobName = _jobName;
		this.buildNumber = _buildNumber;
		this.buildDate = (Calendar)_buildDate.clone();
		this.duration = duration;
		setNodeName(nodeName);
	}

	@Override
	public String toString() {
		return new StringBuilder("jobName=").append(jobName).append(", buildNumber=").append(buildNumber)
						.append(", result=").append(result).append(", buildDate=").append(buildDate)
						.append(", duration=").append(duration).append(", nodeName=").append(nodeName).toString();
	}
	
	public BuildResult getResult() {
		return result;
	}

	public String getJobName() {
		return jobName;
	}

	public Calendar getBuildDate() {
		return buildDate;
	}
	
	public int getBuildNumber(){
		return buildNumber;
	}
	
	public long getDuration() {
		return duration;
	}
	
	public String getNodeName() {
		return nodeName;
	}
	
	public boolean isJobBuiltOnMaster(){
		return MASTER_NODE_NAME.equals(nodeName);
	}
	
	public boolean isDurationEmpty(){
		return EMPTY_DURATION == duration;
	}
	
	public boolean isNodeNameEmpty(){
		return nodeName == EMPTY_NODE_NAME;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof JobBuildResult){
			JobBuildResult r = (JobBuildResult)obj;
			return r.buildNumber == this.buildNumber && r.jobName.equals(this.jobName)
				// In general, "not build" results implies a job result with same jobName & buildNumber but error | failure status
				&& r.result.equals(this.result);
		}
		
		return false;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public void setNodeName(String nodeName) {
		// @see {@link Node#getNodeName}
		if("".equals(nodeName)){
			this.nodeName = MASTER_NODE_NAME;
		} else {
			this.nodeName = nodeName;
		}
	}
}

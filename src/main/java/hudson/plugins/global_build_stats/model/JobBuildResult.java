package hudson.plugins.global_build_stats.model;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Comparator;

/**
 * Data persisted in the end of every build
 * WARNING : if any change is made to this class, don't miss to create a new
 * data migrator in the hudson.plugins.global_build_stats.xstream.migration package ! 
 * @author fcamblor
 */
public class JobBuildResult implements Serializable {

	private static final long serialVersionUID = -4697202185011561179L;
	public static final long EMPTY_DURATION = -1;
	public static final String EMPTY_NODE_NAME = null;
	public static final String EMPTY_USER_NAME = null;
	public static final String MASTER_NODE_NAME = "master";
	
	private BuildResult result;
	private String jobName;
	private int buildNumber;
	private Calendar buildDate;
	private long duration = -1;
	private String nodeName;
	private String userName = null;
	
	public JobBuildResult(BuildResult _result, String _jobName, int _buildNumber, 
						  Calendar _buildDate, long duration, String nodeName, String _userName){
		this.result = _result;
		this.jobName = _jobName;
		this.buildNumber = _buildNumber;
		this.buildDate = (Calendar)_buildDate.clone();
		this.duration = duration;
		setNodeName(nodeName);
		this.userName = _userName;
	}

	@Override
	public String toString() {
		return new StringBuilder("jobName=").append(jobName).append(", buildNumber=").append(buildNumber)
						.append(", result=").append(result).append(", buildDate=").append(buildDate)
						.append(", duration=").append(duration).append(", nodeName=").append(nodeName)
						.append(", userName=").append(userName).toString();
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
	
	public boolean isUserNameEmpty(){
		return EMPTY_USER_NAME == userName;
	}
	
	public boolean isNodeNameEmpty(){
		return nodeName == EMPTY_NODE_NAME;
	}

	@Override
	@SuppressFBWarnings("HE_EQUALS_USE_HASHCODE")
	public boolean equals(Object obj) {
		if(obj instanceof JobBuildResult){
			JobBuildResult r = (JobBuildResult)obj;
            return is(r.buildNumber, r.jobName, r.result);
		}

		return false;
	}

    public boolean is(int buildNumber, String jobName, BuildResult result){
        return buildNumber == this.buildNumber && jobName.equals(this.jobName)
				// In general, "not build" results implies a job result with same jobName &
				// buildNumber but error | failure status
				&& result.equals(this.result);
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

	public String getUserName() {
		return userName;
	}

	@SuppressFBWarnings("SE_COMPARATOR_SHOULD_BE_SERIALIZABLE")
	public void setUserName(String userName) {
		this.userName = userName;
	}

	public static class ChronologicalComparator implements Comparator<JobBuildResult>{
        public int compare(JobBuildResult jbr1, JobBuildResult jbr2) {
            return jbr1.buildDate.compareTo(jbr2.buildDate);
        }
    }
    public static class AntiChronologicalComparator extends ChronologicalComparator {
        public int compare(JobBuildResult jbr1, JobBuildResult jbr2){
            return super.compare(jbr1, jbr2)*-1;
        }
    }
}

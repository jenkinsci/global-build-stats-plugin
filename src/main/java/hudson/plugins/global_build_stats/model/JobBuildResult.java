package hudson.plugins.global_build_stats.model;


import java.io.Serializable;
import java.util.Calendar;

public class JobBuildResult implements Serializable {

	private BuildResult result;
	private String jobName;
	private int buildNumber;
	private Calendar buildDate;
	
	public JobBuildResult(BuildResult _result, String _jobName, int _buildNumber, Calendar _buildDate){
		this.result = _result;
		this.jobName = _jobName;
		this.buildNumber = _buildNumber;
		this.buildDate = (Calendar)_buildDate.clone();
	}

	@Override
	public String toString() {
		return new StringBuilder("jobName=").append(jobName).append(", buildNumber=").append(buildNumber)
						.append(", result=").append(result).append(", buildDate=").append(buildDate).toString();
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
}

package hudson.plugins.global_build_stats;

import hudson.model.*;
import hudson.model.Cause.UserCause;
import hudson.model.Cause.UserIdCause;
import hudson.plugins.global_build_stats.model.BuildResult;
import hudson.plugins.global_build_stats.model.JobBuildResult;
import hudson.plugins.global_build_stats.model.JobBuildSearchResult;
import jenkins.model.Jenkins;

public class JobBuildResultFactory {

	public static final JobBuildResultFactory INSTANCE = new JobBuildResultFactory();
	private static final String SYSTEM_USERNAME = "SYSTEM";
	
	private JobBuildResultFactory(){
	}
	
	public JobBuildResult createJobBuildResult(AbstractBuild build){
		String buildName = build.getProject().getFullName();
		long duration = build.getDuration();
		String nodeName = build.getBuiltOnStr();
		/* Can't do that since MavenModuleSet is in maven-plugin artefact which is in test scope
		if(build.getProject() instanceof MavenModuleSet){
			buildName = ((MavenModuleSet)build.getProject()).getRootModule().toString();
		}*/
    	return new JobBuildResult(createBuildResult(build.getResult()), buildName, 
    			build.getNumber(), build.getTimestamp(), duration, nodeName, extractUserNameIn(build));
	}

    public JobBuildSearchResult createJobBuildSearchResult(AbstractBuild build){
        return createJobBuildSearchResult(createJobBuildResult(build));
    }

    public JobBuildSearchResult createJobBuildSearchResult(JobBuildResult r){
        boolean isJobAccessible = false;
        boolean isBuildAccessible = false;

        Job targetJob = ((Job) Jenkins.getInstance().getItemByFullName(r.getJobName()));
        // Link to job will be provided only if job has not been deleted/renamed
        if(targetJob != null){
            isJobAccessible = true;
            if(targetJob.getBuildByNumber(r.getBuildNumber()) != null){
                // Link to build infos will be provided only if build result has not been purged
                // @see issue #7240
                isBuildAccessible = true;
            }
        }

        return new JobBuildSearchResult(r, isJobAccessible, isBuildAccessible);
    }
	
	public static String extractUserNameIn(AbstractBuild build){
		String userName = null;
		UserCause uc = retrieveUserCause(build);
		UserIdCause uic = retrieveUserIdCause(build);
		if(uc != null){
			userName = uc.getUserName();
		} else if(uic != null){
			userName = uic.getUserId();
		} 
		// If no UserCause has been found, SYSTEM user should have launched the build
		else {
			userName = SYSTEM_USERNAME;
		}
		return userName;
	}
	
	private static UserCause retrieveUserCause(AbstractBuild build){
		for(CauseAction a : build.getActions(CauseAction.class)){
			for(Cause c : a.getCauses()){
				if(c instanceof UserCause){
					return (UserCause)c;
				}
			}
		}
		return null;
	}
	
	private static UserIdCause retrieveUserIdCause(AbstractBuild build){
		for(CauseAction a : build.getActions(CauseAction.class)){
			for(Cause c : a.getCauses()){
				if(c instanceof UserIdCause){
					return (UserIdCause)c;
				}
			}
		}
		return null;
	}	
	
	public BuildResult createBuildResult(Result result){
		if(Result.ABORTED.equals(result)){
			return BuildResult.ABORTED;
		} else if(Result.FAILURE.equals(result)){
			return BuildResult.FAILURE;
		} else if(Result.NOT_BUILT.equals(result)){
			return BuildResult.NOT_BUILD;
		} else if(Result.SUCCESS.equals(result)){
			return BuildResult.SUCCESS;
		} else /*if(Result.UNSTABLE.equals(result))*/{
			return BuildResult.UNSTABLE;
		}
	}
}

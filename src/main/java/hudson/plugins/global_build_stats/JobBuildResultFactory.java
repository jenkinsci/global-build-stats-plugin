package hudson.plugins.global_build_stats;

import hudson.model.*;
import hudson.plugins.global_build_stats.model.BuildResult;
import hudson.plugins.global_build_stats.model.JobBuildResult;
import hudson.plugins.global_build_stats.model.JobBuildSearchResult;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

public class JobBuildResultFactory {

	public static final JobBuildResultFactory INSTANCE = new JobBuildResultFactory();
    /** @see hudson.security.ACL#SYSTEM */
	private static final String SYSTEM_USERNAME = "SYSTEM";
	
	private JobBuildResultFactory(){
	}
	
	public JobBuildResult createJobBuildResult(Run r){
        String builtOn = "pipeline";
        String name = "";

        if (r instanceof AbstractBuild) {
            builtOn = ((AbstractBuild)r).getBuiltOnStr();
            name = ((AbstractBuild)r).getProject().getFullName();
        }

        if (r instanceof WorkflowRun) {
            name = ((WorkflowRun)r).getFullDisplayName().replace(" » " ,"/").replaceAll(" #[1-9]+$","");
        }

        return new JobBuildResult(
                        createBuildResult(r.getResult()),
                        name,
                        r.getNumber(),
                        r.getTimestamp(),
                        r.getDuration(),
                        builtOn,
                        extractUserNameIn(r)
                    );
	}

    public JobBuildSearchResult createJobBuildSearchResult(Run build){
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
	
	public static String extractUserNameIn(Run<?,?> build){
		String userName;
        @SuppressWarnings("deprecation") Cause.UserCause uc = build.getCause(Cause.UserCause.class);
		Cause.UserIdCause uic = build.getCause(Cause.UserIdCause.class);
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

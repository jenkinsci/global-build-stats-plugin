package hudson.plugins.global_build_stats;

import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.plugins.global_build_stats.model.BuildResult;
import hudson.plugins.global_build_stats.model.JobBuildResult;

public class JobBuildResultFactory {

	public static final JobBuildResultFactory INSTANCE = new JobBuildResultFactory();
	
	private JobBuildResultFactory(){
	}
	
	public JobBuildResult createJobBuildResult(AbstractBuild build){
		String buildName = build.getProject().getName();
		long duration = build.getDuration();
		String nodeName = build.getBuiltOnStr();
		/* Can't do that since MavenModuleSet is in maven-plugin artefact which is in test scope
		if(build.getProject() instanceof MavenModuleSet){
			buildName = ((MavenModuleSet)build.getProject()).getRootModule().toString();
		}*/
    	return new JobBuildResult(createBuildResult(build.getResult()), buildName, 
    			build.getNumber(), build.getTimestamp(), duration, nodeName);
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

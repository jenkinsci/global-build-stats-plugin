package hudson.plugins.global_build_stats.xstream.migration;

import hudson.model.AbstractBuild;
import hudson.model.Hudson;
import hudson.model.Job;
import hudson.plugins.global_build_stats.model.BuildStatConfiguration;
import hudson.plugins.global_build_stats.model.JobBuildResult;
import hudson.plugins.global_build_stats.model.ModelIdGenerator;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;

public abstract class AbstractMigrator<TFROM extends GlobalBuildStatsPOJO, TTO extends GlobalBuildStatsPOJO> implements GlobalBuildStatsDataMigrator<TFROM, TTO> {
	
	public TTO migrate(TFROM pojo){
		TTO migratedPojo = createMigratedPojo();
		
		migratedPojo.setBuildStatConfigs( migrateBuildStatConfigs(pojo.getBuildStatConfigs()) );
		migratedPojo.setJobBuildResults( migrateJobBuildResults(pojo.getJobBuildResults()) );
		
		return migratedPojo;
	}
	
	public TTO readGlobalBuildStatsPOJO(
			HierarchicalStreamReader reader, UnmarshallingContext context) {
		
		TTO pojo = createMigratedPojo();
		
		reader.moveDown();
		List<JobBuildResult> jobBuildResults = new ArrayList<JobBuildResult>();
		while(reader.hasMoreChildren()){
			reader.moveDown();
			
			JobBuildResult jbr = (JobBuildResult)context.convertAnother(pojo, JobBuildResult.class);
			jobBuildResults.add(jbr);
			
			reader.moveUp();
		}
		reader.moveUp();
		
		reader.moveDown();
		List<BuildStatConfiguration> buildStatConfigs = new ArrayList<BuildStatConfiguration>();
		while(reader.hasMoreChildren()){
			reader.moveDown();
			
			BuildStatConfiguration bsc = (BuildStatConfiguration)context.convertAnother(pojo, BuildStatConfiguration.class);
			buildStatConfigs.add(bsc);

			if(registerBuildStatConfigId()){
				// Registering BuildStatConfiguration's id in the ModelIdGenerator
				ModelIdGenerator.INSTANCE.registerIdForClass(BuildStatConfiguration.class, bsc.getId());
			}
			
			reader.moveUp();
		}
		reader.moveUp();

		pojo.setJobBuildResults(jobBuildResults);
		pojo.setBuildStatConfigs(buildStatConfigs);
		
		return pojo;
	}
	
	// Overridable
	protected List<BuildStatConfiguration> migrateBuildStatConfigs(List<BuildStatConfiguration> buildStatConfigs){
		return new ArrayList<BuildStatConfiguration>(buildStatConfigs);
	}
	
	// Overridable
	protected List<JobBuildResult> migrateJobBuildResults(List<JobBuildResult> jobBuildResults){
		return new ArrayList<JobBuildResult>(jobBuildResults);
	}
	
	// Overridable
	protected boolean registerBuildStatConfigId(){
		return true;
	}
	
	protected static AbstractBuild retrieveBuildFromJobBuildResult(JobBuildResult jbr){
		Job job = (Job)Hudson.getInstance().getItem(jbr.getJobName());
		if(job != null){
			return (AbstractBuild)job.getBuildByNumber(jbr.getBuildNumber());
		}
		return null;
	}
	
	protected abstract TTO createMigratedPojo();
}

package hudson.plugins.global_build_stats.xstream.migration;

import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import hudson.model.AbstractBuild;
import hudson.model.Hudson;
import hudson.model.Job;
import hudson.plugins.global_build_stats.model.BuildStatConfiguration;
import hudson.plugins.global_build_stats.model.JobBuildResult;
import hudson.plugins.global_build_stats.model.JobBuildResultSharder;
import hudson.plugins.global_build_stats.model.ModelIdGenerator;
import hudson.plugins.global_build_stats.rententionstrategies.RetentionStragegy;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMigrator<TFROM extends GlobalBuildStatsPOJO, TTO extends GlobalBuildStatsPOJO> implements GlobalBuildStatsDataMigrator<TFROM, TTO> {
	
	public TTO migrate(TFROM pojo){
		TTO migratedPojo = createMigratedPojo();
		
		migratedPojo.setBuildStatConfigs( migrateBuildStatConfigs(pojo.getBuildStatConfigs()) );
		migratedPojo.setJobBuildResults( migrateJobBuildResults(pojo.getJobBuildResults()) );
        migratedPojo.setRetentionStrategies( migrateRetentionStrategies(pojo.getRetentionStrategies()) );
		
		return migratedPojo;
	}

    public TTO readGlobalBuildStatsPOJO(
			HierarchicalStreamReader reader, UnmarshallingContext context) {
		
		TTO pojo = createMigratedPojo();

        // Since v8, reading JobBuildResults evolved : it is sharded in monthly files
        List<JobBuildResult> jobBuildResults = JobBuildResultSharder.load();

        // Build stat configurations
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

        // Retention strategies
        reader.moveDown();
        List<RetentionStragegy> retentionStrategiesFakedInstances = (List<RetentionStragegy>)context.convertAnother(pojo, List.class);
        List<RetentionStragegy> retentionStrategies = new ArrayList<RetentionStragegy>(retentionStrategiesFakedInstances.size());
        // Retention strategies read are not the same instance as the one in RetentionStrategies.IMPLEMENTATIONS
        for(RetentionStragegy fakeStrategy : retentionStrategiesFakedInstances){
            // So we must convert it to a "true" instance
            RetentionStragegy rs = RetentionStragegy.valueOf(fakeStrategy.getId());
            rs.from(fakeStrategy);
            retentionStrategies.add(rs);
        }
        reader.moveUp();

		pojo.setJobBuildResults(jobBuildResults);
		pojo.setBuildStatConfigs(buildStatConfigs);
        pojo.setRetentionStrategies(retentionStrategies);

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
    protected List<RetentionStragegy> migrateRetentionStrategies(List<RetentionStragegy> retentionStrategies) {
        return new ArrayList<RetentionStragegy>(retentionStrategies);
    }

	// Overridable
	protected boolean registerBuildStatConfigId(){
		return true;
	}
	
	protected static AbstractBuild retrieveBuildFromJobBuildResult(JobBuildResult jbr){
		Job job = (Job)Hudson.getInstance().getItemByFullName(jbr.getJobName());
		if(job != null){
			return (AbstractBuild)job.getBuildByNumber(jbr.getBuildNumber());
		}
		return null;
	}
	
	protected abstract TTO createMigratedPojo();
}

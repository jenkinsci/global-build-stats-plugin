package hudson.plugins.global_build_stats.xstream.migration;

import com.google.common.io.Files;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import hudson.model.Hudson;
import hudson.model.Job;
import hudson.model.Run;
import hudson.plugins.global_build_stats.model.BuildStatConfiguration;
import hudson.plugins.global_build_stats.model.JobBuildResult;
import hudson.plugins.global_build_stats.model.JobBuildResultSharder;
import hudson.plugins.global_build_stats.model.ModelIdGenerator;
import hudson.plugins.global_build_stats.xstream.GlobalBuildStatsXStreamConverter;
import hudson.plugins.global_build_stats.xstream.migration.GlobalBuildStatsDataMigrator;
import hudson.plugins.global_build_stats.xstream.migration.GlobalBuildStatsPOJO;
import jenkins.model.Jenkins;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class PreV9AbstractMigrator<TFROM extends GlobalBuildStatsPOJO, TTO extends GlobalBuildStatsPOJO> implements GlobalBuildStatsDataMigrator<TFROM, TTO> {
	
	public TTO migrate(TFROM pojo){
		TTO migratedPojo = createMigratedPojo();
		
		migratedPojo.setBuildStatConfigs( migrateBuildStatConfigs(pojo.getBuildStatConfigs()) );
		migratedPojo.setJobBuildResults( migrateJobBuildResults(pojo.getJobBuildResults()) );
		
		return migratedPojo;
	}
	
	public TTO readGlobalBuildStatsPOJO(
			HierarchicalStreamReader reader, UnmarshallingContext context) {
		
		TTO pojo = createMigratedPojo();

        // Since v8, reading JobBuildResults evolved : it is sharded in monthly files
        List<JobBuildResult> jobBuildResults = JobBuildResultSharder.load();

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
	
	protected static Run<?, ?> retrieveBuildFromJobBuildResult(JobBuildResult jbr){
		Job job = (Job) Jenkins.getInstance().getItemByFullName(jbr.getJobName());
		if(job != null){
			return (Run<?, ?>)job.getBuildByNumber(jbr.getBuildNumber());
		}
		return null;
	}
	
	protected abstract TTO createMigratedPojo();
}

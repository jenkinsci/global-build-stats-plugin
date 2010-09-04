package hudson.plugins.global_build_stats.xstream.migration.v3;

import hudson.model.AbstractBuild;
import hudson.model.Hudson;
import hudson.model.Job;
import hudson.plugins.global_build_stats.model.BuildStatConfiguration;
import hudson.plugins.global_build_stats.model.JobBuildResult;
import hudson.plugins.global_build_stats.xstream.migration.GlobalBuildStatsDataMigrator;
import hudson.plugins.global_build_stats.xstream.migration.v2.V2GlobalBuildStatsPOJO;

import java.util.ArrayList;

/**
 * V3 Evolutions :
 * - JobBuildResult.duration and JobBuildResult.nodeName attributes added
 * @author fcamblor
 */
public class V2ToV3Migrator implements GlobalBuildStatsDataMigrator<V2GlobalBuildStatsPOJO, V3GlobalBuildStatsPOJO> {

	public V3GlobalBuildStatsPOJO migrate(V2GlobalBuildStatsPOJO pojo) {
		V3GlobalBuildStatsPOJO migratedPojo = new V3GlobalBuildStatsPOJO();
		
		migratedPojo.buildStatConfigs = new ArrayList<BuildStatConfiguration>();
		migratedPojo.jobBuildResults = new ArrayList<JobBuildResult>();
		
		for(JobBuildResult jbr : pojo.jobBuildResults){
			// Providing JobBuildResult.duration & nodeName attributes
			long duration = JobBuildResult.EMPTY_DURATION;
			String nodeName = JobBuildResult.EMPTY_NODE_NAME;
			Job job = (Job)Hudson.getInstance().getItem(jbr.getJobName());
			if(job != null){
				AbstractBuild b = (AbstractBuild)job.getBuildByNumber(jbr.getBuildNumber());
				if(b != null){
					duration = b.getDuration();
					nodeName = b.getBuiltOnStr();
				}
			}
			
			jbr.setDuration(duration);
			jbr.setNodeName(nodeName);
			
			migratedPojo.jobBuildResults.add(jbr);
		}
		migratedPojo.buildStatConfigs.addAll(pojo.buildStatConfigs);
		
		return migratedPojo;
	}

}

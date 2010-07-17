package hudson.plugins.global_build_stats.xstream.migration.v1;

import java.util.ArrayList;

import hudson.plugins.global_build_stats.JobFilterFactory;
import hudson.plugins.global_build_stats.model.BuildStatConfiguration;
import hudson.plugins.global_build_stats.model.JobBuildResult;
import hudson.plugins.global_build_stats.model.ModelIdGenerator;
import hudson.plugins.global_build_stats.xstream.migration.GlobalBuildStatsDataMigrator;
import hudson.plugins.global_build_stats.xstream.migration.v0.V0GlobalBuildStatsPOJO;

/**
 * V1 Evolutions :
 * - No more empty BuildStatConfig's jobFilter in data configuration
 * - BuildStatConfiguration id added
 * @author fcamblor
 */
public class V0ToV1Migrator implements GlobalBuildStatsDataMigrator<V0GlobalBuildStatsPOJO, V1GlobalBuildStatsPOJO> {

	public V1GlobalBuildStatsPOJO migrate(V0GlobalBuildStatsPOJO pojo) {
		V1GlobalBuildStatsPOJO migratedPojo = new V1GlobalBuildStatsPOJO();
		
		migratedPojo.buildStatConfigs = new ArrayList<BuildStatConfiguration>();
		migratedPojo.jobBuildResults = new ArrayList<JobBuildResult>();
		
		for(BuildStatConfiguration cfg : pojo.buildStatConfigs){
			// For some reasons, in v0, job filter was able to be empty... fixed this !
			if(null==cfg.getJobFilter() || "".equals(cfg.getJobFilter())){
				cfg.setJobFilter(JobFilterFactory.ALL_JOBS_FILTER_PATTERN);
			}
			
			// Providing buildStatConfiguration id
			cfg.setId(ModelIdGenerator.INSTANCE.generateIdForClass(BuildStatConfiguration.class));
			
			migratedPojo.buildStatConfigs.add(cfg);
		}
		migratedPojo.jobBuildResults.addAll(pojo.jobBuildResults);
		
		return migratedPojo;
	}

}

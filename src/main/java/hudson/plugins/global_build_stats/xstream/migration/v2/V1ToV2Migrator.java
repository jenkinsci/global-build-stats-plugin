package hudson.plugins.global_build_stats.xstream.migration.v2;

import hudson.plugins.global_build_stats.model.BuildStatConfiguration;
import hudson.plugins.global_build_stats.model.JobBuildResult;
import hudson.plugins.global_build_stats.model.YAxisChartType;
import hudson.plugins.global_build_stats.xstream.migration.GlobalBuildStatsDataMigrator;
import hudson.plugins.global_build_stats.xstream.migration.v1.V1GlobalBuildStatsPOJO;

import java.util.ArrayList;

/**
 * V2 Evolutions :
 * - BuildStatConfiguration.yAxisChartType attribute added
 * @author fcamblor
 */
public class V1ToV2Migrator implements GlobalBuildStatsDataMigrator<V1GlobalBuildStatsPOJO, V2GlobalBuildStatsPOJO> {

	public V2GlobalBuildStatsPOJO migrate(V1GlobalBuildStatsPOJO pojo) {
		V2GlobalBuildStatsPOJO migratedPojo = new V2GlobalBuildStatsPOJO();
		
		migratedPojo.buildStatConfigs = new ArrayList<BuildStatConfiguration>();
		migratedPojo.jobBuildResults = new ArrayList<JobBuildResult>();
		
		for(BuildStatConfiguration cfg : pojo.buildStatConfigs){
			// Providing buildStatConfiguration.yAxisChartType attribute
			cfg.setyAxisChartType(YAxisChartType.COUNT);
			
			migratedPojo.buildStatConfigs.add(cfg);
		}
		migratedPojo.jobBuildResults.addAll(pojo.jobBuildResults);
		
		return migratedPojo;
	}

}

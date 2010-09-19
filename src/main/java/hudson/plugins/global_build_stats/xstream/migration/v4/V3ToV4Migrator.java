package hudson.plugins.global_build_stats.xstream.migration.v4;

import hudson.plugins.global_build_stats.model.BuildStatConfiguration;
import hudson.plugins.global_build_stats.model.JobBuildResult;
import hudson.plugins.global_build_stats.model.YAxisChartDimension;
import hudson.plugins.global_build_stats.xstream.migration.GlobalBuildStatsDataMigrator;
import hudson.plugins.global_build_stats.xstream.migration.v3.V3GlobalBuildStatsPOJO;

import java.util.ArrayList;

/**
 * V4 Evolutions :
 * - BuildStatConfiguration.dimensionsShown attribute added
 * @author fcamblor
 */
public class V3ToV4Migrator implements GlobalBuildStatsDataMigrator<V3GlobalBuildStatsPOJO, V4GlobalBuildStatsPOJO> {

	public V4GlobalBuildStatsPOJO migrate(V3GlobalBuildStatsPOJO pojo) {
		V4GlobalBuildStatsPOJO migratedPojo = new V4GlobalBuildStatsPOJO();
		
		migratedPojo.buildStatConfigs = new ArrayList<BuildStatConfiguration>();
		migratedPojo.jobBuildResults = new ArrayList<JobBuildResult>();
		
		for(BuildStatConfiguration cfg : pojo.buildStatConfigs){
			// By default, we were only displaying count dimension (and not average/total build duration)
			cfg.setDimensionsShown(new YAxisChartDimension[]{ YAxisChartDimension.BUILD_COUNTER });
			
			migratedPojo.buildStatConfigs.add(cfg);
		}
		migratedPojo.jobBuildResults.addAll(pojo.jobBuildResults);
		
		return migratedPojo;
	}

}

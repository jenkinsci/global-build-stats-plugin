package hudson.plugins.global_build_stats.xstream.migration.v1;

import hudson.plugins.global_build_stats.xstream.migration.GlobalBuildStatsDataMigrator;
import hudson.plugins.global_build_stats.xstream.migration.v0.V0GlobalBuildStatsPOJO;


public class V0ToV1Migrator implements GlobalBuildStatsDataMigrator<V0GlobalBuildStatsPOJO, V1GlobalBuildStatsPOJO> {

	public V1GlobalBuildStatsPOJO migrate(V0GlobalBuildStatsPOJO pojo) {
		V1GlobalBuildStatsPOJO migratedPojo = new V1GlobalBuildStatsPOJO();
		
		migratedPojo.buildStatConfigs = pojo.buildStatConfigs;
		migratedPojo.jobBuildResults = pojo.jobBuildResults;
		
		return migratedPojo;
	}

}

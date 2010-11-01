package hudson.plugins.global_build_stats.xstream.migration.v2;

import hudson.plugins.global_build_stats.model.BuildStatConfiguration;
import hudson.plugins.global_build_stats.model.YAxisChartType;
import hudson.plugins.global_build_stats.xstream.migration.AbstractMigrator;
import hudson.plugins.global_build_stats.xstream.migration.v1.V1GlobalBuildStatsPOJO;

import java.util.ArrayList;
import java.util.List;

/**
 * V2 Evolutions :
 * - BuildStatConfiguration.yAxisChartType attribute added
 * @author fcamblor
 */
public class V1ToV2Migrator extends AbstractMigrator<V1GlobalBuildStatsPOJO, V2GlobalBuildStatsPOJO> {

	@Override
	protected V2GlobalBuildStatsPOJO createMigratedPojo() {
		return new V2GlobalBuildStatsPOJO();
	}
	
	@Override
	protected List<BuildStatConfiguration> migrateBuildStatConfigs(
			List<BuildStatConfiguration> buildStatConfigs) {
		
		ArrayList<BuildStatConfiguration> migratedBuildStatConfigs = new ArrayList<BuildStatConfiguration>();
		for(BuildStatConfiguration cfg : buildStatConfigs){
			// Providing buildStatConfiguration.yAxisChartType attribute
			cfg.setyAxisChartType(YAxisChartType.COUNT);
			
			migratedBuildStatConfigs.add(cfg);
		}
		return migratedBuildStatConfigs;
	}
}

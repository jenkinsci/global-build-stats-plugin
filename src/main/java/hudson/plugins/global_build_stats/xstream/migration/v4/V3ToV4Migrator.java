package hudson.plugins.global_build_stats.xstream.migration.v4;

import hudson.plugins.global_build_stats.model.BuildStatConfiguration;
import hudson.plugins.global_build_stats.model.YAxisChartDimension;
import hudson.plugins.global_build_stats.xstream.migration.AbstractMigrator;
import hudson.plugins.global_build_stats.xstream.migration.v3.V3GlobalBuildStatsPOJO;

import java.util.ArrayList;
import java.util.List;

/**
 * V4 Evolutions :
 * - BuildStatConfiguration.dimensionsShown attribute added
 * @author fcamblor
 */
public class V3ToV4Migrator extends AbstractMigrator<V3GlobalBuildStatsPOJO, V4GlobalBuildStatsPOJO> {

	@Override
	protected V4GlobalBuildStatsPOJO createMigratedPojo() {
		return new V4GlobalBuildStatsPOJO();
	}
	
	@Override
	protected List<BuildStatConfiguration> migrateBuildStatConfigs(
			List<BuildStatConfiguration> buildStatConfigs) {
		
		ArrayList<BuildStatConfiguration> migratedBuildStatConfigs = new ArrayList<BuildStatConfiguration>();
		for(BuildStatConfiguration cfg : buildStatConfigs){
			// By default, we were only displaying count dimension (and not average/total build duration)
			cfg.setDimensionsShown(new YAxisChartDimension[]{ YAxisChartDimension.BUILD_COUNTER });
			
			migratedBuildStatConfigs.add(cfg);
		}
		return migratedBuildStatConfigs;
	}
}

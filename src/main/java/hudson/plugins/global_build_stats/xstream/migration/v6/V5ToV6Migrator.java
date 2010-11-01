package hudson.plugins.global_build_stats.xstream.migration.v6;

import hudson.plugins.global_build_stats.model.BuildSearchCriteria;
import hudson.plugins.global_build_stats.model.BuildStatConfiguration;
import hudson.plugins.global_build_stats.xstream.migration.AbstractMigrator;
import hudson.plugins.global_build_stats.xstream.migration.v5.V5GlobalBuildStatsPOJO;

import java.util.ArrayList;
import java.util.List;

/**
 * V6 Evolutions :
 * - Creation of BuildStatConfiguration.buildFilters and move of jobFilter, nodeFilter and shownBuildResults into this encapsulated class
 * @author fcamblor
 */
public class V5ToV6Migrator extends AbstractMigrator<V5GlobalBuildStatsPOJO, V6GlobalBuildStatsPOJO> {

	@Override
	protected V6GlobalBuildStatsPOJO createMigratedPojo() {
		return new V6GlobalBuildStatsPOJO();
	}
	
	@Override
	protected List<BuildStatConfiguration> migrateBuildStatConfigs(
			List<BuildStatConfiguration> buildStatConfigs) {
		
		ArrayList<BuildStatConfiguration> migratedBuildStatConfigs = new ArrayList<BuildStatConfiguration>();
		for(BuildStatConfiguration cfg : buildStatConfigs){
			// Migrating old data into new BuildSearchCriteria which will be persisted !
			BuildSearchCriteria criteria = new BuildSearchCriteria(
					cfg.getJobFilter(), 
					cfg.getNodeFilter(), 
					cfg.isSuccessShown(), 
					cfg.isFailuresShown(), 
					cfg.isUnstablesShown(), 
					cfg.isAbortedShown(), 
					cfg.isNotBuildShown());
			
			cfg.setBuildFilters(criteria);
			
			migratedBuildStatConfigs.add(cfg);
		}
		return migratedBuildStatConfigs;
	}
}

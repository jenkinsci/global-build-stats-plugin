package hudson.plugins.global_build_stats.xstream.migration.v6;

import hudson.plugins.global_build_stats.model.BuildSearchCriteria;
import hudson.plugins.global_build_stats.model.BuildStatConfiguration;
import hudson.plugins.global_build_stats.model.JobBuildResult;
import hudson.plugins.global_build_stats.xstream.migration.GlobalBuildStatsDataMigrator;
import hudson.plugins.global_build_stats.xstream.migration.v5.V5GlobalBuildStatsPOJO;

import java.util.ArrayList;

/**
 * V6 Evolutions :
 * - Creation of BuildStatConfiguration.buildFilters and move of jobFilter, nodeFilter and shownBuildResults into this encapsulated class
 * @author fcamblor
 */
public class V5ToV6Migrator implements GlobalBuildStatsDataMigrator<V5GlobalBuildStatsPOJO, V6GlobalBuildStatsPOJO> {

	public V6GlobalBuildStatsPOJO migrate(V5GlobalBuildStatsPOJO pojo) {
		V6GlobalBuildStatsPOJO migratedPojo = new V6GlobalBuildStatsPOJO();
		
		migratedPojo.buildStatConfigs = new ArrayList<BuildStatConfiguration>();
		migratedPojo.jobBuildResults = new ArrayList<JobBuildResult>();
		
		for(BuildStatConfiguration cfg : pojo.buildStatConfigs){
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
			
			migratedPojo.buildStatConfigs.add(cfg);
		}
		migratedPojo.jobBuildResults.addAll(pojo.jobBuildResults);
		
		return migratedPojo;
	}

}

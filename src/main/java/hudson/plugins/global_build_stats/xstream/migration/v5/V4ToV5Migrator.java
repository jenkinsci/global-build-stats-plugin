package hudson.plugins.global_build_stats.xstream.migration.v5;

import hudson.plugins.global_build_stats.FieldFilterFactory;
import hudson.plugins.global_build_stats.model.BuildStatConfiguration;
import hudson.plugins.global_build_stats.model.JobBuildResult;
import hudson.plugins.global_build_stats.xstream.migration.GlobalBuildStatsDataMigrator;
import hudson.plugins.global_build_stats.xstream.migration.v4.V4GlobalBuildStatsPOJO;

import java.util.ArrayList;

/**
 * V5 Evolutions :
 * - BuildStatConfiguration.nodeFilter attribute added
 * - BuildStatConfiguration.jobFilter values refactored ("jobNameRegex" => "fieldRegex")
 * @author fcamblor
 */
public class V4ToV5Migrator implements GlobalBuildStatsDataMigrator<V4GlobalBuildStatsPOJO, V5GlobalBuildStatsPOJO> {

	public V5GlobalBuildStatsPOJO migrate(V4GlobalBuildStatsPOJO pojo) {
		V5GlobalBuildStatsPOJO migratedPojo = new V5GlobalBuildStatsPOJO();
		
		migratedPojo.buildStatConfigs = new ArrayList<BuildStatConfiguration>();
		migratedPojo.jobBuildResults = new ArrayList<JobBuildResult>();
		
		for(BuildStatConfiguration cfg : pojo.buildStatConfigs){
			// By default, no filtering on node names
			cfg.setNodeFilter(FieldFilterFactory.ALL_VALUES_FILTER_LABEL);
			
			// Replacing "jobNameRegex" by "fieldRegex" in job filters
			cfg.setJobFilter(cfg.getJobFilter().replaceAll(FieldFilterFactory.OLD_JOB_NAME_REGEX_LABEL, FieldFilterFactory.REGEX_FIELD_FILTER_LABEL));
			
			migratedPojo.buildStatConfigs.add(cfg);
		}
		migratedPojo.jobBuildResults.addAll(pojo.jobBuildResults);
		
		return migratedPojo;
	}

}

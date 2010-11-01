package hudson.plugins.global_build_stats.xstream.migration.v5;

import hudson.plugins.global_build_stats.FieldFilterFactory;
import hudson.plugins.global_build_stats.model.BuildStatConfiguration;
import hudson.plugins.global_build_stats.xstream.migration.AbstractMigrator;
import hudson.plugins.global_build_stats.xstream.migration.v4.V4GlobalBuildStatsPOJO;

import java.util.ArrayList;
import java.util.List;

/**
 * V5 Evolutions :
 * - BuildStatConfiguration.nodeFilter attribute added
 * - BuildStatConfiguration.jobFilter values refactored ("jobNameRegex" => "fieldRegex")
 * @author fcamblor
 */
public class V4ToV5Migrator extends AbstractMigrator<V4GlobalBuildStatsPOJO, V5GlobalBuildStatsPOJO> {

	@Override
	protected V5GlobalBuildStatsPOJO createMigratedPojo() {
		return new V5GlobalBuildStatsPOJO();
	}
	
	@Override
	protected List<BuildStatConfiguration> migrateBuildStatConfigs(
			List<BuildStatConfiguration> buildStatConfigs) {
		
		ArrayList<BuildStatConfiguration> migratedBuildStatConfigs = new ArrayList<BuildStatConfiguration>();
		for(BuildStatConfiguration cfg : buildStatConfigs){
			// By default, no filtering on node names
			cfg.setNodeFilter(FieldFilterFactory.ALL_VALUES_FILTER_LABEL);
			
			// Replacing "jobNameRegex" by "fieldRegex" in job filters
			cfg.setJobFilter(cfg.getJobFilter().replaceAll(FieldFilterFactory.OLD_JOB_NAME_REGEX_LABEL, FieldFilterFactory.REGEX_FIELD_FILTER_LABEL));
			
			migratedBuildStatConfigs.add(cfg);
		}
		return migratedBuildStatConfigs;
	}
}

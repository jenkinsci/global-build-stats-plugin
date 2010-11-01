package hudson.plugins.global_build_stats.xstream.migration.v1;

import hudson.plugins.global_build_stats.FieldFilterFactory;
import hudson.plugins.global_build_stats.model.BuildStatConfiguration;
import hudson.plugins.global_build_stats.model.ModelIdGenerator;
import hudson.plugins.global_build_stats.xstream.migration.AbstractMigrator;
import hudson.plugins.global_build_stats.xstream.migration.v0.V0GlobalBuildStatsPOJO;

import java.util.ArrayList;
import java.util.List;

/**
 * V1 Evolutions :
 * - No more empty BuildStatConfig's jobFilter in data configuration
 * - BuildStatConfiguration id added
 * @author fcamblor
 */
public class V0ToV1Migrator extends AbstractMigrator<V0GlobalBuildStatsPOJO, V1GlobalBuildStatsPOJO> {

	@Override
	protected V1GlobalBuildStatsPOJO createMigratedPojo() {
		return new V1GlobalBuildStatsPOJO();
	}
	
	@Override
	protected List<BuildStatConfiguration> migrateBuildStatConfigs(
			List<BuildStatConfiguration> buildStatConfigs) {
		
		ArrayList<BuildStatConfiguration> migratedBuildStatConfigs = new ArrayList<BuildStatConfiguration>();
		for(BuildStatConfiguration cfg : buildStatConfigs){
			// For some reasons, in v0, job filter was able to be empty... fixed this !
			if(null==cfg.getJobFilter() || "".equals(cfg.getJobFilter())){
				cfg.setJobFilter(FieldFilterFactory.ALL_VALUES_FILTER_LABEL);
			}
			
			// Providing buildStatConfiguration id
			cfg.setId(ModelIdGenerator.INSTANCE.generateIdForClass(BuildStatConfiguration.class));
			
			migratedBuildStatConfigs.add(cfg);
		}
		return migratedBuildStatConfigs;
	}
}

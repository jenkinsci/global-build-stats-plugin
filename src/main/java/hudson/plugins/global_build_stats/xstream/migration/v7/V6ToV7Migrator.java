package hudson.plugins.global_build_stats.xstream.migration.v7;

import hudson.model.AbstractBuild;
import hudson.plugins.global_build_stats.FieldFilterFactory;
import hudson.plugins.global_build_stats.JobBuildResultFactory;
import hudson.plugins.global_build_stats.model.BuildSearchCriteria;
import hudson.plugins.global_build_stats.model.BuildStatConfiguration;
import hudson.plugins.global_build_stats.model.JobBuildResult;
import hudson.plugins.global_build_stats.xstream.migration.AbstractMigrator;
import hudson.plugins.global_build_stats.xstream.migration.v6.V6GlobalBuildStatsPOJO;

import java.util.ArrayList;
import java.util.List;

/**
 * V7 Evolutions :
 * - Provided username having launched the build in JobBuildResult
 * @author fcamblor
 */
public class V6ToV7Migrator extends AbstractMigrator<V6GlobalBuildStatsPOJO, V7GlobalBuildStatsPOJO> {

	@Override
	protected V7GlobalBuildStatsPOJO createMigratedPojo() {
		return new V7GlobalBuildStatsPOJO();
	}
	
	@Override
	protected List<BuildStatConfiguration> migrateBuildStatConfigs(
			List<BuildStatConfiguration> buildStatConfigs) {
		
		ArrayList<BuildStatConfiguration> migratedBuildStatConfigs = new ArrayList<BuildStatConfiguration>();
		for(BuildStatConfiguration cfg : buildStatConfigs){
			// Migrating old data into new BuildSearchCriteria which will be persisted !
			BuildSearchCriteria criteria = new BuildSearchCriteria(
					cfg.getBuildFilters().getJobFilter(), 
					cfg.getBuildFilters().getNodeFilter(),
					// Providing username filter
					FieldFilterFactory.ALL_VALUES_FILTER_LABEL,
					cfg.getBuildFilters().isSuccessShown(), 
					cfg.getBuildFilters().isFailuresShown(), 
					cfg.getBuildFilters().isUnstablesShown(), 
					cfg.getBuildFilters().isAbortedShown(), 
					cfg.getBuildFilters().isNotBuildShown());
			
			cfg.setBuildFilters(criteria);
			
			migratedBuildStatConfigs.add(cfg);
		}
		return migratedBuildStatConfigs;
	}
	
	@Override
	protected List<JobBuildResult> migrateJobBuildResults(List<JobBuildResult> jobBuildResults) {

		ArrayList<JobBuildResult> migratedJobBuildResults = new ArrayList<JobBuildResult>();
		for(JobBuildResult jbr : jobBuildResults){
			AbstractBuild b = retrieveBuildFromJobBuildResult(jbr);
			if(b != null){
				String userName = JobBuildResultFactory.extractUserNameIn(b);
				jbr.setUserName(userName);
			}
			// If build information cannot be retrieved, don't provide any userName information
			
			migratedJobBuildResults.add(jbr);
		}
		return migratedJobBuildResults;
	}
}

package hudson.plugins.global_build_stats.xstream.migration.v3;

import hudson.model.AbstractBuild;
import hudson.model.Run;
import hudson.plugins.global_build_stats.model.JobBuildResult;
import hudson.plugins.global_build_stats.xstream.migration.PreV8AbstractMigrator;
import hudson.plugins.global_build_stats.xstream.migration.v2.V2GlobalBuildStatsPOJO;

import java.util.ArrayList;
import java.util.List;

/**
 * V3 Evolutions :
 * - JobBuildResult.duration and JobBuildResult.nodeName attributes added
 * @author fcamblor
 */
public class V2ToV3Migrator extends PreV8AbstractMigrator<V2GlobalBuildStatsPOJO, V3GlobalBuildStatsPOJO> {

	@Override
	protected V3GlobalBuildStatsPOJO createMigratedPojo() {
		return new V3GlobalBuildStatsPOJO();
	}
	
	@Override
	protected List<JobBuildResult> migrateJobBuildResults(
			List<JobBuildResult> jobBuildResults) {

		ArrayList<JobBuildResult> migratedJobBuildResults = new ArrayList<JobBuildResult>();
		for(JobBuildResult jbr : jobBuildResults){
			// Providing JobBuildResult.duration & nodeName attributes
			long duration = JobBuildResult.EMPTY_DURATION;
			String nodeName = JobBuildResult.EMPTY_NODE_NAME;
			Run<?, ?> b = retrieveBuildFromJobBuildResult(jbr);
			if(b != null){
				duration = b.getDuration();
				nodeName = (b instanceof AbstractBuild)
						? ((AbstractBuild<?, ?>) b).getBuiltOnStr()
						: "";
			}
			
			jbr.setDuration(duration);
			jbr.setNodeName(nodeName);
			
			migratedJobBuildResults.add(jbr);
		}
		return migratedJobBuildResults;
	}
}

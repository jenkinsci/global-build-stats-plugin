package hudson.plugins.global_build_stats.xstream.migration.v1;

import hudson.plugins.global_build_stats.model.BuildStatConfiguration;
import hudson.plugins.global_build_stats.model.JobBuildResult;
import hudson.plugins.global_build_stats.xstream.migration.GlobalBuildStatsXStreamReader;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;

/**
 * Reader for GlobalBuildStats v1 XStream representation
 * @author fcamblor
 */
public class V1XStreamReader implements GlobalBuildStatsXStreamReader<V1GlobalBuildStatsPOJO>{

	public V1GlobalBuildStatsPOJO readGlobalBuildStatsPOJO(
			HierarchicalStreamReader reader, UnmarshallingContext context) {

		V1GlobalBuildStatsPOJO pojo = new V1GlobalBuildStatsPOJO();
		
		reader.moveDown();
		List<JobBuildResult> jobBuildResults = new ArrayList<JobBuildResult>();
		while(reader.hasMoreChildren()){
			reader.moveDown();
			
			JobBuildResult jbr = (JobBuildResult)context.convertAnother(pojo, JobBuildResult.class);
			jobBuildResults.add(jbr);
			
			reader.moveUp();
		}
		reader.moveUp();
		
		reader.moveDown();
		List<BuildStatConfiguration> buildStatConfigs = new ArrayList<BuildStatConfiguration>();
		while(reader.hasMoreChildren()){
			reader.moveDown();
			
			BuildStatConfiguration bsc = (BuildStatConfiguration)context.convertAnother(pojo, BuildStatConfiguration.class);
			buildStatConfigs.add(bsc);

			reader.moveUp();
		}
		reader.moveUp();

		pojo.jobBuildResults = jobBuildResults;
		pojo.buildStatConfigs = buildStatConfigs;
		
		return pojo;
	}

}

package hudson.plugins.global_build_stats.xstream.migration.v4;

import hudson.plugins.global_build_stats.model.BuildStatConfiguration;
import hudson.plugins.global_build_stats.model.JobBuildResult;
import hudson.plugins.global_build_stats.model.ModelIdGenerator;
import hudson.plugins.global_build_stats.xstream.migration.GlobalBuildStatsXStreamReader;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;

/**
 * Reader for GlobalBuildStats v4 XStream representation
 * @author fcamblor
 */
public class V4XStreamReader implements GlobalBuildStatsXStreamReader<V4GlobalBuildStatsPOJO>{

	public V4GlobalBuildStatsPOJO readGlobalBuildStatsPOJO(
			HierarchicalStreamReader reader, UnmarshallingContext context) {

		V4GlobalBuildStatsPOJO pojo = new V4GlobalBuildStatsPOJO();
		
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
			
			// Registering BuildStatConfiguration's id in the ModelIdGenerator
			ModelIdGenerator.INSTANCE.registerIdForClass(BuildStatConfiguration.class, bsc.getId());

			reader.moveUp();
		}
		reader.moveUp();

		pojo.jobBuildResults = jobBuildResults;
		pojo.buildStatConfigs = buildStatConfigs;
		
		return pojo;
	}
}

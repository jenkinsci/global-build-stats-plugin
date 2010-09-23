package hudson.plugins.global_build_stats.xstream;

import hudson.plugins.global_build_stats.GlobalBuildStatsPlugin;
import hudson.plugins.global_build_stats.model.BuildStatConfiguration;
import hudson.plugins.global_build_stats.model.JobBuildResult;
import hudson.plugins.global_build_stats.xstream.migration.GlobalBuildStatsDataMigrator;
import hudson.plugins.global_build_stats.xstream.migration.GlobalBuildStatsPOJO;
import hudson.plugins.global_build_stats.xstream.migration.GlobalBuildStatsXStreamReader;
import hudson.plugins.global_build_stats.xstream.migration.v0.V0XStreamReader;
import hudson.plugins.global_build_stats.xstream.migration.v1.V0ToV1Migrator;
import hudson.plugins.global_build_stats.xstream.migration.v1.V1XStreamReader;
import hudson.plugins.global_build_stats.xstream.migration.v2.V1ToV2Migrator;
import hudson.plugins.global_build_stats.xstream.migration.v2.V2XStreamReader;
import hudson.plugins.global_build_stats.xstream.migration.v3.V2ToV3Migrator;
import hudson.plugins.global_build_stats.xstream.migration.v3.V3XStreamReader;
import hudson.plugins.global_build_stats.xstream.migration.v4.V3ToV4Migrator;
import hudson.plugins.global_build_stats.xstream.migration.v4.V4XStreamReader;
import hudson.plugins.global_build_stats.xstream.migration.v5.V4ToV5Migrator;
import hudson.plugins.global_build_stats.xstream.migration.v5.V5XStreamReader;
import hudson.plugins.global_build_stats.xstream.migration.v6.V5ToV6Migrator;
import hudson.plugins.global_build_stats.xstream.migration.v6.V6GlobalBuildStatsPOJO;
import hudson.plugins.global_build_stats.xstream.migration.v6.V6XStreamReader;

import java.util.logging.Logger;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * XStream converter for GlobalBuildStatsPlugin XStream data
 * Allows to provide API to migrate from one version to another of persisted global build stats data
 * When creating a new migrator you must :
 * - Create a new package hudson.plugins.global_build_stats.xstream.migration.v[X]
 * - Inside this package, copy/paste every classes located in hudson.plugins.global_build_stats.xstream.migration.v[X-1]
 * - Rename every *V[X-1]* POJOs to *V[X]* POJO
 * - Eventually, change attributes in V[X]GlobalBuildStatsPOJO (for example, if additionnal attribute has appeared)
 * - If parsing algorithm has changed, update V[X]XstreamReader with the new algorithm (if, for example, new root elements
 * has appeared in XStream file)
 * - Update GlobalBuildStatsXStreamConverter.populateGlobalBuildStatsPlugin() and cast pojo parameter to last
 * V[X]GlobalBuildStatsPOJO
 * - Update GlobalBuildStatsXStreamConverter.READERS and GlobalBuildStatsXStreamConverter.MIGRATORS with new provided classes
 * @author fcamblor
 */
public class GlobalBuildStatsXStreamConverter implements Converter {
	
    private static final Logger LOGGER = Logger.getLogger(GlobalBuildStatsXStreamConverter.class.getName());
    
    public static final String BUILD_STAT_CONFIG_CLASS_ALIAS = "bsc";
    public static final String JOB_BUILD_RESULT_CLASS_ALIAS = "jbr";
    public static final String BUILD_SEARCH_CRITERIA_CLASS_ALIAS = "bscr";
    public static final String HISTORIC_SCALE_CLASS_ALIAS = "GBS_HS";
    public static final String YAXIS_CHART_TYPE_CLASS_ALIAS = "GBS_YACT";
    public static final String YAXIS_CHART_DIMENSION_CLASS_ALIAS = "GBS_YACD";

	/**
	 * List of XStream readers for previous XStream representations of
	 * GlobalBuildStatsPlugin
	 */
	private static final GlobalBuildStatsXStreamReader[] READERS = new GlobalBuildStatsXStreamReader[]{
		new V0XStreamReader(),
		new V1XStreamReader(),
		new V2XStreamReader(),
		new V3XStreamReader(),
		new V4XStreamReader(),
		new V5XStreamReader(),
		new V6XStreamReader()
	};

	/**
	 * Migrators for old versions of GlobalBuildStatsPlugin data representations
	 */
	private static final GlobalBuildStatsDataMigrator[] MIGRATORS = new GlobalBuildStatsDataMigrator[]{
		new V0ToV1Migrator(),
		new V1ToV2Migrator(),
		new V2ToV3Migrator(),
		new V3ToV4Migrator(),
		new V4ToV5Migrator(),
		new V5ToV6Migrator()
	};

	/**
	 * Converter is only applicable on GlobalBuildStatsPlugin data
	 */
	public boolean canConvert(Class type) {
		return GlobalBuildStatsPlugin.class.isAssignableFrom(type);
	}

	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		
		GlobalBuildStatsPlugin plugin = (GlobalBuildStatsPlugin)source;
		
		// Since "v1", providing version number in globalbuildstats heading tag
		writer.addAttribute("version", String.valueOf(getCurrentGlobalBuildStatsVersionNumber()));
		
		// Serializing job build results
		writer.startNode("jobBuildResults");
		if(plugin.getJobBuildResults() != null){
			for(JobBuildResult r: plugin.getJobBuildResults()){
				writer.startNode(BUILD_STAT_CONFIG_CLASS_ALIAS);
				context.convertAnother(r);
				writer.endNode();
			}
		}
		writer.endNode();
		
		// Serializing build stat configurations
		writer.startNode("buildStatConfigs");
		if(plugin.getBuildStatConfigs() != null){
			for(BuildStatConfiguration c: plugin.getBuildStatConfigs()){
				writer.startNode(JOB_BUILD_RESULT_CLASS_ALIAS);
				context.convertAnother(c);
				writer.endNode();
			}
		}
		writer.endNode();
	}
	
	/**
	 * @return current version number of global build stats plugin
	 * data representation in XStream
	 */
	private static int getCurrentGlobalBuildStatsVersionNumber(){
		return MIGRATORS.length;
	}

	/**
	 * Will transform global build stats XStream data representation into
	 * current GlobalBuildStatsPlugin instance
	 */
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		
		GlobalBuildStatsPlugin plugin;
		if(context.currentObject() == null || !(context.currentObject() instanceof GlobalBuildStatsPlugin)){
			// Retrieving already instantiated GlobalBuildStats plugin into current context ..
			plugin = new GlobalBuildStatsPlugin();
		} else {
			// This should never happen to get here
			plugin = (GlobalBuildStatsPlugin)context.currentObject();
		}

		// Retrieving data representation version number
		String version = reader.getAttribute("version");
		// Before version 1 (version 0), there wasn't any version in the globalbuildstats 
		// configuration file
		int versionNumber = 0;
		if(version != null){
			versionNumber = Integer.parseInt(version);
		}
		
		if(versionNumber != getCurrentGlobalBuildStatsVersionNumber()){
			// There will be a data migration ..
			LOGGER.info("Your version of persisted GlobalBuildStatsPlugin data is not up-to-date (v"+versionNumber+" < v"+getCurrentGlobalBuildStatsVersionNumber()+") : data will be migrated !");
		}
		
		// Calling version's reader to read data representation
		GlobalBuildStatsPOJO pojo = READERS[versionNumber].readGlobalBuildStatsPOJO(reader, context);
		
		// Migrating old data into up-to-date data
		for(int i=versionNumber; i<getCurrentGlobalBuildStatsVersionNumber(); i++){
			pojo = MIGRATORS[i].migrate(pojo);
		}
		
		// Populating latest POJO information into GlobalBuildStatsPlugin
		populateGlobalBuildStatsPlugin(plugin, pojo);
		
		return plugin;
	}
	
	protected void populateGlobalBuildStatsPlugin(GlobalBuildStatsPlugin plugin, GlobalBuildStatsPOJO pojo){
		// Latest POJO is v5
		// Update this line every time you add a new migrator !
		V6GlobalBuildStatsPOJO versionedPojo = (V6GlobalBuildStatsPOJO)pojo;
		
		plugin.getBuildStatConfigs().clear();
		plugin.getBuildStatConfigs().addAll(versionedPojo.buildStatConfigs);
		
		plugin.getJobBuildResults().clear();
		plugin.getJobBuildResults().addAll(versionedPojo.jobBuildResults);
	}
}

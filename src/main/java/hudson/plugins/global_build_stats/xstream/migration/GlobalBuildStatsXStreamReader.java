package hudson.plugins.global_build_stats.xstream.migration;

import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;

/**
 * Behavior for GlobalBuildStats readers
 * @author fcamblor
 * @param <T>
 */
public interface GlobalBuildStatsXStreamReader<T extends GlobalBuildStatsPOJO> {
	T readGlobalBuildStatsPOJO(HierarchicalStreamReader reader, UnmarshallingContext context);
}

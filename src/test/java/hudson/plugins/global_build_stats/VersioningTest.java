package hudson.plugins.global_build_stats;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.jvnet.hudson.test.recipes.LocalData;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author fcamblor
 */
@WithJenkins
class VersioningTest {

	private GlobalBuildStatsPlugin plugin;

	@BeforeEach
	void setUp(JenkinsRule r) {
		plugin = GlobalBuildStatsPlugin.getInstance();
	}

	@Test
	@LocalData
	void testMigrationFromV7() {
		assertEquals(4, plugin.getBuildStatConfigs().size());
		assertEquals(23, plugin.getJobBuildResults().size());
		assertEquals(0, plugin.getRetentionStrategies().size());
	}

	@Test
	@LocalData
	void testMigrationFromV8() {
		assertEquals(4, plugin.getBuildStatConfigs().size());
		assertEquals(30, plugin.getJobBuildResults().size());
		assertEquals(0, plugin.getRetentionStrategies().size());
	}

	@Test
	@LocalData
	void testMigrationFromV9() {
		assertEquals(1, plugin.getBuildStatConfigs().size());
		assertEquals(16, plugin.getJobBuildResults().size());
		assertEquals(3, plugin.getRetentionStrategies().size());
	}
}

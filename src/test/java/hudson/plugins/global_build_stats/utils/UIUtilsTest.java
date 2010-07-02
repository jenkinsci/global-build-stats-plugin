package hudson.plugins.global_build_stats.utils;

import hudson.plugins.global_build_stats.GlobalBuildStatsPlugin;

import org.junit.Test;


public class UIUtilsTest {

	@Test
	public void testEscapeAntiSlashes(){
		assert ".*test\\\\sbuild.*".equals(GlobalBuildStatsPlugin.escapeAntiSlashes(".*test\\sbuild.*"));
		assert null == GlobalBuildStatsPlugin.escapeAntiSlashes(null);
	}
	
}

package hudson.plugins.global_build_stats.utils;

import org.junit.Test;


public class UIUtilsTest {

	@Test
	public void testEscapeAntiSlashes(){
		assert ".*test\\\\sbuild.*".equals(UIUtils.escapeAntiSlashes(".*test\\sbuild.*"));
		assert null == UIUtils.escapeAntiSlashes(null);
	}
	
}

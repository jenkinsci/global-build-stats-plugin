package hudson.plugins.global_build_stats;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class GlobalBuildStatsPluginTest {

    @Test
    void testEscapeAntiSlashes() {
        assertEquals(".*test\\\\sbuild.*", GlobalBuildStatsPlugin.escapeAntiSlashes(".*test\\sbuild.*"));
        assertNull(GlobalBuildStatsPlugin.escapeAntiSlashes(null));
    }
}

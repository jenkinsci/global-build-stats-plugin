package hudson.plugins.global_build_stats;

import org.codehaus.plexus.util.FileUtils;
import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.recipes.Recipe;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author fcamblor
 */
public class VersioningTest extends HudsonTestCase {

    private static final Pattern VERSION_EXTRACTION_PATTERN = Pattern.compile("^testMigrationFrom(.+)$");

    private GlobalBuildStatsPlugin plugin;

    @Override
    public void setUp() throws Exception {
        final String testName = this.getName();
        recipes.add(new Recipe.Runner() {
            @Override
            public void decorateHome(HudsonTestCase testCase, File home) throws Exception {
                Matcher m = VERSION_EXTRACTION_PATTERN.matcher(testName);
                if(!m.matches()){
                    throw new IllegalStateException("Every test method should be named testMigrationFromXXX !");
                } else {
                    String versionUnderTest = m.group(1);
                    ClassPathResource testDirectoryRoot = new ClassPathResource("versioning/"+versionUnderTest.toLowerCase());
                    if(!testDirectoryRoot.exists()){
                        throw new IllegalStateException("Directory versioning/"+versionUnderTest+" not found in classpath !");
                    }
                    FileUtils.copyDirectoryStructure(testDirectoryRoot.getFile(), home);
                }
            }
        });
        super.setUp();

        plugin = GlobalBuildStatsPlugin.getInstance();
    }

    public void testMigrationFromV7() throws Exception {
        assertEquals(4, plugin.getBuildStatConfigs().size());
        assertEquals(23, plugin.getJobBuildResults().size());
    }
}

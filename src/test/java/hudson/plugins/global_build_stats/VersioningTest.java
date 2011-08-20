package hudson.plugins.global_build_stats;

import org.codehaus.plexus.util.FileUtils;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
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
                    File testDirectoryRoot = new ClassPathResource("versioning/"+versionUnderTest.toLowerCase()).getFile();
                    if(!testDirectoryRoot.exists()){
                        throw new IllegalArgumentException("Directory versioning/"+versionUnderTest+" not found in classpath !");
                    }
                    FileUtils.copyDirectory(testDirectoryRoot, home);
                }
            }
        });
        super.setUp();
    }

    public void testMigrationFromV7() throws Exception {
        GlobalBuildStatsPlugin plugin = GlobalBuildStatsPlugin.getInstance();
        assertEquals(4, plugin.getBuildStatConfigs().size());
        assertEquals(23, plugin.getJobBuildResults().size());
    }
}

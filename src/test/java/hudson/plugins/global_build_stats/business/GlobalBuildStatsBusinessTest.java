package hudson.plugins.global_build_stats.business;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.plugins.global_build_stats.GlobalBuildStatsPlugin;
import org.jvnet.hudson.test.HudsonTestCase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * @author Kohsuke Kawaguchi
 */
public class GlobalBuildStatsBusinessTest extends HudsonTestCase {
    private GlobalBuildStatsPlugin plugin;
    private GlobalBuildStatsBusiness business;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        plugin = GlobalBuildStatsPlugin.getInstance();
        business = GlobalBuildStatsPlugin.getPluginBusiness();
    }

    /**
     * Make sure builds are recorded and written out correctly.
     */
    public void testCallback() throws Exception {
        List<FreeStyleProject> projects = new ArrayList<FreeStyleProject>();
        for (int i=0; i<5; i++)
            projects.add(createFreeStyleProject());

        hudson.setNumExecutors(5);

        for (int i=0; i<5; i++) {
            List<Future<FreeStyleBuild>> builds = new ArrayList<Future<FreeStyleBuild>>();
            for (FreeStyleProject p : projects) {
                builds.add(p.scheduleBuild2(0));
            }
            // this simulates a lengthy plugin.save() and cause the grouping writes.
            business.writer.submit(new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });

            for (Future<FreeStyleBuild> f : builds) {
                FreeStyleBuild b = assertBuildStatusSuccess(f);
            }
        }

        // make sure we flush all the pending writes
        business.writer.submit(new Runnable() {
            public void run() {
            }
        }).get();

        assertEquals(25,plugin.getJobBuildResults().size());
    }
}

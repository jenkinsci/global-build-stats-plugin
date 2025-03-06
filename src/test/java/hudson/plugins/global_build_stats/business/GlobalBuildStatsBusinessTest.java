package hudson.plugins.global_build_stats.business;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.plugins.global_build_stats.GlobalBuildStatsPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Kohsuke Kawaguchi
 */
@WithJenkins
class GlobalBuildStatsBusinessTest {
	private GlobalBuildStatsPlugin plugin;
	private GlobalBuildStatsBusiness business;

	private JenkinsRule r;

	@BeforeEach
	void setUp(JenkinsRule r) {
		this.r = r;
		plugin = GlobalBuildStatsPlugin.getInstance();
		business = GlobalBuildStatsPlugin.getPluginBusiness();
	}

	/**
	 * Make sure builds are recorded and written out correctly.
	 */
	@Test
	void testCallback() throws Exception {
		List<FreeStyleProject> projects = new ArrayList<>();
		for (int i = 0; i < 5; i++)
			projects.add(r.createFreeStyleProject());

		r.jenkins.setNumExecutors(5);

		for (int i = 0; i < 5; i++) {
			List<Future<FreeStyleBuild>> builds = new ArrayList<>();
			for (FreeStyleProject p : projects) {
				builds.add(p.scheduleBuild2(0));
			}
			// this simulates a lengthy plugin.save() and cause the grouping writes.
			business.pluginSaver.writer.submit(() -> {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			});

			for (Future<FreeStyleBuild> f : builds) {
				r.assertBuildStatusSuccess(f);
			}
		}

		// make sure we flush all the pending writes
		business.pluginSaver.writer.submit(() -> {
		}).get();

		assertEquals(25, plugin.getJobBuildResults().size());
	}
}

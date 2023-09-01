package hudson.plugins.global_build_stats.rententionstrategies;

import hudson.model.Run;
import hudson.plugins.global_build_stats.GlobalBuildStatsPlugin;
import hudson.plugins.global_build_stats.JobBuildResultFactory;
import hudson.plugins.global_build_stats.business.GlobalBuildStatsPluginSaver;
import hudson.plugins.global_build_stats.model.JobBuildResult;
import hudson.plugins.global_build_stats.rententionstrategies.strategybehaviours.BuildDeletedListener;

/**
 * @author fcamblor
 */
public class DoNotKeepBuildResultWhenDiscarded extends RetentionStrategy<DoNotKeepBuildResultWhenDiscarded>
                implements BuildDeletedListener {

    @Override
    public String getConfigPage() {
        return "doNotKeepBuildResultWhenDiscarded.jelly";
    }

    public void buildDeleted(final Run build, GlobalBuildStatsPluginSaver pluginSaver) {
        pluginSaver.updatePlugin(new GlobalBuildStatsPluginSaver.BeforeSavePluginCallback() {
            @Override
            public void changePluginStateBeforeSavingIt(GlobalBuildStatsPlugin plugin) {
                JobBuildResult jbr = JobBuildResultFactory.INSTANCE.createJobBuildResult(build);

                plugin.getJobBuildResultsSharder().queueResultToRemove(jbr);
            }
        });
    }

    @Override
    public void strategyActivated(GlobalBuildStatsPluginSaver pluginSaver) {
    }
}

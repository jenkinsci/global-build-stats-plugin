package hudson.plugins.global_build_stats.rententionstrategies.strategybehaviours;

import hudson.model.Run;
import hudson.plugins.global_build_stats.business.GlobalBuildStatsPluginSaver;

/**
 * @author fcamblor
 */
public interface BuildCompletedListener {
    public void buildCompleted(Run buils, GlobalBuildStatsPluginSaver pluginSaver);
}

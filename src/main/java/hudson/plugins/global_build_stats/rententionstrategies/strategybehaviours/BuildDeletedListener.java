package hudson.plugins.global_build_stats.rententionstrategies.strategybehaviours;

import hudson.model.Run;
import hudson.plugins.global_build_stats.business.GlobalBuildStatsPluginSaver;

/**
 * @author fcamblor
 */
public interface BuildDeletedListener {
    public void buildDeleted(Run buils, GlobalBuildStatsPluginSaver pluginSaver);
}

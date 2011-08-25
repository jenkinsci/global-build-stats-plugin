package hudson.plugins.global_build_stats.rententionstrategies.strategybehaviours;

import hudson.model.AbstractBuild;
import hudson.plugins.global_build_stats.business.GlobalBuildStatsPluginSaver;

/**
 * @author fcamblor
 */
public interface BuildDeletedListener {
    public void buildDeleted(AbstractBuild buils, GlobalBuildStatsPluginSaver pluginSaver);
}

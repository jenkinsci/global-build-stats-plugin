package hudson.plugins.global_build_stats.rententionstrategies;

import java.util.Map;

/**
 * @author fcamblor
 */
public class DoNotKeepBuildResultWhenDiscarded extends RetentionStragegy<DoNotKeepBuildResultWhenDiscarded> {

    @Override
    public String getConfigPage() {
        return "doNotKeepBuildResultWhenDiscarded.jelly";
    }
}

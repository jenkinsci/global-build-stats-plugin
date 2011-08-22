package hudson.plugins.global_build_stats.rententionstrategies;

/**
 * @author fcamblor
 */
public class DoNotKeepBuildResultWhenDiscarded extends RetentionStragegy {

    @Override
    public String getConfigPage() {
        return "doNotKeepBuildResultWhenDiscarded.jelly";
    }
}

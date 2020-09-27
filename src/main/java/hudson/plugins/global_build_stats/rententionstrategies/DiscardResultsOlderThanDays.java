package hudson.plugins.global_build_stats.rententionstrategies;

import hudson.model.Run;
import hudson.plugins.global_build_stats.GlobalBuildStatsPlugin;
import hudson.plugins.global_build_stats.JobBuildResultFactory;
import hudson.plugins.global_build_stats.business.GlobalBuildStatsPluginSaver;
import hudson.plugins.global_build_stats.model.JobBuildResult;
import hudson.plugins.global_build_stats.model.JobBuildSearchResult;
import hudson.plugins.global_build_stats.rententionstrategies.strategybehaviours.BuildCompletedListener;
import hudson.plugins.global_build_stats.rententionstrategies.strategybehaviours.ParameterizedStrategy;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author fcamblor
 */
public class DiscardResultsOlderThanDays extends RetentionStrategy<DiscardResultsOlderThanDays>
                implements ParameterizedStrategy<DiscardResultsOlderThanDays>, BuildCompletedListener {

    private static final long PURGE_FREQUENCY = 1000L * 3600L * 24L; // Let's purge job build results once a day

    private long days = 365L;
    private transient Date lastPurgeDate = null;

    @Override
    public String getConfigPage() {
        return "discardResultsOlderThanDays.jelly";
    }

    public long getDays() {
        return days;
    }

    public void updateStrategyParameters(Map<String, String[]> parameters) {
        this.days = Long.valueOf(parameters.get("discardResultsOlderThanDays")[0]);
    }

    public void updateStrategyParameters(DiscardResultsOlderThanDays otherStrategyToCopy) {
        this.days = otherStrategyToCopy.days;
    }

    @Override
    public void strategyActivated(GlobalBuildStatsPluginSaver pluginSaver) {
        purgeOldBuildResults(pluginSaver, System.currentTimeMillis());
    }

    public void buildCompleted(Run build, GlobalBuildStatsPluginSaver pluginSaver) {
        final long now = System.currentTimeMillis();
        if(lastPurgeDate == null || now > lastPurgeDate.getTime() + PURGE_FREQUENCY){
            purgeOldBuildResults(pluginSaver, now);
        }
    }

    protected void purgeOldBuildResults(GlobalBuildStatsPluginSaver pluginSaver, final long now){
        pluginSaver.updatePlugin(new GlobalBuildStatsPluginSaver.BeforeSavePluginCallback(){
            @Override
            public void changePluginStateBeforeSavingIt(GlobalBuildStatsPlugin plugin) {
                List<JobBuildResult> jobBuildResultsToRemove = new ArrayList<JobBuildResult>();
                for(JobBuildResult jbr : plugin.getJobBuildResults()){
                    if(jbr.getBuildDate().getTimeInMillis() + (days * 24L * 3600L * 1000L) < now){
                        jobBuildResultsToRemove.add(jbr);
                    }
                }

                plugin.getJobBuildResultsSharder().queueResultsToRemove(jobBuildResultsToRemove);
            }
        });

        lastPurgeDate = new Date(now);
    }
}

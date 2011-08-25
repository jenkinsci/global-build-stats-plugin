package hudson.plugins.global_build_stats.rententionstrategies;

import hudson.plugins.global_build_stats.GlobalBuildStatsPlugin;
import hudson.plugins.global_build_stats.JobBuildResultFactory;
import hudson.plugins.global_build_stats.business.GlobalBuildStatsPluginSaver;
import hudson.plugins.global_build_stats.model.JobBuildResult;
import hudson.plugins.global_build_stats.model.JobBuildSearchResult;

import java.util.ArrayList;
import java.util.List;

/**
 * @author fcamblor
 */
public class KeepExistingJobResultsOnly extends RetentionStragegy<KeepExistingJobResultsOnly> {
    @Override
    public String getConfigPage() {
        return "keepExistingJobResultsOnly.jelly";
    }

    @Override
    public void strategyActivated(GlobalBuildStatsPluginSaver pluginSaver) {
        pluginSaver.updatePlugin(new GlobalBuildStatsPluginSaver.BeforeSavePluginCallback(){
            @Override
            public void changePluginStateBeforeSavingIt(GlobalBuildStatsPlugin plugin) {
                List<JobBuildResult> jobBuildResultsToRemove = new ArrayList<JobBuildResult>();
                for(JobBuildResult jbr : plugin.getJobBuildResults()){
                    JobBuildSearchResult searchResult = JobBuildResultFactory.INSTANCE.createJobBuildSearchResult(jbr);
                    if(!searchResult.isBuildAccessible()){
                        jobBuildResultsToRemove.add(jbr);
                    }
                }

                plugin.getJobBuildResultsSharder().queueResultsToRemove(jobBuildResultsToRemove);
            }
        });
    }
}

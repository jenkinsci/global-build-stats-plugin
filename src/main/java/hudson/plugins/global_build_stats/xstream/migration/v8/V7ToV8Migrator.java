package hudson.plugins.global_build_stats.xstream.migration.v8;

import hudson.plugins.global_build_stats.xstream.migration.PreV9AbstractMigrator;
import hudson.plugins.global_build_stats.xstream.migration.v7.V7GlobalBuildStatsPOJO;

/**
 * @author fcamblor
 */
public class V7ToV8Migrator extends PreV9AbstractMigrator<V7GlobalBuildStatsPOJO, V8GlobalBuildStatsPOJO> {

    @Override
    protected V8GlobalBuildStatsPOJO createMigratedPojo() {
        return new V8GlobalBuildStatsPOJO();  //To change body of implemented methods use File | Settings | File Templates.
    }
}


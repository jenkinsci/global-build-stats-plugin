package hudson.plugins.global_build_stats.xstream.migration.v9;

import hudson.plugins.global_build_stats.xstream.migration.AbstractMigrator;
import hudson.plugins.global_build_stats.xstream.migration.PreV9AbstractMigrator;
import hudson.plugins.global_build_stats.xstream.migration.v7.V7GlobalBuildStatsPOJO;

/**
 * @author fcamblor
 */
public class V8ToV9Migrator extends AbstractMigrator<V7GlobalBuildStatsPOJO, V9GlobalBuildStatsPOJO> {

    @Override
    protected V9GlobalBuildStatsPOJO createMigratedPojo() {
        return new V9GlobalBuildStatsPOJO();  //To change body of implemented methods use File | Settings | File Templates.
    }
}


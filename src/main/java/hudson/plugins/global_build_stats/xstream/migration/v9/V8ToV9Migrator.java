package hudson.plugins.global_build_stats.xstream.migration.v9;

import hudson.plugins.global_build_stats.rententionstrategies.RetentionStragegy;
import hudson.plugins.global_build_stats.xstream.migration.AbstractMigrator;
import hudson.plugins.global_build_stats.xstream.migration.PreV9AbstractMigrator;
import hudson.plugins.global_build_stats.xstream.migration.v7.V7GlobalBuildStatsPOJO;
import hudson.plugins.global_build_stats.xstream.migration.v8.V8GlobalBuildStatsPOJO;

import java.util.ArrayList;
import java.util.List;

/**
 * @author fcamblor
 */
public class V8ToV9Migrator extends AbstractMigrator<V8GlobalBuildStatsPOJO, V9GlobalBuildStatsPOJO> {

    @Override
    protected V9GlobalBuildStatsPOJO createMigratedPojo() {
        return new V9GlobalBuildStatsPOJO();  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected List<RetentionStragegy> migrateRetentionStrategies(List<RetentionStragegy> retentionStrategies) {
        if(retentionStrategies == null){
            return new ArrayList<RetentionStragegy>();
        } else {
            return super.migrateRetentionStrategies(retentionStrategies);
        }
    }
}


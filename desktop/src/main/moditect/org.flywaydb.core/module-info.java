module org.flywaydb.core {

    requires transitive java.sql;
    requires transitive java.logging;

    // optional dependencies
    requires static org.slf4j;
    requires static org.apache.commons.logging;

    exports org.flywaydb.core;
    exports org.flywaydb.core.api;
    exports org.flywaydb.core.api.callback;
    exports org.flywaydb.core.api.configuration;
    exports org.flywaydb.core.api.exception;
    exports org.flywaydb.core.api.executor;
    exports org.flywaydb.core.api.logging;
    exports org.flywaydb.core.api.migration;
    exports org.flywaydb.core.api.output;
    exports org.flywaydb.core.api.pattern;
    exports org.flywaydb.core.api.resolver;
    exports org.flywaydb.core.api.resource;

    // Flyway reads versions.txt from internal package root
    opens org.flywaydb.core.internal;

    // only most common DBMS included, free Flyway version supports some more
    uses org.flywaydb.core.internal.database.DatabaseType;
    provides org.flywaydb.core.internal.database.DatabaseType with
            org.flywaydb.core.internal.database.cockroachdb.CockroachDBDatabaseType,
            org.flywaydb.core.internal.database.redshift.RedshiftDatabaseType,
            org.flywaydb.core.internal.database.db2.DB2DatabaseType,
            org.flywaydb.core.internal.database.derby.DerbyDatabaseType,
            org.flywaydb.core.internal.database.h2.H2DatabaseType,
            org.flywaydb.core.internal.database.hsqldb.HSQLDBDatabaseType,
            org.flywaydb.core.internal.database.informix.InformixDatabaseType,
            org.flywaydb.core.internal.database.oracle.OracleDatabaseType,
            org.flywaydb.core.internal.database.postgresql.PostgreSQLDatabaseType,
            org.flywaydb.core.internal.database.saphana.SAPHANADatabaseType,
            org.flywaydb.core.internal.database.snowflake.SnowflakeDatabaseType,
            org.flywaydb.core.internal.database.sqlite.SQLiteDatabaseType,
            org.flywaydb.core.internal.database.sybasease.SybaseASEJConnectDatabaseType,
            org.flywaydb.core.internal.database.sybasease.SybaseASEJTDSDatabaseType,
            org.flywaydb.core.internal.database.base.TestContainersDatabaseType,
            org.flywaydb.core.internal.schemahistory.BaseAppliedMigration,
            org.flywaydb.core.internal.resource.CoreResourceTypeProvider,
            org.flywaydb.core.internal.database.postgresql.PostgreSQLConfigurationExtension,
            org.flywaydb.core.internal.command.clean.CleanModeConfigurationExtension;

    exports org.flywaydb.core.extensibility;
    uses org.flywaydb.core.extensibility.FlywayExtension;
}

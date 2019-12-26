package corf.tests;

import com.p6spy.engine.spy.P6DataSource;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import javax.sql.DataSource;

public class HSQLMemoryDatabaseResolver implements ParameterResolver {

    protected final DataSource dataSource;

    public HSQLMemoryDatabaseResolver() {
        dataSource = dataSource();
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext,
                                     ExtensionContext extensionContext) throws ParameterResolutionException {
        Class<?> type = parameterContext.getParameter().getType();
        return type == DataSource.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext,
                                   ExtensionContext extensionContext) throws ParameterResolutionException {
        Class<?> type = parameterContext.getParameter().getType();
        if (type == DataSource.class) { return dataSource; }
        throw new ParameterResolutionException("Unable to resolve " + type.getCanonicalName());
    }

    protected DataSource dataSource() {
        // setting this system property false avoids reconfiguring the logging system such as Log4J or JUL
        // if the property does not exist or is true, reconfiguration takes place
        System.setProperty("hsqldb.reconfig_logging", "false");

        JDBCDataSource dataSource = new JDBCDataSource();
        dataSource.setDatabase("jdbc:hsqldb:mem:test");

        // wrap to P6Spy data source to log SQL queries, because some drivers may have troubles
        // with proper logging (e.g. HSQL can only log SQL queries to file and only when file
        // type of database used)
        return new P6DataSource(dataSource);
    }
}

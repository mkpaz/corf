package telekit.base.service.jdbc;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.*;
import java.util.logging.Logger;

final class TransactionalDataSource implements DataSource {

    private final JdbcTransactionManager tm;
    private final DataSource delegate;

    public TransactionalDataSource(JdbcTransactionManager tm) {
        this.tm = tm;
        this.delegate = tm.getManagedDataSource();
    }

    @Override
    public Connection getConnection() throws SQLException {
        return tm.getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return tm.getConnection();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Delegated                                                             //
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return delegate.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        delegate.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        delegate.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return delegate.getLoginTimeout();
    }

    @Override
    public ConnectionBuilder createConnectionBuilder() throws SQLException {
        return delegate.createConnectionBuilder();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return delegate.getParentLogger();
    }

    @Override
    public ShardingKeyBuilder createShardingKeyBuilder() throws SQLException {
        return delegate.createShardingKeyBuilder();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T unwrap(Class<T> type) throws SQLException {
        if (type.isInstance(this)) {
            return (T) this;
        } else if (type.isInstance(delegate)) {
            return (T) delegate;
        } else {
            return delegate.unwrap(type);
        }
    }

    @Override
    public boolean isWrapperFor(Class<?> type) throws SQLException {
        if (type.isInstance(this)) {
            return true;
        } else if (type.isInstance(delegate)) {
            return true;
        } else {
            return delegate.isWrapperFor(type);
        }
    }
}

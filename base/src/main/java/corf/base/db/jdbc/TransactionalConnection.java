package corf.base.db.jdbc;

import java.sql.*;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.Executor;

final class TransactionalConnection implements Connection {

    private final Connection delegate;

    private TransactionalConnection(Connection delegate) {
        this.delegate = delegate;
    }

    Connection getDelegate() {
        return delegate;
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        if (autoCommit) {
            throw new UnsupportedOperationException("Auto-commit cannot be enabled within transaction");
        }
        delegate.setAutoCommit(false);
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        return delegate.getAutoCommit();
    }

    @Override
    public void commit() {
        // connection wrapper is only used within transactions, fail-fast
        throw new UnsupportedOperationException(
                "You're trying to manually commit connection that is used inside transaction"
        );
    }

    @Override
    public void rollback() {
        // connection wrapper is only used within transactions, fail-fast
        throw new UnsupportedOperationException(
                "You're trying to manually rollback connection that is used inside transaction"
        );
    }

    @Override
    public void close() {
        // NO-OP, because repository (DAO) implementation cannot avoid calling Connection#close()
        // this also means no one can acquire underlying connection until transaction finished
    }

    @Override
    public Savepoint setSavepoint() {
        throw new UnsupportedOperationException("Savepoints are not supported");
    }

    @Override
    public Savepoint setSavepoint(String name) {
        throw new UnsupportedOperationException("Savepoints are not supported");
    }

    @Override
    public void rollback(Savepoint savepoint) {
        throw new UnsupportedOperationException("Savepoints are not supported");
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) {
        throw new UnsupportedOperationException("Savepoints are not supported");
    }

    @Override
    public <T> T unwrap(Class<T> type) throws SQLException {
        return getClass().isAssignableFrom(type) ? type.cast(this) : delegate.unwrap(type);
    }

    @Override
    public boolean isWrapperFor(Class<?> type) throws SQLException {
        return getClass().isAssignableFrom(type) || delegate.isWrapperFor(type);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Delegated                                                             //
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public Statement createStatement() throws SQLException {
        return delegate.createStatement();
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return delegate.prepareStatement(sql);
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        return delegate.prepareCall(sql);
    }

    @Override
    public String nativeSQL(String sql) throws SQLException {
        return delegate.nativeSQL(sql);
    }

    @Override
    public boolean isClosed() throws SQLException {
        return delegate.isClosed();
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        return delegate.getMetaData();
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
        delegate.setReadOnly(readOnly);
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return delegate.isReadOnly();
    }

    @Override
    public void setCatalog(String catalog) throws SQLException {
        delegate.setCatalog(catalog);
    }

    @Override
    public String getCatalog() throws SQLException {
        return delegate.getCatalog();
    }

    @Override
    public void setTransactionIsolation(int level) throws SQLException {
        delegate.setTransactionIsolation(level);
    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        return delegate.getTransactionIsolation();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return delegate.getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
        delegate.clearWarnings();
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        return delegate.createStatement(resultSetType, resultSetConcurrency);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType,
                                              int resultSetConcurrency) throws SQLException {
        return delegate.prepareStatement(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType,
                                         int resultSetConcurrency) throws SQLException {
        return delegate.prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return delegate.getTypeMap();
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        delegate.setTypeMap(map);
    }

    @Override
    public void setHoldability(int holdability) throws SQLException {
        delegate.setHoldability(holdability);
    }

    @Override
    public int getHoldability() throws SQLException {
        return delegate.getHoldability();
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency,
                                     int resultSetHoldability) throws SQLException {
        return delegate.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
                                              int resultSetHoldability) throws SQLException {
        return delegate.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
                                         int resultSetHoldability) throws SQLException {
        return delegate.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        return delegate.prepareStatement(sql, autoGeneratedKeys);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        return delegate.prepareStatement(sql, columnIndexes);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        return delegate.prepareStatement(sql, columnNames);
    }

    @Override
    public Clob createClob() throws SQLException {
        return delegate.createClob();
    }

    @Override
    public Blob createBlob() throws SQLException {
        return delegate.createBlob();
    }

    @Override
    public NClob createNClob() throws SQLException {
        return delegate.createNClob();
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        return delegate.createSQLXML();
    }

    @Override
    public boolean isValid(int timeout) throws SQLException {
        return delegate.isValid(timeout);
    }

    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        delegate.setClientInfo(name, value);
    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        delegate.setClientInfo(properties);
    }

    @Override
    public String getClientInfo(String name) throws SQLException {
        return delegate.getClientInfo(name);
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        return delegate.getClientInfo();
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        return delegate.createArrayOf(typeName, elements);
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        return delegate.createStruct(typeName, attributes);
    }

    @Override
    public void setSchema(String schema) throws SQLException {
        delegate.setSchema(schema);
    }

    @Override
    public String getSchema() throws SQLException {
        return delegate.getSchema();
    }

    @Override
    public void abort(Executor executor) throws SQLException {
        delegate.abort(executor);
    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        delegate.setNetworkTimeout(executor, milliseconds);
    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        return delegate.getNetworkTimeout();
    }

    @Override
    public void beginRequest() throws SQLException {
        delegate.beginRequest();
    }

    @Override
    public void endRequest() throws SQLException {
        delegate.endRequest();
    }

    @Override
    public boolean setShardingKeyIfValid(ShardingKey shardingKey, ShardingKey superShardingKey,
                                         int timeout) throws SQLException {
        return delegate.setShardingKeyIfValid(shardingKey, superShardingKey, timeout);
    }

    @Override
    public boolean setShardingKeyIfValid(ShardingKey shardingKey, int timeout) throws SQLException {
        return delegate.setShardingKeyIfValid(shardingKey, timeout);
    }

    @Override
    public void setShardingKey(ShardingKey shardingKey, ShardingKey superShardingKey) throws SQLException {
        delegate.setShardingKey(shardingKey, superShardingKey);
    }

    @Override
    public void setShardingKey(ShardingKey shardingKey) throws SQLException {
        delegate.setShardingKey(shardingKey);
    }

    ///////////////////////////////////////////////////////////////////////////

    static TransactionalConnection wrap(Connection delegate) {
        Objects.requireNonNull(delegate, "delegate");
        return new TransactionalConnection(delegate);
    }

    // special use case for transaction marker
    @SuppressWarnings({ "NullAway" })
    static TransactionalConnection empty() {
        return new TransactionalConnection(null);
    }
}

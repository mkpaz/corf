package corf.base.db.jdbc;

import corf.base.db.TransactionManager;
import corf.base.exception.AppException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

import static corf.base.i18n.I18n.t;
import static corf.base.i18n.M.MGG_DATABASE_ERROR;
import static java.lang.System.Logger.Level.*;

public class JdbcTransactionManager implements TransactionManager {

    private static final System.Logger LOGGER = System.getLogger(JdbcTransactionManager.class.getName());
    private static final TransactionalConnection TRANSACTION_MARKER = TransactionalConnection.empty();
    private static final ThreadLocal<TransactionalConnection> CONNECTIONS = new ThreadLocal<>();

    private final DataSource rawDataSource;
    private final DataSource txDataSource;

    public JdbcTransactionManager(DataSource rawDataSource) {
        this.rawDataSource = Objects.requireNonNull(rawDataSource, "rawDataSource");
        this.txDataSource = new TransactionalDataSource(this);
    }

    public DataSource getManagedDataSource() {
        return txDataSource;
    }

    @Override
    public void begin() {
        if (CONNECTIONS.get() != null) {
            throw new IllegalStateException("Transaction is already in progress");
        }
        LOGGER.log(DEBUG, "[" + Thread.currentThread().getId() + "] Starting new transaction");
        CONNECTIONS.set(TRANSACTION_MARKER);
    }

    @Override
    public void commit() {
        pullUnderlyingConnection().ifPresent(connection -> {
            try {
                LOGGER.log(DEBUG,"[" + Thread.currentThread().getId() + "] Perform transaction commit");
                connection.commit();
            } catch (SQLException e) {
                throw new AppException(t(MGG_DATABASE_ERROR), e);
            } finally {
                closeQuietly(connection);
            }
        });
    }

    @Override
    public void rollback() {
        pullUnderlyingConnection().ifPresent(connection -> {
            try {
                LOGGER.log(DEBUG,"[" + Thread.currentThread().getId() + "] Perform transaction rollback");
                connection.rollback();
            } catch (SQLException e) {
                throw new AppException(t(MGG_DATABASE_ERROR), e);
            } finally {
                closeQuietly(connection);
            }
        });
    }

    private Optional<Connection> pullUnderlyingConnection() {
        TransactionalConnection threadLocal = CONNECTIONS.get();
        if (threadLocal == null) {
            throw new IllegalStateException("Transaction is not active");
        }
        CONNECTIONS.remove();
        return threadLocal == TRANSACTION_MARKER ? Optional.empty() : Optional.of(threadLocal.getDelegate());
    }

    Connection getConnection() throws SQLException {
        TransactionalConnection threadLocal = CONNECTIONS.get();

        // if transaction is not set, return normal connection
        if (threadLocal == null) {
            LOGGER.log(DEBUG,"No transactions active, returning unwrapped connection");
            return rawDataSource.getConnection();
        }

        if (threadLocal == TRANSACTION_MARKER) {
            LOGGER.log(DEBUG,"Creating transaction-aware connection wrapper");
            Connection rawConnection = rawDataSource.getConnection();
            CONNECTIONS.set(threadLocal = TransactionalConnection.wrap(rawConnection));
            if (threadLocal.getAutoCommit()) { threadLocal.setAutoCommit(false); }
        }

        return threadLocal;
    }

    private void closeQuietly(Connection con) {
        try {
            con.close();
        } catch (SQLException ignored) { /* ignored */ }
    }
}

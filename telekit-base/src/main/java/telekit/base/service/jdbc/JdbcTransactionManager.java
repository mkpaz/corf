package telekit.base.service.jdbc;

import telekit.base.domain.exception.TelekitException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Logger;

import static telekit.base.i18n.BaseMessages.MGG_DATABASE_ERROR;
import static telekit.base.i18n.I18n.t;

public class JdbcTransactionManager implements TransactionManager {

    private static final Logger LOG = Logger.getLogger(JdbcTransactionManager.class.getName());
    private static final TransactionalConnection TRANSACTION_MARKER = new TransactionalConnection(null);
    private static final ThreadLocal<TransactionalConnection> CONNECTIONS = new ThreadLocal<>();

    private final DataSource rawDataSource;
    private final DataSource txDataSource;

    public JdbcTransactionManager(DataSource rawDataSource) {
        this.rawDataSource = rawDataSource;
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
        LOG.fine("[" + Thread.currentThread().getId() + "] Starting new transaction");
        CONNECTIONS.set(TRANSACTION_MARKER);
    }

    @Override
    public void commit() {
        pullUnderlyingConnection().ifPresent(connection -> {
            try {
                LOG.fine("[" + Thread.currentThread().getId() + "] Perform transaction commit");
                connection.commit();
            } catch (SQLException e) {
                throw new TelekitException(t(MGG_DATABASE_ERROR), e);
            } finally {
                closeQuietly(connection);
            }
        });
    }

    @Override
    public void rollback() {
        pullUnderlyingConnection().ifPresent(connection -> {
            try {
                LOG.fine("[" + Thread.currentThread().getId() + "] Perform transaction rollback");
                connection.rollback();
            } catch (SQLException e) {
                throw new TelekitException(t(MGG_DATABASE_ERROR), e);
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
            LOG.fine("No transactions active, returning unwrapped connection");
            return rawDataSource.getConnection();
        }

        if (threadLocal == TRANSACTION_MARKER) {
            LOG.fine("Creating transaction-aware connection wrapper");
            Connection rawConnection = rawDataSource.getConnection();
            CONNECTIONS.set(threadLocal = new TransactionalConnection(rawConnection));
            if (threadLocal.getAutoCommit()) { threadLocal.setAutoCommit(false); }
        }

        return threadLocal;
    }

    private void closeQuietly(Connection con) {
        try {
            con.close();
        } catch (SQLException ignored) {}
    }
}

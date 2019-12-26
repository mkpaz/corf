package corf.base.db.jdbc;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import corf.base.db.DatabaseTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DatabaseTest
public class JdbcTransactionManagerTest {

    private final JdbcTransactionManager tm;
    private final DataSource rawDataSource;
    private final DataSource txDataSource;

    public JdbcTransactionManagerTest(DataSource dataSource) {
        this.rawDataSource = dataSource;
        this.tm = new JdbcTransactionManager(dataSource);
        this.txDataSource = tm.getManagedDataSource();
    }

    @BeforeEach
    public void beforeEach() {
        try (Connection con = rawDataSource.getConnection()) {
            con.createStatement().execute("CREATE TABLE IF NOT EXISTS tm_test (x INTEGER, y INTEGER);");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    public void afterEach() {
        try (Connection con = rawDataSource.getConnection()) {
            con.createStatement().execute("TRUNCATE TABLE tm_test;");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void insertRow() throws SQLException {
        // must use txDataSource
        try (Connection con = txDataSource.getConnection()) {
            con.createStatement().executeUpdate("INSERT INTO tm_test (x, y) VALUES (1, 2);");
        }
    }

    int countRows() throws SQLException {
        // must use txDataSource
        try (Connection con = txDataSource.getConnection()) {
            try (ResultSet rs = con.createStatement().executeQuery("SELECT COUNT(*) FROM tm_test;")) {
                if (!rs.next()) { return 0; }
                return rs.getInt(1);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Tests                                                                 //
    ///////////////////////////////////////////////////////////////////////////

    @Test
    public void testBeginAndCommitForMultipleRows() throws SQLException {
        tm.begin();
        for (int i = 0; i < 10; i++) {
            insertRow(); // each insert requests a connection from data source
        }
        assertThat(countRows()).isEqualTo(10);

        tm.commit();
        assertThat(countRows()).isEqualTo(10);
    }

    @Test
    public void testBeginAndRollbackForMultipleRows() throws SQLException {
        tm.begin();
        for (int i = 0; i < 10; i++) {
            insertRow(); // each insert requests a connection from data source
        }
        assertThat(countRows()).isEqualTo(10);

        tm.rollback();
        assertThat(countRows()).isEqualTo(0);
    }

    @Test
    public void testMultiThreadedScenario() throws SQLException, InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(50);
        for (int i = 0; i < 1000; ++i) {
            int id = i;
            executor.submit(() -> {
                try {
                    tm.begin();
                    insertRow();
                    if (id % 2 == 1) {
                        throw new RuntimeException("Trigger rollback");
                    }
                    tm.commit();
                } catch (Exception e) {
                    tm.rollback();
                }
            });
        }
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS); // ~1.5 sec for 50 threads

        assertThat(countRows()).isEqualTo(500);
    }

    @Test
    public void testNoOpIsAllowed() {
        tm.begin();
        tm.commit();

        tm.begin();
        tm.rollback();
    }

    @Test
    public void testTransactionCannotBeStartedTwice() {
        try {
            assertThatThrownBy(() -> {
                tm.begin();
                tm.begin();
            }).isInstanceOf(IllegalStateException.class);
        } finally {
            tm.rollback();
        }
    }

    @Test
    public void testCommitIsNotAllowedWithoutTransaction() {
        assertThatThrownBy(tm::commit).isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void testRollbackIsNotAllowedWithoutTransaction() {
        assertThatThrownBy(tm::rollback).isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void testManagedDataSourceCanBeUsedWithoutTransaction() throws SQLException {
        for (int i = 0; i < 10; i++) {
            insertRow();
            assertThat(countRows()).isEqualTo(i + 1); // each row auto-committed
        }
    }

    @Test
    public void testExecuteRunnableThenCommit() throws SQLException {
        Runnable runnable = () -> {
            try {
                for (int i = 0; i < 10; i++) {
                    insertRow();
                }
                assertThat(countRows()).isEqualTo(10);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
        tm.execute(runnable);
        assertThat(countRows()).isEqualTo(10);
    }

    @Test
    public void testExecuteRunnableThenRollback() throws SQLException {
        Runnable runnable = () -> {
            try {
                for (int i = 0; i < 10; i++) {
                    insertRow();
                }
                assertThat(countRows()).isEqualTo(10);
                throw new InternalError();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };

        assertThatThrownBy(() -> tm.execute(runnable)).isInstanceOf(InternalError.class);
        assertThat(countRows()).isEqualTo(0);
    }

    @Test
    public void testExecuteSupplierThenCommit() throws SQLException {
        Supplier<Integer> supplier = () -> {
            try {
                for (int i = 0; i < 10; i++) {
                    insertRow();
                }
                return countRows();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
        int count = tm.execute(supplier);
        assertThat(countRows()).isEqualTo(count);
    }

    @Test
    public void testExecuteSupplierThenRollback() throws SQLException {
        Supplier<Integer> supplier = () -> {
            try {
                for (int i = 0; i < 10; i++) {
                    insertRow();
                }
                assertThat(countRows()).isEqualTo(10);
                throw new InternalError();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };

        assertThatThrownBy(() -> tm.execute(supplier)).isInstanceOf(InternalError.class);
        assertThat(countRows()).isEqualTo(0);
    }
}
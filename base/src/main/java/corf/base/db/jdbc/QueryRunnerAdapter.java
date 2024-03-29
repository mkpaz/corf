package corf.base.db.jdbc;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import corf.base.exception.AppException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static corf.base.i18n.M.MGG_DATABASE_ERROR;
import static corf.base.i18n.I18n.t;

public final class QueryRunnerAdapter {

    private final QueryRunner runner;

    public QueryRunnerAdapter(DataSource datasource) {
        this.runner = new QueryRunner(datasource);
    }

    public int update(Connection con, String sql, Object... params) {
        try {
            return runner.update(con, sql, params);
        } catch (SQLException e) {
            throw new AppException(t(MGG_DATABASE_ERROR), e);
        }
    }

    public int update(String sql, Object... params) {
        try {
            return runner.update(sql, params);
        } catch (SQLException e) {
            throw new AppException(t(MGG_DATABASE_ERROR), e);
        }
    }

    public <T> T query(String sql, ResultSetHandler<T> handler) {
        return query(sql, handler, new Object[]{});
    }

    public <T> T query(Connection con, String sql, ResultSetHandler<T> handler, Object... params) {
        try {
            return runner.query(con, sql, handler, params);
        } catch (SQLException e) {
            throw new AppException(t(MGG_DATABASE_ERROR), e);
        }
    }

    public <T> T query(String sql, ResultSetHandler<T> handler, Object... params) {
        try {
            return runner.query(sql, handler, params);
        } catch (SQLException e) {
            throw new AppException(t(MGG_DATABASE_ERROR), e);
        }
    }

    public QueryRunner unwrap() {
        return runner;
    }
}

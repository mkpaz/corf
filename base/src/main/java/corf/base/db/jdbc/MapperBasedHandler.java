package corf.base.db.jdbc;

import org.apache.commons.dbutils.ResultSetHandler;
import org.jetbrains.annotations.Nullable;
import corf.base.db.jdbc.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class MapperBasedHandler<T> implements ResultSetHandler<T> {

    protected final ResultSetMapper<T> mapper;

    public MapperBasedHandler(ResultSetMapper<T> mapper) {
        this.mapper = Objects.requireNonNull(mapper, "mapper");
    }

    @Override
    public @Nullable T handle(final ResultSet resultSet) throws SQLException {
        if (!resultSet.next()) {
            return null;
        }
        return mapper.map(resultSet);
    }
}

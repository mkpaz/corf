package telekit.base.util.jdbc;

import org.apache.commons.dbutils.ResultSetHandler;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MapperBasedHandler<T> implements ResultSetHandler<T> {

    protected final ResultSetMapper<T> mapper;

    public MapperBasedHandler(ResultSetMapper<T> mapper) {
        this.mapper = mapper;
    }

    @Override
    public T handle(final ResultSet resultSet) throws SQLException {
        if (!resultSet.next()) {
            return null;
        }
        return mapper.map(resultSet);
    }
}
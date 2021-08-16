package org.telekit.base.util.jdbc;

import org.telekit.base.domain.exception.DatabaseException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

// IMPORTANT NOTE:
// Do not use `rs.getObject(name, class)`, because for some reason it may return null
// when called from another module, which is probably JDBC driver or JDK bug.
public interface ResultSetMapper<T> {

    T map(ResultSet rs) throws SQLException;

    default UUID getUUID(ResultSet rs, String colName) throws SQLException {
        Object o = rs.getObject(colName);
        if (o instanceof UUID uuid) {
            return uuid;
        } else {
            throw new DatabaseException("Unexpected column type: " + colName);
        }
    }

    default LocalDateTime getLocalDateTime(ResultSet rs, String colName) throws SQLException {
        Object o = rs.getObject(colName);
        if (o == null) { return null; }

        if (o instanceof OffsetDateTime odt) {
            return odt.toLocalDateTime();
        } else {
            throw new DatabaseException("Unexpected column type: " + colName);
        }
    }
}
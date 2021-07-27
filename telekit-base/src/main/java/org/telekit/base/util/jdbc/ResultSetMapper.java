package org.telekit.base.util.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

public interface ResultSetMapper<T> {

    T map(ResultSet rs) throws SQLException;

    default UUID getUUID(ResultSet rs, String colName) throws SQLException {
        return rs.getObject(colName, UUID.class);
    }

    default LocalDateTime getLocalDateTime(ResultSet rs, String colName) throws SQLException {
        OffsetDateTime offsetDateTime = rs.getObject(colName, OffsetDateTime.class);
        return offsetDateTime != null ? offsetDateTime.toLocalDateTime() : null;
    }
}
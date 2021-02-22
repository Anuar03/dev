package kz.report.dev.utils.dbutils;

import java.sql.Connection;

public interface ConnectionPool {
    Connection getConnection();
    void returnConnection(Connection con);
}

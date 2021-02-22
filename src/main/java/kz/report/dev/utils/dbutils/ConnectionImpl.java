package kz.report.dev.utils.dbutils;

import java.sql.Connection;
import java.util.List;
import java.util.Stack;

public enum ConnectionImpl implements ConnectionPool {
    ConnectionPoolImpl {

        List<Connection> connectionStack = new Stack<>();


        @Override
        public Connection getConnection() {
            return null;
        }

        @Override
        public void returnConnection(Connection con) {

        }
    }
}

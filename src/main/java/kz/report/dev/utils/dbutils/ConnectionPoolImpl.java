package kz.report.dev.utils.dbutils;

import kz.arta.synergy.datasource.utils.ConnectionUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

public class ConnectionPoolImpl implements ConnectionPool {

    private static ConnectionPool connectionPool;
    private int size;

    private Stack<Connection> connectionStack = new Stack<>();

    public static ConnectionPool getInstance(int size) {
        if (connectionPool == null) {
            synchronized (ConnectionPoolImpl.class) {
                connectionPool = new ConnectionPoolImpl(size);
            }
            return connectionPool;
        }

        return connectionPool;

    }

    private ConnectionPoolImpl() {
    }

    private ConnectionPoolImpl(int size) {
        if (size <= 0) {
            return;
        }
//        fillStack(size);
        this.size = size;
    }

    private void fillStack(int size) {
        for (int i = 0; i < size; i++) {
            connectionStack.add(ConnectionUtil.getXAConnection());
//            try {
//                connectionStack.add(DriverManager.getConnection("jdbc:mysql://localhost:3306/synergy?user=root&password=root"));
//            } catch (Exception e) {
//                e.printStackTrace();
//            }

        }
    }

    @Override
    public synchronized Connection getConnection() {
//        try {
//            return DriverManager.getConnection("jdbc:mysql://localhost:3306/synergy?user=root&password=root");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        if (connectionStack.empty()) {
//            return getIfEmpty();
//        }
//        return connectionStack.pop();
        return ConnectionUtil.getXAConnection();
    }

    private Connection getIfEmpty() {
        while (connectionStack.empty()) {
            System.out.println("Все конекшены заняты");
        }
        return connectionStack.pop();

    }

    @Override
    public void returnConnection(Connection con) {

//        if (Objects.isNull(con)) return;
//        if (connectionStack.size() == size) {
//            try {
//                con.close();
//            } catch (SQLException e) {
//                System.out.println("Ошибка при закрытии connection");
//            }
//            return;
//        }
//        connectionStack.add(con);

        try {
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

//    @Override
//    protected void finalize() throws Throwable {
//        System.out.println("Close connections");
//        for (Connection connection : connectionStack) {
//            connection.close();
//        }
//    }
}

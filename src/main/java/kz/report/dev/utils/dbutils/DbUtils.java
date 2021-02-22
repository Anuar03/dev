package kz.report.dev.utils.dbutils;

import kz.arta.synergy.datasource.utils.ConnectionUtil;

import javax.annotation.Nullable;
import java.sql.*;

public class DbUtils {

    @Nullable
    public static ResultSet execSql(String sql, Object ...arg) {
        try (Connection con = getConnection();
             PreparedStatement preparedStatement = con.prepareStatement(sql)) {

            ResultSet resultSet = preparedStatement.executeQuery();


            return resultSet;
        } catch (SQLException e) {
            System.out.println(String.format("Запрос %s не выполнился", sql));
            System.out.println(String.format("Причина %s", e.getMessage()));
        }

        return null;
    }

    private void paramExecutor(PreparedStatement statement, Object ...arg) {
        if (arg == null) return;
        if (arg.length == 0) return;
        for (int i = 1; i <= arg.length; i++) {
            try {
                if (arg[i] == null) continue;
                if (arg[i] instanceof String) statement.setString(i, (String) arg[i]);
                if (arg[i] instanceof Integer) statement.setInt(i, (Integer) arg[i]);
            } catch (SQLException e) {
                System.out.println(String.format("Ошибка при установке параметра SQL запроса на позиции %d значение %s", i, arg[i]));
            }
        }
    }

    private static Connection getConnection() {
        return ConnectionUtil.getXAConnection();
    }

}

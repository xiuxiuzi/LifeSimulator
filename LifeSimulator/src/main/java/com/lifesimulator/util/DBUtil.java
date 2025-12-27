package com.lifesimulator.util;

import java.sql.*;

public class DBUtil {
    // MySQL 9.x的连接URL格式不同
    private static final String URL = "jdbc:mysql://localhost:3306/life_simulator?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai";
    private static final String USER = "root";
    private static final String PASSWORD = "pupu3377@"; // 修改这里！

    static {
        try {
            // MySQL 9.x的驱动类名
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✅ MySQL 9.x驱动加载成功");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ MySQL驱动加载失败");
            System.err.println("请确保 mysql-connector-j-9.5.0.jar 在 WEB-INF/lib/ 目录下");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void close(AutoCloseable... closeables) {
        for (AutoCloseable c : closeables) {
            if (c != null) {
                try {
                    c.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 测试数据库连接
    public static void testConnection() {
        Connection conn = null;
        try {
            conn = getConnection();
            System.out.println("✅ 数据库连接成功！");

            // 列出所有表测试
            DatabaseMetaData meta = conn.getMetaData();
            System.out.println("数据库产品: " + meta.getDatabaseProductName());
            System.out.println("数据库版本: " + meta.getDatabaseProductVersion());

        } catch (SQLException e) {
            System.err.println("❌ 数据库连接失败: " + e.getMessage());
            System.err.println("请检查:");
            System.err.println("1. MySQL服务是否运行");
            System.err.println("2. 用户名密码是否正确");
            System.err.println("3. 数据库life_simulator是否存在");
        } finally {
            close(conn);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== MySQL 9.x 数据库连接测试 ===");
        testConnection();
    }
}
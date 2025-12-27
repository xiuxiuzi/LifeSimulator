package com.lifesimulator.servlet;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.*;
import java.sql.*;

@WebServlet("/testDB")
public class TestServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println("<html><head><title>数据库测试</title></head>");
        out.println("<body style='font-family: Arial; padding: 20px;'>");
        out.println("<h1>🧪 数据库连接测试</h1>");

        Connection conn = null;
        try {
            // 1. 测试驱动加载
            out.println("<h2>1. 测试MySQL驱动</h2>");
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                out.println("<p style='color:green'>✅ MySQL 9.x驱动加载成功</p>");
            } catch (ClassNotFoundException e) {
                out.println("<p style='color:red'>❌ MySQL驱动加载失败</p>");
                out.println("<pre>" + e.getMessage() + "</pre>");
                return;
            }

            // 2. 测试连接
            out.println("<h2>2. 测试数据库连接</h2>");
            String url = "jdbc:mysql://localhost:3306/life_simulator";
            String user = "root";
            String password = "你的MySQL密码"; // 修改这里！

            conn = DriverManager.getConnection(url, user, password);
            out.println("<p style='color:green'>✅ 数据库连接成功</p>");

            // 3. 测试查询
            out.println("<h2>3. 测试数据查询</h2>");
            Statement stmt = conn.createStatement();

            // 测试life_node表
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM life_node");
            if (rs.next()) {
                out.println("<p>life_node表记录数: " + rs.getInt(1) + "</p>");
            }

            // 测试choice表
            rs = stmt.executeQuery("SELECT COUNT(*) FROM choice");
            if (rs.next()) {
                out.println("<p>choice表记录数: " + rs.getInt(1) + "</p>");
            }

            // 显示所有专业
            rs = stmt.executeQuery("SELECT choice_name, annual_income, satisfaction FROM choice WHERE node_id=1");
            out.println("<h3>可选专业列表:</h3>");
            out.println("<ul>");
            while (rs.next()) {
                out.println("<li>" + rs.getString("choice_name") +
                        " - 年收入:" + rs.getString("annual_income") +
                        ", 满意度:" + rs.getString("satisfaction") + "</li>");
            }
            out.println("</ul>");

        } catch (SQLException e) {
            out.println("<h2 style='color:red'>❌ 数据库错误</h2>");
            out.println("<pre>错误信息: " + e.getMessage() + "</pre>");
            out.println("<pre>SQL状态: " + e.getSQLState() + "</pre>");
            out.println("<pre>错误码: " + e.getErrorCode() + "</pre>");
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) {}
            }
        }

        out.println("<hr>");
        out.println("<a href='/LifeSimulator/'>返回首页</a>");
        out.println("</body></html>");
    }
}
package com.lifesimulator.dao;

import com.lifesimulator.util.DBUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecordDAO {

    // 保存用户选择记录
    public boolean saveRecord(String userId, String sessionId, int nodeId, int choiceId) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            String sql = "INSERT INTO simulate_record (user_id, session_id, node_id, choice_id) VALUES (?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userId);
            pstmt.setString(2, sessionId);
            pstmt.setInt(3, nodeId);
            pstmt.setInt(4, choiceId);

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DBUtil.close(pstmt, conn);
        }
    }

    // 获取用户的所有选择记录
    public List<Map<String, Object>> getUserRecords(String userId, String sessionId) {
        List<Map<String, Object>> records = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT r.*, c.choice_name, c.education, c.occupation, c.annual_income, c.satisfaction, " +
                    "n.node_name, n.age " +
                    "FROM simulate_record r " +
                    "JOIN choice c ON r.choice_id = c.choice_id " +
                    "JOIN life_node n ON r.node_id = n.node_id " +
                    "WHERE r.user_id = ? AND r.session_id = ? " +
                    "ORDER BY r.node_id";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userId);
            pstmt.setString(2, sessionId);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> record = new HashMap<>();
                record.put("node_name", rs.getString("node_name"));
                record.put("age", rs.getInt("age"));
                record.put("choice_name", rs.getString("choice_name"));
                record.put("education", rs.getString("education"));
                record.put("occupation", rs.getString("occupation"));
                record.put("annual_income", rs.getString("annual_income"));
                record.put("satisfaction", rs.getString("satisfaction"));
                record.put("create_time", rs.getTimestamp("create_time"));
                records.add(record);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(rs, pstmt, conn);
        }

        return records;
    }

    // 获取用户上一个选择ID
    public Integer getLastChoiceId(String userId, String sessionId, int nodeId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT choice_id FROM simulate_record " +
                    "WHERE user_id = ? AND session_id = ? AND node_id = ? " +
                    "ORDER BY create_time DESC LIMIT 1";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userId);
            pstmt.setString(2, sessionId);
            pstmt.setInt(3, nodeId - 1); // 获取上一个节点的选择

            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("choice_id");
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            DBUtil.close(rs, pstmt, conn);
        }
    }

    // 测试方法
    public static void main(String[] args) {
        RecordDAO dao = new RecordDAO();
        System.out.println("=== 测试RecordDAO ===");

        // 测试保存记录
        boolean saved = dao.saveRecord("test_user", "test_session", 1, 1);
        System.out.println("保存记录结果: " + (saved ? "成功" : "失败"));

        // 测试查询记录
        List<Map<String, Object>> records = dao.getUserRecords("test_user", "test_session");
        System.out.println("查询到记录数: " + records.size());
    }
}
package com.lifesimulator.dao;

import com.lifesimulator.util.DBUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChoiceDAO {

    // 获取指定阶段的所有选项
    public List<Map<String, Object>> getChoicesByStage(int stage, Integer previousChoiceId) {
        List<Map<String, Object>> choices = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            String sql;
            if (previousChoiceId != null && stage > 1) {
                // 有前一个选择：显示该选择的后续选项 + 通用选项
                sql = "SELECT * FROM choice WHERE node_id = ? AND " +
                        "(previous_choice_id = ? OR previous_choice_id IS NULL)";
                pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, stage);
                pstmt.setInt(2, previousChoiceId);
            } else {
                // 第一阶段或无前一个选择：显示所有该阶段的选项
                sql = "SELECT * FROM choice WHERE node_id = ?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, stage);
            }

            System.out.println("执行查询: " + sql + " (stage=" + stage + ", previous=" + previousChoiceId + ")");

            rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> choice = new HashMap<>();
                choice.put("choice_id", rs.getInt("choice_id"));
                choice.put("choice_name", rs.getString("choice_name"));
                choice.put("education", rs.getString("education"));
                choice.put("occupation", rs.getString("occupation"));
                choice.put("annual_income", rs.getString("annual_income"));
                choice.put("satisfaction", rs.getString("satisfaction"));
                choice.put("description", rs.getString("description"));
                choice.put("icon_class", rs.getString("icon_class"));
                choices.add(choice);
            }

            System.out.println("找到 " + choices.size() + " 个选项");

        } catch (SQLException e) {
            System.err.println("查询错误: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.close(rs, pstmt, conn);
        }

        return choices;
    }
    // 根据ID获取选项详情
    public Map<String, Object> getChoiceById(int choiceId) {
        Map<String, Object> choice = new HashMap<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT * FROM choice WHERE choice_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, choiceId);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                choice.put("choice_id", rs.getInt("choice_id"));
                choice.put("choice_name", rs.getString("choice_name"));
                choice.put("education", rs.getString("education"));
                choice.put("occupation", rs.getString("occupation"));
                choice.put("annual_income", rs.getString("annual_income"));
                choice.put("satisfaction", rs.getString("satisfaction"));
                choice.put("description", rs.getString("description"));
                choice.put("icon_class", rs.getString("icon_class"));
                choice.put("node_id", rs.getInt("node_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(rs, pstmt, conn);
        }

        return choice;
    }

    // 获取节点信息
    public Map<String, Object> getNodeInfo(int nodeId) {
        Map<String, Object> node = new HashMap<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT * FROM life_node WHERE node_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, nodeId);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                node.put("node_id", rs.getInt("node_id"));
                node.put("node_name", rs.getString("node_name"));
                node.put("age", rs.getInt("age"));
                node.put("description", rs.getString("description"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(rs, pstmt, conn);
        }

        return node;
    }

    // 测试方法
    public static void main(String[] args) {
        ChoiceDAO dao = new ChoiceDAO();
        System.out.println("=== 测试ChoiceDAO ===");

        // 测试获取节点信息
        Map<String, Object> node = dao.getNodeInfo(1);
        System.out.println("节点信息: " + node);

        // 测试获取选项
        List<Map<String, Object>> choices = dao.getChoicesByStage(1, null);
        System.out.println("第一阶段选项数: " + choices.size());
        for (Map<String, Object> choice : choices) {
            System.out.println(" - " + choice.get("choice_name"));
        }
    }
}
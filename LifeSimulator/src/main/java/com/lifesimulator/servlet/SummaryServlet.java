package com.lifesimulator.servlet;

import com.lifesimulator.dao.RecordDAO;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.*;
import java.util.List;
import java.util.Map;

@WebServlet("/summary")
public class SummaryServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String userId = request.getParameter("userId");
        String sessionId = request.getParameter("sessionId");

        if (userId == null || sessionId == null) {
            response.sendRedirect("index.html");
            return;
        }

        try {
            RecordDAO recordDAO = new RecordDAO();
            List<Map<String, Object>> records = recordDAO.getUserRecords(userId, sessionId);

            if (records.isEmpty()) {
                out.println("<h1>没有找到你的模拟记录</h1>");
                out.println("<a href='/LifeSimulator/'>返回首页重新开始</a>");
                return;
            }

            // 生成总结页面
            out.println(generateSummaryPage(records));

        } catch (Exception e) {
            e.printStackTrace();
            out.println("<h1>系统错误，请稍后重试</h1>");
        }
    }

    private String generateSummaryPage(List<Map<String, Object>> records) {
        StringBuilder html = new StringBuilder();

        // 获取最终状态（最后一条记录）
        Map<String, Object> finalRecord = records.get(records.size() - 1);

        html.append("<!DOCTYPE html>")
                .append("<html lang=\"zh-CN\">")
                .append("<head>")
                .append("<meta charset=\"UTF-8\">")
                .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">")
                .append("<title>人生总结 - 人生选择模拟器</title>")
                .append("<link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css\">")
                .append("<link rel=\"stylesheet\" href=\"css/original-style.css\">")
                .append("<style>")
                .append("  .summary-container { margin-top: 40px; }")
                .append("  .final-status { text-align: center; margin: 40px 0; }")
                .append("  .final-stats { display: flex; justify-content: center; gap: 40px; margin: 30px 0; }")
                .append("  .final-stat { text-align: center; }")
                .append("  .final-stat .value { font-size: 2.5rem; font-weight: bold; color: #6e8efb; }")
                .append("  .final-stat .label { color: #aaa; margin-top: 10px; }")
                .append("  table { width: 100%; border-collapse: collapse; margin: 30px 0; background: rgba(255,255,255,0.05); border-radius: 15px; overflow: hidden; }")
                .append("  th, td { padding: 15px; text-align: left; border-bottom: 1px solid rgba(255,255,255,0.1); }")
                .append("  th { background: rgba(110, 142, 251, 0.2); color: #6e8efb; }")
                .append("  tr:hover { background: rgba(255,255,255,0.03); }")
                .append("  .restart-btn { display: block; width: 300px; margin: 50px auto; text-align: center; }")
                .append("</style>")
                .append("</head>")
                .append("<body>")
                .append("<div class=\"container\">")
                .append("<header>")
                .append("<div class=\"header-bg\"></div>")
                .append("<div class=\"logo\">")
                .append("<div class=\"logo-icon\"><i class=\"fas fa-flag-checkered\"></i></div>")
                .append("<h1>人生模拟总结</h1>")
                .append("</div>")
                .append("<p class=\"subtitle\">恭喜你完成了人生模拟！以下是你的完整人生轨迹。</p>")
                .append("</header>");

        // 最终状态
        html.append("<div class=\"final-status\">")
                .append("<h2><i class=\"fas fa-crown\"></i> 最终人生状态（60岁退休）</h2>")
                .append("<div class=\"final-stats\">")
                .append("<div class=\"final-stat\"><div class=\"value\">").append(finalRecord.get("education")).append("</div><div class=\"label\">最终学历</div></div>")
                .append("<div class=\"final-stat\"><div class=\"value\">").append(finalRecord.get("occupation")).append("</div><div class=\"label\">最终职业</div></div>")
                .append("<div class=\"final-stat\"><div class=\"value\">").append(finalRecord.get("annual_income")).append("</div><div class=\"label\">最终年收入</div></div>")
                .append("<div class=\"final-stat\"><div class=\"value\">").append(finalRecord.get("satisfaction")).append("</div><div class=\"label\">生活满意度</div></div>")
                .append("</div>")
                .append("</div>");

        // 人生轨迹表格
        html.append("<div class=\"summary-container\">")
                .append("<h2><i class=\"fas fa-history\"></i> 人生轨迹回顾</h2>")
                .append("<table>")
                .append("<thead><tr><th>年龄</th><th>关键选择</th><th>职业</th><th>年收入</th><th>满意度</th></tr></thead>")
                .append("<tbody>");

        for (Map<String, Object> record : records) {
            html.append("<tr>")
                    .append("<td>").append(record.get("age")).append("岁</td>")
                    .append("<td>").append(record.get("choice_name")).append("</td>")
                    .append("<td>").append(record.get("occupation")).append("</td>")
                    .append("<td>").append(record.get("annual_income")).append("</td>")
                    .append("<td>").append(record.get("satisfaction")).append("</td>")
                    .append("</tr>");
        }

        html.append("</tbody>")
                .append("</table>")
                .append("</div>");

        // 添加人生评价
        String finalIncome = (String) finalRecord.get("annual_income");
        String finalSatisfaction = (String) finalRecord.get("satisfaction");

        String incomeLevel = getIncomeLevel(finalIncome);
        String satisfactionLevel = getSatisfactionLevel(finalSatisfaction);

        html.append("<div class=\"final-evaluation\" style=\"background: rgba(255,255,255,0.05); padding: 30px; border-radius: 15px; margin: 30px 0;\">")
                .append("<h3><i class=\"fas fa-chart-line\"></i> 人生评价</h3>")
                .append("<p>收入水平：<span style=\"color:#6e8efb; font-weight:bold;\">").append(incomeLevel).append("</span></p>")
                .append("<p>生活满意度：<span style=\"color:#6e8efb; font-weight:bold;\">").append(satisfactionLevel).append("</span></p>")
                .append("<p>职业成就：<span style=\"color:#6e8efb; font-weight:bold;\">").append(finalRecord.get("occupation")).append("</span></p>")
                .append("</div>");

        // 添加人生故事总结
        html.append("<div class=\"life-story\" style=\"background: rgba(255,255,255,0.05); padding: 30px; border-radius: 15px; margin: 30px 0;\">")
                .append("<h3><i class=\"fas fa-book-open\"></i> 你的人生故事</h3>")
                .append("<p>");

        // 根据选择生成故事
        for (int i = 0; i < records.size(); i++) {
            Map<String, Object> record = records.get(i);
            if (i > 0) html.append(" → ");
            html.append(record.get("age")).append("成为").append(record.get("occupation"));
        }

        html.append("</p>")
                .append("<p>一路走来，你经历了")
                .append(records.size())
                .append("个重要的人生阶段，每个选择都塑造了今天的你。</p>")
                .append("</div>");

        // 添加建议
        html.append("<div class=\"advice\" style=\"background: linear-gradient(135deg, rgba(110,142,251,0.1), rgba(167,119,227,0.1)); padding: 25px; border-radius: 15px; margin: 30px 0; border-left: 5px solid #6e8efb;\">")
                .append("<h3><i class=\"fas fa-lightbulb\"></i> 人生感悟</h3>")
                .append("<p>人生没有标准答案，每个选择都有其独特的价值。重要的不是选择了什么，而是如何在选择后活出自己的精彩。</p>")
                .append("<p style=\"font-style: italic; color: #aaa; margin-top: 15px;\">" +
                        "「人生的意义不在于到达终点，而在于沿途的风景和成长的过程。」</p>")
                .append("</div>");


        // 重新开始按钮
        html.append("<div class=\"actions\">")
                .append("<a href=\"/LifeSimulator/start\" class=\"btn restart-btn\">")
                .append("<i class=\"fas fa-redo\"></i> 重新开始新的人生模拟")
                .append("</a>")
                .append("<a href=\"/LifeSimulator/\" class=\"btn btn-secondary\">")
                .append("<i class=\"fas fa-home\"></i> 返回首页")
                .append("</a>")
                .append("</div>")
                .append("</div>")
                .append("</body>")
                .append("</html>");


        return html.toString();
    }
    private String getIncomeLevel(String income) {
        try {
            int incomeValue = Integer.parseInt(income.replace("万", ""));
            if (incomeValue >= 50) return "💎 高收入精英";
            else if (incomeValue >= 20) return "💰 中等收入";
            else return "💼 普通收入";
        } catch (Exception e) {
            return "📊 收入稳定";
        }
    }

    private String getSatisfactionLevel(String satisfaction) {
        try {
            int satValue = Integer.parseInt(satisfaction.replace("分", ""));
            if (satValue >= 85) return "😄 非常幸福";
            else if (satValue >= 70) return "🙂 比较满意";
            else return "😐 普通生活";
        } catch (Exception e) {
            return "😊 生活充实";
        }
    }
}
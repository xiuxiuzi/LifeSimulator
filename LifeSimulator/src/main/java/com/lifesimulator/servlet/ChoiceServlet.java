package com.lifesimulator.servlet;

import com.lifesimulator.dao.ChoiceDAO;
import com.lifesimulator.dao.RecordDAO;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.*;
import java.util.*;

@WebServlet("/choice")
public class ChoiceServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        // 获取当前阶段
        String stageParam = request.getParameter("stage");
        int currentStage = 1;
        try {
            currentStage = Integer.parseInt(stageParam);
        } catch (NumberFormatException e) {
            currentStage = 1;
        }

        // 从Session获取用户信息
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect("index.html");
            return;
        }

        String userId = (String) session.getAttribute("userId");
        String sessionId = (String) session.getAttribute("sessionId");
        session.setAttribute("currentStage", currentStage);

        try {
            ChoiceDAO choiceDAO = new ChoiceDAO();
            RecordDAO recordDAO = new RecordDAO();

            // 获取当前阶段信息
            Map<String, Object> nodeInfo = choiceDAO.getNodeInfo(currentStage);

            // 获取用户上一个选择（用于过滤选项）
            Integer previousChoiceId = null;
            if (currentStage > 1) {
                previousChoiceId = recordDAO.getLastChoiceId(userId, sessionId, currentStage);
            }

            // 获取当前阶段的选项
            List<Map<String, Object>> choices = choiceDAO.getChoicesByStage(currentStage, previousChoiceId);

            // 生成HTML页面（使用原版UI框架）
            out.println(generateOriginalStylePage(userId, sessionId, currentStage, nodeInfo, choices));

        } catch (Exception e) {
            e.printStackTrace();
            out.println("<h1>系统错误，请稍后重试</h1>");
            out.println("<pre>" + e.getMessage() + "</pre>");
        }

    }

    private String generateOriginalStylePage(String userId, String sessionId, int currentStage,
                                             Map<String, Object> nodeInfo, List<Map<String, Object>> choices) {

        StringBuilder html = new StringBuilder();

        // 页面头部 - 使用原版CSS
        html.append("<!DOCTYPE html>")
                .append("<html lang=\"zh-CN\">")
                .append("<head>")
                .append("<meta charset=\"UTF-8\">")
                .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">")
                .append("<title>").append(nodeInfo.get("age")).append("岁：").append(nodeInfo.get("node_name")).append(" - 人生选择模拟器</title>")
                .append("<link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css\">")
                .append("<link rel=\"stylesheet\" href=\"css/original-style.css\">")
                .append("</head>")
                .append("<body>")
                .append("<div class=\"container\">");

        // 选择页面
        html.append("<div id=\"choicePage\" class=\"page active\">")
                .append("<header>")
                .append("<div class=\"header-bg\"></div>")
                .append("<div class=\"logo\">")
                .append("<div class=\"logo-icon\">")
                .append("<i class=\"fas fa-route\"></i>")
                .append("</div>")
                .append("<h1>人生选择模拟器</h1>")
                .append("</div>")
                .append("<p class=\"subtitle\">体验不同人生轨迹，每个选择都将开启一段独特的故事。从").append(nodeInfo.get("age")).append("岁到50岁，书写属于你的人生篇章。</p>")
                .append("</header>");

        // 进度条
        html.append(generateOriginalProgressBar(currentStage));

        // 状态栏（简化版，后续可增强）
        html.append("<div class=\"status-container fade-in\">")
                .append("<div class=\"status-item\">")
                .append("<div class=\"status-label\"><i class=\"fas fa-user-clock\"></i>当前年龄</div>")
                .append("<div class=\"status-value\" id=\"currentAge\">").append(nodeInfo.get("age")).append("岁</div>")
                .append("</div>")
                .append("<div class=\"status-item\">")
                .append("<div class=\"status-label\"><i class=\"fas fa-graduation-cap\"></i>当前学历</div>")
                .append("<div class=\"status-value\" id=\"currentEducation\">高中</div>")
                .append("</div>")
                .append("<div class=\"status-item\">")
                .append("<div class=\"status-label\"><i class=\"fas fa-briefcase\"></i>当前职业</div>")
                .append("<div class=\"status-value\" id=\"currentOccupation\">无</div>")
                .append("</div>")
                .append("<div class=\"status-item\">")
                .append("<div class=\"status-label\"><i class=\"fas fa-money-bill-wave\"></i>年收入</div>")
                .append("<div class=\"status-value\" id=\"currentIncome\">0万</div>")
                .append("</div>")
                .append("<div class=\"status-item\">")
                .append("<div class=\"status-label\"><i class=\"fas fa-smile\"></i>生活满意度</div>")
                .append("<div class=\"status-value\" id=\"currentSatisfaction\">60分</div>")
                .append("</div>")
                .append("</div>");

        // 选择标题
        html.append("<div class=\"choices-title fade-in\">")
                .append("<h2 id=\"stageTitle\"><i class=\"fas fa-graduation-cap\"></i> ")
                .append(nodeInfo.get("age")).append("岁：").append(nodeInfo.get("node_name")).append("</h2>")
                .append("<p id=\"stageDescription\">").append(nodeInfo.get("description")).append("</p>")
                .append("</div>");

        // 选择网格
        html.append("<div class=\"choices-grid\" id=\"choicesGrid\">");

        // 动态生成选择卡片（原版样式）
        int index = 1;
        for (Map<String, Object> choice : choices) {
            html.append(generateOriginalChoiceCard(choice, index));
            index++;
        }

        html.append("</div>");

        // 操作按钮
        html.append("<div class=\"actions fade-in\">")
                .append("<form id=\"choiceForm\" action=\"submit\" method=\"post\">")
                .append("<input type=\"hidden\" name=\"userId\" value=\"").append(userId).append("\">")
                .append("<input type=\"hidden\" name=\"sessionId\" value=\"").append(sessionId).append("\">")
                .append("<input type=\"hidden\" name=\"stage\" value=\"").append(currentStage).append("\">")
                .append("<input type=\"hidden\" id=\"selectedChoice\" name=\"choiceId\" value=\"\">")
                .append("<button type=\"button\" class=\"btn\" id=\"nextBtn\" disabled onclick=\"submitChoice()\">")
                .append("<i class=\"fas fa-book-open\"></i> 开启人生故事")
                .append("</button>")
                .append("</form>")
                .append("<button type=\"button\" class=\"btn btn-secondary\" onclick=\"resetChoices()\">")
                .append("<i class=\"fas fa-redo\"></i> 重新选择")
                .append("</button>")
                .append("</div>")
                .append("</div>")  // 关闭choicePage
                .append("</div>"); // 关闭container

        // JavaScript（基础功能）
        html.append("<script>")
                .append(getBasicJavaScript())
                .append("</script>")
                .append("</body>")
                .append("</html>");

        return html.toString();
    }

    private String generateOriginalProgressBar(int currentStage) {
        String[] ages = {"18岁", "22岁", "30岁", "50岁"};
        String[] labels = {"大学专业选择", "第一份工作", "中期决策", "后期决策"};
        int[] progress = {25, 50, 75, 100};

        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"progress-container\">")
                .append("<div class=\"progress-bar\">")
                .append("<div class=\"progress-fill\" id=\"progressFill\" style=\"width:").append(progress[currentStage-1]).append("%\"></div>")
                .append("</div>")
                .append("<div class=\"age-markers\">");

        for (int i = 0; i < 4; i++) {
            String active = (i+1 <= currentStage) ? "active" : "";
            sb.append("<div class=\"age-marker ").append(active).append("\" data-age=\"").append(ages[i].replace("岁", "")).append("\">")
                    .append("<span>").append(ages[i]).append("</span>")
                    .append("<div class=\"age-label\">").append(labels[i]).append("</div>")
                    .append("</div>");
        }

        sb.append("</div>").append("</div>");
        return sb.toString();
    }

    private String generateOriginalChoiceCard(Map<String, Object> choice, int index) {
        String iconClass = (String) choice.getOrDefault("icon_class", "icon-general");
        String icon = getIconByClass(iconClass);

        // 不显示description，只显示基本信息
        return "<div class=\"choice-card fade-in\" data-choice-id=\"" + choice.get("choice_id") + "\" " +
                "onclick=\"selectChoice(" + choice.get("choice_id") + ")\">" +
                "<div class=\"choice-icon " + iconClass + "\">" + icon + "</div>" +
                "<h3>" + choice.get("choice_name") + "</h3>" +
                "<div class=\"choice-effects\">" +
                "<div class=\"effect-item\"><i class=\"fas fa-user-graduate\"></i><span>学历：" + choice.get("education") + "</span></div>" +
                "<div class=\"effect-item\"><i class=\"fas fa-briefcase\"></i><span>职业：" + choice.get("occupation") + "</span></div>" +
                "<div class=\"effect-item\"><i class=\"fas fa-money-bill-wave\"></i><span>年收入：" + choice.get("annual_income") + "</span></div>" +
                "<div class=\"effect-item\"><i class=\"fas fa-smile\"></i><span>满意度：" + choice.get("satisfaction") + "</span></div>" +
                "</div>" +
                "</div>";
    }    private String getIconByClass(String iconClass) {
        switch (iconClass) {
            case "icon-esports": return "<i class=\"fas fa-gamepad\"></i>";
            case "icon-it": return "<i class=\"fas fa-database\"></i>";
            case "icon-ai": return "<i class=\"fas fa-robot\"></i>";
            case "icon-archaeology": return "<i class=\"fas fa-monument\"></i>";
            case "icon-law": return "<i class=\"fas fa-gavel\"></i>";
            case "icon-magic": return "<i class=\"fas fa-hat-wizard\"></i>";
            default: return "<i class=\"fas fa-briefcase\"></i>";
        }
    }

    private String getBasicJavaScript() {
        return
                "// 基础JavaScript功能\n" +
                        "let selectedChoiceId = null;\n" +
                        "let selectedChoiceData = null;\n" +
                        "\n" +
                        "function selectChoice(choiceId, choiceData) {\n" +
                        "    // 移除之前的选择样式\n" +
                        "    document.querySelectorAll('.choice-card.selected').forEach(card => {\n" +
                        "        card.classList.remove('selected');\n" +
                        "    });\n" +
                        "\n" +
                        "    // 添加当前选择样式\n" +
                        "    const selectedCard = document.querySelector('.choice-card[data-choice-id=\"' + choiceId + '\"]');\n" +
                        "    if (selectedCard) {\n" +
                        "        selectedCard.classList.add('selected');\n" +
                        "    }\n" +
                        "\n" +
                        "    // 记录选择\n" +
                        "    selectedChoiceId = choiceId;\n" +
                        "    selectedChoiceData = choiceData;\n" +
                        "    document.getElementById('selectedChoice').value = choiceId;\n" +
                        "\n" +
                        "    // 启用下一步按钮\n" +
                        "    document.getElementById('nextBtn').disabled = false;\n" +
                        "\n" +
                        "    // 更新状态预览\n" +
                        "    updateStatusPreview(choiceData);\n" +
                        "\n" +
                        "    // 显示提示\n" +
                        "    showToast('✅ 已选择：' + choiceData.occupation.split('/')[0]);\n" +
                        "}\n" +
                        "\n" +
                        "function updateStatusPreview(choice) {\n" +
                        "    // 这里可以更新状态栏，但目前先简单处理\n" +
                        "    const age = document.getElementById('currentAge').textContent;\n" +
                        "    document.getElementById('currentEducation').textContent = choice.education;\n" +
                        "    document.getElementById('currentOccupation').textContent = choice.occupation.split('/')[0];\n" +
                        "    document.getElementById('currentIncome').textContent = choice.annual_income;\n" +
                        "    document.getElementById('currentSatisfaction').textContent = choice.satisfaction;\n" +
                        "}\n" +
                        "\n" +
                        "function resetChoices() {\n" +
                        "    selectedChoiceId = null;\n" +
                        "    selectedChoiceData = null;\n" +
                        "    document.getElementById('selectedChoice').value = '';\n" +
                        "    document.getElementById('nextBtn').disabled = true;\n" +
                        "\n" +
                        "    // 移除选择样式\n" +
                        "    document.querySelectorAll('.choice-card.selected').forEach(card => {\n" +
                        "        card.classList.remove('selected');\n" +
                        "    });\n" +
                        "\n" +
                        "    // 重置状态预览为默认值\n" +
                        "    const age = document.getElementById('currentAge').textContent;\n" +
                        "    if (age === '18岁') {\n" +
                        "        document.getElementById('currentEducation').textContent = '高中';\n" +
                        "        document.getElementById('currentOccupation').textContent = '无';\n" +
                        "        document.getElementById('currentIncome').textContent = '0万';\n" +
                        "        document.getElementById('currentSatisfaction').textContent = '60分';\n" +
                        "    }\n" +
                        "\n" +
                        "    showToast('选择已重置，请重新选择');\n" +
                        "}\n" +
                        "\n" +
                        "function submitChoice() {\n" +
                        "    if (!selectedChoiceId) {\n" +
                        "        showToast('请先选择一个选项！');\n" +
                        "        return;\n" +
                        "    }\n" +
                        "    document.getElementById('choiceForm').submit();\n" +
                        "}\n" +
                        "\n" +
                        "function showToast(message, type = 'info') {\n" +
                        "    // 简单的提示功能\n" +
                        "    alert(message);\n" +
                        "}\n" +
                        "\n" +
                        "// 页面加载时的初始化\n" +
                        "document.addEventListener('DOMContentLoaded', function() {\n" +
                        "    console.log('页面加载完成');\n" +
                        "});";
    }

    private String escapeJs(Object obj) {
        if (obj == null) return "";
        return obj.toString().replace("'", "\\'").replace("\"", "\\\"");
    }
}
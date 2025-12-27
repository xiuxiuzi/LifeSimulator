package com.lifesimulator.servlet;

import com.lifesimulator.dao.ChoiceDAO;
import com.lifesimulator.dao.RecordDAO;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@WebServlet("/submit")
public class SubmitServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            // 获取表单参数
            String userId = request.getParameter("userId");
            String sessionId = request.getParameter("sessionId");
            int stage = Integer.parseInt(request.getParameter("stage"));
            int choiceId = Integer.parseInt(request.getParameter("choiceId"));

            // 获取选择详情
            ChoiceDAO choiceDAO = new ChoiceDAO();
            Map<String, Object> choice = choiceDAO.getChoiceById(choiceId);

            if (choice.isEmpty()) {
                out.println("<h1>错误：选择不存在</h1>");
                return;
            }

            // 保存记录到数据库
            RecordDAO recordDAO = new RecordDAO();
            boolean saved = recordDAO.saveRecord(userId, sessionId, stage, choiceId);

            if (!saved) {
                out.println("<h1>保存失败，请重试</h1>");
                return;
            }

            // 更新Session中的当前阶段
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.setAttribute("currentStage", stage);
            }

            // ========== 修改这里：显示情景模块 ==========
            // 判断是否是最后一个阶段
            boolean isLastStage = (stage == 4);

            // 显示情景页面
            out.println(generateStoryPage(userId, sessionId, stage, choice, isLastStage));

        } catch (NumberFormatException e) {
            out.println("<h1>参数错误</h1>");
            e.printStackTrace();
        } catch (Exception e) {
            out.println("<h1>系统错误，请稍后重试</h1>");
            e.printStackTrace();
        }
    }

    private String generateStoryPage(String userId, String sessionId, int stage,
                                     Map<String, Object> choice, boolean isLastStage) {

        StringBuilder html = new StringBuilder();

        // 阶段标题 - 为50岁补充更具总结性的副标题
        String[] stageTitles = {
                "18岁：大学专业选择",
                "22岁：第一份工作选择",
                "30岁：中期人生决策",
                "50岁：半生沉淀 · 人生归途"  // 优化50岁标题，更有总结感
        };

        String stageTitle = stageTitles[stage-1];
        String nextStage = String.valueOf(stage + 1);
        String description = (String) choice.get("description");

        // 分割句子
        String[] sentences = description.split("。");
        List<String> sentenceList = new ArrayList<>();
        for (String sentence : sentences) {
            if (!sentence.trim().isEmpty()) {
                sentenceList.add(sentence.trim() + "。");
            }
        }

        html.append("<!DOCTYPE html>")
                .append("<html lang=\"zh-CN\">")
                .append("<head>")
                .append("<meta charset=\"UTF-8\">")
                .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">")
                .append("<title>").append(stageTitle).append(" - 人生故事</title>")
                .append("<link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css\">")
                .append("<link rel=\"stylesheet\" href=\"css/original-style.css\">")
                .append("<style>")
                .append("  .story-container {")
                .append("    background: linear-gradient(135deg, rgba(20, 25, 40, 0.9), rgba(30, 35, 50, 0.9));")
                .append("    border-radius: 25px;")
                .append("    padding: 50px;")
                .append("    margin: 40px 0;")
                .append("    box-shadow: 0 15px 50px rgba(0, 0, 0, 0.4);")
                .append("    border: 1px solid rgba(255, 255, 255, 0.05);")
                .append("    backdrop-filter: blur(10px);")
                .append("    min-height: 400px;")
                .append("    position: relative;")
                .append("    overflow: hidden;")
                .append("  }")
                // 50岁阶段专属背景样式 - 增加金色渐变，突出最终阶段
                .append("  .story-container.last-stage {")
                .append("    background: linear-gradient(135deg, rgba(25, 20, 45, 0.95), rgba(40, 30, 60, 0.95));")
                .append("    border: 1px solid rgba(255, 215, 0, 0.2);")
                .append("  }")
                .append("  .story-container::before {")
                .append("    content: '';")
                .append("    position: absolute;")
                .append("    top: 0;")
                .append("    left: 0;")
                .append("    width: 100%;")
                .append("    height: 100%;")
                .append("    background: radial-gradient(circle at 20% 30%, rgba(110, 142, 251, 0.05) 0%, transparent 50%),")
                .append("                radial-gradient(circle at 80% 70%, rgba(167, 119, 227, 0.05) 0%, transparent 50%);")
                .append("    z-index: 0;")
                .append("  }")
                // 50岁阶段专属渐变
                .append("  .story-container.last-stage::before {")
                .append("    background: radial-gradient(circle at 20% 30%, rgba(255, 215, 0, 0.08) 0%, transparent 50%),")
                .append("                radial-gradient(circle at 80% 70%, rgba(255, 165, 0, 0.08) 0%, transparent 50%);")
                .append("  }")
                .append("  .story-header { text-align: center; margin-bottom: 40px; position: relative; z-index: 1; }")
                .append("  .story-title { font-size: 2.2rem; color: #fff; margin-bottom: 15px; }")
                // 50岁标题专属样式
                .append("  .last-stage .story-title { color: #ffd700; text-shadow: 0 0 20px rgba(255, 215, 0, 0.3); }")
                .append("  .story-subtitle { color: #aaa; font-size: 1.1rem; }")
                .append("  .story-content {")
                .append("    font-size: 1.3rem;")
                .append("    line-height: 1.8;")
                .append("    color: #e6e6e6;")
                .append("    margin-bottom: 40px;")
                .append("    padding: 30px;")
                .append("    background: rgba(0, 0, 0, 0.1);")
                .append("    border-radius: 15px;")
                .append("    border: 1px solid rgba(255, 255, 255, 0.05);")
                .append("    min-height: 200px;")
                .append("    position: relative;")
                .append("    z-index: 1;")
                .append("  }")
                // 50岁内容区边框高亮
                .append("  .last-stage .story-content { border: 1px solid rgba(255, 215, 0, 0.15); }")
                .append("  .story-sentence {")
                .append("    margin-bottom: 25px;")
                .append("    opacity: 0;")
                .append("    transform: translateY(20px);")
                .append("    transition: opacity 0.8s ease, transform 0.8s ease;")
                .append("  }")
                .append("  .story-sentence.visible {")
                .append("    opacity: 1;")
                .append("    transform: translateY(0);")
                .append("  }")
                .append("  .sentence-highlight { color: #6e8efb; font-weight: 600; }")
                // 50岁高亮色改为金色
                .append("  .last-stage .sentence-highlight { color: #ffd700; }")
                .append("  .story-progress {")
                .append("    display: flex;")
                .append("    align-items: center;")
                .append("    justify-content: center;")
                .append("    gap: 15px;")
                .append("    margin-top: 30px;")
                .append("    position: relative;")
                .append("    z-index: 1;")
                .append("  }")
                .append("  .story-dots { display: flex; gap: 10px; }")
                .append("  .story-dot {")
                .append("    width: 12px;")
                .append("    height: 12px;")
                .append("    border-radius: 50%;")
                .append("    background-color: rgba(255, 255, 255, 0.1);")
                .append("    transition: all 0.3s ease;")
                .append("  }")
                .append("  .story-dot.active {")
                .append("    background-color: #6e8efb;")
                .append("    box-shadow: 0 0 10px rgba(110, 142, 251, 0.5);")
                .append("  }")
                // 50岁进度点高亮色改为金色
                .append("  .last-stage .story-dot.active {")
                .append("    background-color: #ffd700;")
                .append("    box-shadow: 0 0 10px rgba(255, 215, 0, 0.5);")
                .append("  }")
                .append("  .btn-story {")
                .append("    background: linear-gradient(90deg, #ff9a3c, #ff6b6b);")
                .append("    box-shadow: 0 10px 25px rgba(255, 154, 60, 0.3);")
                .append("  }")
                // 50岁专属按钮样式
                .append("  .btn-summary {")
                .append("    background: linear-gradient(90deg, #ffd700, #ffb900);")
                .append("    box-shadow: 0 10px 25px rgba(255, 215, 0, 0.3);")
                .append("    color: #000 !important;")
                .append("    font-weight: 600;")
                .append("  }")
                .append("  .choice-summary {")
                .append("    text-align: center;")
                .append("    margin-top: 30px;")
                .append("    padding: 20px;")
                .append("    background: rgba(0, 0, 0, 0.2);")
                .append("    border-radius: 10px;")
                .append("    border: 1px solid rgba(255, 255, 255, 0.1);")
                .append("  }")
                // 50岁总结卡片样式
                .append("  .last-stage .choice-summary {")
                .append("    background: rgba(25, 20, 45, 0.3);")
                .append("    border: 1px solid rgba(255, 215, 0, 0.2);")
                .append("  }")
                .append("</style>")
                .append("</head>")
                .append("<body>")
                .append("<div class=\"container\">")
                // 为50岁阶段添加last-stage类
                .append("<div class=\"story-container ").append(isLastStage ? "last-stage" : "").append("\">")
                .append("<div class=\"story-header\">")
                .append("<h2 class=\"story-title\">").append(stageTitle).append("</h2>")
                // 50岁副标题补充人生总结文案
                .append("<p class=\"story-subtitle\">")
                .append(isLastStage ?
                        choice.get("choice_name") + " - 半生已过，你的人生答卷已书写完成..." :
                        choice.get("choice_name") + " - 你的人生故事正在展开...")
                .append("</p>")
                .append("</div>")
                .append("<div class=\"story-content\" id=\"storyContent\">");

        // 动态生成句子
        for (int i = 0; i < sentenceList.size(); i++) {
            html.append("<div class=\"story-sentence\" id=\"sentence-").append(i).append("\">")
                    .append(highlightKeywords(sentenceList.get(i)))
                    .append("</div>");
        }

        html.append("</div>")
                .append("<div class=\"story-progress\">")
                .append("<div class=\"story-dots\" id=\"storyDots\">");

        // 生成进度点
        for (int i = 0; i < sentenceList.size(); i++) {
            html.append("<div class=\"story-dot\" id=\"dot-").append(i).append("\"></div>");
        }

        html.append("</div>")
                .append("</div>")
                .append("<div class=\"choice-summary\">")
                .append("<p><i class=\"fas fa-check-circle\" style=\"color:#4CAF50;\"></i> 你选择了：<strong>").append(choice.get("choice_name")).append("</strong></p>")
                .append("<p>")
                .append("<span style=\"margin-right: 20px;\"><i class=\"fas fa-user-graduate\"></i> ").append(choice.get("education")).append("</span>")
                .append("<span style=\"margin-right: 20px;\"><i class=\"fas fa-briefcase\"></i> ").append(choice.get("occupation")).append("</span>")
                .append("<span style=\"margin-right: 20px;\"><i class=\"fas fa-money-bill-wave\"></i> ").append(choice.get("annual_income")).append("</span>")
                .append("<span><i class=\"fas fa-smile\"></i> ").append(choice.get("satisfaction")).append("</span>")
                .append("</p>")
                // 50岁阶段添加人生总结语
                .append(isLastStage ?
                        "<p style=\"margin-top:15px; color:#ffd700;\"><i class=\"fas fa-quote-left\"></i> 半生风雨，半生收获，这就是属于你的人生轨迹 <i class=\"fas fa-quote-right\"></i></p>" : "")
                .append("</div>")
                .append("</div>") // 关闭story-container

                // 操作按钮
                .append("<div class=\"actions\">")
                .append("<button type=\"button\" class=\"btn btn-secondary\" onclick=\"backToChoice()\" style=\"display:none;\" id=\"backBtn\">")
                .append("<i class=\"fas fa-arrow-left\"></i> 返回重选")
                .append("</button>")
                .append("<button type=\"button\" class=\"btn btn-story\" id=\"continueBtn\" onclick=\"continueStory()\" disabled>")
                .append("<i class=\"fas fa-forward\"></i> 继续故事")
                .append("</button>")
                // 50岁阶段替换为人生总结表单
                .append("<form action=\"").append(isLastStage ? "summary" : "choice").append("\" method=\"get\" style=\"display:none;\" id=\"nextStageForm\">")
                .append("<input type=\"hidden\" name=\"stage\" value=\"").append(nextStage).append("\">")
                // 始终携带userId和sessionId，无需仅在50岁添加
                .append("<input type=\"hidden\" name=\"userId\" value=\"").append(userId).append("\">")
                .append("<input type=\"hidden\" name=\"sessionId\" value=\"").append(sessionId).append("\">")
                .append("<button type=\"submit\" class=\"btn ").append(isLastStage ? "btn-summary" : "").append("\" id=\"nextStageBtn\">")
                .append(isLastStage ?
                        "<i class=\"fas fa-flag-checkered\"></i> 查看完整人生总结" :
                        "<i class=\"fas fa-arrow-right\"></i> 进入下一阶段")
                .append("</button>")
                .append("</form>")
                .append("</div>")
                .append("</div>") // 关闭container

                // JavaScript动画逻辑
                .append("<script>")
                .append("// 故事句子数据\n")
                .append("const storySentences = ").append(sentenceList.size()).append(";\n")
                .append("let currentSentenceIndex = 0;\n")
                .append("let autoShowInterval = null;\n")
                // 50岁阶段放慢展示速度，更有沉浸感
                .append("let typingSpeed = ").append(isLastStage ?
                calculateTypingSpeed(sentenceList) + 1000 :
                calculateTypingSpeed(sentenceList)).append(";\n")
                .append("\n")
                .append("// 页面加载完成后开始自动显示\n")
                .append("document.addEventListener('DOMContentLoaded', function() {\n")
                .append("  // 激活第一个进度点\n")
                .append("  document.getElementById('dot-0').classList.add('active');\n")
                .append("  \n")
                .append("  // 开始自动显示句子\n")
                .append("  startAutoShow();\n")
                .append("});\n")
                .append("\n")
                .append("function startAutoShow() {\n")
                .append("  // 先显示第一句\n")
                .append("  showCurrentSentence();\n")
                .append("  \n")
                .append("  // 设置自动显示下一句的间隔\n")
                .append("  autoShowInterval = setInterval(function() {\n")
                .append("    if (currentSentenceIndex < storySentences) {\n")
                .append("      showCurrentSentence();\n")
                .append("    } else {\n")
                .append("      completeStory();\n")
                .append("      clearInterval(autoShowInterval);\n")
                .append("    }\n")
                .append("  }, typingSpeed);\n")
                .append("}\n")
                .append("\n")
                .append("function showCurrentSentence() {\n")
                .append("  if (currentSentenceIndex >= storySentences) {\n")
                .append("    return;\n")
                .append("  }\n")
                .append("  \n")
                .append("  // 显示当前句子\n")
                .append("  const currentSentence = document.getElementById('sentence-' + currentSentenceIndex);\n")
                .append("  const currentDot = document.getElementById('dot-' + currentSentenceIndex);\n")
                .append("  \n")
                .append("  if (currentSentence) {\n")
                .append("    currentSentence.classList.add('visible');\n")
                .append("    \n")
                .append("    // 滚动到最新显示的句子\n")
                .append("    currentSentence.scrollIntoView({ behavior: 'smooth', block: 'nearest' });\n")
                .append("  }\n")
                .append("  if (currentDot) {\n")
                .append("    currentDot.classList.add('active');\n")
                .append("  }\n")
                .append("  \n")
                .append("  currentSentenceIndex++;\n")
                .append("  \n")
                .append("  // 更新按钮状态\n")
                .append("  if (currentSentenceIndex >= storySentences) {\n")
                .append("    document.getElementById('continueBtn').innerHTML = '<i class=\"fas fa-check\"></i> 完成故事';\n")
                .append("    document.getElementById('continueBtn').disabled = false;\n")
                .append("  }\n")
                .append("}\n")
                .append("\n")
                .append("function continueStory() {\n")
                .append("  if (currentSentenceIndex < storySentences) {\n")
                .append("    // 如果故事还没显示完，立即显示下一句\n")
                .append("    clearInterval(autoShowInterval);\n")
                .append("    showCurrentSentence();\n")
                .append("    \n")
                .append("    // 如果还有更多句子，重新启动自动显示\n")
                .append("    if (currentSentenceIndex < storySentences) {\n")
                .append("      startAutoShow();\n")
                .append("    } else {\n")
                .append("      completeStory();\n")
                .append("    }\n")
                .append("  } else {\n")
                .append("    // 如果故事已显示完，完成故事\n")
                .append("    completeStory();\n")
                .append("  }\n")
                .append("}\n")
                .append("\n")
                .append("function completeStory() {\n")
                .append("  clearInterval(autoShowInterval);\n")
                .append("  \n")
                .append("  // 故事结束，显示导航按钮\n")
                .append("  document.getElementById('continueBtn').style.display = 'none';\n")
                .append("  document.getElementById('backBtn').style.display = 'inline-flex';\n")
                .append("  document.getElementById('nextStageForm').style.display = 'inline-block';\n")
                .append("  \n")
                // 移除冗余的stage判断，直接用isLastStage参数
                .append("  if (").append(isLastStage).append(") {\n")
                .append("    document.getElementById('nextStageBtn').innerHTML = '<i class=\"fas fa-flag-checkered\"></i> 查看完整人生总结';\n")
                .append("    document.getElementById('nextStageForm').action = 'summary';\n")
                .append("  }\n")
                .append("}\n")
                .append("\n")
                .append("function backToChoice() {\n")
                .append("  window.history.back();\n")
                .append("}\n")
                .append("\n")
                .append("// 关键词高亮函数\n")
                .append("function highlightKeywords(text) {\n")
                .append("  return text.replace(/(\\d+)(万|年|月|天|小时|次)/g, '<span class=\"sentence-highlight\">$1$2</span>');\n")
                .append("}\n")
                .append("</script>")
                .append("</body>")
                .append("</html>");

        return html.toString();
    }

    // 添加辅助方法来计算打字速度
    private int calculateTypingSpeed(List<String> sentences) {
        // 基础时间（毫秒）
        int baseSpeed = 2000; // 2秒

        // 计算平均句子长度
        int totalLength = 0;
        for (String sentence : sentences) {
            totalLength += sentence.length();
        }
        int avgLength = sentences.isEmpty() ? 0 : totalLength / sentences.size();

        // 根据平均长度调整速度
        if (avgLength > 80) {
            return 3000; // 长句子：3秒
        } else if (avgLength > 50) {
            return 2500; // 中等句子：2.5秒
        } else {
            return 2000; // 短句子：2秒
        }
    }

    private String highlightKeywords(String text) {
        if (text == null) return "";
        return text.replaceAll("(\\d+)(万|年|月|天|小时|次)", "<span class=\"sentence-highlight\">$1$2</span>");
    }

}
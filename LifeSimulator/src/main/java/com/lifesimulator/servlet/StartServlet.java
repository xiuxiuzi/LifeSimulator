package com.lifesimulator.servlet;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@WebServlet("/start")
public class StartServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 生成用户ID和会话ID
        String ip = request.getRemoteAddr();
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String userId = ip + "_" + timestamp;
        String sessionId = UUID.randomUUID().toString().substring(0, 8);

        // 保存到Session
        HttpSession session = request.getSession(true);
        session.setAttribute("userId", userId);
        session.setAttribute("sessionId", sessionId);
        session.setAttribute("currentStage", 1);

        // 重定向到第一个选择页面
        response.sendRedirect("choice?stage=1");
    }
}
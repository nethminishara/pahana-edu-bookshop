package com.pahanaedu.servlet;

import com.pahanaedu.model.Bill;
import com.pahanaedu.service.BillService;
import com.google.gson.Gson;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/bill/*")
public class BillServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private BillService billService;
    private Gson gson;
    
    @Override
    public void init() throws ServletException {
        billService = new BillService();
        gson = new Gson();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        
        try {
            if (pathInfo != null && pathInfo.equals("/stats")) {
                // Return bill statistics
                Map<String, Object> stats = new HashMap<>();
                stats.put("totalBills", 0);
                stats.put("totalRevenue", 0.0);
                out.print(gson.toJson(stats));
            } else {
                // Return empty array for now
                out.print("[]");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"Internal server error\"}");
            e.printStackTrace();
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        
        try {
            // For now, return success
            out.print("{\"success\":true,\"message\":\"Bill functionality coming soon\",\"billId\":1}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\":false,\"message\":\"Internal server error\"}");
            e.printStackTrace();
        }
    }
}

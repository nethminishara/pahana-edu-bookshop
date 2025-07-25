package com.pahanaedu.servlet;

import com.pahanaedu.model.Item;
import com.pahanaedu.service.ItemService;
import com.google.gson.Gson;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/item/*")
public class ItemServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private ItemService itemService;
    private Gson gson;
    
    @Override
    public void init() throws ServletException {
        itemService = new ItemService();
        gson = new Gson();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // Get all items
                List<Item> items = itemService.getAllItems();
                out.print(gson.toJson(items));
            } else if (pathInfo.startsWith("/code/")) {
                // Get item by code
                String itemCode = pathInfo.substring(6);
                Item item = itemService.getItemByCode(itemCode);
                if (item != null) {
                    out.print(gson.toJson(item));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"error\":\"Item not found\"}");
                }
            } else if (pathInfo.startsWith("/")) {
                // Get item by ID
                try {
                    int itemId = Integer.parseInt(pathInfo.substring(1));
                    Item item = itemService.getItemById(itemId);
                    if (item != null) {
                        out.print(gson.toJson(item));
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        out.print("{\"error\":\"Item not found\"}");
                    }
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"Invalid item ID\"}");
                }
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
            // Create new item
            Item item = new Item();
            item.setItemCode(request.getParameter("itemCode"));
            item.setTitle(request.getParameter("title"));
            item.setAuthor(request.getParameter("author"));
            item.setCategory(request.getParameter("category"));
            item.setPrice(Double.parseDouble(request.getParameter("price")));
            item.setStockQuantity(Integer.parseInt(request.getParameter("stockQuantity")));
            
            if (itemService.validateItemData(item)) {
                boolean success = itemService.addItem(item);
                if (success) {
                    out.print("{\"success\":true,\"message\":\"Item added successfully\"}");
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.print("{\"success\":false,\"message\":\"Failed to add item\"}");
                }
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\":false,\"message\":\"Invalid item data\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\":false,\"message\":\"Internal server error\"}");
            e.printStackTrace();
        }
    }
    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        
        try {
            if (pathInfo != null && pathInfo.length() > 1) {
                int itemId = Integer.parseInt(pathInfo.substring(1));
                
                Item item = new Item();
                item.setItemId(itemId);
                item.setTitle(request.getParameter("title"));
                item.setAuthor(request.getParameter("author"));
                item.setCategory(request.getParameter("category"));
                item.setPrice(Double.parseDouble(request.getParameter("price")));
                item.setStockQuantity(Integer.parseInt(request.getParameter("stockQuantity")));
                
                if (itemService.validateItemData(item)) {
                    boolean success = itemService.updateItem(item);
                    if (success) {
                        out.print("{\"success\":true,\"message\":\"Item updated successfully\"}");
                    } else {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        out.print("{\"success\":false,\"message\":\"Failed to update item\"}");
                    }
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"success\":false,\"message\":\"Invalid item data\"}");
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\":false,\"message\":\"Internal server error\"}");
            e.printStackTrace();
        }
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        
        try {
            if (pathInfo != null && pathInfo.length() > 1) {
                int itemId = Integer.parseInt(pathInfo.substring(1));
                boolean success = itemService.deleteItem(itemId);
                
                if (success) {
                    out.print("{\"success\":true,\"message\":\"Item deleted successfully\"}");
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.print("{\"success\":false,\"message\":\"Failed to delete item\"}");
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\":false,\"message\":\"Internal server error\"}");
            e.printStackTrace();
        }
    }
}

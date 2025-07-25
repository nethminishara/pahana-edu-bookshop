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
            // Get and validate parameters
            String itemCode = request.getParameter("itemCode");
            String title = request.getParameter("title");
            String author = request.getParameter("author");
            String category = request.getParameter("category");
            String priceStr = request.getParameter("price");
            String stockStr = request.getParameter("stockQuantity");
            
            // Validate required fields
            if (itemCode == null || itemCode.trim().isEmpty() ||
                title == null || title.trim().isEmpty() ||
                priceStr == null || priceStr.trim().isEmpty() ||
                stockStr == null || stockStr.trim().isEmpty()) {
                
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\":false,\"message\":\"All required fields must be filled\"}");
                return;
            }
            
            // Parse numeric values with error handling
            double price;
            int stockQuantity;
            
            try {
                price = Double.parseDouble(priceStr.trim());
                stockQuantity = Integer.parseInt(stockStr.trim());
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\":false,\"message\":\"Invalid price or stock quantity\"}");
                return;
            }
            
            // Create item object
            Item item = new Item();
            item.setItemCode(itemCode.trim());
            item.setTitle(title.trim());
            item.setAuthor(author != null ? author.trim() : "");
            item.setCategory(category != null ? category.trim() : "");
            item.setPrice(price);
            item.setStockQuantity(stockQuantity);
            
            if (itemService.validateItemData(item)) {
                boolean success = itemService.addItem(item);
                if (success) {
                    out.print("{\"success\":true,\"message\":\"Item added successfully\"}");
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.print("{\"success\":false,\"message\":\"Failed to add item - database error\"}");
                }
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\":false,\"message\":\"Invalid item data\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\":false,\"message\":\"Internal server error: " + e.getMessage() + "\"}");
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

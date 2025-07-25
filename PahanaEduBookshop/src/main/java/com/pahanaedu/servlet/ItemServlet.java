package com.pahanaedu.servlet;

import com.pahanaedu.model.Item;
import com.pahanaedu.service.ItemService;
import com.google.gson.Gson;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String method = request.getMethod();
        if ("PUT".equals(method)) {
            doPut(request, response);
        } else if ("DELETE".equals(method)) {
            doDelete(request, response);
        } else {
            super.service(request, response);
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
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
            out.print("{\"error\":\"Internal server error: " + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            // Get and validate parameters
            String itemCode = getParameterSafely(request, "itemCode");
            String title = getParameterSafely(request, "title");
            String author = getParameterSafely(request, "author");
            String category = getParameterSafely(request, "category");
            String priceStr = getParameterSafely(request, "price");
            String stockStr = getParameterSafely(request, "stockQuantity");
            
            System.out.println("POST - Received parameters - ItemCode: " + itemCode + ", Title: " + title + 
                             ", Author: " + author + ", Category: " + category + 
                             ", Price: " + priceStr + ", Stock: " + stockStr);
            
            // Validate required fields
            if (itemCode == null || title == null || priceStr == null || stockStr == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\":false,\"message\":\"Item code, title, price, and stock quantity are required\"}");
                return;
            }
            
            // Parse numeric values with error handling
            double price;
            int stockQuantity;
            
            try {
                price = Double.parseDouble(priceStr);
                stockQuantity = Integer.parseInt(stockStr);
                
                if (price < 0 || stockQuantity < 0) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"success\":false,\"message\":\"Price and stock quantity must be non-negative\"}");
                    return;
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\":false,\"message\":\"Invalid price or stock quantity format\"}");
                return;
            }
            
            // Create item object
            Item item = new Item();
            item.setItemCode(itemCode);
            item.setTitle(title);
            item.setAuthor(author != null ? author : "");
            item.setCategory(category != null ? category : "");
            item.setPrice(price);
            item.setStockQuantity(stockQuantity);
            
            boolean success = itemService.addItem(item);
            if (success) {
                out.print("{\"success\":true,\"message\":\"Item added successfully\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"success\":false,\"message\":\"Failed to add item - database error\"}");
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
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            if (pathInfo != null && pathInfo.length() > 1) {
                int itemId = Integer.parseInt(pathInfo.substring(1));
                
                // Parse parameters from request body for PUT
                Map<String, String> params = parseRequestBody(request);
                
                String title = params.get("title");
                String author = params.get("author");
                String category = params.get("category");
                String priceStr = params.get("price");
                String stockStr = params.get("stockQuantity");
                
                System.out.println("PUT - Received parameters - Title: " + title + ", Author: " + author + 
                                 ", Category: " + category + ", Price: " + priceStr + ", Stock: " + stockStr);
                
                // Validate required fields
                if (title == null || priceStr == null || stockStr == null) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"success\":false,\"message\":\"Title, price, and stock quantity are required\"}");
                    return;
                }
                
                // Parse numeric values
                double price = Double.parseDouble(priceStr);
                int stockQuantity = Integer.parseInt(stockStr);
                
                if (price < 0 || stockQuantity < 0) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"success\":false,\"message\":\"Price and stock quantity must be non-negative\"}");
                    return;
                }
                
                Item item = new Item();
                item.setItemId(itemId);
                item.setTitle(title);
                item.setAuthor(author != null ? author : "");
                item.setCategory(category != null ? category : "");
                item.setPrice(price);
                item.setStockQuantity(stockQuantity);
                
                boolean success = itemService.updateItem(item);
                if (success) {
                    out.print("{\"success\":true,\"message\":\"Item updated successfully\"}");
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.print("{\"success\":false,\"message\":\"Failed to update item\"}");
                }
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\":false,\"message\":\"Item ID is required\"}");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"success\":false,\"message\":\"Invalid number format\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\":false,\"message\":\"Internal server error: " + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
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
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\":false,\"message\":\"Item ID is required\"}");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"success\":false,\"message\":\"Invalid item ID format\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\":false,\"message\":\"Internal server error: " + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }
    
    // Helper method to safely get parameters
    private String getParameterSafely(HttpServletRequest request, String paramName) {
        String value = request.getParameter(paramName);
        return (value != null && !value.trim().isEmpty()) ? value.trim() : null;
    }
    
    // Helper method to parse request body for PUT requests
    private Map<String, String> parseRequestBody(HttpServletRequest request) throws IOException {
        Map<String, String> params = new HashMap<>();
        
        StringBuilder body = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            body.append(line);
        }
        
        String bodyString = body.toString();
        System.out.println("Raw request body: " + bodyString);
        
        if (!bodyString.isEmpty()) {
            String[] pairs = bodyString.split("&");
            for (String pair : pairs) {
                if (pair.contains("=")) {
                    String[] keyValue = pair.split("=", 2);
                    try {
                        String key = URLDecoder.decode(keyValue[0], "UTF-8");
                        String value = keyValue.length > 1 ? URLDecoder.decode(keyValue[1], "UTF-8") : "";
                        // Only add non-empty values
                        if (!value.trim().isEmpty()) {
                            params.put(key, value.trim());
                        }
                        System.out.println("Parsed parameter: " + key + " = " + value);
                    } catch (Exception e) {
                        System.err.println("Error parsing parameter: " + pair);
                    }
                }
            }
        }
        
        return params;
    }
}

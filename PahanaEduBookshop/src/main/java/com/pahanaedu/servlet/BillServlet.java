package com.pahanaedu.servlet;

import com.pahanaedu.model.*;
import com.pahanaedu.service.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@WebServlet("/bill/*")
public class BillServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private BillService billService;
    private CustomerService customerService;
    private ItemService itemService;
    private Gson gson;
    
    @Override
    public void init() throws ServletException {
        billService = new BillService();
        customerService = new CustomerService();
        itemService = new ItemService();
        gson = new Gson();
    }
    
    // Override service method to handle all HTTP methods
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
            if (pathInfo != null && pathInfo.equals("/stats")) {
                // Return bill statistics
                Map<String, Object> stats = new HashMap<>();
                stats.put("totalBills", billService.getTotalBillCount());
                stats.put("totalRevenue", billService.getTotalRevenue());
                out.print(gson.toJson(stats));
            } else if (pathInfo != null && pathInfo.startsWith("/")) {
                // Get specific bill by ID
                try {
                    int billId = Integer.parseInt(pathInfo.substring(1));
                    Bill bill = billService.getBillById(billId);
                    if (bill != null) {
                        out.print(gson.toJson(bill));
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        out.print("{\"error\":\"Bill not found\"}");
                    }
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"Invalid bill ID\"}");
                }
            } else {
                // Return all bills
                List<Bill> bills = billService.getAllBills();
                out.print(gson.toJson(bills));
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
            // Read JSON data from request body
            StringBuilder jsonBuffer = new StringBuilder();
            String line;
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null) {
                jsonBuffer.append(line);
            }
            
            String jsonString = jsonBuffer.toString();
            if (jsonString.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\":false,\"message\":\"Empty request body\"}");
                return;
            }
            
            // Parse JSON
            JsonObject billData = JsonParser.parseString(jsonString).getAsJsonObject();
            
            // Validate required fields
            if (!billData.has("customerId") || !billData.has("items") || !billData.has("totalAmount")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\":false,\"message\":\"Missing required fields: customerId, items, totalAmount\"}");
                return;
            }
            
            int customerId = billData.get("customerId").getAsInt();
            JsonArray itemsArray = billData.getAsJsonArray("items");
            double totalAmount = billData.get("totalAmount").getAsDouble();
            
            // Validate customer exists
            Customer customer = customerService.getCustomerById(customerId);
            if (customer == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\":false,\"message\":\"Customer not found\"}");
                return;
            }
            
            // Validate items and check stock
            List<BillItem> billItems = new ArrayList<>();
            double calculatedTotal = 0.0;
            
            for (int i = 0; i < itemsArray.size(); i++) {
                JsonObject itemData = itemsArray.get(i).getAsJsonObject();
                
                if (!itemData.has("itemId") || !itemData.has("quantity") || !itemData.has("unitPrice")) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"success\":false,\"message\":\"Invalid item data at index " + i + "\"}");
                    return;
                }
                
                int itemId = itemData.get("itemId").getAsInt();
                int quantity = itemData.get("quantity").getAsInt();
                double unitPrice = itemData.get("unitPrice").getAsDouble();
                
                // Validate item exists and has sufficient stock
                Item item = itemService.getItemById(itemId);
                if (item == null) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"success\":false,\"message\":\"Item with ID " + itemId + " not found\"}");
                    return;
                }
                
                if (item.getStockQuantity() < quantity) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"success\":false,\"message\":\"Insufficient stock for item: " + item.getTitle() + 
                            ". Available: " + item.getStockQuantity() + ", Requested: " + quantity + "\"}");
                    return;
                }
                
                BillItem billItem = new BillItem();
                billItem.setItemId(itemId);
                billItem.setQuantity(quantity);
                billItem.setUnitPrice(unitPrice);
                billItem.setTotalPrice(quantity * unitPrice);
                billItem.setItem(item);
                
                billItems.add(billItem);
                calculatedTotal += billItem.getTotalPrice();
            }
            
            // Create bill
            Bill bill = new Bill();
            bill.setBillNumber(generateBillNumber());
            bill.setCustomerId(customerId);
            bill.setCustomer(customer);
            bill.setTotalAmount(totalAmount);
            bill.setStatus("PENDING");
            bill.setBillItems(billItems);
            
            // Save bill and update stock
            boolean success = billService.createBill(bill);
            
            if (success) {
                // Update item stock quantities
                for (BillItem billItem : billItems) {
                    Item item = billItem.getItem();
                    int newStock = item.getStockQuantity() - billItem.getQuantity();
                    itemService.updateStock(item.getItemId(), newStock);
                }
                
                out.print("{\"success\":true,\"message\":\"Bill generated successfully\",\"billId\":" + 
                        bill.getBillId() + ",\"billNumber\":\"" + bill.getBillNumber() + "\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"success\":false,\"message\":\"Failed to generate bill - database error\"}");
            }
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\":false,\"message\":\"Internal server error: " + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }
    
    private String generateBillNumber() {
        return "BILL" + System.currentTimeMillis() + (new Random().nextInt(1000));
    }
}

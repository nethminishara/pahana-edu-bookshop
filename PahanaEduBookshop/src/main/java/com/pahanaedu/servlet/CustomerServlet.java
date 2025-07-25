package com.pahanaedu.servlet;

import com.pahanaedu.model.Customer;
import com.pahanaedu.service.CustomerService;
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

@WebServlet("/customer/*")
public class CustomerServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private CustomerService customerService;
    private Gson gson;
    
    @Override
    public void init() throws ServletException {
        customerService = new CustomerService();
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
                // Get all customers
                List<Customer> customers = customerService.getAllCustomers();
                out.print(gson.toJson(customers));
            } else if (pathInfo.startsWith("/account/")) {
                // Get customer by account number
                String accountNumber = pathInfo.substring(9);
                Customer customer = customerService.getCustomerByAccountNumber(accountNumber);
                if (customer != null) {
                    out.print(gson.toJson(customer));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"error\":\"Customer not found\"}");
                }
            } else if (pathInfo.startsWith("/")) {
                // Get customer by ID
                try {
                    int customerId = Integer.parseInt(pathInfo.substring(1));
                    Customer customer = customerService.getCustomerById(customerId);
                    if (customer != null) {
                        out.print(gson.toJson(customer));
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        out.print("{\"error\":\"Customer not found\"}");
                    }
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"Invalid customer ID\"}");
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
            // Get parameters using the helper method
            String accountNumber = getParameterSafely(request, "accountNumber");
            String name = getParameterSafely(request, "name");
            String address = getParameterSafely(request, "address");
            String telephone = getParameterSafely(request, "telephone");
            String email = getParameterSafely(request, "email");
            
            System.out.println("POST - Received parameters - Name: " + name + ", Telephone: " + telephone + 
                             ", Email: " + email + ", Address: " + address + ", AccountNumber: " + accountNumber);
            
            // Validate required fields
            if (name == null || name.isEmpty() || telephone == null || telephone.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\":false,\"message\":\"Name and telephone are required\"}");
                return;
            }
            
            // Create customer object
            Customer customer = new Customer();
            customer.setAccountNumber(accountNumber != null && !accountNumber.isEmpty() ? 
                                    accountNumber : generateAccountNumber());
            customer.setName(name);
            customer.setAddress(address != null ? address : "");
            customer.setTelephone(telephone);
            customer.setEmail(email != null ? email : "");
            
            boolean success = customerService.addCustomer(customer);
            if (success) {
                out.print("{\"success\":true,\"message\":\"Customer added successfully\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"success\":false,\"message\":\"Failed to add customer - database error\"}");
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
                int customerId = Integer.parseInt(pathInfo.substring(1));
                
                // Parse parameters from request body for PUT
                Map<String, String> params = parseRequestBody(request);
                
                String name = params.get("name");
                String address = params.get("address");
                String telephone = params.get("telephone");
                String email = params.get("email");
                
                System.out.println("PUT - Received parameters - Name: " + name + ", Telephone: " + telephone + 
                                 ", Email: " + email + ", Address: " + address);
                
                // Validate required fields
                if (name == null || name.isEmpty() || telephone == null || telephone.isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"success\":false,\"message\":\"Name and telephone are required\"}");
                    return;
                }
                
                Customer customer = new Customer();
                customer.setCustomerId(customerId);
                customer.setName(name);
                customer.setAddress(address != null ? address : "");
                customer.setTelephone(telephone);
                customer.setEmail(email != null ? email : "");
                
                boolean success = customerService.updateCustomer(customer);
                if (success) {
                    out.print("{\"success\":true,\"message\":\"Customer updated successfully\"}");
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.print("{\"success\":false,\"message\":\"Failed to update customer\"}");
                }
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\":false,\"message\":\"Customer ID is required\"}");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"success\":false,\"message\":\"Invalid customer ID format\"}");
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
                int customerId = Integer.parseInt(pathInfo.substring(1));
                boolean success = customerService.deleteCustomer(customerId);
                
                if (success) {
                    out.print("{\"success\":true,\"message\":\"Customer deleted successfully\"}");
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.print("{\"success\":false,\"message\":\"Failed to delete customer\"}");
                }
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\":false,\"message\":\"Customer ID is required\"}");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"success\":false,\"message\":\"Invalid customer ID format\"}");
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
    
    private String generateAccountNumber() {
        return "PAH" + System.currentTimeMillis();
    }
}

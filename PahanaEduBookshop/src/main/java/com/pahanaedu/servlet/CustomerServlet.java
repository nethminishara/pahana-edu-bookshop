package com.pahanaedu.servlet;

import com.pahanaedu.model.Customer;
import com.pahanaedu.service.CustomerService;
import com.google.gson.Gson;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/customer/*")
public class CustomerServlet extends HttpServlet {
    private CustomerService customerService;
    private Gson gson;
    
    @Override
    public void init() throws ServletException {
        customerService = new CustomerService();
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
            // Create new customer
            Customer customer = new Customer();
            customer.setAccountNumber(request.getParameter("accountNumber"));
            customer.setName(request.getParameter("name"));
            customer.setAddress(request.getParameter("address"));
            customer.setTelephone(request.getParameter("telephone"));
            customer.setEmail(request.getParameter("email"));
            
            if (customerService.validateCustomerData(customer)) {
                boolean success = customerService.addCustomer(customer);
                if (success) {
                    out.print("{\"success\":true,\"message\":\"Customer added successfully\"}");
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.print("{\"success\":false,\"message\":\"Failed to add customer\"}");
                }
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\":false,\"message\":\"Invalid customer data\"}");
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
                int customerId = Integer.parseInt(pathInfo.substring(1));
                
                Customer customer = new Customer();
                customer.setCustomerId(customerId);
                customer.setName(request.getParameter("name"));
                customer.setAddress(request.getParameter("address"));
                customer.setTelephone(request.getParameter("telephone"));
                customer.setEmail(request.getParameter("email"));
                
                if (customerService.validateCustomerData(customer)) {
                    boolean success = customerService.updateCustomer(customer);
                    if (success) {
                        out.print("{\"success\":true,\"message\":\"Customer updated successfully\"}");
                    } else {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        out.print("{\"success\":false,\"message\":\"Failed to update customer\"}");
                    }
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"success\":false,\"message\":\"Invalid customer data\"}");
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
                int customerId = Integer.parseInt(pathInfo.substring(1));
                boolean success = customerService.deleteCustomer(customerId);
                
                if (success) {
                    out.print("{\"success\":true,\"message\":\"Customer deleted successfully\"}");
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.print("{\"success\":false,\"message\":\"Failed to delete customer\"}");
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\":false,\"message\":\"Internal server error\"}");
            e.printStackTrace();
        }
    }
}

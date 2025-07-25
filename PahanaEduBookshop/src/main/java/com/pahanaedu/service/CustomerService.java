package com.pahanaedu.service;

import com.pahanaedu.dao.CustomerDAO;
import com.pahanaedu.model.Customer;
import java.util.List;
import java.util.Random;

public class CustomerService {
    private CustomerDAO customerDAO;
    
    public CustomerService() {
        this.customerDAO = new CustomerDAO();
    }
    
    public boolean addCustomer(Customer customer) {
        // Generate unique account number if not provided
        if (customer.getAccountNumber() == null || customer.getAccountNumber().isEmpty()) {
            customer.setAccountNumber(generateAccountNumber());
        }
        return customerDAO.addCustomer(customer);
    }
    
    public Customer getCustomerById(int customerId) {
        return customerDAO.getCustomerById(customerId);
    }
    
    public Customer getCustomerByAccountNumber(String accountNumber) {
        return customerDAO.getCustomerByAccountNumber(accountNumber);
    }
    
    public List<Customer> getAllCustomers() {
        return customerDAO.getAllCustomers();
    }
    
    public boolean updateCustomer(Customer customer) {
        return customerDAO.updateCustomer(customer);
    }
    
    public boolean deleteCustomer(int customerId) {
        return customerDAO.deleteCustomer(customerId);
    }
    
    private String generateAccountNumber() {
        Random random = new Random();
        return "PAH" + System.currentTimeMillis() + random.nextInt(1000);
    }
    
    public boolean validateCustomerData(Customer customer) {
        return customer.getName() != null && !customer.getName().trim().isEmpty() &&
               customer.getTelephone() != null && !customer.getTelephone().trim().isEmpty();
    }
}

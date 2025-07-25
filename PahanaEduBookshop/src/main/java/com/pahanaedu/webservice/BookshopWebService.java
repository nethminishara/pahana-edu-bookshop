package com.pahanaedu.webservice;

import com.pahanaedu.model.Customer;
import com.pahanaedu.model.Item;
import com.pahanaedu.service.CustomerService;
import com.pahanaedu.service.ItemService;
import com.google.gson.Gson;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import java.util.List;

@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class BookshopWebService {
    
    private CustomerService customerService;
    private ItemService itemService;
    private Gson gson;
    
    public BookshopWebService() {
        this.customerService = new CustomerService();
        this.itemService = new ItemService();
        this.gson = new Gson();
    }
    
    @WebMethod
    public String getAllCustomers() {
        try {
            List<Customer> customers = customerService.getAllCustomers();
            return gson.toJson(customers);
        } catch (Exception e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }
    
    @WebMethod
    public String getCustomerByAccount(String accountNumber) {
        try {
            Customer customer = customerService.getCustomerByAccountNumber(accountNumber);
            if (customer != null) {
                return gson.toJson(customer);
            } else {
                return "{\"error\":\"Customer not found\"}";
            }
        } catch (Exception e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }
    
    @WebMethod
    public String getAllItems() {
        try {
            List<Item> items = itemService.getAllItems();
            return gson.toJson(items);
        } catch (Exception e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }
    
    @WebMethod
    public String getItemByCode(String itemCode) {
        try {
            Item item = itemService.getItemByCode(itemCode);
            if (item != null) {
                return gson.toJson(item);
            } else {
                return "{\"error\":\"Item not found\"}";
            }
        } catch (Exception e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }
    
    @WebMethod
    public boolean addCustomer(String customerJson) {
        try {
            Customer customer = gson.fromJson(customerJson, Customer.class);
            return customerService.addCustomer(customer);
        } catch (Exception e) {
            return false;
        }
    }
    
    @WebMethod
    public boolean addItem(String itemJson) {
        try {
            Item item = gson.fromJson(itemJson, Item.class);
            return itemService.addItem(item);
        } catch (Exception e) {
            return false;
        }
    }
}

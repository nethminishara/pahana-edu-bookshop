package com.pahanaedu.model;

import java.util.List;

public class Bill {
    private int billId;
    private String billNumber;
    private int customerId;
    private Customer customer;
    private double totalAmount;
    private String billDate;
    private String status;
    private List<BillItem> billItems;
    
    // Constructors
    public Bill() {}
    
    public Bill(String billNumber, int customerId, double totalAmount, String status) {
        this.billNumber = billNumber;
        this.customerId = customerId;
        this.totalAmount = totalAmount;
        this.status = status;
    }
    
    // Getters and Setters
    public int getBillId() { return billId; }
    public void setBillId(int billId) { this.billId = billId; }
    
    public String getBillNumber() { return billNumber; }
    public void setBillNumber(String billNumber) { this.billNumber = billNumber; }
    
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    
    public String getBillDate() { return billDate; }
    public void setBillDate(String billDate) { this.billDate = billDate; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public List<BillItem> getBillItems() { return billItems; }
    public void setBillItems(List<BillItem> billItems) { this.billItems = billItems; }
}

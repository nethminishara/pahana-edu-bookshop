package com.pahanaedu.model;

public class BillItem {
    private int id;
    private int billId;
    private int itemId;
    private Item item;
    private int quantity;
    private double unitPrice;
    private double totalPrice;
    
    // Constructors
    public BillItem() {}
    
    public BillItem(int billId, int itemId, int quantity, double unitPrice, double totalPrice) {
        this.billId = billId;
        this.itemId = itemId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getBillId() { return billId; }
    public void setBillId(int billId) { this.billId = billId; }
    
    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }
    
    public Item getItem() { return item; }
    public void setItem(Item item) { this.item = item; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
    
    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
}

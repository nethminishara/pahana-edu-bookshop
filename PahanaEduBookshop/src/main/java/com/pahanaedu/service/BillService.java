package com.pahanaedu.service;

import com.pahanaedu.dao.BillDAO;
import com.pahanaedu.model.Bill;
import java.util.List;

public class BillService {
    private BillDAO billDAO;
    
    public BillService() {
        this.billDAO = new BillDAO();
    }
    
    public boolean createBill(Bill bill) {
        return billDAO.addBill(bill);
    }
    
    public List<Bill> getAllBills() {
        return billDAO.getAllBills();
    }
    
    public Bill getBillById(int billId) {
        return billDAO.getBillById(billId);
    }
    
    public Bill getBillByNumber(String billNumber) {
        return billDAO.getBillByNumber(billNumber);
    }
    
    public boolean updateBillStatus(int billId, String status) {
        return billDAO.updateBillStatus(billId, status);
    }
    
    public int getTotalBillCount() {
        return billDAO.getTotalBillCount();
    }
    
    public double getTotalRevenue() {
        return billDAO.getTotalRevenue();
    }
    
    public List<Bill> getBillsByCustomer(int customerId) {
        return billDAO.getBillsByCustomer(customerId);
    }
    
    public boolean validateBillData(Bill bill) {
        return bill.getCustomerId() > 0 && 
               bill.getTotalAmount() > 0 && 
               bill.getBillItems() != null && 
               !bill.getBillItems().isEmpty();
    }
}

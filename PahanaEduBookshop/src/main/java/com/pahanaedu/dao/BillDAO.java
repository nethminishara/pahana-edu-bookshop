package com.pahanaedu.dao;

import com.pahanaedu.model.*;
import com.pahanaedu.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BillDAO {
    
    public boolean addBill(Bill bill) {
        String billSql = "INSERT INTO bills (bill_number, customer_id, total_amount, status) VALUES (?, ?, ?, ?)";
        String itemSql = "INSERT INTO bill_items (bill_id, item_id, quantity, unit_price, total_price) VALUES (?, ?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);
            
            // Insert bill
            PreparedStatement billStmt = conn.prepareStatement(billSql, Statement.RETURN_GENERATED_KEYS);
            billStmt.setString(1, bill.getBillNumber());
            billStmt.setInt(2, bill.getCustomerId());
            billStmt.setDouble(3, bill.getTotalAmount());
            billStmt.setString(4, bill.getStatus());
            
            int result = billStmt.executeUpdate();
            
            if (result > 0) {
                ResultSet rs = billStmt.getGeneratedKeys();
                if (rs.next()) {
                    int billId = rs.getInt(1);
                    bill.setBillId(billId);
                    
                    // Insert bill items
                    PreparedStatement itemStmt = conn.prepareStatement(itemSql);
                    for (BillItem item : bill.getBillItems()) {
                        itemStmt.setInt(1, billId);
                        itemStmt.setInt(2, item.getItemId());
                        itemStmt.setInt(3, item.getQuantity());
                        itemStmt.setDouble(4, item.getUnitPrice());
                        itemStmt.setDouble(5, item.getTotalPrice());
                        itemStmt.addBatch();
                    }
                    itemStmt.executeBatch();
                }
            }
            
            conn.commit();
            return true;
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public List<Bill> getAllBills() {
        List<Bill> bills = new ArrayList<>();
        String sql = "SELECT b.*, c.name as customer_name FROM bills b " +
                    "LEFT JOIN customers c ON b.customer_id = c.customer_id ORDER BY b.bill_date DESC";
        
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Bill bill = mapResultSetToBill(rs);
                bills.add(bill);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bills;
    }
    
    public Bill getBillById(int billId) {
        Bill bill = null;
        String sql = "SELECT b.*, c.name as customer_name FROM bills b " +
                    "LEFT JOIN customers c ON b.customer_id = c.customer_id WHERE b.bill_id = ?";
        
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, billId);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                bill = mapResultSetToBill(rs);
                bill.setBillItems(getBillItems(billId));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bill;
    }
    
    public Bill getBillByNumber(String billNumber) {
        Bill bill = null;
        String sql = "SELECT b.*, c.name as customer_name FROM bills b " +
                    "LEFT JOIN customers c ON b.customer_id = c.customer_id WHERE b.bill_number = ?";
        
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, billNumber);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                bill = mapResultSetToBill(rs);
                bill.setBillItems(getBillItems(bill.getBillId()));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bill;
    }
    
    public boolean updateBillStatus(int billId, String status) {
        String sql = "UPDATE bills SET status = ? WHERE bill_id = ?";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status);
            pstmt.setInt(2, billId);
            
            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public int getTotalBillCount() {
        String sql = "SELECT COUNT(*) FROM bills";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    public double getTotalRevenue() {
        String sql = "SELECT SUM(total_amount) FROM bills WHERE status != 'CANCELLED'";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }
    
    public List<Bill> getBillsByCustomer(int customerId) {
        List<Bill> bills = new ArrayList<>();
        String sql = "SELECT b.*, c.name as customer_name FROM bills b " +
                    "LEFT JOIN customers c ON b.customer_id = c.customer_id " +
                    "WHERE b.customer_id = ? ORDER BY b.bill_date DESC";
        
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Bill bill = mapResultSetToBill(rs);
                bills.add(bill);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bills;
    }
    
    private List<BillItem> getBillItems(int billId) {
        List<BillItem> items = new ArrayList<>();
        String sql = "SELECT bi.*, i.title, i.item_code FROM bill_items bi " +
                    "LEFT JOIN items i ON bi.item_id = i.item_id WHERE bi.bill_id = ?";
        
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, billId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                BillItem item = new BillItem();
                item.setId(rs.getInt("id"));
                item.setBillId(rs.getInt("bill_id"));
                item.setItemId(rs.getInt("item_id"));
                item.setQuantity(rs.getInt("quantity"));
                item.setUnitPrice(rs.getDouble("unit_price"));
                item.setTotalPrice(rs.getDouble("total_price"));
                
                // Create item object with basic info
                Item itemInfo = new Item();
                itemInfo.setItemId(rs.getInt("item_id"));
                itemInfo.setTitle(rs.getString("title"));
                itemInfo.setItemCode(rs.getString("item_code"));
                item.setItem(itemInfo);
                
                items.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }
    
    private Bill mapResultSetToBill(ResultSet rs) throws SQLException {
        Bill bill = new Bill();
        bill.setBillId(rs.getInt("bill_id"));
        bill.setBillNumber(rs.getString("bill_number"));
        bill.setCustomerId(rs.getInt("customer_id"));
        bill.setTotalAmount(rs.getDouble("total_amount"));
        bill.setBillDate(rs.getString("bill_date"));
        bill.setStatus(rs.getString("status"));
        
        // Create customer object with basic info
        Customer customer = new Customer();
        customer.setCustomerId(rs.getInt("customer_id"));
        customer.setName(rs.getString("customer_name"));
        bill.setCustomer(customer);
        
        return bill;
    }
}

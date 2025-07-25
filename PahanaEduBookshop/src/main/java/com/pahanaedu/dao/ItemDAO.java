package com.pahanaedu.dao;

import com.pahanaedu.model.Item;
import com.pahanaedu.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemDAO {
    
    public boolean addItem(Item item) {
        String sql = "INSERT INTO items (item_code, title, author, category, price, stock_quantity) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, item.getItemCode());
            pstmt.setString(2, item.getTitle());
            pstmt.setString(3, item.getAuthor());
            pstmt.setString(4, item.getCategory());
            pstmt.setDouble(5, item.getPrice());
            pstmt.setInt(6, item.getStockQuantity());
            
            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public Item getItemById(int itemId) {
        Item item = null;
        String sql = "SELECT * FROM items WHERE item_id = ?";
        
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, itemId);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                item = mapResultSetToItem(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return item;
    }
    
    public Item getItemByCode(String itemCode) {
        Item item = null;
        String sql = "SELECT * FROM items WHERE item_code = ?";
        
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, itemCode);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                item = mapResultSetToItem(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return item;
    }
    
    public List<Item> getAllItems() {
        List<Item> items = new ArrayList<>();
        String sql = "SELECT * FROM items ORDER BY title";
        
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                items.add(mapResultSetToItem(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }
    
    public boolean updateItem(Item item) {
        String sql = "UPDATE items SET title = ?, author = ?, category = ?, price = ?, stock_quantity = ? WHERE item_id = ?";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, item.getTitle());
            pstmt.setString(2, item.getAuthor());
            pstmt.setString(3, item.getCategory());
            pstmt.setDouble(4, item.getPrice());
            pstmt.setInt(5, item.getStockQuantity());
            pstmt.setInt(6, item.getItemId());
            
            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean deleteItem(int itemId) {
        String sql = "DELETE FROM items WHERE item_id = ?";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, itemId);
            
            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean updateStock(int itemId, int newQuantity) {
        String sql = "UPDATE items SET stock_quantity = ? WHERE item_id = ?";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, newQuantity);
            pstmt.setInt(2, itemId);
            
            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private Item mapResultSetToItem(ResultSet rs) throws SQLException {
        Item item = new Item();
        item.setItemId(rs.getInt("item_id"));
        item.setItemCode(rs.getString("item_code"));
        item.setTitle(rs.getString("title"));
        item.setAuthor(rs.getString("author"));
        item.setCategory(rs.getString("category"));
        item.setPrice(rs.getDouble("price"));
        item.setStockQuantity(rs.getInt("stock_quantity"));
        item.setCreatedAt(rs.getString("created_at"));
        item.setUpdatedAt(rs.getString("updated_at"));
        return item;
    }
}

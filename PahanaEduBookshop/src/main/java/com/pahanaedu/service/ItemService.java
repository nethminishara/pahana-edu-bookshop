package com.pahanaedu.service;

import com.pahanaedu.dao.ItemDAO;
import com.pahanaedu.model.Item;
import java.util.List;
import java.util.Random;

public class ItemService {
    private ItemDAO itemDAO;
    
    public ItemService() {
        this.itemDAO = new ItemDAO();
    }
    
    public boolean addItem(Item item) {
        // Generate unique item code if not provided
        if (item.getItemCode() == null || item.getItemCode().isEmpty()) {
            item.setItemCode(generateItemCode());
        }
        return itemDAO.addItem(item);
    }
    
    public Item getItemById(int itemId) {
        return itemDAO.getItemById(itemId);
    }
    
    public Item getItemByCode(String itemCode) {
        return itemDAO.getItemByCode(itemCode);
    }
    
    public List<Item> getAllItems() {
        return itemDAO.getAllItems();
    }
    
    public boolean updateItem(Item item) {
        return itemDAO.updateItem(item);
    }
    
    public boolean deleteItem(int itemId) {
        return itemDAO.deleteItem(itemId);
    }
    
    public boolean updateStock(int itemId, int newQuantity) {
        return itemDAO.updateStock(itemId, newQuantity);
    }
    
    private String generateItemCode() {
        Random random = new Random();
        return "ITEM" + System.currentTimeMillis() + random.nextInt(1000);
    }
    
    public boolean validateItemData(Item item) {
        return item.getTitle() != null && !item.getTitle().trim().isEmpty() &&
               item.getPrice() > 0 && item.getStockQuantity() >= 0;
    }
    
    public List<Item> searchItems(String searchTerm) {
        // Implement search functionality if needed
        return getAllItems();
    }
    
    public List<Item> getItemsByCategory(String category) {
        // Implement category filtering if needed
        return getAllItems();
    }
}

// Global variables
let currentCustomer = null;
let billItems = [];
let billTotal = 0;

$(document).ready(function() {
    // Initialize dashboard
    loadSectionData('dashboard');
    
    // Navigation handling
    $('.nav-link').click(function(e) {
        e.preventDefault();
        
        // Update active nav
        $('.nav-link').removeClass('active');
        $(this).addClass('active');
        
        // Show corresponding section
        const section = $(this).data('section');
        $('.content-section').removeClass('active');
        $('#' + section).addClass('active');
        
        // Load section data
        loadSectionData(section);
    });
    
    // Customer account lookup for billing
    $('#billCustomerAccount').on('blur', function() {
        const accountNumber = $(this).val().trim();
        if (accountNumber) {
            lookupCustomer(accountNumber);
        } else {
            currentCustomer = null;
            $('#billCustomerName').val('');
        }
    });
    
    // Item code lookup for billing
    $('#billItemCode').on('blur', function() {
        const itemCode = $(this).val().trim();
        if (itemCode) {
            lookupItem(itemCode);
        } else {
            $('#addToBill').prop('disabled', true);
            $('#addToBill').removeData('item');
        }
    });
    
    // Add to bill functionality
    $('#addToBill').click(function() {
        addItemToBill();
    });
    
    // Generate bill
    $('#generateBill').click(function() {
        generateBill();
    });
});

function loadSectionData(section) {
    switch(section) {
        case 'dashboard':
            loadDashboardStats();
            break;
        case 'customers':
            loadCustomers();
            break;
        case 'items':
            loadItems();
            break;
        case 'billing':
            resetBillingForm();
            break;
    }
}

function loadDashboardStats() {
    // Load customer count
    $.ajax({
        url: 'customer',
        method: 'GET',
        success: function(data) {
            $('#totalCustomers').text(Array.isArray(data) ? data.length : 0);
        },
        error: function() {
            $('#totalCustomers').text('0');
        }
    });
    
    // Load item count
    $.ajax({
        url: 'item',
        method: 'GET',
        success: function(data) {
            $('#totalItems').text(Array.isArray(data) ? data.length : 0);
        },
        error: function() {
            $('#totalItems').text('0');
        }
    });
    
    // Load bill statistics
    $.ajax({
        url: 'bill/stats',
        method: 'GET',
        success: function(data) {
            $('#totalBills').text(data.totalBills || 0);
            $('#totalRevenue').text('$' + (data.totalRevenue || 0).toFixed(2));
        },
        error: function() {
            $('#totalBills').text('0');
            $('#totalRevenue').text('$0.00');
        }
    });
}

function loadCustomers() {
    $.ajax({
        url: 'customer',
        method: 'GET',
        success: function(data) {
            let tableBody = $('#customersTable tbody');
            tableBody.empty();
            
            if (Array.isArray(data) && data.length > 0) {
                data.forEach(function(customer) {
                    let row = `
                        <tr>
                            <td>${customer.accountNumber || 'N/A'}</td>
                            <td>${customer.name}</td>
                            <td>${customer.telephone}</td>
                            <td>${customer.email || 'N/A'}</td>
                            <td>
                                <button class="btn btn-sm btn-primary" onclick="editCustomer(${customer.customerId})">
                                    <i class="fas fa-edit"></i> Edit
                                </button>
                                <button class="btn btn-sm btn-danger ml-1" onclick="deleteCustomer(${customer.customerId})">
                                    <i class="fas fa-trash"></i> Delete
                                </button>
                            </td>
                        </tr>
                    `;
                    tableBody.append(row);
                });
            } else {
                tableBody.append('<tr><td colspan="5" class="text-center">No customers found</td></tr>');
            }
        },
        error: function(xhr) {
            showAlert('Error loading customers: ' + getErrorMessage(xhr), 'danger');
            $('#customersTable tbody').html('<tr><td colspan="5" class="text-center text-danger">Error loading data</td></tr>');
        }
    });
}

function loadItems() {
    $.ajax({
        url: 'item',
        method: 'GET',
        success: function(data) {
            let tableBody = $('#itemsTable tbody');
            tableBody.empty();
            
            if (Array.isArray(data) && data.length > 0) {
                data.forEach(function(item) {
                    let row = `
                        <tr>
                            <td>${item.itemCode}</td>
                            <td>${item.title}</td>
                            <td>${item.author || 'N/A'}</td>
                            <td>${item.category || 'N/A'}</td>
                            <td>$${parseFloat(item.price).toFixed(2)}</td>
                            <td>${item.stockQuantity}</td>
                            <td>
                                <button class="btn btn-sm btn-primary" onclick="editItem(${item.itemId})">
                                    <i class="fas fa-edit"></i> Edit
                                </button>
                                <button class="btn btn-sm btn-danger ml-1" onclick="deleteItem(${item.itemId})">
                                    <i class="fas fa-trash"></i> Delete
                                </button>
                            </td>
                        </tr>
                    `;
                    tableBody.append(row);
                });
            } else {
                tableBody.append('<tr><td colspan="7" class="text-center">No items found</td></tr>');
            }
        },
        error: function(xhr) {
            showAlert('Error loading items: ' + getErrorMessage(xhr), 'danger');
            $('#itemsTable tbody').html('<tr><td colspan="7" class="text-center text-danger">Error loading data</td></tr>');
        }
    });
}

// CUSTOMER FUNCTIONS - FIXED VERSION
function saveCustomer() {
    // Get form values directly instead of using FormData
    const name = $('#addCustomerForm input[name="name"]').val();
    const telephone = $('#addCustomerForm input[name="telephone"]').val();
    const email = $('#addCustomerForm input[name="email"]').val();
    const address = $('#addCustomerForm textarea[name="address"]').val();
    const accountNumber = $('#addCustomerForm input[name="accountNumber"]').val();
    
    console.log("Sending customer data:", {name, telephone, email, address, accountNumber});
    
    // Validate form
    if (!name || !name.trim() || !telephone || !telephone.trim()) {
        showAlert('Name and telephone are required fields', 'warning');
        return;
    }
    
    $.ajax({
        url: 'customer',
        method: 'POST',
        data: {
            name: name.trim(),
            telephone: telephone.trim(),
            email: email ? email.trim() : '',
            address: address ? address.trim() : '',
            accountNumber: accountNumber ? accountNumber.trim() : ''
        },
        success: function(response) {
            console.log("Customer save response:", response);
            if (response.success) {
                $('#addCustomerModal').modal('hide');
                showAlert('Customer added successfully', 'success');
                loadCustomers();
                $('#addCustomerForm')[0].reset();
            } else {
                showAlert(response.message || 'Failed to add customer', 'danger');
            }
        },
        error: function(xhr) {
            console.error("Customer save error:", xhr);
            showAlert('Error adding customer: ' + getErrorMessage(xhr), 'danger');
        }
    });
}

function editCustomer(customerId) {
    if (!customerId) {
        showAlert('Invalid customer ID', 'warning');
        return;
    }
    
    $.ajax({
        url: 'customer/' + customerId,
        method: 'GET',
        success: function(customer) {
            // Populate edit form
            $('#editCustomerModal input[name="customerId"]').val(customer.customerId);
            $('#editCustomerModal input[name="accountNumber"]').val(customer.accountNumber || '');
            $('#editCustomerModal input[name="name"]').val(customer.name);
            $('#editCustomerModal textarea[name="address"]').val(customer.address || '');
            $('#editCustomerModal input[name="telephone"]').val(customer.telephone);
            $('#editCustomerModal input[name="email"]').val(customer.email || '');
            
            $('#editCustomerModal').modal('show');
        },
        error: function(xhr) {
            showAlert('Error loading customer data: ' + getErrorMessage(xhr), 'danger');
        }
    });
}

function updateCustomer() {
    const customerId = $('#editCustomerModal input[name="customerId"]').val();
    
    // Get form values directly
    const name = $('#editCustomerForm input[name="name"]').val();
    const telephone = $('#editCustomerForm input[name="telephone"]').val();
    const email = $('#editCustomerForm input[name="email"]').val();
    const address = $('#editCustomerForm textarea[name="address"]').val();
    
    console.log("Updating customer:", {customerId, name, telephone, email, address});
    
    // Validate form
    if (!name || !name.trim() || !telephone || !telephone.trim()) {
        showAlert('Name and telephone are required fields', 'warning');
        return;
    }
    
    // Convert to URL-encoded string for PUT request
    const formData = $.param({
        name: name.trim(),
        telephone: telephone.trim(),
        email: email ? email.trim() : '',
        address: address ? address.trim() : ''
    });
    
    $.ajax({
        url: 'customer/' + customerId,
        method: 'PUT',
        data: formData,
        contentType: 'application/x-www-form-urlencoded',
        success: function(response) {
            console.log("Customer update response:", response);
            if (response.success) {
                $('#editCustomerModal').modal('hide');
                showAlert('Customer updated successfully', 'success');
                loadCustomers();
            } else {
                showAlert(response.message || 'Failed to update customer', 'danger');
            }
        },
        error: function(xhr) {
            console.error("Customer update error:", xhr);
            showAlert('Error updating customer: ' + getErrorMessage(xhr), 'danger');
        }
    });
}

function deleteCustomer(customerId) {
    if (!customerId) {
        showAlert('Invalid customer ID', 'warning');
        return;
    }
    
    if (confirm('Are you sure you want to delete this customer?')) {
        $.ajax({
            url: 'customer/' + customerId,
            method: 'DELETE',
            success: function(response) {
                if (response.success) {
                    showAlert('Customer deleted successfully', 'success');
                    loadCustomers();
                } else {
                    showAlert(response.message || 'Failed to delete customer', 'danger');
                }
            },
            error: function(xhr) {
                showAlert('Error deleting customer: ' + getErrorMessage(xhr), 'danger');
            }
        });
    }
}

// ITEM FUNCTIONS - FIXED VERSION
function saveItem() {
    // Get form values directly
    const itemCode = $('#addItemForm input[name="itemCode"]').val();
    const title = $('#addItemForm input[name="title"]').val();
    const author = $('#addItemForm input[name="author"]').val();
    const category = $('#addItemForm select[name="category"]').val();
    const price = $('#addItemForm input[name="price"]').val();
    const stockQuantity = $('#addItemForm input[name="stockQuantity"]').val();
    
    console.log("Sending item data:", {itemCode, title, author, category, price, stockQuantity});
    
    // Validate form
    if (!itemCode || !itemCode.trim() || !title || !title.trim() || 
        !price || !stockQuantity) {
        showAlert('Item code, title, price, and stock quantity are required fields', 'warning');
        return;
    }
    
    if (parseFloat(price) < 0 || parseInt(stockQuantity) < 0) {
        showAlert('Price and stock quantity must be non-negative', 'warning');
        return;
    }
    
    $.ajax({
        url: 'item',
        method: 'POST',
        data: {
            itemCode: itemCode.trim(),
            title: title.trim(),
            author: author ? author.trim() : '',
            category: category || '',
            price: price,
            stockQuantity: stockQuantity
        },
        success: function(response) {
            console.log("Item save response:", response);
            if (response.success) {
                $('#addItemModal').modal('hide');
                showAlert('Item added successfully', 'success');
                loadItems();
                $('#addItemForm')[0].reset();
            } else {
                showAlert(response.message || 'Failed to add item', 'danger');
            }
        },
        error: function(xhr) {
            console.error("Item save error:", xhr);
            showAlert('Error adding item: ' + getErrorMessage(xhr), 'danger');
        }
    });
}

function editItem(itemId) {
    if (!itemId) {
        showAlert('Invalid item ID', 'warning');
        return;
    }
    
    $.ajax({
        url: 'item/' + itemId,
        method: 'GET',
        success: function(item) {
            // Populate edit form
            $('#editItemModal input[name="itemId"]').val(item.itemId);
            $('#editItemModal input[name="itemCode"]').val(item.itemCode);
            $('#editItemModal input[name="title"]').val(item.title);
            $('#editItemModal input[name="author"]').val(item.author || '');
            $('#editItemModal select[name="category"]').val(item.category || '');
            $('#editItemModal input[name="price"]').val(item.price);
            $('#editItemModal input[name="stockQuantity"]').val(item.stockQuantity);
            
            $('#editItemModal').modal('show');
        },
        error: function(xhr) {
            showAlert('Error loading item data: ' + getErrorMessage(xhr), 'danger');
        }
    });
}

function updateItem() {
    const itemId = $('#editItemModal input[name="itemId"]').val();
    
    // Get form values directly
    const title = $('#editItemForm input[name="title"]').val();
    const author = $('#editItemForm input[name="author"]').val();
    const category = $('#editItemForm select[name="category"]').val();
    const price = $('#editItemForm input[name="price"]').val();
    const stockQuantity = $('#editItemForm input[name="stockQuantity"]').val();
    
    console.log("Updating item:", {itemId, title, author, category, price, stockQuantity});
    
    // Validate form
    if (!title || !title.trim() || !price || !stockQuantity) {
        showAlert('Title, price, and stock quantity are required fields', 'warning');
        return;
    }
    
    if (parseFloat(price) < 0 || parseInt(stockQuantity) < 0) {
        showAlert('Price and stock quantity must be non-negative', 'warning');
        return;
    }
    
    // Convert to URL-encoded string for PUT request
    const formData = $.param({
        title: title.trim(),
        author: author ? author.trim() : '',
        category: category || '',
        price: price,
        stockQuantity: stockQuantity
    });
    
    $.ajax({
        url: 'item/' + itemId,
        method: 'PUT',
        data: formData,
        contentType: 'application/x-www-form-urlencoded',
        success: function(response) {
            console.log("Item update response:", response);
            if (response.success) {
                $('#editItemModal').modal('hide');
                showAlert('Item updated successfully', 'success');
                loadItems();
            } else {
                showAlert(response.message || 'Failed to update item', 'danger');
            }
        },
        error: function(xhr) {
            console.error("Item update error:", xhr);
            showAlert('Error updating item: ' + getErrorMessage(xhr), 'danger');
        }
    });
}

function deleteItem(itemId) {
    if (!itemId) {
        showAlert('Invalid item ID', 'warning');
        return;
    }
    
    if (confirm('Are you sure you want to delete this item?')) {
        $.ajax({
            url: 'item/' + itemId,
            method: 'DELETE',
            success: function(response) {
                if (response.success) {
                    showAlert('Item deleted successfully', 'success');
                    loadItems();
                } else {
                    showAlert(response.message || 'Failed to delete item', 'danger');
                }
            },
            error: function(xhr) {
                showAlert('Error deleting item: ' + getErrorMessage(xhr), 'danger');
            }
        });
    }
}

// BILLING FUNCTIONS
function lookupCustomer(accountNumber) {
    $.ajax({
        url: 'customer/account/' + accountNumber,
        method: 'GET',
        success: function(customer) {
            currentCustomer = customer;
            $('#billCustomerName').val(customer.name);
            showAlert('Customer found: ' + customer.name, 'success');
        },
        error: function() {
            currentCustomer = null;
            $('#billCustomerName').val('');
            showAlert('Customer not found with account number: ' + accountNumber, 'warning');
        }
    });
}

function lookupItem(itemCode) {
    $.ajax({
        url: 'item/code/' + itemCode,
        method: 'GET',
        success: function(item) {
            if (item.stockQuantity <= 0) {
                showAlert('Item is out of stock: ' + item.title, 'warning');
                $('#addToBill').prop('disabled', true);
                return;
            }
            
            $('#addToBill').prop('disabled', false);
            $('#addToBill').data('item', item);
            showAlert('Item found: ' + item.title + ' (Stock: ' + item.stockQuantity + ', Price: $' + 
                     item.price.toFixed(2) + ')', 'success');
        },
        error: function() {
            $('#addToBill').prop('disabled', true);
            $('#addToBill').removeData('item');
            showAlert('Item not found with code: ' + itemCode, 'warning');
        }
    });
}

function addItemToBill() {
    const item = $('#addToBill').data('item');
    const quantity = parseInt($('#billQuantity').val());
    
    if (!item) {
        showAlert('Please select a valid item first', 'warning');
        return;
    }
    
    if (!quantity || quantity <= 0) {
        showAlert('Please enter a valid quantity', 'warning');
        return;
    }
    
    if (quantity > item.stockQuantity) {
        showAlert('Insufficient stock. Available: ' + item.stockQuantity, 'warning');
        return;
    }
    
    // Check if item already exists in bill
    const existingIndex = billItems.findIndex(bi => bi.item.itemId === item.itemId);
    
    if (existingIndex >= 0) {
        // Update existing item
        const totalQty = billItems[existingIndex].quantity + quantity;
        if (totalQty > item.stockQuantity) {
            showAlert('Total quantity exceeds stock. Available: ' + item.stockQuantity + 
                     ', Already in bill: ' + billItems[existingIndex].quantity, 'warning');
            return;
        }
        billItems[existingIndex].quantity = totalQty;
        billItems[existingIndex].totalPrice = totalQty * item.price;
    } else {
        // Add new item
        billItems.push({
            item: item,
            quantity: quantity,
            unitPrice: item.price,
            totalPrice: quantity * item.price
        });
    }
    
    updateBillDisplay();
    
    // Reset form
    $('#billItemCode').val('');
    $('#billQuantity').val(1);
    $('#addToBill').prop('disabled', true);
    $('#addToBill').removeData('item');
    
    showAlert('Item added to bill successfully', 'success');
}

function updateBillDisplay() {
    const billItemsDiv = $('#billItems');
    billItemsDiv.empty();
    
    billTotal = 0;
    
    if (billItems.length === 0) {
        billItemsDiv.html('<p class="text-muted">No items added yet</p>');
        $('#generateBill').prop('disabled', true);
    } else {
        billItems.forEach(function(billItem, index) {
            billTotal += billItem.totalPrice;
            
            const itemDiv = `
                <div class="d-flex justify-content-between align-items-center mb-2 p-2 border rounded">
                    <div>
                        <strong>${billItem.item.title}</strong><br>
                        <small class="text-muted">Code: ${billItem.item.itemCode}</small><br>
                        <small>Qty: ${billItem.quantity} × $${billItem.unitPrice.toFixed(2)}</small>
                    </div>
                    <div class="text-right">
                        <div class="font-weight-bold">$${billItem.totalPrice.toFixed(2)}</div>
                        <button class="btn btn-sm btn-danger mt-1" onclick="removeBillItem(${index})" title="Remove item">
                            <i class="fas fa-times"></i>
                        </button>
                    </div>
                </div>
            `;
            billItemsDiv.append(itemDiv);
        });
        
        $('#generateBill').prop('disabled', false);
    }
    
    $('#billTotal').text(billTotal.toFixed(2));
}

function removeBillItem(index) {
    if (index >= 0 && index < billItems.length) {
        const removedItem = billItems[index];
        billItems.splice(index, 1);
        updateBillDisplay();
        showAlert('Removed ' + removedItem.item.title + ' from bill', 'info');
    }
}

function generateBill() {
    if (!currentCustomer) {
        showAlert('Please select a customer first', 'warning');
        $('#billCustomerAccount').focus();
        return;
    }
    
    if (billItems.length === 0) {
        showAlert('Please add items to the bill', 'warning');
        $('#billItemCode').focus();
        return;
    }
    
    // Confirm bill generation
    if (!confirm(`Generate bill for ${currentCustomer.name} with total amount $${billTotal.toFixed(2)}?`)) {
        return;
    }
    
    const billData = {
        customerId: currentCustomer.customerId,
        items: billItems.map(bi => ({
            itemId: bi.item.itemId,
            quantity: bi.quantity,
            unitPrice: bi.unitPrice
        })),
        totalAmount: billTotal
    };
    
    // Disable button to prevent double submission
    $('#generateBill').prop('disabled', true).html('<i class="fas fa-spinner fa-spin"></i> Generating...');
    
    $.ajax({
        url: 'bill',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(billData),
        success: function(response) {
            if (response.success) {
                showAlert('Bill generated successfully! Bill Number: ' + 
                         (response.billNumber || response.billId || 'Generated'), 'success');
                
                // Generate and download HTML/PDF
                generateBillPDF(response);
                
                resetBillingForm();
                loadDashboardStats(); // Refresh stats
            } else {
                showAlert(response.message || 'Failed to generate bill', 'danger');
            }
        },
        error: function(xhr) {
            showAlert('Error generating bill: ' + getErrorMessage(xhr), 'danger');
        },
        complete: function() {
            // Re-enable button
            $('#generateBill').prop('disabled', false).html('<i class="fas fa-file-invoice"></i> Generate Bill');
        }
    });
}

function generateBillPDF(billResponse) {
    // Generate automatic filename with timestamp and customer name
    const now = new Date();
    const dateStr = now.getFullYear() + 
                   String(now.getMonth() + 1).padStart(2, '0') + 
                   String(now.getDate()).padStart(2, '0');
    const timeStr = String(now.getHours()).padStart(2, '0') + 
                   String(now.getMinutes()).padStart(2, '0') + 
                   String(now.getSeconds()).padStart(2, '0');
    
    // Clean customer name for filename
    const cleanCustomerName = currentCustomer.name.replace(/[^a-zA-Z0-9]/g, '_').substring(0, 20);
    
    // Generate descriptive filename
    const filename = `Invoice_${cleanCustomerName}_${dateStr}_${timeStr}_${billResponse.billNumber}`;
    
    // Create professional HTML content for printing/PDF
    const billContent = `
        <!DOCTYPE html>
        <html>
        <head>
            <title>${filename}</title>
            <style>
                body { font-family: Arial, sans-serif; margin: 20px; }
                .header { text-align: center; margin-bottom: 30px; border-bottom: 2px solid #333; padding-bottom: 20px; }
                .bill-details { margin-bottom: 20px; }
                .customer-info { background-color: #f9f9f9; padding: 15px; border-radius: 5px; margin-bottom: 20px; }
                table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }
                th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }
                th { background-color: #4CAF50; color: white; }
                .total-row { background-color: #f2f2f2; font-weight: bold; font-size: 16px; }
                .text-right { text-align: right; }
                .footer { margin-top: 40px; text-align: center; font-size: 12px; color: #666; }
                @media print {
                    body { margin: 0; }
                    .no-print { display: none; }
                }
            </style>
        </head>
        <body>
            <div class="header">
                <h1 style="color: #4CAF50; margin: 0;">PAHANA EDU BOOKSHOP</h1>
                <p style="margin: 5px 0;">123 Education Street, Knowledge City</p>
                <p style="margin: 5px 0;">Phone: +1-234-567-8900 | Email: info@pahanaedu.com</p>
                <h2 style="color: #333; margin-top: 20px;">INVOICE</h2>
            </div>
            
            <div class="bill-details">
                <div style="display: flex; justify-content: space-between;">
                    <div>
                        <p><strong>Bill Number:</strong> ${billResponse.billNumber}</p>
                        <p><strong>Date:</strong> ${new Date().toLocaleDateString()}</p>
                        <p><strong>Time:</strong> ${new Date().toLocaleTimeString()}</p>
                    </div>
                    <div>
                        <p><strong>Status:</strong> PENDING</p>
                        <p><strong>Payment:</strong> Cash/Card</p>
                    </div>
                </div>
            </div>
            
            <div class="customer-info">
                <h3 style="margin-top: 0; color: #333;">Customer Information</h3>
                <p><strong>Name:</strong> ${currentCustomer.name}</p>
                <p><strong>Account Number:</strong> ${currentCustomer.accountNumber || 'N/A'}</p>
                <p><strong>Phone:</strong> ${currentCustomer.telephone}</p>
                <p><strong>Email:</strong> ${currentCustomer.email || 'N/A'}</p>
            </div>
            
            <table>
                <thead>
                    <tr>
                        <th style="width: 50%;">Item Description</th>
                        <th style="width: 15%;">Quantity</th>
                        <th style="width: 17.5%;">Unit Price</th>
                        <th style="width: 17.5%;">Total Price</th>
                    </tr>
                </thead>
                <tbody>
                    ${billItems.map(item => `
                        <tr>
                            <td>
                                <strong>${item.item.title}</strong><br>
                                <small style="color: #666;">Code: ${item.item.itemCode}</small>
                                ${item.item.author ? '<br><small style="color: #666;">Author: ' + item.item.author + '</small>' : ''}
                            </td>
                            <td class="text-right">${item.quantity}</td>
                            <td class="text-right">$${item.unitPrice.toFixed(2)}</td>
                            <td class="text-right">$${item.totalPrice.toFixed(2)}</td>
                        </tr>
                    `).join('')}
                </tbody>
                <tfoot>
                    <tr class="total-row">
                        <td colspan="3" class="text-right">TOTAL AMOUNT:</td>
                        <td class="text-right">$${billTotal.toFixed(2)}</td>
                    </tr>
                </tfoot>
            </table>
            
            <div class="footer">
                <p><strong>Thank you for your business!</strong></p>
                <p>For any queries, please contact us at info@pahanaedu.com</p>
                <p style="margin-top: 20px;">Generated on: ${new Date().toLocaleString()}</p>
            </div>
            
            <div class="no-print" style="margin-top: 30px; text-align: center;">
                <button onclick="window.print()" style="background: #4CAF50; color: white; padding: 10px 20px; border: none; border-radius: 5px; cursor: pointer;">Print Bill</button>
                <button onclick="window.close()" style="background: #f44336; color: white; padding: 10px 20px; border: none; border-radius: 5px; cursor: pointer; margin-left: 10px;">Close</button>
            </div>
        </body>
        </html>
    `;
    
    // Open in new window with automatic filename
    const printWindow = window.open('', '_blank');
    printWindow.document.write(billContent);
    printWindow.document.close();
    
    // Set document title for printing (this becomes the suggested filename)
    printWindow.document.title = filename;
    
    // Auto-trigger print dialog after content loads
    printWindow.onload = function() {
        printWindow.print();
    };
    
    showAlert(`Invoice ready for printing: ${filename}`, 'success');
}




function resetBillingForm() {
    currentCustomer = null;
    billItems = [];
    billTotal = 0;
    
    $('#billingForm')[0].reset();
    $('#billCustomerName').val('');
    $('#addToBill').prop('disabled', true);
    $('#addToBill').removeData('item');
    updateBillDisplay();
}

// UTILITY FUNCTIONS
function showAlert(message, type) {
    const alertDiv = `
        <div class="alert alert-${type} alert-dismissible fade show" role="alert">
            <i class="fas fa-${type === 'success' ? 'check-circle' : 
                              type === 'danger' ? 'exclamation-triangle' : 
                              type === 'warning' ? 'exclamation-triangle' : 'info-circle'}"></i>
            ${message}
            <button type="button" class="close" data-dismiss="alert">
                <span>&times;</span>
            </button>
        </div>
    `;
    
    // Remove existing alerts
    $('.alert').remove();
    
    // Add new alert at the top of main content
    $('.main-content').prepend(alertDiv);
    
    // Auto dismiss after 5 seconds
    setTimeout(function() {
        $('.alert').alert('close');
    }, 5000);
}

function getErrorMessage(xhr) {
    if (xhr.responseJSON && xhr.responseJSON.message) {
        return xhr.responseJSON.message;
    } else if (xhr.responseJSON && xhr.responseJSON.error) {
        return xhr.responseJSON.error;
    } else {
        return 'Unknown error occurred';
    }
}

function generateCustomerReport() {
    showAlert('Customer report generation feature will be implemented soon', 'info');
}

function generateSalesReport() {
    showAlert('Sales report generation feature will be implemented soon', 'info');
}

$(document).ready(function() {
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
    
    // Load dashboard data on page load
    loadSectionData('dashboard');
    
    // Customer account lookup
    $('#billCustomerAccount').on('blur', function() {
        const accountNumber = $(this).val().trim();
        if (accountNumber) {
            lookupCustomer(accountNumber);
        }
    });
    
    // Item code lookup
    $('#billItemCode').on('blur', function() {
        const itemCode = $(this).val().trim();
        if (itemCode) {
            lookupItem(itemCode);
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

// Global variables for billing
let currentCustomer = null;
let billItems = [];
let billTotal = 0;

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
            $('#totalCustomers').text(data.length);
        },
        error: function() {
            $('#totalCustomers').text('Error');
        }
    });
    
    // Load item count
    $.ajax({
        url: 'item',
        method: 'GET',
        success: function(data) {
            $('#totalItems').text(data.length);
        },
        error: function() {
            $('#totalItems').text('Error');
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
            $('#totalBills').text('Error');
            $('#totalRevenue').text('Error');
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
            
            data.forEach(function(customer) {
                let row = `
                    <tr>
                        <td>${customer.accountNumber}</td>
                        <td>${customer.name}</td>
                        <td>${customer.telephone}</td>
                        <td>${customer.email || 'N/A'}</td>
                        <td>
                            <button class="btn btn-sm btn-warning" onclick="editCustomer(${customer.customerId})">
                                <i class="fas fa-edit"></i>
                            </button>
                            <button class="btn btn-sm btn-danger" onclick="deleteCustomer(${customer.customerId})">
                                <i class="fas fa-trash"></i>
                            </button>
                        </td>
                    </tr>
                `;
                tableBody.append(row);
            });
        },
        error: function() {
            showAlert('Error loading customers', 'danger');
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
            
            data.forEach(function(item) {
                let row = `
                    <tr>
                        <td>${item.itemCode}</td>
                        <td>${item.title}</td>
                        <td>${item.author || 'N/A'}</td>
                        <td>${item.category || 'N/A'}</td>
                        <td>$${item.price.toFixed(2)}</td>
                        <td>${item.stockQuantity}</td>
                        <td>
                            <button class="btn btn-sm btn-warning" onclick="editItem(${item.itemId})">
                                <i class="fas fa-edit"></i>
                            </button>
                            <button class="btn btn-sm btn-danger" onclick="deleteItem(${item.itemId})">
                                <i class="fas fa-trash"></i>
                            </button>
                        </td>
                    </tr>
                `;
                tableBody.append(row);
            });
        },
        error: function() {
            showAlert('Error loading items', 'danger');
        }
    });
}

function saveCustomer() {
    const formData = new FormData(document.getElementById('addCustomerForm'));
    
    $.ajax({
        url: 'customer',
        method: 'POST',
        data: formData,
        processData: false,
        contentType: false,
        success: function(response) {
            if (response.success) {
                $('#addCustomerModal').modal('hide');
                showAlert('Customer added successfully', 'success');
                loadCustomers();
                $('#addCustomerForm')[0].reset();
            } else {
                showAlert(response.message, 'danger');
            }
        },
        error: function() {
            showAlert('Error adding customer', 'danger');
        }
    });
}

function saveItem() {
    const formData = new FormData(document.getElementById('addItemForm'));
    
    $.ajax({
        url: 'item',
        method: 'POST',
        data: formData,
        processData: false,
        contentType: false,
        success: function(response) {
            if (response.success) {
                $('#addItemModal').modal('hide');
                showAlert('Item added successfully', 'success');
                loadItems();
                $('#addItemForm')[0].reset();
            } else {
                showAlert(response.message, 'danger');
            }
        },
        error: function() {
            showAlert('Error adding item', 'danger');
        }
    });
}

function lookupCustomer(accountNumber) {
    $.ajax({
        url: 'customer/account/' + accountNumber,
        method: 'GET',
        success: function(customer) {
            currentCustomer = customer;
            $('#billCustomerName').val(customer.name);
        },
        error: function() {
            currentCustomer = null;
            $('#billCustomerName').val('');
            showAlert('Customer not found', 'warning');
        }
    });
}

function lookupItem(itemCode) {
    $.ajax({
        url: 'item/code/' + itemCode,
        method: 'GET',
        success: function(item) {
            // Item found, enable add to bill
            $('#addToBill').prop('disabled', false);
            $('#addToBill').data('item', item);
        },
        error: function() {
            $('#addToBill').prop('disabled', true);
            showAlert('Item not found', 'warning');
        }
    });
}

function addItemToBill() {
    const item = $('#addToBill').data('item');
    const quantity = parseInt($('#billQuantity').val());
    
    if (!item || quantity <= 0) {
        showAlert('Invalid item or quantity', 'warning');
        return;
    }
    
    if (quantity > item.stockQuantity) {
        showAlert('Insufficient stock', 'warning');
        return;
    }
    
    // Check if item already exists in bill
    const existingIndex = billItems.findIndex(bi => bi.item.itemId === item.itemId);
    
    if (existingIndex >= 0) {
        // Update existing item
        billItems[existingIndex].quantity += quantity;
        billItems[existingIndex].totalPrice = billItems[existingIndex].quantity * item.price;
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
                <div class="d-flex justify-content-between align-items-center mb-2">
                    <div>
                        <strong>${billItem.item.title}</strong><br>
                        <small>Qty: ${billItem.quantity} × $${billItem.unitPrice.toFixed(2)}</small>
                    </div>
                    <div class="text-right">
                        <div>$${billItem.totalPrice.toFixed(2)}</div>
                        <button class="btn btn-sm btn-outline-danger" onclick="removeBillItem(${index})">
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
    billItems.splice(index, 1);
    updateBillDisplay();
}

function generateBill() {
    if (!currentCustomer) {
        showAlert('Please select a customer', 'warning');
        return;
    }
    
    if (billItems.length === 0) {
        showAlert('Please add items to the bill', 'warning');
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
    
    $.ajax({
        url: 'bill',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(billData),
        success: function(response) {
            if (response.success) {
                showAlert('Bill generated successfully', 'success');
                printBill(response.billId);
                resetBillingForm();
            } else {
                showAlert(response.message, 'danger');
            }
        },
        error: function() {
            showAlert('Error generating bill', 'danger');
        }
    });
}

function printBill(billId) {
    // Open bill in new window for printing
    window.open('bill/print/' + billId, '_blank');
}

function resetBillingForm() {
    currentCustomer = null;
    billItems = [];
    billTotal = 0;
    
    $('#billingForm')[0].reset();
    $('#billCustomerName').val('');
    updateBillDisplay();
}

function deleteCustomer(customerId) {
    if (confirm('Are you sure you want to delete this customer?')) {
        $.ajax({
            url: 'customer/' + customerId,
            method: 'DELETE',
            success: function(response) {
                if (response.success) {
                    showAlert('Customer deleted successfully', 'success');
                    loadCustomers();
                } else {
                    showAlert(response.message, 'danger');
                }
            },
            error: function() {
                showAlert('Error deleting customer', 'danger');
            }
        });
    }
}

function deleteItem(itemId) {
    if (confirm('Are you sure you want to delete this item?')) {
        $.ajax({
            url: 'item/' + itemId,
            method: 'DELETE',
            success: function(response) {
                if (response.success) {
                    showAlert('Item deleted successfully', 'success');
                    loadItems();
                } else {
                    showAlert(response.message, 'danger');
                }
            },
            error: function() {
                showAlert('Error deleting item', 'danger');
            }
        });
    }
}

function generateCustomerReport() {
    window.open('reports/customers', '_blank');
}

function generateSalesReport() {
    window.open('reports/sales', '_blank');
}

function showAlert(message, type) {
    const alertDiv = `
        <div class="alert alert-${type} alert-dismissible fade show" role="alert">
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

function editCustomer(customerId) {
    $.ajax({
        url: 'customer/' + customerId,
        method: 'GET',
        success: function(customer) {
            // Populate edit form
            $('#editCustomerModal input[name="customerId"]').val(customer.customerId);
            $('#editCustomerModal input[name="accountNumber"]').val(customer.accountNumber);
            $('#editCustomerModal input[name="name"]').val(customer.name);
            $('#editCustomerModal textarea[name="address"]').val(customer.address);
            $('#editCustomerModal input[name="telephone"]').val(customer.telephone);
            $('#editCustomerModal input[name="email"]').val(customer.email);
            
            $('#editCustomerModal').modal('show');
        },
        error: function() {
            showAlert('Error loading customer data', 'danger');
        }
    });
}

function editItem(itemId) {
    $.ajax({
        url: 'item/' + itemId,
        method: 'GET',
        success: function(item) {
            // Populate edit form
            $('#editItemModal input[name="itemId"]').val(item.itemId);
            $('#editItemModal input[name="itemCode"]').val(item.itemCode);
            $('#editItemModal input[name="title"]').val(item.title);
            $('#editItemModal input[name="author"]').val(item.author);
            $('#editItemModal select[name="category"]').val(item.category);
            $('#editItemModal input[name="price"]').val(item.price);
            $('#editItemModal input[name="stockQuantity"]').val(item.stockQuantity);
            
            $('#editItemModal').modal('show');
        },
        error: function() {
            showAlert('Error loading item data', 'danger');
        }
    });
}

function updateCustomer() {
    const customerId = $('#editCustomerModal input[name="customerId"]').val();
    const formData = new FormData(document.getElementById('editCustomerForm'));
    
    $.ajax({
        url: 'customer/' + customerId,
        method: 'PUT',
        data: formData,
        processData: false,
        contentType: false,
        success: function(response) {
            if (response.success) {
                $('#editCustomerModal').modal('hide');
                showAlert('Customer updated successfully', 'success');
                loadCustomers();
            } else {
                showAlert(response.message, 'danger');
            }
        },
        error: function() {
            showAlert('Error updating customer', 'danger');
        }
    });
}

function updateItem() {
    const itemId = $('#editItemModal input[name="itemId"]').val();
    const formData = new FormData(document.getElementById('editItemForm'));
    
    $.ajax({
        url: 'item/' + itemId,
        method: 'PUT',
        data: formData,
        processData: false,
        contentType: false,
        success: function(response) {
            if (response.success) {
                $('#editItemModal').modal('hide');
                showAlert('Item updated successfully', 'success');
                loadItems();
            } else {
                showAlert(response.message, 'danger');
            }
        },
        error: function() {
            showAlert('Error updating item', 'danger');
        }
    });
}

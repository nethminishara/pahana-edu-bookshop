<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.pahanaedu.model.User" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect("login");
        return;
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Pahana Edu Bookshop - Dashboard</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@4.6.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css" rel="stylesheet">
    <style>
        body {
            background-color: #f8f9fa;
        }
        .sidebar {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            color: white;
        }
        .sidebar .nav-link {
            color: rgba(255, 255, 255, 0.8);
            padding: 15px 20px;
            border-radius: 8px;
            margin: 5px 10px;
            transition: all 0.3s ease;
        }
        .sidebar .nav-link:hover,
        .sidebar .nav-link.active {
            background: rgba(255, 255, 255, 0.2);
            color: white;
            transform: translateX(5px);
        }
        .main-content {
            padding: 20px;
        }
        .card {
            border: none;
            border-radius: 10px;
            box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
            transition: transform 0.3s ease;
        }
        .card:hover {
            transform: translateY(-5px);
        }
        .stats-card {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
        }
        .navbar-brand {
            font-weight: bold;
        }
        .content-section {
            display: none;
        }
        .content-section.active {
            display: block;
        }
    </style>
</head>
<body>
    <!-- Navigation -->
    <nav class="navbar navbar-expand-lg navbar-dark bg-primary">
        <a class="navbar-brand" href="#">
            <i class="fas fa-book"></i> Pahana Edu Bookshop
        </a>
        <div class="navbar-nav ml-auto">
            <span class="navbar-text mr-3">
                Welcome, <%= user.getUsername() %> (<%= user.getRole() %>)
            </span>
            <a class="nav-link" href="logout" onclick="return confirm('Are you sure you want to logout?')">
                <i class="fas fa-sign-out-alt"></i> Logout
            </a>
        </div>
    </nav>
    
    <div class="container-fluid">
        <div class="row">
            <!-- Sidebar -->
            <div class="col-md-3 col-lg-2 sidebar">
                <div class="nav flex-column nav-pills pt-4">
                    <a class="nav-link active" href="#" data-section="dashboard">
                        <i class="fas fa-tachometer-alt"></i> Dashboard
                    </a>
                    <a class="nav-link" href="#" data-section="customers">
                        <i class="fas fa-users"></i> Customers
                    </a>
                    <a class="nav-link" href="#" data-section="items">
                        <i class="fas fa-book"></i> Items
                    </a>
                    <a class="nav-link" href="#" data-section="billing">
                        <i class="fas fa-file-invoice-dollar"></i> Billing
                    </a>
                    <a class="nav-link" href="#" data-section="reports">
                        <i class="fas fa-chart-bar"></i> Reports
                    </a>
                    <a class="nav-link" href="#" data-section="help">
                        <i class="fas fa-question-circle"></i> Help
                    </a>
                </div>
            </div>
            
            <!-- Main Content -->
            <div class="col-md-9 col-lg-10 main-content">
                <!-- Dashboard Section -->
                <div id="dashboard" class="content-section active">
                    <h2 class="mb-4">Dashboard</h2>
                    <div class="row">
                        <div class="col-md-3 mb-4">
                            <div class="card stats-card">
                                <div class="card-body text-center">
                                    <i class="fas fa-users fa-3x mb-3"></i>
                                    <h4 id="totalCustomers">0</h4>
                                    <p>Total Customers</p>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-3 mb-4">
                            <div class="card stats-card">
                                <div class="card-body text-center">
                                    <i class="fas fa-book fa-3x mb-3"></i>
                                    <h4 id="totalItems">0</h4>
                                    <p>Total Items</p>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-3 mb-4">
                            <div class="card stats-card">
                                <div class="card-body text-center">
                                    <i class="fas fa-file-invoice fa-3x mb-3"></i>
                                    <h4 id="totalBills">0</h4>
                                    <p>Total Bills</p>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-3 mb-4">
                            <div class="card stats-card">
                                <div class="card-body text-center">
                                    <i class="fas fa-dollar-sign fa-3x mb-3"></i>
                                    <h4 id="totalRevenue">$0</h4>
                                    <p>Total Revenue</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                
                <!-- Customers Section -->
                <div id="customers" class="content-section">
                    <div class="d-flex justify-content-between align-items-center mb-4">
                        <h2>Customer Management</h2>
                        <button class="btn btn-primary" data-toggle="modal" data-target="#addCustomerModal">
                            <i class="fas fa-plus"></i> Add Customer
                        </button>
                    </div>
                    
                    <div class="card">
                        <div class="card-body">
                            <div class="table-responsive">
                                <table class="table table-striped" id="customersTable">
                                    <thead>
                                        <tr>
                                            <th>Account Number</th>
                                            <th>Name</th>
                                            <th>Telephone</th>
                                            <th>Email</th>
                                            <th>Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <!-- Customer data will be loaded here -->
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
                
                <!-- Items Section -->
                <div id="items" class="content-section">
                    <div class="d-flex justify-content-between align-items-center mb-4">
                        <h2>Item Management</h2>
                        <button class="btn btn-primary" data-toggle="modal" data-target="#addItemModal">
                            <i class="fas fa-plus"></i> Add Item
                        </button>
                    </div>
                    
                    <div class="card">
                        <div class="card-body">
                            <div class="table-responsive">
                                <table class="table table-striped" id="itemsTable">
                                    <thead>
                                        <tr>
                                            <th>Item Code</th>
                                            <th>Title</th>
                                            <th>Author</th>
                                            <th>Category</th>
                                            <th>Price</th>
                                            <th>Stock</th>
                                            <th>Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <!-- Item data will be loaded here -->
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
                
                <!-- Billing Section -->
                <div id="billing" class="content-section">
                    <h2 class="mb-4">Billing System</h2>
                    
                    <div class="row">
                        <div class="col-md-8">
                            <div class="card">
                                <div class="card-header">
                                    <h5>Create New Bill</h5>
                                </div>
                                <div class="card-body">
                                    <form id="billingForm">
                                        <div class="row">
                                            <div class="col-md-6">
                                                <div class="form-group">
                                                    <label>Customer Account Number</label>
                                                    <input type="text" class="form-control" id="billCustomerAccount" placeholder="Enter account number">
                                                </div>
                                            </div>
                                            <div class="col-md-6">
                                                <div class="form-group">
                                                    <label>Customer Name</label>
                                                    <input type="text" class="form-control" id="billCustomerName" readonly>
                                                </div>
                                            </div>
                                        </div>
                                        
                                        <hr>
                                        
                                        <div class="row">
                                            <div class="col-md-4">
                                                <div class="form-group">
                                                    <label>Item Code</label>
                                                    <input type="text" class="form-control" id="billItemCode" placeholder="Enter item code">
                                                </div>
                                            </div>
                                            <div class="col-md-4">
                                                <div class="form-group">
                                                    <label>Quantity</label>
                                                    <input type="number" class="form-control" id="billQuantity" min="1" value="1">
                                                </div>
                                            </div>
                                            <div class="col-md-4">
                                                <div class="form-group">
                                                    <label>&nbsp;</label>
                                                    <button type="button" class="btn btn-success btn-block" id="addToBill">
                                                        <i class="fas fa-plus"></i> Add to Bill
                                                    </button>
                                                </div>
                                            </div>
                                        </div>
                                    </form>
                                </div>
                            </div>
                        </div>
                        
                        <div class="col-md-4">
                            <div class="card">
                                <div class="card-header">
                                    <h5>Bill Summary</h5>
                                </div>
                                <div class="card-body">
                                    <div id="billItems">
                                        <p class="text-muted">No items added yet</p>
                                    </div>
                                    <hr>
                                    <div class="d-flex justify-content-between">
                                        <strong>Total: $<span id="billTotal">0.00</span></strong>
                                    </div>
                                    <hr>
                                    <button class="btn btn-primary btn-block" id="generateBill" disabled>
                                        <i class="fas fa-file-invoice"></i> Generate Bill
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                
                <!-- Reports Section -->
                <div id="reports" class="content-section">
                    <h2 class="mb-4">Reports</h2>
                    
                    <div class="row">
                        <div class="col-md-6 mb-4">
                            <div class="card">
                                <div class="card-header">
                                    <h5>Customer Report</h5>
                                </div>
                                <div class="card-body">
                                    <p>Generate comprehensive customer reports</p>
                                    <button class="btn btn-info" onclick="generateCustomerReport()">
                                        <i class="fas fa-file-pdf"></i> Generate PDF
                                    </button>
                                </div>
                            </div>
                        </div>
                        
                        <div class="col-md-6 mb-4">
                            <div class="card">
                                <div class="card-header">
                                    <h5>Sales Report</h5>
                                </div>
                                <div class="card-body">
                                    <p>Generate sales and revenue reports</p>
                                    <button class="btn btn-info" onclick="generateSalesReport()">
                                        <i class="fas fa-file-pdf"></i> Generate PDF
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                
                <!-- Help Section -->
                <div id="help" class="content-section">
                    <h2 class="mb-4">Help & Documentation</h2>
                    
                    <div class="card">
                        <div class="card-body">
                            <div class="accordion" id="helpAccordion">
                                <div class="card">
                                    <div class="card-header">
                                        <h5 class="mb-0">
                                            <button class="btn btn-link" type="button" data-toggle="collapse" data-target="#help1">
                                                How to add a new customer?
                                            </button>
                                        </h5>
                                    </div>
                                    <div id="help1" class="collapse show" data-parent="#helpAccordion">
                                        <div class="card-body">
                                            <ol>
                                                <li>Go to the Customers section</li>
                                                <li>Click the "Add Customer" button</li>
                                                <li>Fill in all required fields</li>
                                                <li>Click "Save Customer"</li>
                                            </ol>
                                        </div>
                                    </div>
                                </div>
                                
                                <div class="card">
                                    <div class="card-header">
                                        <h5 class="mb-0">
                                            <button class="btn btn-link" type="button" data-toggle="collapse" data-target="#help2">
                                                How to create a bill?
                                            </button>
                                        </h5>
                                    </div>
                                    <div id="help2" class="collapse" data-parent="#helpAccordion">
                                        <div class="card-body">
                                            <ol>
                                                <li>Go to the Billing section</li>
                                                <li>Enter customer account number</li>
                                                <li>Add items by entering item codes and quantities</li>
                                                <li>Review the bill summary</li>
                                                <li>Click "Generate Bill" to create the invoice</li>
                                            </ol>
                                        </div>
                                    </div>
                                </div>
                                
                                <div class="card">
                                    <div class="card-header">
                                        <h5 class="mb-0">
                                            <button class="btn btn-link" type="button" data-toggle="collapse" data-target="#help3">
                                                How to manage inventory?
                                            </button>
                                        </h5>
                                    </div>
                                    <div id="help3" class="collapse" data-parent="#helpAccordion">
                                        <div class="card-body">
                                            <ol>
                                                <li>Go to the Items section</li>
                                                <li>Use "Add Item" to add new books</li>
                                                <li>Edit existing items by clicking the edit button</li>
                                                <li>Monitor stock levels regularly</li>
                                                <li>Update quantities as needed</li>
                                            </ol>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <!-- Add Customer Modal -->
    <div class="modal fade" id="addCustomerModal" tabindex="-1">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">Add New Customer</h5>
                    <button type="button" class="close" data-dismiss="modal">
                        <span>&times;</span>
                    </button>
                </div>
                <div class="modal-body">
                    <form id="addCustomerForm">
                        <div class="form-group">
                            <label>Account Number</label>
                            <input type="text" class="form-control" name="accountNumber" placeholder="Auto-generated if empty">
                        </div>
                        <div class="form-group">
                            <label>Name *</label>
                            <input type="text" class="form-control" name="name" required>
                        </div>
                        <div class="form-group">
                            <label>Address</label>
                            <textarea class="form-control" name="address" rows="3"></textarea>
                        </div>
                        <div class="form-group">
                            <label>Telephone *</label>
                            <input type="text" class="form-control" name="telephone" required>
                        </div>
                        <div class="form-group">
                            <label>Email</label>
                            <input type="email" class="form-control" name="email">
                        </div>
                    </form>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
                    <button type="button" class="btn btn-primary" onclick="saveCustomer()">Save Customer</button>
                </div>
            </div>
        </div>
    </div>
    
    <!-- Add Item Modal -->
    <div class="modal fade" id="addItemModal" tabindex="-1">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">Add New Item</h5>
                    <button type="button" class="close" data-dismiss="modal">
                        <span>&times;</span>
                    </button>
                </div>
                <div class="modal-body">
                    <form id="addItemForm">
                        <div class="form-group">
                            <label>Item Code *</label>
                            <input type="text" class="form-control" name="itemCode" required>
                        </div>
                        <div class="form-group">
                            <label>Title *</label>
                            <input type="text" class="form-control" name="title" required>
                        </div>
                        <div class="form-group">
                            <label>Author</label>
                            <input type="text" class="form-control" name="author">
                        </div>
                        <div class="form-group">
                            <label>Category</label>
                            <select class="form-control" name="category">
                                <option value="">Select Category</option>
                                <option value="Fiction">Fiction</option>
                                <option value="Non-Fiction">Non-Fiction</option>
                                <option value="Academic">Academic</option>
                                <option value="Children">Children</option>
                                <option value="Reference">Reference</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label>Price *</label>
                            <input type="number" class="form-control" name="price" step="0.01" min="0" required>
                        </div>
                        <div class="form-group">
                            <label>Stock Quantity *</label>
                            <input type="number" class="form-control" name="stockQuantity" min="0" required>
                        </div>
                    </form>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
                    <button type="button" class="btn btn-primary" onclick="saveItem()">Save Item</button>
                </div>
            </div>
        </div>
    </div>
    
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@4.6.0/dist/js/bootstrap.bundle.min.js"></script>
    <script src="js/dashboard.js"></script>
</body>
</html>

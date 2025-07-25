<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>Page Not Found</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@4.6.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
    <div class="container mt-5">
        <div class="text-center">
            <h1>404 - Page Not Found</h1>
            <p>The requested page could not be found.</p>
            <a href="${pageContext.request.contextPath}/login.jsp" class="btn btn-primary">Go to Login</a>
        </div>
    </div>
</body>
</html>

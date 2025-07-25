<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>Internal Server Error</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@4.6.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
    <div class="container mt-5">
        <div class="text-center">
            <h1>500 - Internal Server Error</h1>
            <p>Something went wrong on our end.</p>
            <a href="${pageContext.request.contextPath}/login.jsp" class="btn btn-primary">Go to Login</a>
        </div>
    </div>
</body>
</html>

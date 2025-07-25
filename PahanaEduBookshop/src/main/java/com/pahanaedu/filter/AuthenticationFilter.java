package com.pahanaedu.filter;

import com.pahanaedu.model.User;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class AuthenticationFilter implements Filter {
    
    private List<String> excludePatterns;
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String excludePatternsParam = filterConfig.getInitParameter("excludePatterns");
        if (excludePatternsParam != null) {
            excludePatterns = Arrays.asList(excludePatternsParam.split(","));
        }
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String requestURI = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        String path = requestURI.substring(contextPath.length());
        
        // Check if path should be excluded from authentication
        if (isExcluded(path)) {
            chain.doFilter(request, response);
            return;
        }
        
        // Check if user is logged in
        HttpSession session = httpRequest.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        
        if (user == null) {
            // User not logged in, redirect to login
            httpResponse.sendRedirect(contextPath + "/login.jsp");
        } else {
            // User is logged in, continue with request
            chain.doFilter(request, response);
        }
    }
    
    private boolean isExcluded(String path) {
        if (excludePatterns == null) return false;
        
        for (String pattern : excludePatterns) {
            pattern = pattern.trim();
            if (pattern.endsWith("/*")) {
                String prefix = pattern.substring(0, pattern.length() - 2);
                if (path.startsWith(prefix)) return true;
            } else if (path.equals(pattern) || path.startsWith(pattern)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public void destroy() {
        // Cleanup if needed
    }
}

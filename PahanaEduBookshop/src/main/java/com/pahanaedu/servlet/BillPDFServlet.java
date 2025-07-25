package com.pahanaedu.servlet;

import com.pahanaedu.model.Bill;
import com.pahanaedu.service.BillService;
import com.pahanaedu.service.PDFGenerationService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

@WebServlet("/bill/pdf/*")
public class BillPDFServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private BillService billService;
    private PDFGenerationService pdfService;
    
    @Override
    public void init() throws ServletException {
        billService = new BillService();
        pdfService = new PDFGenerationService();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        System.out.println("PDF Request - PathInfo: " + pathInfo);
        
        if (pathInfo != null && pathInfo.length() > 1) {
            try {
                int billId = Integer.parseInt(pathInfo.substring(1));
                System.out.println("Fetching bill with ID: " + billId);
                
                Bill bill = billService.getBillById(billId);
                
                if (bill != null) {
                    System.out.println("Bill found: " + bill.getBillNumber());
                    
                    // Generate PDF
                    byte[] pdfBytes = pdfService.generateBillPDF(bill);
                    System.out.println("PDF generated, size: " + pdfBytes.length + " bytes");
                    
                    if (pdfBytes.length > 0) {
                        // Generate filename
                        String customerName = bill.getCustomer() != null ? 
                            bill.getCustomer().getName().replaceAll("[^a-zA-Z0-9]", "_") : "Customer";
                        String filename = "Invoice_" + customerName + "_" + bill.getBillNumber() + ".pdf";
                        
                        // Enhanced headers for Chrome compatibility
                        response.setContentType("application/pdf");
                        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
                        response.setHeader("Content-Length", String.valueOf(pdfBytes.length));
                        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, private");
                        response.setHeader("Pragma", "no-cache");
                        response.setHeader("Expires", "0");
                        
                        // CORS headers for Chrome
                        response.setHeader("Access-Control-Allow-Origin", "*");
                        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
                        
                        // Set status explicitly
                        response.setStatus(HttpServletResponse.SC_OK);
                        
                        // Write PDF to response
                        OutputStream out = response.getOutputStream();
                        out.write(pdfBytes);
                        out.flush();
                        out.close();
                        
                        System.out.println("PDF sent successfully: " + filename);
                    } else {
                        System.err.println("PDF generation resulted in 0 bytes");
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        response.getWriter().write("PDF generation failed - empty content");
                    }
                } else {
                    System.err.println("Bill not found with ID: " + billId);
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().write("Bill not found");
                }
            } catch (Exception e) {
                System.err.println("Error generating PDF: " + e.getMessage());
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("Error generating PDF: " + e.getMessage());
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Bill ID is required");
        }
    }

}

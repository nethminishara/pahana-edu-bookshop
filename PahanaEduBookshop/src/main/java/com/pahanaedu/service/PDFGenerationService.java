package com.pahanaedu.service;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.pahanaedu.model.Bill;
import com.pahanaedu.model.BillItem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PDFGenerationService {
    
    public byte[] generateBillPDF(Bill bill) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);
            
            // Set fonts
            PdfFont titleFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont headerFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont normalFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            
            // Company Header
            Paragraph companyName = new Paragraph("PAHANA EDU BOOKSHOP")
                    .setFont(titleFont)
                    .setFontSize(20)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.BLUE);
            document.add(companyName);
            
            Paragraph companyAddress = new Paragraph("123 Education Street, Knowledge City\nPhone: +1-234-567-8900 | Email: info@pahanaedu.com")
                    .setFont(normalFont)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(companyAddress);
            
            // Invoice Title
            Paragraph invoiceTitle = new Paragraph("INVOICE")
                    .setFont(titleFont)
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    .setPadding(10)
                    .setMarginBottom(20);
            document.add(invoiceTitle);
            
            // Bill Information Table
            Table billInfoTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginBottom(20);
            
            // Left side - Bill details
            Cell leftCell = new Cell()
                    .setBorder(null)
                    .add(new Paragraph("Bill Number: " + (bill.getBillNumber() != null ? bill.getBillNumber() : "N/A")).setFont(headerFont))
                    .add(new Paragraph("Date: " + (bill.getBillDate() != null ? bill.getBillDate() : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()))).setFont(normalFont))
                    .add(new Paragraph("Status: " + (bill.getStatus() != null ? bill.getStatus() : "PENDING")).setFont(normalFont));
            
            // Right side - Customer details
            String customerName = "Unknown Customer";
            String accountNumber = "N/A";
            
            if (bill.getCustomer() != null) {
                customerName = bill.getCustomer().getName() != null ? bill.getCustomer().getName() : "Unknown Customer";
                accountNumber = bill.getCustomer().getAccountNumber() != null ? bill.getCustomer().getAccountNumber() : "N/A";
            }
            
            Cell rightCell = new Cell()
                    .setBorder(null)
                    .add(new Paragraph("Bill To:").setFont(headerFont))
                    .add(new Paragraph(customerName).setFont(normalFont))
                    .add(new Paragraph("Account: " + accountNumber).setFont(normalFont));
            
            billInfoTable.addCell(leftCell);
            billInfoTable.addCell(rightCell);
            document.add(billInfoTable);
            
            // Items Table
            Table itemsTable = new Table(UnitValue.createPercentArray(new float[]{3, 1, 2, 2}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginBottom(20);
            
            // Table headers
            itemsTable.addHeaderCell(new Cell().add(new Paragraph("Description").setFont(headerFont))
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY));
            itemsTable.addHeaderCell(new Cell().add(new Paragraph("Qty").setFont(headerFont))
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY).setTextAlignment(TextAlignment.CENTER));
            itemsTable.addHeaderCell(new Cell().add(new Paragraph("Unit Price").setFont(headerFont))
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY).setTextAlignment(TextAlignment.RIGHT));
            itemsTable.addHeaderCell(new Cell().add(new Paragraph("Total").setFont(headerFont))
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY).setTextAlignment(TextAlignment.RIGHT));
            
            // Add items
            double subtotal = 0;
            if (bill.getBillItems() != null && !bill.getBillItems().isEmpty()) {
                for (BillItem item : bill.getBillItems()) {
                    String itemTitle = item.getItem() != null && item.getItem().getTitle() != null ? 
                                     item.getItem().getTitle() : "Unknown Item";
                    
                    itemsTable.addCell(new Cell().add(new Paragraph(itemTitle).setFont(normalFont)));
                    itemsTable.addCell(new Cell().add(new Paragraph(String.valueOf(item.getQuantity())).setFont(normalFont))
                            .setTextAlignment(TextAlignment.CENTER));
                    itemsTable.addCell(new Cell().add(new Paragraph("$" + String.format("%.2f", item.getUnitPrice())).setFont(normalFont))
                            .setTextAlignment(TextAlignment.RIGHT));
                    itemsTable.addCell(new Cell().add(new Paragraph("$" + String.format("%.2f", item.getTotalPrice())).setFont(normalFont))
                            .setTextAlignment(TextAlignment.RIGHT));
                    subtotal += item.getTotalPrice();
                }
            } else {
                // Add a placeholder row if no items
                itemsTable.addCell(new Cell().add(new Paragraph("No items").setFont(normalFont)));
                itemsTable.addCell(new Cell().add(new Paragraph("0").setFont(normalFont))
                        .setTextAlignment(TextAlignment.CENTER));
                itemsTable.addCell(new Cell().add(new Paragraph("$0.00").setFont(normalFont))
                        .setTextAlignment(TextAlignment.RIGHT));
                itemsTable.addCell(new Cell().add(new Paragraph("$0.00").setFont(normalFont))
                        .setTextAlignment(TextAlignment.RIGHT));
            }
            
            document.add(itemsTable);
            
            // Total section
            Table totalTable = new Table(UnitValue.createPercentArray(new float[]{3, 1}))
                    .setWidth(UnitValue.createPercentValue(100));
            
            totalTable.addCell(new Cell().add(new Paragraph("Subtotal:").setFont(headerFont))
                    .setBorder(null).setTextAlignment(TextAlignment.RIGHT));
            totalTable.addCell(new Cell().add(new Paragraph("$" + String.format("%.2f", subtotal)).setFont(headerFont))
                    .setBorder(null).setTextAlignment(TextAlignment.RIGHT));
            
            totalTable.addCell(new Cell().add(new Paragraph("Tax (0%):").setFont(normalFont))
                    .setBorder(null).setTextAlignment(TextAlignment.RIGHT));
            totalTable.addCell(new Cell().add(new Paragraph("$0.00").setFont(normalFont))
                    .setBorder(null).setTextAlignment(TextAlignment.RIGHT));
            
            totalTable.addCell(new Cell().add(new Paragraph("TOTAL:").setFont(titleFont).setFontSize(14))
                    .setBorder(null).setTextAlignment(TextAlignment.RIGHT)
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY).setPadding(5));
            totalTable.addCell(new Cell().add(new Paragraph("$" + String.format("%.2f", bill.getTotalAmount())).setFont(titleFont).setFontSize(14))
                    .setBorder(null).setTextAlignment(TextAlignment.RIGHT)
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY).setPadding(5));
            
            document.add(totalTable);
            
            // Footer
            Paragraph footer = new Paragraph("\nThank you for your business!\nFor any queries, please contact us at info@pahanaedu.com")
                    .setFont(normalFont)
                    .setFontSize(9)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(30)
                    .setFontColor(ColorConstants.GRAY);
            document.add(footer);
            
            // Generated timestamp
            Paragraph timestamp = new Paragraph("Generated on: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()))
                    .setFont(normalFont)
                    .setFontSize(8)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setFontColor(ColorConstants.GRAY);
            document.add(timestamp);
            
            // Close document
            document.close();
            
            System.out.println("PDF document created successfully, size: " + baos.size() + " bytes");
            
        } catch (Exception e) {
            System.err.println("Error creating PDF: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Failed to generate PDF", e);
        }
        
        return baos.toByteArray();
    }
}

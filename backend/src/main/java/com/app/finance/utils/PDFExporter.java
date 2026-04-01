package com.app.finance.utils;

import com.app.finance.exception.DataExportException;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class PDFExporter {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DeviceRgb HEADER_BG = new DeviceRgb(31, 73, 125);
    private static final DeviceRgb ALT_ROW_BG = new DeviceRgb(220, 230, 241);

    public byte[] export(String title, List<String> headers, List<Map<String, Object>> data) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Title
            document.add(new Paragraph(title != null ? title : "Finance Report")
                    .setBold()
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(10));

            // Generated timestamp
            document.add(new Paragraph("Generated: " + LocalDateTime.now().format(FORMATTER))
                    .setFontSize(9)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginBottom(15));

            // Table
            float[] colWidths = new float[headers.size()];
            for (int i = 0; i < headers.size(); i++) colWidths[i] = 1f;
            Table table = new Table(UnitValue.createPercentArray(colWidths)).useAllAvailableWidth();

            // Header row
            for (String header : headers) {
                Cell cell = new Cell()
                        .add(new Paragraph(header).setBold().setFontColor(ColorConstants.WHITE).setFontSize(9))
                        .setBackgroundColor(HEADER_BG)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setPadding(5);
                table.addHeaderCell(cell);
            }

            // Data rows
            int rowIdx = 0;
            for (Map<String, Object> record : data) {
                DeviceRgb bg = (rowIdx % 2 == 1) ? ALT_ROW_BG : null;
                for (String header : headers) {
                    Object value = record.get(header);
                    String text = value == null ? "" :
                            (value instanceof LocalDateTime ldt ? ldt.format(FORMATTER) : value.toString());
                    Cell cell = new Cell()
                            .add(new Paragraph(text).setFontSize(8))
                            .setPadding(4);
                    if (bg != null) cell.setBackgroundColor(bg);
                    table.addCell(cell);
                }
                rowIdx++;
            }

            document.add(table);

            // Footer
            document.add(new Paragraph("Total records: " + data.size())
                    .setFontSize(9)
                    .setBold()
                    .setMarginTop(10));

            document.close();
        } catch (Exception e) {
            throw new DataExportException("Failed to generate PDF file", e);
        }
        return out.toByteArray();
    }
}

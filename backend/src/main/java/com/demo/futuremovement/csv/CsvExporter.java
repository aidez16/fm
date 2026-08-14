package com.demo.futuremovement.csv;

import com.demo.futuremovement.dto.DailySummaryRecord;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Converts summary records to CSV.
 */
@Component
public class CsvExporter {

    private static final String[] HEADERS = {"Client_Information", "Product_Information", "Total_Transaction_Amount"};
    private static final String LINE_SEPARATOR = "\r\n";

    public String toCsv(List<DailySummaryRecord> rows) {
        StringBuilder csv = new StringBuilder();
        csv.append(String.join(",", HEADERS)).append(LINE_SEPARATOR);
        for (DailySummaryRecord row : rows) {
            csv.append(escape(row.clientInformation())).append(',')
                    .append(escape(row.productInformation())).append(',')
                    .append(row.totalTransactionAmount().toPlainString())
                    .append(LINE_SEPARATOR);
        }
        return csv.toString();
    }

    /**
     * Adds CSV quotes when the value contains special characters.
     */
    private String escape(String value) {
        if (value == null) {
            return "";
        }
        boolean needsQuoting = value.contains(",") || value.contains("\"") || value.contains("\n");
        if (!needsQuoting) {
            return value;
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}

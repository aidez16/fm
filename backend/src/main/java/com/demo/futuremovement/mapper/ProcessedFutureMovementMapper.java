package com.demo.futuremovement.mapper;

import com.demo.futuremovement.exception.FixedWidthParseException;
import com.demo.futuremovement.model.ProcessedFutureMovement;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

/**
 * Maps the parser's raw field map to a typed {@link ProcessedFutureMovement}.
 * The parser knows column positions; this knows what the columns mean.
 */
@Component
public class ProcessedFutureMovementMapper {

    private static final DateTimeFormatter COMPACT_DATE = DateTimeFormatter.BASIC_ISO_DATE; // CCYYMMDD

    public ProcessedFutureMovement map(Map<String, String> fields) {
        return new ProcessedFutureMovement(
                required(fields, "CLIENT_TYPE"),
                required(fields, "CLIENT_NUMBER"),
                required(fields, "ACCOUNT_NUMBER"),
                required(fields, "SUBACCOUNT_NUMBER"),
                required(fields, "EXCHANGE_CODE"),
                required(fields, "PRODUCT_GROUP_CODE"),
                required(fields, "SYMBOL"),
                parseDate(fields.get("EXPIRATION_DATE")),
                fields.get("CURRENCY_CODE"),
                fields.get("BUY_SELL_CODE"),
                signedQuantity(fields.get("QUANTITY_LONG_SIGN"), fields.get("QUANTITY_LONG")),
                signedQuantity(fields.get("QUANTITY_SHORT_SIGN"), fields.get("QUANTITY_SHORT")),
                parseDate(fields.get("TRANSACTION_DATE"))
        );
    }

    /**
     * Gets a required field and validates that it is not empty.
     */
    private String required(Map<String, String> fields, String name) {
        String value = fields.get(name);
        if (value == null || value.isBlank()) {
            throw new FixedWidthParseException("Missing required field '" + name + "' in record: " + fields);
        }
        return value;
    }

    /**
     * Applies the sign to a quantity.
     */
    private BigDecimal signedQuantity(String sign, String magnitude) {
        if (magnitude == null || magnitude.isBlank()) {
            return BigDecimal.ZERO;
        }
        BigDecimal value = new BigDecimal(magnitude);
        if ("-".equals(sign)) {
            return value.negate();
        }
        return value;
    }

    /**
     * Parses a date in CCYYMMDD format.
     */
    private LocalDate parseDate(String rawDate) {
        if (rawDate == null || rawDate.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(rawDate, COMPACT_DATE);
        } catch (DateTimeParseException e) {
            throw new FixedWidthParseException("Invalid date value '" + rawDate + "' (expected CCYYMMDD)", e);
        }
    }
}

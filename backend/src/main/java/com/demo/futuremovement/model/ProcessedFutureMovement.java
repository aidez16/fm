package com.demo.futuremovement.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Represents one PROCESSED FUTURE MOVEMENT transaction.
 */
public record ProcessedFutureMovement(
        String clientType,
        String clientNumber,
        String accountNumber,
        String subAccountNumber,
        String exchangeCode,
        String productGroupCode,
        String symbol,
        LocalDate expirationDate,
        String currencyCode,
        String buySellCode,
        BigDecimal quantityLong,
        BigDecimal quantityShort,
        LocalDate transactionDate
) {

    private static final DateTimeFormatter COMPACT_DATE = DateTimeFormatter.BASIC_ISO_DATE; // CCYYMMDD

    /**
     * Builds the client grouping key.
     */
    public String clientInformationKey() {
        return String.join("-", clientType, clientNumber, accountNumber, subAccountNumber);
    }

    /**
     * Builds the product grouping key.
     */
    public String productInformationKey() {
        String expiration = expirationDate != null ? expirationDate.format(COMPACT_DATE) : "";
        return String.join("-", exchangeCode, productGroupCode, symbol, expiration);
    }

    /**
     * Calculates long quantity minus short quantity.
     */
    public BigDecimal netQuantity() {
        return quantityLong.subtract(quantityShort);
    }
}

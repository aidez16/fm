package com.demo.futuremovement.mapper;

import com.demo.futuremovement.exception.FixedWidthParseException;
import com.demo.futuremovement.model.ProcessedFutureMovement;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProcessedFutureMovementMapperTest {

    private final ProcessedFutureMovementMapper mapper = new ProcessedFutureMovementMapper();

    private Map<String, String> baseFields() {
        Map<String, String> fields = new HashMap<>();
        fields.put("CLIENT_TYPE", "CL");
        fields.put("CLIENT_NUMBER", "4321");
        fields.put("ACCOUNT_NUMBER", "0002");
        fields.put("SUBACCOUNT_NUMBER", "0001");
        fields.put("EXCHANGE_CODE", "SGX");
        fields.put("PRODUCT_GROUP_CODE", "FU");
        fields.put("SYMBOL", "NK");
        fields.put("EXPIRATION_DATE", "20100910");
        fields.put("CURRENCY_CODE", "JPY");
        fields.put("BUY_SELL_CODE", "B");
        fields.put("QUANTITY_LONG_SIGN", "");
        fields.put("QUANTITY_LONG", "0000000001");
        fields.put("QUANTITY_SHORT_SIGN", "");
        fields.put("QUANTITY_SHORT", "0000000000");
        fields.put("TRANSACTION_DATE", "20100820");
        return fields;
    }

    @Test
    void mapsAllFieldsToCorrectTypes() {
        ProcessedFutureMovement movement = mapper.map(baseFields());

        assertThat(movement.clientType()).isEqualTo("CL");
        assertThat(movement.clientNumber()).isEqualTo("4321");
        assertThat(movement.accountNumber()).isEqualTo("0002");
        assertThat(movement.subAccountNumber()).isEqualTo("0001");
        assertThat(movement.exchangeCode()).isEqualTo("SGX");
        assertThat(movement.productGroupCode()).isEqualTo("FU");
        assertThat(movement.symbol()).isEqualTo("NK");
        assertThat(movement.expirationDate()).isEqualTo(LocalDate.of(2010, 9, 10));
        assertThat(movement.quantityLong()).isEqualByComparingTo("1");
        assertThat(movement.quantityShort()).isEqualByComparingTo("0");
        assertThat(movement.transactionDate()).isEqualTo(LocalDate.of(2010, 8, 20));
    }

    @Test
    void negativeSignFlipsQuantityToNegative() {
        Map<String, String> fields = baseFields();
        fields.put("QUANTITY_SHORT_SIGN", "-");
        fields.put("QUANTITY_SHORT", "0000000003");

        ProcessedFutureMovement movement = mapper.map(fields);

        assertThat(movement.quantityShort()).isEqualByComparingTo(new BigDecimal("-3"));
    }

    @Test
    void netQuantityIsLongMinusShort() {
        Map<String, String> fields = baseFields();
        fields.put("QUANTITY_LONG", "0000000010");
        fields.put("QUANTITY_SHORT", "0000000004");

        ProcessedFutureMovement movement = mapper.map(fields);

        assertThat(movement.netQuantity()).isEqualByComparingTo("6");
    }

    @Test
    void clientAndProductKeysAreStableJoinsOfTheirComponentFields() {
        ProcessedFutureMovement movement = mapper.map(baseFields());

        assertThat(movement.clientInformationKey()).isEqualTo("CL-4321-0002-0001");
        assertThat(movement.productInformationKey()).isEqualTo("SGX-FU-NK-20100910");
    }

    @Test
    void missingRequiredFieldThrows() {
        Map<String, String> fields = baseFields();
        fields.remove("CLIENT_NUMBER");

        assertThatThrownBy(() -> mapper.map(fields))
                .isInstanceOf(FixedWidthParseException.class)
                .hasMessageContaining("CLIENT_NUMBER");
    }

    @Test
    void invalidDateThrows() {
        Map<String, String> fields = baseFields();
        fields.put("EXPIRATION_DATE", "20109999");

        assertThatThrownBy(() -> mapper.map(fields))
                .isInstanceOf(FixedWidthParseException.class);
    }
}

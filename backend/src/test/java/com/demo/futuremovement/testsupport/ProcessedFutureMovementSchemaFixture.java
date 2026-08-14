package com.demo.futuremovement.testsupport;

import com.demo.futuremovement.parser.FieldDefinition;
import com.demo.futuremovement.parser.RecordSchema;

import java.util.List;

/**
 * The "315" layout from {@code record-schemas.yml}, so tests can parse for
 * real without a Spring context. Keep in sync with the YAML.
 */
public final class ProcessedFutureMovementSchemaFixture {

    private ProcessedFutureMovementSchemaFixture() {
    }

    public static RecordSchema schema() {
        RecordSchema schema = new RecordSchema();
        schema.setName("PROCESSED_FUTURE_MOVEMENT");
        schema.setTotalLength(303);
        schema.setFields(List.of(
                new FieldDefinition("RECORD_CODE", 1, 3),
                new FieldDefinition("CLIENT_TYPE", 4, 7),
                new FieldDefinition("CLIENT_NUMBER", 8, 11),
                new FieldDefinition("ACCOUNT_NUMBER", 12, 15),
                new FieldDefinition("SUBACCOUNT_NUMBER", 16, 19),
                new FieldDefinition("OPPOSITE_PARTY_CODE", 20, 25),
                new FieldDefinition("PRODUCT_GROUP_CODE", 26, 27),
                new FieldDefinition("EXCHANGE_CODE", 28, 31),
                new FieldDefinition("SYMBOL", 32, 37),
                new FieldDefinition("EXPIRATION_DATE", 38, 45),
                new FieldDefinition("CURRENCY_CODE", 46, 48),
                new FieldDefinition("MOVEMENT_CODE", 49, 50),
                new FieldDefinition("BUY_SELL_CODE", 51, 51),
                new FieldDefinition("QUANTITY_LONG_SIGN", 52, 52),
                new FieldDefinition("QUANTITY_LONG", 53, 62),
                new FieldDefinition("QUANTITY_SHORT_SIGN", 63, 63),
                new FieldDefinition("QUANTITY_SHORT", 64, 73),
                new FieldDefinition("EXCH_BROKER_FEE", 74, 85),
                new FieldDefinition("EXCH_BROKER_FEE_DC", 86, 86),
                new FieldDefinition("EXCH_BROKER_FEE_CUR", 87, 89),
                new FieldDefinition("CLEARING_FEE", 90, 101),
                new FieldDefinition("CLEARING_FEE_DC", 102, 102),
                new FieldDefinition("CLEARING_FEE_CUR", 103, 105),
                new FieldDefinition("COMMISSION", 106, 117),
                new FieldDefinition("COMMISSION_DC", 118, 118),
                new FieldDefinition("COMMISSION_CUR", 119, 121),
                new FieldDefinition("TRANSACTION_DATE", 122, 129),
                new FieldDefinition("FUTURE_REFERENCE", 130, 135),
                new FieldDefinition("TICKET_NUMBER", 136, 141),
                new FieldDefinition("EXTERNAL_NUMBER", 142, 147),
                new FieldDefinition("TRANSACTION_PRICE", 148, 162),
                new FieldDefinition("TRADER_INITIALS", 163, 168),
                new FieldDefinition("OPPOSITE_TRADER_ID", 169, 175),
                new FieldDefinition("OPEN_CLOSE_CODE", 176, 176)
        ));
        return schema;
    }
}

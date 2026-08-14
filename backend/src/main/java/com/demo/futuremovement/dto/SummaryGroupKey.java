package com.demo.futuremovement.dto;

import com.demo.futuremovement.model.ProcessedFutureMovement;

/**
 * Groups transactions by client and product.
 */
public record SummaryGroupKey(String clientInformation, String productInformation) {

    public static SummaryGroupKey of(ProcessedFutureMovement movement) {
        return new SummaryGroupKey(movement.clientInformationKey(), movement.productInformationKey());
    }
}

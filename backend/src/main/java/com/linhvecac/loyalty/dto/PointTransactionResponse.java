package com.linhvecac.loyalty.dto;

import com.linhvecac.loyalty.PointTransaction;
import com.linhvecac.loyalty.PointTransactionType;

import java.time.LocalDateTime;

public record PointTransactionResponse(
        Long id,
        PointTransactionType type,
        long delta,
        long balanceAfter,
        String description,
        LocalDateTime createdAt) {

    public static PointTransactionResponse from(PointTransaction tx) {
        return new PointTransactionResponse(
                tx.getId(),
                tx.getType(),
                tx.getDelta(),
                tx.getBalanceAfter(),
                tx.getDescription(),
                tx.getCreatedAt());
    }
}

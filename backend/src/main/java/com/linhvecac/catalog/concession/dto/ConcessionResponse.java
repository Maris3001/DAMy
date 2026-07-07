package com.linhvecac.catalog.concession.dto;

import com.linhvecac.catalog.concession.Concession;
import com.linhvecac.catalog.concession.ConcessionCategory;

public record ConcessionResponse(
        Long id,
        String name,
        String description,
        long price,
        String imageUrl,
        ConcessionCategory category,
        boolean active) {

    public static ConcessionResponse from(Concession c) {
        return new ConcessionResponse(
                c.getId(),
                c.getName(),
                c.getDescription(),
                c.getPrice(),
                c.getImageUrl(),
                c.getCategory(),
                c.isActive());
    }
}

package com.linhvecac.catalog.cinema.dto;

import com.linhvecac.catalog.cinema.CinemaChain;

public record ChainResponse(Long id, String name, String logoUrl) {

    public static ChainResponse from(CinemaChain c) {
        return new ChainResponse(c.getId(), c.getName(), c.getLogoUrl());
    }
}

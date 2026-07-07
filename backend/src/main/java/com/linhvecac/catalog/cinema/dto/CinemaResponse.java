package com.linhvecac.catalog.cinema.dto;

import com.linhvecac.catalog.cinema.Cinema;

public record CinemaResponse(
        Long id,
        String name,
        String address,
        Long regionId,
        String regionName,
        Long chainId,
        String chainName) {

    public static CinemaResponse from(Cinema c) {
        return new CinemaResponse(
                c.getId(),
                c.getName(),
                c.getAddress(),
                c.getRegion().getId(),
                c.getRegion().getName(),
                c.getChain().getId(),
                c.getChain().getName());
    }
}

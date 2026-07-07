package com.linhvecac.catalog.region.dto;

import com.linhvecac.catalog.region.Region;

public record RegionResponse(Long id, String name) {

    public static RegionResponse from(Region region) {
        return new RegionResponse(region.getId(), region.getName());
    }
}

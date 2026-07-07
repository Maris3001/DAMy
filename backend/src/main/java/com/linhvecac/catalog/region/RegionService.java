package com.linhvecac.catalog.region;

import com.linhvecac.catalog.region.dto.RegionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RegionService {

    private final RegionRepository regionRepository;

    @Transactional(readOnly = true)
    public List<RegionResponse> list() {
        return regionRepository.findAllByOrderBySortOrderAsc()
                .stream().map(RegionResponse::from).toList();
    }
}

package com.linhvecac.catalog.concession;

import com.linhvecac.catalog.concession.dto.ConcessionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/concessions")
@RequiredArgsConstructor
public class ConcessionController {

    private final ConcessionService concessionService;

    @GetMapping
    public List<ConcessionResponse> list() {
        return concessionService.listPublic();
    }
}

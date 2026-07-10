package com.linhvecac.catalog.concession;

import com.linhvecac.catalog.concession.dto.ConcessionRequest;
import com.linhvecac.catalog.concession.dto.ConcessionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/concessions")
@RequiredArgsConstructor
public class AdminConcessionController {

    private final ConcessionService concessionService;

    @GetMapping
    public List<ConcessionResponse> list() {
        return concessionService.adminList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ConcessionResponse create(@Valid @RequestBody ConcessionRequest request) {
        return concessionService.create(request);
    }

    @PutMapping("/{id}")
    public ConcessionResponse update(@PathVariable("id") Long id, @Valid @RequestBody ConcessionRequest request) {
        return concessionService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        concessionService.delete(id);
    }
}

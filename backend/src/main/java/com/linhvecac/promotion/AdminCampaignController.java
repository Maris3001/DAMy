package com.linhvecac.promotion;

import com.linhvecac.promotion.dto.CampaignRequest;
import com.linhvecac.promotion.dto.CampaignResponse;
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

/** CRUD chiến dịch KM — chặn ADMIN qua SecurityConfig (/api/admin/**). */
@RestController
@RequestMapping("/api/admin/campaigns")
@RequiredArgsConstructor
public class AdminCampaignController {

    private final CampaignService campaignService;

    @GetMapping
    public List<CampaignResponse> list() {
        return campaignService.adminList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CampaignResponse create(@Valid @RequestBody CampaignRequest request) {
        return campaignService.create(request);
    }

    @PutMapping("/{id}")
    public CampaignResponse update(@PathVariable("id") Long id, @Valid @RequestBody CampaignRequest request) {
        return campaignService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        campaignService.delete(id);
    }
}

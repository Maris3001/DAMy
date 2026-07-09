package com.linhvecac.promotion;

import com.linhvecac.common.ApiException;
import com.linhvecac.promotion.dto.CampaignRequest;
import com.linhvecac.promotion.dto.CampaignResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** CRUD chiến dịch KM cho admin (mirror ConcessionService). issuedCount = số voucher đã phát từ campaign. */
@Service
@RequiredArgsConstructor
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final UserVoucherRepository userVoucherRepository;

    @Transactional(readOnly = true)
    public List<CampaignResponse> adminList() {
        return campaignRepository.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
                .map(c -> CampaignResponse.from(c, userVoucherRepository.countByCampaignId(c.getId())))
                .toList();
    }

    @Transactional
    public CampaignResponse create(CampaignRequest request) {
        campaignRepository.findByCode(request.code().trim()).ifPresent(x -> {
            throw new ApiException(HttpStatus.CONFLICT, "Mã campaign đã tồn tại");
        });
        Campaign c = new Campaign();
        apply(c, request);
        return CampaignResponse.from(campaignRepository.save(c), 0);
    }

    @Transactional
    public CampaignResponse update(Long id, CampaignRequest request) {
        Campaign c = get(id);
        campaignRepository.findByCode(request.code().trim())
                .filter(other -> !other.getId().equals(id))
                .ifPresent(x -> {
                    throw new ApiException(HttpStatus.CONFLICT, "Mã campaign đã tồn tại");
                });
        apply(c, request);
        return CampaignResponse.from(campaignRepository.save(c), userVoucherRepository.countByCampaignId(id));
    }

    @Transactional
    public void delete(Long id) {
        Campaign c = get(id);
        if (userVoucherRepository.countByCampaignId(id) > 0) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Không thể xóa: đã có voucher phát hành từ chương trình này. Hãy tắt hoạt động thay vì xóa.");
        }
        campaignRepository.delete(c);
    }

    private void apply(Campaign c, CampaignRequest r) {
        c.setCode(r.code().trim());
        c.setName(r.name().trim());
        c.setDescription(r.description());
        c.setVoucherType(r.voucherType());
        c.setDiscountValue(r.discountValue());
        c.setMaxDiscountAmount(r.maxDiscountAmount());
        c.setMinOrderAmount(r.minOrderAmount());
        c.setMinTier(r.minTier());
        c.setPointsCost(r.pointsCost());
        c.setTriggerType(r.triggerType());
        c.setValidDays(r.validDays());
        c.setQuantity(r.quantity());
        c.setPerUserLimit(r.perUserLimit());
        c.setActive(r.active() == null || r.active());
    }

    private Campaign get(Long id) {
        return campaignRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy chương trình"));
    }
}

package com.linhvecac.catalog.concession;

import com.linhvecac.catalog.concession.dto.ConcessionRequest;
import com.linhvecac.catalog.concession.dto.ConcessionResponse;
import com.linhvecac.common.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConcessionService {

    private final ConcessionRepository concessionRepository;

    @Transactional(readOnly = true)
    public List<ConcessionResponse> listPublic() {
        return concessionRepository.findByActiveTrueOrderByCategoryAscNameAsc()
                .stream().map(ConcessionResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ConcessionResponse> adminList() {
        return concessionRepository.findAll().stream().map(ConcessionResponse::from).toList();
    }

    @Transactional
    public ConcessionResponse create(ConcessionRequest request) {
        Concession c = new Concession();
        apply(c, request);
        return ConcessionResponse.from(concessionRepository.save(c));
    }

    @Transactional
    public ConcessionResponse update(Long id, ConcessionRequest request) {
        Concession c = getConcession(id);
        apply(c, request);
        return ConcessionResponse.from(concessionRepository.save(c));
    }

    @Transactional
    public void delete(Long id) {
        concessionRepository.delete(getConcession(id));
    }

    private void apply(Concession c, ConcessionRequest r) {
        c.setName(r.name().trim());
        c.setDescription(r.description());
        c.setPrice(r.price());
        c.setImageUrl(r.imageUrl());
        c.setCategory(r.category());
        c.setActive(r.active() == null || r.active());
    }

    private Concession getConcession(Long id) {
        return concessionRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy món"));
    }
}

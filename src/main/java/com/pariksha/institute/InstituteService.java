package com.pariksha.institute;

import com.pariksha.common.ResourceNotFoundException;
import com.pariksha.institute.InstituteDtos.InstituteResponse;
import com.pariksha.institute.InstituteDtos.UpdateInstituteRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InstituteService {

    private final InstituteRepository instituteRepository;

    public InstituteResponse getMine(Long instituteId) {
        return InstituteResponse.from(find(instituteId));
    }

    @Transactional
    public InstituteResponse update(Long instituteId, UpdateInstituteRequest request) {
        Institute institute = find(instituteId);
        institute.setName(request.name().trim());
        institute.setLogoUrl(request.logoUrl());
        return InstituteResponse.from(institute);
    }

    private Institute find(Long id) {
        return instituteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Institute not found"));
    }
}

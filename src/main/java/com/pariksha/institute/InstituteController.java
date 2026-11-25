package com.pariksha.institute;

import com.pariksha.institute.InstituteDtos.InstituteResponse;
import com.pariksha.institute.InstituteDtos.UpdateInstituteRequest;
import com.pariksha.security.AuthUser;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/institutes")
@RequiredArgsConstructor
@Tag(name = "Institute")
public class InstituteController {

    private final InstituteService instituteService;

    @GetMapping("/me")
    public InstituteResponse myInstitute(@AuthenticationPrincipal AuthUser user) {
        return instituteService.getMine(user.instituteId());
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('ADMIN')")
    public InstituteResponse updateMyInstitute(@AuthenticationPrincipal AuthUser user,
                                               @Valid @RequestBody UpdateInstituteRequest request) {
        return instituteService.update(user.instituteId(), request);
    }
}

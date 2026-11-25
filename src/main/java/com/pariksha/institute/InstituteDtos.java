package com.pariksha.institute;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class InstituteDtos {

    private InstituteDtos() {
    }

    public record UpdateInstituteRequest(
            @NotBlank @Size(max = 255) String name,
            @Size(max = 500) String logoUrl) {
    }

    public record InstituteResponse(Long id, String name, String code, String logoUrl, boolean active) {
        public static InstituteResponse from(Institute i) {
            return new InstituteResponse(i.getId(), i.getName(), i.getCode(), i.getLogoUrl(), i.isActive());
        }
    }
}

package com.elshimma.erp.hr.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PositionRequest {
    @NotBlank(message = "Position title is required")
    private String title;
    private String description;
}

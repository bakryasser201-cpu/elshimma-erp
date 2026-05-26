package com.elshimma.erp.hr.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PositionResponse {
    private Long id;
    private String title;
    private String description;
}

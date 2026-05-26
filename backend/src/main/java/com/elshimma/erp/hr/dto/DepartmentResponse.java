package com.elshimma.erp.hr.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DepartmentResponse {
    private Long id;
    private String name;
    private String description;
}

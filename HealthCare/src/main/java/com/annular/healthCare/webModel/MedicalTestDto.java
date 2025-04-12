package com.annular.healthCare.webModel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class MedicalTestDto {
	
    private Integer medicalId;
    private String medicalTestName;
    private Integer hospitalId;
    private Integer updatedBy;
    private float mrp;
    private BigDecimal gst;

}

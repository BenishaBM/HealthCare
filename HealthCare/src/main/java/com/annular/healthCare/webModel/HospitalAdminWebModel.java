package com.annular.healthCare.webModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HospitalAdminWebModel {
	
    private Integer userAdminId;
    private Integer createdBy;

}

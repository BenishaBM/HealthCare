package com.annular.healthCare.webModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class DoctorSlotWebModel {
	
    private Integer userId;   // Reference to the doctor (User)
    private Integer createdBy;
    private Boolean isActive;

}

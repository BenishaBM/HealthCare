package com.annular.healthCare.webModel;

import java.util.Date;

import com.annular.healthCare.model.DoctorSlotTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorSlotTimeOverrideWebModel {
	

    private Integer overrideId;
    private DoctorSlotTime originalSlot;
    private Date overrideDate;
    private String newSlotTime;
    private String reason;
	private Integer createdBy;
	private Date createdOn;
	private Integer updatedBy;
	private Date updatedOn;
	private Boolean isActive;
	private Integer doctorSlotTimeId;

}

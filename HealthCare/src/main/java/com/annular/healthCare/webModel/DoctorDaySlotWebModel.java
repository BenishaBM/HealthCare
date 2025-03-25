package com.annular.healthCare.webModel;

import java.util.Date;
import java.util.List;

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
public class DoctorDaySlotWebModel {
	
    private Integer doctorSlotId;  // Reference to DoctorSlot
    private String day;            // Day of the week (e.g., Monday, Tuesday)
    private Date startSlotDate;
    private Date endSlotDate;
    private Integer createdBy;
    private Boolean isActive;
    private List<DoctorSlotTimeWebModel> doctorSlotTimes;

}

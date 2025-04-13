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
public class DaySlotWebModel {
	
    private Integer medicalTestDaySlotId;
    private String day;
    private Date startSlotDate;
    private Date endSlotDate;
    private List<TimeSlotModel> timeSlots;

}

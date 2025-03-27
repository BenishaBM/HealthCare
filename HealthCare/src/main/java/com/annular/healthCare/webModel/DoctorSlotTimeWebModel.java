package com.annular.healthCare.webModel;

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
public class DoctorSlotTimeWebModel {
	
    private Integer doctorDaySlotId;  // Reference to DoctorDaySlot
    private String slotStartTime;     // Start time (e.g., "09:00 AM")
    private String slotEndTime;       // End time (e.g., "10:00 AM")
    private String slotTime;          // Duration of the slot (e.g., "30 mins")
    private Integer createdBy;
    private Boolean isActive;
    private List<SplitSlotDurationWebModel> splitSlotDuration;

}

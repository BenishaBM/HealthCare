package com.annular.healthCare.webModel;

import java.util.Date;
import java.util.List;
import java.util.Map;

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
public class TimeSlotModel {
    private Integer medicalTestSlotTimeId;
    private String slotStartTime;
    private String slotEndTime;
    private String slotTime;
    private List<Map<String, Object>> slotDates;


}

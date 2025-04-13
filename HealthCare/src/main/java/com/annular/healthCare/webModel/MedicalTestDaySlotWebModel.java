package com.annular.healthCare.webModel;

import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalTestDaySlotWebModel {

    private String day;
    private Date startSlotDate;
    private Date endSlotDate;
    private List<MedicalTestSlotTimeWebModel> medicalTestSlotTimes;
}

package com.annular.healthCare.webModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalTestSlotTimeWebModel {

    private String slotStartTime;
    private String slotEndTime;
    private String slotTime;
}

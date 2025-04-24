package com.annular.healthCare.webModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.annular.healthCare.webModel.HospitalDataListWebModel.MedicineDetail;

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
public class MedicineScheduleWebModel {
	
    private Integer medicineId;
    private Integer morningBF;
    private Integer morningAF;
    private Integer afternoonBF;
    private Integer afternoonAF;
    private Integer nightBF;
    private Integer nightAF;
    private Boolean every6Hours;
    private Boolean every8Hours;
    private Integer days;
    private Integer totalTabletCount;

}

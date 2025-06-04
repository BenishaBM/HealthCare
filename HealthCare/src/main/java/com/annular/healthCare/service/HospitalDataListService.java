package com.annular.healthCare.service;

import org.springframework.http.ResponseEntity;

import com.annular.healthCare.webModel.DoctorSlotTimeOverrideWebModel;
import com.annular.healthCare.webModel.HospitalDataListWebModel;

public interface HospitalDataListService {

	ResponseEntity<?> register(HospitalDataListWebModel userWebModel);

	ResponseEntity<?> getHospitalDataByUserTypeAndHospitalId(String userType, Integer hospitalId);

	ResponseEntity<?> getHospitalDataByUserId(Integer hospitalDataId);

    ResponseEntity<?> updateHospitalDataByUserId(HospitalDataListWebModel userWebModel);

	ResponseEntity<?> deleteHospitalDataByUserId(Integer hospitalDataId);

	ResponseEntity<?> getByHopitalName(Integer pageNo, Integer pageSize);

	ResponseEntity<?> getByDoctorSpeciallity();

	ResponseEntity<?> saveDoctorSlotTimeOverride(DoctorSlotTimeOverrideWebModel userWebModel);

	ResponseEntity<?> getSpecialitiesDoctorList(String speciality);


}

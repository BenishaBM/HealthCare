package com.annular.healthCare.service;

import org.springframework.http.ResponseEntity;

import com.annular.healthCare.webModel.HospitalDataListWebModel;

public interface HospitalDataListService {

	ResponseEntity<?> register(HospitalDataListWebModel userWebModel);

	ResponseEntity<?> getHospitalDataByUserTypeAndHospitalId(String userType, Integer hospitalId);

}

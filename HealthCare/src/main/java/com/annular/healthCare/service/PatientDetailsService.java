package com.annular.healthCare.service;

import org.springframework.http.ResponseEntity;

import com.annular.healthCare.webModel.PatientDetailsWebModel;

public interface PatientDetailsService {

	ResponseEntity<?> register(PatientDetailsWebModel userWebModel);

	ResponseEntity<?> getAllPatientDetails(Integer hospitalId, Integer pageNo, Integer pageSize);

	ResponseEntity<?> updatePatientDetails(PatientDetailsWebModel userWebModel);

	ResponseEntity<?> getPatientDetailsById(Integer patientDetailsID);

	ResponseEntity<?> getDoctorListByHospitalId(Integer hospitalId);

	
	ResponseEntity<?> adminPatientRegister(PatientDetailsWebModel userWebModel);

	ResponseEntity<?> patientAppoitmentByOffline(PatientDetailsWebModel patientDetailsWebModel);

	ResponseEntity<?> getPatientDetailsByMobileNumberAndHospitalId(String phoneNumber, Integer hospitalId);

	ResponseEntity<?> getPatientDetailsByMobileNumber(String mobileNumber);

	ResponseEntity<?> patientAppoitmentByOnline(PatientDetailsWebModel patientDetailsWebModel);

	ResponseEntity<?> patientSubChildRegister(PatientDetailsWebModel userWebModel);

	ResponseEntity<?> getPatientRelationShipDetails(Integer patientDetailsId, String relationshipType);

	ResponseEntity<?> getPatientMappedDetailsById(Integer patientDetailsId, Integer hospitalId);

	ResponseEntity<?> getDoctorfeesById(Integer userId);

	ResponseEntity<?> getAllAddressData();

	ResponseEntity<?> getDropDownByLabList();

	ResponseEntity<?> getDropDownBySupportStaffList();

}

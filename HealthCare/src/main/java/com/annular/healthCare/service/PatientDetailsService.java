package com.annular.healthCare.service;

import org.springframework.http.ResponseEntity;

import com.annular.healthCare.webModel.PatientDetailsWebModel;

public interface PatientDetailsService {

	ResponseEntity<?> register(PatientDetailsWebModel userWebModel);

	ResponseEntity<?> getAllPatientDetails(Integer hospitalId);

	ResponseEntity<?> updatePatientDetails(PatientDetailsWebModel userWebModel);

	ResponseEntity<?> getPatientDetailsById(Integer patientDetailsID);

	ResponseEntity<?> getDoctorListByHospitalId(Integer hospitalId);

	boolean deleteMediaFilesById(Integer fileId);

	ResponseEntity<?> adminPatientRegister(PatientDetailsWebModel userWebModel);

	ResponseEntity<?> patientAppoitmentByOffline(PatientDetailsWebModel patientDetailsWebModel);

	ResponseEntity<?> getPatientDetailsByMobileNumberAndHospitalId(String phoneNumber, Integer hospitalId);

	ResponseEntity<?> getPatientDetailsByMobileNumber(String mobileNumber);

	ResponseEntity<?> patientAppoitmentByOnline(PatientDetailsWebModel patientDetailsWebModel);

	ResponseEntity<?> patientSubChildRegister(PatientDetailsWebModel userWebModel);

	ResponseEntity<?> getPatientRelationShipDetails(Integer patientDetailsId, String relationshipType);

}

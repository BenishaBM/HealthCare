package com.annular.healthCare.service;

import java.util.Date;

import org.springframework.http.ResponseEntity;

import com.annular.healthCare.webModel.HospitalDataListWebModel;

public interface DoctorAppoitmentService {

	ResponseEntity<?> getAllPatientAppointmentByFilter(String appointmentDate, String appointmentType, Integer doctorId);

	ResponseEntity<?> saveDoctorAppoitment(HospitalDataListWebModel userWebModel);

	ResponseEntity<?> getAllMedicineDetailsByHospitalId(Integer hospitalId);

	ResponseEntity<?> getAllMedicalTestByHospitalId(Integer hospitalId);

	ResponseEntity<?> getAllPatientPharamcyByHospitalIdAndDate(Integer hospitalId, String currentDatae);

	ResponseEntity<?> getAllPatientPharamcyBypatientIdAndDate(Integer patientId, String currentDatae);

	ResponseEntity<?> saveMedicineDetailByPharamacy(HospitalDataListWebModel userWebModel);

}

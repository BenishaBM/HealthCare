package com.annular.healthCare.service;

import java.util.Date;

import org.springframework.http.ResponseEntity;

import com.annular.healthCare.webModel.HospitalDataListWebModel;
import com.annular.healthCare.webModel.PatientAppointmentWebModel;

public interface DoctorAppoitmentService {

	ResponseEntity<?> getAllPatientAppointmentByFilter(String appointmentDate, String appointmentType, Integer doctorId);

	ResponseEntity<?> saveDoctorAppoitment(HospitalDataListWebModel userWebModel);

	ResponseEntity<?> getAllMedicineDetailsByHospitalId(Integer hospitalId);

	ResponseEntity<?> getAllMedicalTestByHospitalId(Integer hospitalId);

	ResponseEntity<?> getAllPatientPharamcyByHospitalIdAndDate(Integer hospitalId, String currentDatae);

	ResponseEntity<?> getAllPatientPharamcyBypatientIdAndDate(Integer patientId, String currentDatae,
			Integer appontmentId);

	ResponseEntity<?> saveMedicineDetailByPharamacy(HospitalDataListWebModel userWebModel);

	ResponseEntity<?> getAllPatientMedicalTestByHospitalIdAndDate(Integer hospitalId, String currentDatae);

	ResponseEntity<?> getAllPatientMedicalTestBypatientIdAndDate(Integer patientId, String currentDatae, Integer appointmentId);

	ResponseEntity<?> deleteParticularSpiltSlot(Integer id);

	ResponseEntity<?> getAllPatientAppointment(String appointmentDate, Integer doctorId);

	ResponseEntity<?> getAllPatientAppointmentDetails(Integer patientId, String currentDatae, Integer appointmentId);

	ResponseEntity<?> saveDoctorFees(HospitalDataListWebModel userWebModel);

	ResponseEntity<?> updateFeesStatus(HospitalDataListWebModel userWebModel);

	ResponseEntity<?> getAllDoctorFilterByLocation(String location);

	ResponseEntity<?> cancelAppointmentOnlineAndOffline(HospitalDataListWebModel userWebModel);

	ResponseEntity<?> rescheduleAppointmentOnlineAndOffline(HospitalDataListWebModel userWebModel);

	ResponseEntity<?> addDischargeSummaryByAppointmentId(PatientAppointmentWebModel userWebModel);

	

}

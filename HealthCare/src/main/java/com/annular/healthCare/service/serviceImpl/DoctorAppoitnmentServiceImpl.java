package com.annular.healthCare.service.serviceImpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Column;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.annular.healthCare.Response;
import com.annular.healthCare.model.AppointmentMedicalTest;
import com.annular.healthCare.model.AppointmentMedicine;
import com.annular.healthCare.model.MedicalTest;
import com.annular.healthCare.model.Medicines;
import com.annular.healthCare.model.PatientAppointmentTable;
import com.annular.healthCare.model.PatientDetails;
import com.annular.healthCare.model.PatientMappedHospitalId;
import com.annular.healthCare.repository.AppointmentMedicalTestRepository;
import com.annular.healthCare.repository.AppointmentMedicineRepository;
import com.annular.healthCare.repository.MedicalTestRepository;
import com.annular.healthCare.repository.MedicinesRepository;
import com.annular.healthCare.repository.PatientAppoitmentTablerepository;
import com.annular.healthCare.repository.PatientDetailsRepository;
import com.annular.healthCare.repository.PatientMappedHospitalIdRepository;
import com.annular.healthCare.service.DoctorAppoitmentService;
import com.annular.healthCare.webModel.HospitalDataListWebModel;
import com.annular.healthCare.webModel.MedicineScheduleWebModel;

@Service
public class DoctorAppoitnmentServiceImpl implements DoctorAppoitmentService{
	
	@Autowired
	PatientAppoitmentTablerepository patientAppointmentRepository;
	
	@Autowired
	PatientDetailsRepository patientDetailsRepository;
	
	@Autowired
	MedicinesRepository medicineRepository;
	
	@Autowired
	MedicalTestRepository medicalTestRepository;
	
	@Autowired
	private AppointmentMedicineRepository appointmentMedicineRepository;
	
	@Autowired
	PatientMappedHospitalIdRepository patientMappedHospitalIdRepository;

	@Autowired
	private AppointmentMedicalTestRepository appointmentMedicalTestRepository;


	 @Override
	    public ResponseEntity<?> getAllPatientAppointmentByFilter(String appointmentDate, String appointmentType, Integer doctorId) {
	        try {
	            List<PatientAppointmentTable> appointments = patientAppointmentRepository.findAppointmentsByFilter(appointmentDate, appointmentType, doctorId);
	            
	            if (appointments.isEmpty()) {
	            	return ResponseEntity.ok(new Response(0, "No Appointments Found", null));
	            }

	            List<Map<String, Object>> appointmentDetails = new ArrayList<>();

	            for (PatientAppointmentTable appointment : appointments) {
	                Map<String, Object> details = new HashMap<>();
	            // Appointment details
	                // Appointment details
	                details.put("appointmentId", appointment.getAppointmentId());
	                details.put("appointmentDate", appointment.getAppointmentDate());
	                details.put("appointmentType", appointment.getAppointmentType());
	                details.put("appointmentStatus", appointment.getAppointmentStatus());
	                details.put("slotStartTime", appointment.getSlotStartTime());
	                details.put("slotEndTime", appointment.getSlotEndTime());
	                details.put("slotTime", appointment.getSlotTime());
	                details.put("isActive", appointment.getIsActive());
	                details.put("createdBy", appointment.getCreatedBy());
	                details.put("createdOn", appointment.getCreatedOn());
	                details.put("updatedBy", appointment.getUpdatedBy());
	                details.put("updatedOn", appointment.getUpdatedOn());
	                details.put("patientNotes", appointment.getPatientNotes());
	                details.put("doctorSlotId", appointment.getDoctorSlotId());
	                details.put("daySlotId", appointment.getDaySlotId());
	                details.put("timeSlotId", appointment.getTimeSlotId());
	                details.put("age", appointment.getAge());
	                details.put("dob", appointment.getDateOfBirth());
	                details.put("relationshipType", appointment.getRelationShipType());
	                details.put("patientName", appointment.getPatientName());
	                details.put("token", appointment.getToken());

	            // Doctor details
	            details.put("doctorId", appointment.getDoctor().getUserId());
	            details.put("doctorName", appointment.getDoctor().getUserName());

	            // Fetching Patient Details
	            Optional<PatientDetails> patientDetailsOpt = patientDetailsRepository.findById(appointment.getPatient().getPatientDetailsId());
	            if (patientDetailsOpt.isPresent()) {
	                PatientDetails patient = patientDetailsOpt.get();
	                details.put("patientId", patient.getPatientDetailsId());
	                details.put("patientName", patient.getPatientName());
	                details.put("patientAge", patient.getAge());
	                details.put("patientDOB", patient.getDob());
	                details.put("gender", patient.getGender());
	                details.put("bloodGroup", patient.getBloodGroup());
	                details.put("mobileNumber", patient.getMobileNumber());
	                details.put("emailId", patient.getEmailId());
	                details.put("address", patient.getAddress());
	                details.put("emergencyContact", patient.getEmergencyContact());
	               
	                details.put("purposeOfVisit", patient.getPurposeOfVisit());
	                details.put("policyNumber", patient.getPolicyNumber());
	                details.put("disability", patient.getDisability());
	                details.put("previousMedicalHistory", patient.getPreviousMedicalHistory());
	            }
	                appointmentDetails.add(details);
	            }

	            return ResponseEntity.ok(new Response(1, "Appointments Retrieved", appointmentDetails));

	        } catch (Exception e) {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                    .body(new Response(-1, "Failed to retrieve appointments", e.getMessage()));
	        }
	    }
	 @Override
	 @Transactional
	 public ResponseEntity<?> saveDoctorAppoitment(HospitalDataListWebModel userWebModel) {
	     try {
	         if (userWebModel == null || userWebModel.getAppointmentId() == null) {
	             return ResponseEntity.badRequest().body(new Response(0, "error", "Appointment ID is required."));
	         }

	         Optional<PatientAppointmentTable> optionalAppointment = patientAppointmentRepository.findById(userWebModel.getAppointmentId());

	         if (optionalAppointment.isEmpty()) {
	             return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response(0, "error", "Appointment not found."));
	         }

	         PatientAppointmentTable appointment = optionalAppointment.get();

	         // Set appointment status
	         appointment.setAppointmentStatus("COMPLETED");
	         appointment.setDoctorPrescription(userWebModel.getDoctorPrescription());
	         patientAppointmentRepository.save(appointment);

	         Integer userId = userWebModel.getUserId(); // Who is saving this
	         if (userWebModel.getSchedules() != null) {
	        	    for (MedicineScheduleWebModel medSchedule : userWebModel.getSchedules()) {
	        	        Optional<Medicines> medicineOpt = medicineRepository.findById(medSchedule.getMedicineId());
	        	        if (medicineOpt.isPresent()) {
	        	            AppointmentMedicine am = AppointmentMedicine.builder()
	        	                .appointment(appointment)
	        	                .medicine(medicineOpt.get())
	        	                .isActive(true)
	        	                .createdBy(userId)
	        	                .updatedBy(userId)
	        	                .patientStatus(false)
	        	                .morningBF(medSchedule.getMorningBF())
	        	                .morningAF(medSchedule.getMorningAF())
	        	                .afternoonBF(medSchedule.getAfternoonBF())
	        	                .afternoonAF(medSchedule.getAfternoonAF())
	        	                .nightBF(medSchedule.getNightBF())
	        	                .nightAF(medSchedule.getNightAF())
	        	                .every6Hours(medSchedule.getEvery6Hours())
	        	                .every8Hours(medSchedule.getEvery8Hours())
	        	                .days(medSchedule.getDays())
	        	                .build();

	        	            appointmentMedicineRepository.save(am);
	        	        }
	        	    }
	        	}

	        	


	         // Save Medical Tests
	         if (userWebModel.getMedicalTestIds() != null) {
	             for (Integer testId : userWebModel.getMedicalTestIds()) {
	                 Optional<MedicalTest> testOpt = medicalTestRepository.findById(testId);
	                 if (testOpt.isPresent()) {
	                     AppointmentMedicalTest amt = AppointmentMedicalTest.builder()
	                             .appointment(appointment)
	                             .medicalTest(testOpt.get())
	                             .patientStatus(false)
	                             .isActive(true)
	                             .createdBy(userId)
	                             .updatedBy(userId)
	                             .build();
	                     appointmentMedicalTestRepository.save(amt);
	                 }
	             }
	         }

	         return ResponseEntity.ok(new Response(1, "success", "Appointment updated successfully."));

	     } catch (Exception e) {
	         e.printStackTrace();
	         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                 .body(new Response(0, "error", "An error occurred while updating the appointment."));
	     }
	 }

	 @Override
	 public ResponseEntity<?> getAllMedicineDetailsByHospitalId(Integer hospitalId) {
	     try {
	         List<Medicines> medicineList = medicineRepository.findByHospitalIdAndIsActiveTrue(hospitalId);

	         if (medicineList.isEmpty()) {
	             return ResponseEntity.ok(new Response(0, "success", "No medicines found for the given hospital ID."));
	         }

	         List<Map<String, Object>> result = medicineList.stream().map(med -> {
	             Map<String, Object> map = new HashMap<>();
	             map.put("id", med.getId());
	             map.put("name", med.getName());
	             return map;
	         }).collect(Collectors.toList());

	         return ResponseEntity.ok(new Response(0, "success", result));

	     } catch (Exception e) {
	         e.printStackTrace(); // Optional: use proper logging
	         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	             .body("An error occurred while fetching medicine details.");
	     }
	 }
	 @Override
	 public ResponseEntity<?> getAllMedicalTestByHospitalId(Integer hospitalId) {
	     try {
	         List<MedicalTest> medicalTests = medicalTestRepository.findByHospitalIdAndIsActiveTrue(hospitalId);

	         if (medicalTests.isEmpty()) {
	             return ResponseEntity.ok(new Response(0, "success", "No medical tests found for the given hospital ID."));
	         }

	         List<Map<String, Object>> result = medicalTests.stream().map(test -> {
	             Map<String, Object> map = new HashMap<>();
	             map.put("id", test.getId());
	             map.put("testName", test.getTestName());
	             return map;
	         }).collect(Collectors.toList());

	         return ResponseEntity.ok(new Response(0, "success", result));

	     } catch (Exception e) {
	         e.printStackTrace(); // Log properly in production
	         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	             .body("An error occurred while fetching medical tests.");
	     }
	 }
	 @Override
	 public ResponseEntity<?> getAllPatientPharamcyByHospitalIdAndDate(Integer hospitalId, String currentDate) {
	     try {
	         // Fetch all appointments for the given date
	         List<PatientAppointmentTable> appointments = patientAppointmentRepository.findByAppointmentDate(currentDate);

	         // Fetch all patient-hospital mappings for the given hospitalId
	         List<PatientMappedHospitalId> mappings = patientMappedHospitalIdRepository.findByHospitalId(hospitalId);

	         // Extract patient IDs that belong to the hospital
	         Set<Integer> patientIds = mappings.stream()
	             .map(PatientMappedHospitalId::getPatientId)
	             .collect(Collectors.toSet());

	         // Filter appointments where:
	         // - Patient is not null
	         // - Patient is mapped to the given hospital
	         // - Medicines exist for the appointment
	         List<Map<String, Object>> filteredData = appointments.stream()
	             .filter(app -> app.getPatient() != null
	                         && patientIds.contains(app.getPatient().getPatientDetailsId())
	                         && appointmentMedicineRepository.existsByAppointment(app))
	             .map(app -> {
	                 PatientDetails patient = app.getPatient();
	                 Map<String, Object> map = new HashMap<>();
	                 map.put("patientDetailsId", patient.getPatientDetailsId());
	                 map.put("patientName", patient.getPatientName());
	                 map.put("dob", patient.getDob());
	                 map.put("gender", patient.getGender());
	                 map.put("bloodGroup", patient.getBloodGroup());
	                 map.put("mobileNumber", patient.getMobileNumber());
	                 map.put("emailId", patient.getEmailId());
	                 map.put("address", patient.getAddress());
	                 return map;
	             })
	             .distinct()
	             .collect(Collectors.toList());

	         return ResponseEntity.ok(new Response(1, "success", filteredData));

	     } catch (Exception e) {
	         e.printStackTrace();
	         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	             .body(new Response(0, "error", "An error occurred while fetching patient details."));
	     }
	 }

//	 @Override
//	 public ResponseEntity<?> getAllPatientPharamcyByHospitalIdAndDate(Integer hospitalId, String currentDate) {
//	     try {
//	         List<PatientAppointmentTable> appointments = patientAppointmentRepository.findByAppointmentDate(currentDate);
//
//	         List<PatientMappedHospitalId> mappings = patientMappedHospitalIdRepository.findByHospitalId(hospitalId);
//	         List<Map<String, Object>> filteredData = appointments.stream()
//	             .filter(app -> app.getPatient() != null
//	                         && app.getPatient()..equals(hospitalId)
//	                         && appointmentMedicineRepository.existsByAppointment(app)) // Check if medicine exists
//	             .map(app -> {
//	                 PatientDetails patient = app.getPatient();
//	                 Map<String, Object> map = new HashMap<>();
//	                 map.put("patientDetailsId", patient.getPatientDetailsId());
//	                 map.put("patientName", patient.getPatientName());
//	                 map.put("dob", patient.getDob());
//	                 map.put("gender", patient.getGender());
//	                 map.put("bloodGroup", patient.getBloodGroup());
//	                 map.put("mobileNumber", patient.getMobileNumber());
//	                 map.put("emailId", patient.getEmailId());
//	                 map.put("address", patient.getAddress());
//	                 return map;
//	             })
//	             .distinct()
//	             .collect(Collectors.toList());
//
//	         return ResponseEntity.ok(new Response(1, "success", filteredData));
//
//	     } catch (Exception e) {
//	         e.printStackTrace();
//	         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//	             .body(new Response(0, "error", "An error occurred while fetching patient details."));
//	     }
//	 }

	 @Override
	 public ResponseEntity<?> getAllPatientPharamcyBypatientIdAndDate(Integer patientId, String appointmentDate) {
	     try {
	         // Fetch appointments by patientId and date
	         List<PatientAppointmentTable> appointments =
	                 patientAppointmentRepository.findByPatient_PatientDetailsIdAndAppointmentDate(patientId, appointmentDate);

	         // Set to keep unique combinations of patientDetailsId + appointmentDate
	         Set<String> uniqueKeys = new HashSet<>();

	         List<Map<String, Object>> filteredData = appointments.stream()
	                 .filter(appointment -> {
	                     String key = appointment.getPatient().getPatientDetailsId() + "_" + appointment.getAppointmentDate();
	                     return uniqueKeys.add(key); // Returns false if already exists (duplicate)
	                 })
	                 .map(appointment -> {
	                     Map<String, Object> map = new HashMap<>();

	                     // Appointment data
	                     map.put("doctorSlotId", appointment.getDoctorSlotId());
	                     map.put("daySlotId", appointment.getDaySlotId());
	                     map.put("timeSlotId", appointment.getTimeSlotId());
	                     map.put("appointmentDate", appointment.getAppointmentDate());
	                     map.put("slotStartTime", appointment.getSlotStartTime());
	                     map.put("slotEndTime", appointment.getSlotEndTime());
	                     map.put("slotTime", appointment.getSlotTime());
	                     map.put("isActive", appointment.getIsActive());
	                     map.put("createdBy", appointment.getCreatedBy());
	                     map.put("createdOn", appointment.getCreatedOn());
	                     map.put("updatedBy", appointment.getUpdatedBy());
	                     map.put("updatedOn", appointment.getUpdatedOn());
	                     map.put("appointmentStatus", appointment.getAppointmentStatus());
	                     map.put("patientNotes", appointment.getPatientNotes());
	                     map.put("doctorSlotStartTime", appointment.getDoctorSlotStartTime());
	                     map.put("doctorSlotEndTime", appointment.getDoctorSlotEndTime());
	                     map.put("appointmentType", appointment.getAppointmentType());
	                     map.put("age", appointment.getAge());
	                     map.put("dateOfBirth", appointment.getDateOfBirth());
	                     map.put("patientName", appointment.getPatientName());
	                     map.put("relationshipType", appointment.getRelationShipType());
	                     map.put("token", appointment.getToken());
	                     map.put("pharmacyStatus", appointment.getPharmacyStatus());
	                     map.put("labStatus", appointment.getLabStatus());
	                     map.put("appointmentId", appointment.getAppointmentId());

	                     // PatientDetails data
	                     PatientDetails patient = appointment.getPatient();
	                     if (patient != null) {
	                         map.put("patientDetailsId", patient.getPatientDetailsId());
	                         map.put("patientName", patient.getPatientName());
	                         map.put("dob", patient.getDob());
	                         map.put("gender", patient.getGender());
	                         map.put("bloodGroup", patient.getBloodGroup());
	                         map.put("mobileNumber", patient.getMobileNumber());
	                         map.put("emailId", patient.getEmailId());
	                         map.put("address", patient.getAddress());
	                     }

	                     // AppointmentMedicine data
	                     List<AppointmentMedicine> medicines = appointment.getAppointmentMedicines();
	                     if (medicines != null && !medicines.isEmpty()) {
	                         List<Map<String, Object>> medicineList = medicines.stream().map(med -> {
	                             Map<String, Object> medMap = new HashMap<>();
	                             medMap.put("appointmentMedicineId", med.getId());
	                             medMap.put("isActive", med.getIsActive());
	                             medMap.put("createdBy", med.getCreatedBy());
	                             medMap.put("createdOn", med.getCreatedOn());
	                             medMap.put("updatedBy", med.getUpdatedBy());
	                             medMap.put("updatedOn", med.getUpdatedOn());
	                             medMap.put("medicineStatus", med.getPatientStatus());

	                             Medicines medicine = med.getMedicine();
	                             if (medicine != null) {
	                                 medMap.put("medicineId", medicine.getId());
	                                 medMap.put("name", medicine.getName());
	                                 medMap.put("price", medicine.getPrice());
	                                 medMap.put("manufacturerName", medicine.getManufacturerName());
	                                 medMap.put("type", medicine.getType());
	                                 medMap.put("packSizeLabel", medicine.getPackSizeLabel());
	                                 medMap.put("shortComposition1", medicine.getShortComposition1());
	                                 medMap.put("shortComposition2", medicine.getShortComposition2());
	                             }

	                             return medMap;
	                         }).collect(Collectors.toList());

	                         map.put("appointmentMedicines", medicineList);
	                     }

	                     return map;
	                 })
	                 .collect(Collectors.toList());

	         return ResponseEntity.ok(new Response(1, "success", filteredData));

	     } catch (Exception e) {
	         e.printStackTrace();
	         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                 .body(new Response(0, "error", "An error occurred while fetching pharmacy appointments."));
	     }
	 }
	 @Override
	 public ResponseEntity<?> saveMedicineDetailByPharamacy(HospitalDataListWebModel userWebModel) {
	     try {
	         Integer appointmentId = userWebModel.getAppointmentId();

	         // Get Appointment
	         Optional<PatientAppointmentTable> appointmentOpt = patientAppointmentRepository.findById(appointmentId);
	         if (!appointmentOpt.isPresent()) {
	             return ResponseEntity.badRequest().body("Invalid appointment ID");
	         }
	         PatientAppointmentTable appointment = appointmentOpt.get();

	         List<HospitalDataListWebModel.MedicineDetail> medicineDetails = userWebModel.getMedicineDetails();
	         if (medicineDetails == null || medicineDetails.isEmpty()) {
	             return ResponseEntity.badRequest().body("No medicine details provided.");
	         }

	         for (HospitalDataListWebModel.MedicineDetail detail : medicineDetails) {
	             Integer medicineId = detail.getMedicineId();

	             List<AppointmentMedicine> existingMedicines =
	                 appointmentMedicineRepository.findByAppointmentAppointmentIdAndMedicineId(appointmentId, medicineId);

	             for (AppointmentMedicine existing : existingMedicines) {
	                 existing.setPatientStatus(detail.getPatientStatus());
	                 existing.setUpdatedBy(appointment.getCreatedBy()); // or session user
	                 existing.setUpdatedOn(new Date());
	                 appointmentMedicineRepository.save(existing);
	             }
	         }

	         // Update pharmacy status to COMPLETED
	         appointment.setPharmacyStatus("COMPLETED");
	         patientAppointmentRepository.save(appointment);

	         return ResponseEntity.ok(new Response(1, "success", "Medicines updated successfully"));
	     } catch (Exception e) {
	         e.printStackTrace();
	         return ResponseEntity.internalServerError().body(new Response(0, "error", "An error occurred: " + e.getMessage()));
	     }
	 }
	 @Override
	 public ResponseEntity<?> getAllPatientMedicalTestByHospitalIdAndDate(Integer hospitalId, String currentDate) {
	     try {
	         // Step 1: Get appointments by date
	    	    List<PatientAppointmentTable> appointments = patientAppointmentRepository.findByAppointmentDate(currentDate);

		         // Fetch all patient-hospital mappings for the given hospitalId
		         List<PatientMappedHospitalId> mappings = patientMappedHospitalIdRepository.findByHospitalId(hospitalId);

		         // Extract patient IDs that belong to the hospital
		         Set<Integer> patientIds = mappings.stream()
		             .map(PatientMappedHospitalId::getPatientId)
		             .collect(Collectors.toSet());

		         // Filter appointments where:
		         // - Patient is not null
		         // - Patient is mapped to the given hospital
		         // - Medicines exist for the appointment
		         List<Map<String, Object>> filteredData = appointments.stream()
		             .filter(app -> app.getPatient() != null
		                         && patientIds.contains(app.getPatient().getPatientDetailsId())
		                         && appointmentMedicineRepository.existsByAppointment(app))
	             .map(app -> {
	                 PatientDetails patient = app.getPatient();
	                 Map<String, Object> map = new HashMap<>();
	                 map.put("patientDetailsId", patient.getPatientDetailsId());
	                 map.put("patientName", patient.getPatientName());
	                 map.put("dob", patient.getDob());
	                 map.put("gender", patient.getGender());
	                 map.put("bloodGroup", patient.getBloodGroup());
	                 map.put("mobileNumber", patient.getMobileNumber());
	                 map.put("emailId", patient.getEmailId());
	                 map.put("address", patient.getAddress());
	                 return map;
	             })
	             .distinct() // Optional to avoid duplicate patient maps
	             .collect(Collectors.toList());

	         return ResponseEntity.ok(new Response(1, "success", filteredData));

	     } catch (Exception e) {
	         e.printStackTrace();
	         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	             .body(new Response(0, "error", "An error occurred while fetching patient details."));
	     }
	 }

	@Override
	public ResponseEntity<?> getAllPatientMedicalTestBypatientIdAndDate(Integer patientId, String appointmentDate) {
		 try {
	         // Fetch appointments by patientId and date
	         List<PatientAppointmentTable> appointments =
	                 patientAppointmentRepository.findByPatient_PatientDetailsIdAndAppointmentDate(patientId, appointmentDate);

	         // Set to keep unique combinations of patientDetailsId + appointmentDate
	         Set<String> uniqueKeys = new HashSet<>();

	         List<Map<String, Object>> filteredData = appointments.stream()
	                 .filter(appointment -> {
	                     String key = appointment.getPatient().getPatientDetailsId() + "_" + appointment.getAppointmentDate();
	                     return uniqueKeys.add(key); // Returns false if already exists (duplicate)
	                 })
	                 .map(appointment -> {
	                     Map<String, Object> map = new HashMap<>();

	                     // Appointment data
	                     map.put("doctorSlotId", appointment.getDoctorSlotId());
	                     map.put("daySlotId", appointment.getDaySlotId());
	                     map.put("timeSlotId", appointment.getTimeSlotId());
	                     map.put("appointmentDate", appointment.getAppointmentDate());
	                     map.put("slotStartTime", appointment.getSlotStartTime());
	                     map.put("slotEndTime", appointment.getSlotEndTime());
	                     map.put("slotTime", appointment.getSlotTime());
	                     map.put("isActive", appointment.getIsActive());
	                     map.put("createdBy", appointment.getCreatedBy());
	                     map.put("createdOn", appointment.getCreatedOn());
	                     map.put("updatedBy", appointment.getUpdatedBy());
	                     map.put("updatedOn", appointment.getUpdatedOn());
	                     map.put("appointmentStatus", appointment.getAppointmentStatus());
	                     map.put("patientNotes", appointment.getPatientNotes());
	                     map.put("doctorSlotStartTime", appointment.getDoctorSlotStartTime());
	                     map.put("doctorSlotEndTime", appointment.getDoctorSlotEndTime());
	                     map.put("appointmentType", appointment.getAppointmentType());
	                     map.put("age", appointment.getAge());
	                     map.put("dateOfBirth", appointment.getDateOfBirth());
	                     map.put("patientName", appointment.getPatientName());
	                     map.put("relationshipType", appointment.getRelationShipType());
	                     map.put("token", appointment.getToken());
	                     map.put("pharmacyStatus", appointment.getPharmacyStatus());
	                     map.put("labStatus", appointment.getLabStatus());
	                     map.put("appointmentId", appointment.getAppointmentId());

	                     // PatientDetails data
	                     PatientDetails patient = appointment.getPatient();
	                     if (patient != null) {
	                         map.put("patientDetailsId", patient.getPatientDetailsId());
	                         map.put("patientName", patient.getPatientName());
	                         map.put("dob", patient.getDob());
	                         map.put("gender", patient.getGender());
	                         map.put("bloodGroup", patient.getBloodGroup());
	                         map.put("mobileNumber", patient.getMobileNumber());
	                         map.put("emailId", patient.getEmailId());
	                         map.put("address", patient.getAddress());
	                     }

	                     // AppointmentMedicine data
	                     List<AppointmentMedicalTest> medicines = appointment.getAppointmentMedicalTests();
	                     if (medicines != null && !medicines.isEmpty()) {
	                         List<Map<String, Object>> medicineList = medicines.stream().map(med -> {
	                             Map<String, Object> medMap = new HashMap<>();
	                             medMap.put("appointmentMedicineId", med.getId());
	                             medMap.put("isActive", med.getIsActive());
	                             medMap.put("createdBy", med.getCreatedBy());
	                             medMap.put("createdOn", med.getCreatedOn());
	                             medMap.put("updatedBy", med.getUpdatedBy());
	                             medMap.put("updatedOn", med.getUpdatedOn());
	                             medMap.put("medicineStatus", med.getPatientStatus());

	                             MedicalTest medicine = med.getMedicalTest();
	                             if (medicine != null) {
	                                 medMap.put("medicineId", medicine.getId());
	                                 medMap.put("testname", medicine.getTestName());
	                                 medMap.put("mrp", medicine.getMrp());
	                                 medMap.put("department", medicine.getDepartment());
	                                 medMap.put("isActive", medicine.getIsActive());
	                                
	                             }

	                             return medMap;
	                         }).collect(Collectors.toList());

	                         map.put("appointmentMedicialTest", medicineList);
	                     }

	                     return map;
	                 })
	                 .collect(Collectors.toList());

	         return ResponseEntity.ok(new Response(1, "success", filteredData));

	     } catch (Exception e) {
	         e.printStackTrace();
	         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                 .body(new Response(0, "error", "An error occurred while fetching pharmacy appointments."));
	     }
	}




}

package com.annular.healthCare.service.serviceImpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
import com.annular.healthCare.repository.AppointmentMedicalTestRepository;
import com.annular.healthCare.repository.AppointmentMedicineRepository;
import com.annular.healthCare.repository.MedicalTestRepository;
import com.annular.healthCare.repository.MedicinesRepository;
import com.annular.healthCare.repository.PatientAppoitmentTablerepository;
import com.annular.healthCare.repository.PatientDetailsRepository;
import com.annular.healthCare.service.DoctorAppoitmentService;
import com.annular.healthCare.webModel.HospitalDataListWebModel;

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
	                details.put("hospitalId", patient.getHospitalId());
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
	         patientAppointmentRepository.save(appointment);

	         Integer userId = userWebModel.getUserId(); // Who is saving this

	         // Save Medicines
	         if (userWebModel.getMedicineIds() != null) {
	             for (Integer medicineId : userWebModel.getMedicineIds()) {
	                 Optional<Medicines> medicineOpt = medicineRepository.findById(medicineId);
	                 if (medicineOpt.isPresent()) {
	                     AppointmentMedicine am = AppointmentMedicine.builder()
	                             .appointment(appointment)
	                             .medicine(medicineOpt.get())
	                             .isActive(true)
	                             .createdBy(userId)
	                             .updatedBy(userId)
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



}

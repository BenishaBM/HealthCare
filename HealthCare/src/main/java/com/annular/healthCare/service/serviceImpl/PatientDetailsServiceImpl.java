package com.annular.healthCare.service.serviceImpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.annular.healthCare.Response;
import com.annular.healthCare.Util.Utility;
import com.annular.healthCare.model.MediaFileCategory;
import com.annular.healthCare.model.PatientDetails;
import com.annular.healthCare.model.User;
import com.annular.healthCare.repository.PatientDetailsRepository;
import com.annular.healthCare.repository.UserRepository;
import com.annular.healthCare.service.MediaFileService;
import com.annular.healthCare.service.PatientDetailsService;
import com.annular.healthCare.webModel.FileInputWebModel;
import com.annular.healthCare.webModel.PatientDetailsWebModel;

@Service
public class PatientDetailsServiceImpl implements PatientDetailsService{
	
	
	public static final Logger logger = LoggerFactory.getLogger(PatientDetailsServiceImpl.class);
	
	@Autowired
	PatientDetailsRepository patientDetailsRepository;
	
	@Autowired
	MediaFileService mediaFilesService;
	
	@Autowired
	UserRepository userRepository;

	@Override
	public ResponseEntity<?> register(PatientDetailsWebModel userWebModel) {
	    try {
	        logger.info("Registering patient: {}", userWebModel.getPatientName());

	        if (userWebModel.getPatientName() == null || userWebModel.getMobileNumber() == null) {
	            return ResponseEntity.badRequest().body(
	                new Response(0, "Fail", "Patient name and mobile number are required")
	            );
	        }

	        PatientDetails newPatient = PatientDetails.builder()
	                .patientName(userWebModel.getPatientName())
	                .dob(userWebModel.getDob())
	                .age(userWebModel.getAge())
	                .gender(userWebModel.getGender())
	                .bloodGroup(userWebModel.getBloodGroup())
	                .mobileNumber(userWebModel.getMobileNumber())
	                .emailId(userWebModel.getEmailId())
	                .address(userWebModel.getAddress())
	                .currentAddress(userWebModel.getCurrentAddress())
	                .emergencyContact(userWebModel.getEmergencyContact())
	                .hospitalId(userWebModel.getHospitalId())
	                .purposeOfVisit(userWebModel.getPurposeOfVisit())
	                .doctorId(userWebModel.getDoctorId())
	                .userIsActive(true)
	                .createdBy(userWebModel.getCreatedBy())
	                .userCreatedOn(new Date())
	                .previousMedicalHistory(userWebModel.getPreviousMedicalHistory())
	                .insuranceDetails(userWebModel.getInsuranceDetails())
	                .insurerName(userWebModel.getInsurerName())
	                .insuranceProvider(userWebModel.getInsuranceProvider())
	                .policyNumber(userWebModel.getPolicyNumber())
	                .disability(userWebModel.getDisability())
	                .build();

	        // Save patient first to generate ID
	        PatientDetails savedPatient = patientDetailsRepository.save(newPatient);

	        // Save media files if any
	        if (!Utility.isNullOrEmptyList(userWebModel.getFiles())) {
	            FileInputWebModel fileInput = FileInputWebModel.builder()
	                    .category(MediaFileCategory.patient) // Define a suitable enum value
	                    .categoryRefId(savedPatient.getPatientDetailsId())
	                    .files(userWebModel.getFiles())
	                    .build();

	            User userFromDB = userRepository.findById(userWebModel.getCreatedBy()).orElse(null); // Or handle accordingly

	            mediaFilesService.saveMediaFiles(fileInput, userFromDB);
	        }

	        return ResponseEntity.ok(new Response(1, "Success", "Patient registered successfully"));

	    } catch (Exception e) {
	        logger.error("Registration failed", e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(0, "Fail", "Something went wrong during registration"));
	    }
	}

	@Override
	public ResponseEntity<?> getAllPatientDetails(Integer hospitalId) {
	    Map<String, Object> response = new HashMap<>();
	    try {
	        List<PatientDetails> patients = patientDetailsRepository.findByHospitalId(hospitalId);
	        
	        if (patients.isEmpty()) {
	            response.put("status", 0);
	            response.put("message", "No patients found for the given hospital.");
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	        }

	        List<Map<String, Object>> patientList = new ArrayList<>();

	        for (PatientDetails patient : patients) {
	            Map<String, Object> patientData = new HashMap<>();
	            patientData.put("patientDetailsId", patient.getPatientDetailsId());
	            patientData.put("patientName", patient.getPatientName());
	            patientData.put("dob", patient.getDob());
	            patientData.put("gender", patient.getGender());
	            patientData.put("bloodGroup", patient.getBloodGroup());
	            patientData.put("mobileNumber", patient.getMobileNumber());
	            patientData.put("emailId", patient.getEmailId());
	            patientData.put("address", patient.getAddress());
	            patientData.put("currentAddress", patient.getCurrentAddress());
	            patientData.put("emergencyContact", patient.getEmergencyContact());
	            patientData.put("hospitalId", patient.getHospitalId());
	            patientData.put("purposeOfVisit", patient.getPurposeOfVisit());
	            patientData.put("doctorId", patient.getDoctorId());
	            patientData.put("userIsActive", patient.getUserIsActive());
	            patientData.put("createdBy", patient.getCreatedBy());
	            patientData.put("userCreatedOn", patient.getUserCreatedOn());

	            patientList.add(patientData);
	        }

	        response.put("status", 1);
	        response.put("message", "Success");
	        response.put("data", patientList);
	        return ResponseEntity.ok(response);

	    } catch (Exception e) {
	        response.put("status", 0);
	        response.put("message", "Error retrieving patient details");
	        response.put("error", e.getMessage());
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	    }
	}


}

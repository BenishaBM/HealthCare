package com.annular.healthCare.controller;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.annular.healthCare.Response;
import com.annular.healthCare.service.DoctorAppoitmentService;
import com.annular.healthCare.webModel.HospitalDataListWebModel;

@RestController
@RequestMapping("/doctorAppoitmentHistory")
public class DoctorAppointmentController {
	
	public static final Logger logger = LoggerFactory.getLogger(DoctorAppointmentController.class);
	
	@Autowired
	DoctorAppoitmentService doctorAppoitmentService;
	
	@GetMapping("getAllPatientAppointmentByFilter") // Corrected spelling in endpoint
	public ResponseEntity<?> getAllPatientAppointmentByFilter(
	        @RequestParam("appointmentDate") String  appointmentDate, 
	        @RequestParam("appointmentType") String appointmentType,@RequestParam("doctorId")Integer doctorId) {
	    try {
	        logger.info("getAllPatientAppointmentByFilter request for createdOn: {}, appointmentType: {}", appointmentDate, appointmentType);
	        
	       
	        return doctorAppoitmentService.getAllPatientAppointmentByFilter(appointmentDate, appointmentType, doctorId);
	    } catch (Exception e) {
	        logger.error("getAllPatientAppointmentByFilter Method Exception: {}", e.getMessage(), e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(-1, "Fail", e.getMessage()));
	    }
	}

	@PostMapping("saveDoctorAppoitment")
	public ResponseEntity<?> doctorAppoitmentService(@RequestBody HospitalDataListWebModel userWebModel) {
	    try {
	        // Call the service to perform the update
	        return doctorAppoitmentService.saveDoctorAppoitment(userWebModel);
	    } catch (Exception e) {
	        // Handle errors and return a meaningful response
	        logger.error("doctorAppoitmentService Method Exception: {}", e.getMessage(), e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(-1, "Fail", e.getMessage()));
	    }
	}


	@GetMapping("getAllMedicineDetailsByHospitalId") // Corrected spelling in endpoint
	public ResponseEntity<?> getAllMedicineDetailsByHospitalId(
	        @RequestParam("hospitalId") Integer  hospitalId) {
	    try {
	        logger.info("getAllMedicineDetailsByHospitalId request for createdOn: {}, appointmentType: {}", hospitalId);
	        
	       
	        return doctorAppoitmentService.getAllMedicineDetailsByHospitalId(hospitalId);
	    } catch (Exception e) {
	        logger.error("getAllMedicineDetailsByHospitalId Method Exception: {}", e.getMessage(), e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(-1, "Fail", e.getMessage()));
	    }
	}
	
	@GetMapping("getAllMedicalTestByHospitalId") // Corrected spelling in endpoint
	public ResponseEntity<?> getAllMedicalTestByHospitalId(
	        @RequestParam("hospitalId") Integer  hospitalId) {
	    try {
	        logger.info("getAllMedicalTestByHospitalId request for createdOn: {}, appointmentType: {}", hospitalId);
	        
	       
	        return doctorAppoitmentService.getAllMedicalTestByHospitalId(hospitalId);
	    } catch (Exception e) {
	        logger.error("getAllMedicalTestByHospitalId Method Exception: {}", e.getMessage(), e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(-1, "Fail", e.getMessage()));
	    }
	}
}


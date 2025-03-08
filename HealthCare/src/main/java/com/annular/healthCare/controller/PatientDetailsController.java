package com.annular.healthCare.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.annular.healthCare.Response;
import com.annular.healthCare.service.PatientDetailsService;
import com.annular.healthCare.webModel.PatientDetailsWebModel;
import com.annular.healthCare.webModel.UserWebModel;

@RestController
@RequestMapping("/patientDetails")
public class PatientDetailsController {
	
	public static final Logger logger = LoggerFactory.getLogger(PatientDetailsController.class);
	
	@Autowired
	PatientDetailsService patientDetailService;
	
	@PostMapping("register")
	public ResponseEntity<?> userRegister(@RequestBody PatientDetailsWebModel userWebModel) {
		try {
			logger.info("User register controller start");
			return patientDetailService.register(userWebModel);
		} catch (Exception e) {
			logger.error("userRegister Method Exception {}" + e);
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new Response(-1, "Fail", e.getMessage()));
		}

	}
	

	@GetMapping("getAllPatientDetails")
	public ResponseEntity<?> getAllPatientDetails(@RequestParam("hospitalId")Integer hospitalId) {
		try {
			logger.info("getAllPatientDetails request for userType: {}",  hospitalId);
			return patientDetailService.getAllPatientDetails(hospitalId);
		} catch (Exception e) {
			logger.error("getAllPatientDetails Method Exception: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new Response(-1, "Fail", e.getMessage()));
		}
	}
	

}

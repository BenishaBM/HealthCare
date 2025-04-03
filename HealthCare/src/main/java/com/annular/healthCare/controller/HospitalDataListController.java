package com.annular.healthCare.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.annular.healthCare.Response;
import com.annular.healthCare.service.AuthService;
import com.annular.healthCare.service.HospitalDataListService;
import com.annular.healthCare.webModel.HospitalDataListWebModel;
import com.annular.healthCare.webModel.UserWebModel;

@RestController
@RequestMapping("/user")
public class HospitalDataListController {
	
	public static final Logger logger = LoggerFactory.getLogger(HospitalDataListController.class);
	
	@Autowired
	HospitalDataListService authService;
	
	@PostMapping("register")
	public ResponseEntity<?> userRegister(@RequestBody HospitalDataListWebModel userWebModel) {
		try {
			logger.info("User register controller start");
			return authService.register(userWebModel);
		} catch (Exception e) {
			logger.error("userRegister Method Exception {}" + e);
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new Response(-1, "Fail", e.getMessage()));
		}

	}

	@GetMapping("getHospitalDataByUserTypeAndHospitalId")
	public ResponseEntity<?> getHospitalDataByUserTypeAndHospitalId(@RequestParam("userType") String userType,@RequestParam("hospitalId")Integer hospitalId) {
		try {
			logger.info("getHospitalDataByUserTypeAndHospitalId request for userType: {}", userType);
			return authService.getHospitalDataByUserTypeAndHospitalId(userType,hospitalId);
		} catch (Exception e) {
			logger.error("getUserDetailsByUserType Method Exception: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new Response(-1, "Fail", e.getMessage()));
		}
	}
	
	@GetMapping("getHospitalDataByUserId")
	public ResponseEntity<?> getHospitalDataByUserId(@RequestParam("hospitalDataId")Integer hospitalDataId) {
		try {
			return authService.getHospitalDataByUserId(hospitalDataId);
		} catch (Exception e) {
			logger.error("getUserDetailsByUserType Method Exception: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new Response(-1, "Fail", e.getMessage()));
		}
	}
	
	@PostMapping("updateHospitalDataByUserId")
	public ResponseEntity<?> updateHospitalDataByUserId(@RequestBody HospitalDataListWebModel userWebModel) {
	    try {
	        // Call the service to perform the update
	        return authService.updateHospitalDataByUserId(userWebModel);
	    } catch (Exception e) {
	        // Handle errors and return a meaningful response
	        logger.error("updateHospitalDataByUserId Method Exception: {}", e.getMessage(), e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(-1, "Fail", e.getMessage()));
	    }
	}

	@DeleteMapping("deleteHospitalDataByUserId")
	public ResponseEntity<?> deleteHospitalDataByUserId(@RequestParam("hospitalDataId")Integer hospitalDataId) {
	    try {
	        // Call the service to perform the update
	        return authService.deleteHospitalDataByUserId(hospitalDataId);
	    } catch (Exception e) {
	        // Handle errors and return a meaningful response
	        logger.error("deleteHospitalDataByUserId Method Exception: {}", e.getMessage(), e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(-1, "Fail", e.getMessage()));
	    }
	}
	
	@GetMapping("getByHopitalName")
	public ResponseEntity<?> getByHopitalName() {
	    try {
	        // Call the service to perform the update
	        return authService.getByHopitalName();
	    } catch (Exception e) {
	        // Handle errors and return a meaningful response
	        logger.error("getByHopitalName Method Exception: {}", e.getMessage(), e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(-1, "Fail", e.getMessage()));
	    }
	}
	
	
	@GetMapping("getByDoctorSpeciallity")
	public ResponseEntity<?> getByDoctorSpeciallity() {
	    try {
	        // Call the service to perform the update
	        return authService.getByDoctorSpeciallity();
	    } catch (Exception e) {
	        // Handle errors and return a meaningful response
	        logger.error("getByDoctorSpeciallity Method Exception: {}", e.getMessage(), e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(-1, "Fail", e.getMessage()));
	    }
	}
	

	
	
	

}

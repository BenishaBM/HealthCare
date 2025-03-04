package com.annular.healthCare.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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


}

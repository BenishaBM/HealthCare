package com.annular.healthCare.controller;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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
import com.annular.healthCare.service.MedicalTestConfigService;
import com.annular.healthCare.webModel.HospitalDataListWebModel;
import com.annular.healthCare.webModel.MedicalTestConfigWebModel;

@RestController
@RequestMapping("/medicalTest")
public class MedicalTestConfigController {

	
	public static final Logger logger = LoggerFactory.getLogger(MedicalTestConfigController.class);
	
	@Autowired
	MedicalTestConfigService medicalTestConfigService;
	
	@PostMapping("saveMedicalTestName")
	public ResponseEntity<?> saveMedicalTestName(@RequestBody MedicalTestConfigWebModel medicalTestConfigWebModel) {
		try {
			logger.info("saveMedicalTestName controller start");
			return medicalTestConfigService.saveMedicalTestName(medicalTestConfigWebModel);
		} catch (Exception e) {
			logger.error("saveMedicalTestName Method Exception {}" + e);
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new Response(-1, "Fail", e.getMessage()));
		}

	}
	
	@GetMapping("getAllMedicalTestNameByHospitalId")
	public ResponseEntity<?> getAllMedicalTestNameByHospitalId(@RequestParam("hospitalId")Integer hospitalId) {
		try {
			logger.info("getAllMedicalTestNameByHospitalId controller start");
			return medicalTestConfigService.getAllMedicalTestNameByHospitalId(hospitalId);
		} catch (Exception e) {
			logger.error("getAllMedicalTestNameByHospitalId Method Exception {}" + e);
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new Response(-1, "Fail", e.getMessage()));
		}

	}
	
	@GetMapping("getMedicalTestNameById")
	public ResponseEntity<?> getMedicalTestNameById(@RequestParam("id")Integer id) {
		try {
			logger.info("getMedicalTestNameById controller start");
			return medicalTestConfigService.getMedicalTestNameById(id);
		} catch (Exception e) {
			logger.error("getMedicalTestNameById Method Exception {}" + e);
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new Response(-1, "Fail", e.getMessage()));
		}

	}
	
	@PostMapping("updateMedicalTestName")
	public ResponseEntity<?> updateMedicalTestName(@RequestBody MedicalTestConfigWebModel medicalTestConfigWebModel) {
		try {
			logger.info("updateMedicalTestName controller start");
			return medicalTestConfigService.updateMedicalTestName(medicalTestConfigWebModel);
		} catch (Exception e) {
			logger.error("updateMedicalTestName Method Exception {}" + e);
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new Response(-1, "Fail", e.getMessage()));
		}

	}
	

	@DeleteMapping("deleteMedicalTestNameById")
	public ResponseEntity<?> deleteMedicalTestNameById(@RequestParam("id")Integer id) {
		try {
			logger.info("deleteMedicalTestNameById controller start");
			return medicalTestConfigService.deleteMedicalTestNameById(id);
		} catch (Exception e) {
			logger.error("deleteMedicalTestNameById Method Exception {}" + e);
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new Response(-1, "Fail", e.getMessage()));
		}
}
	
	@DeleteMapping("deleteDepartmentById")
	public ResponseEntity<?> deleteDepartmentById(@RequestParam("id")Integer id) {
		try {
			logger.info("deleteDepartmentById controller start");
			return medicalTestConfigService.deleteDepartmentById(id);
		} catch (Exception e) {
			logger.error("deleteDepartmentById Method Exception {}" + e);
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new Response(-1, "Fail", e.getMessage()));
		}
}
	
	
	@PostMapping("saveMedicalTestSlotByDepartmentId")
	public ResponseEntity<?> saveMedicalTestSlotByDepartmentId(@RequestBody MedicalTestConfigWebModel medicalTestConfigWebModel) {
		try {
			logger.info("saveMedicalTestSlotByDepartmentId controller start");
			return medicalTestConfigService.saveMedicalTestSlotByDepartmentId(medicalTestConfigWebModel);
		} catch (Exception e) {
			logger.error("saveMedicalTestSlotByDepartmentId Method Exception {}" + e);
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new Response(-1, "Fail", e.getMessage()));
		}

	}
	
	@GetMapping("getMedicalTestSlotByDepartmentId")
	public ResponseEntity<?> getMedicalTestSlotByDepartmentId(@RequestParam("id") Integer id) {
		try {
			logger.info("getMedicalTestSlotByDepartmentId controller start");
			return medicalTestConfigService.getMedicalTestSlotByDepartmentId(id);
		} catch (Exception e) {
			logger.error("getMedicalTestSlotByDepartmentId Method Exception {}" + e);
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new Response(-1, "Fail", e.getMessage()));
		}

	}
	
	@PostMapping("saveMedicalTestOvverride")
	public ResponseEntity<?> saveMedicalTestOvverride(@RequestBody MedicalTestConfigWebModel medicalTestConfigWebModel) {
		try {
			logger.info("saveMedicalTestOvverride controller start");
			return medicalTestConfigService.saveMedicalTestOvverride(medicalTestConfigWebModel);
		} catch (Exception e) {
			logger.error("saveMedicalTestOvverride Method Exception {}" + e);
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new Response(-1, "Fail", e.getMessage()));
		}

	}
	
	@PostMapping("addTimeSlotByMedicalTest")
	public ResponseEntity<?> addTimeSlotByMedicalTest(@RequestBody MedicalTestConfigWebModel medicalTestConfigWebModel) {
	    try {
	        // Call the service to perform the update
	        return medicalTestConfigService.addTimeSlotByMedicalTest(medicalTestConfigWebModel);
	    } catch (Exception e) {
	        // Handle errors and return a meaningful response
	        logger.error("addTimeSlotByMedicalTest Method Exception: {}", e.getMessage(), e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(-1, "Fail", e.getMessage()));
	    }
	}
	
	@DeleteMapping("deleteSlotByMedicalTestById")
	public ResponseEntity<?> deleteSlotByMedicalTestById(@RequestParam("id") Integer id) {
	    try {
	        // Call the service to perform the update
	        return medicalTestConfigService.deleteSlotByMedicalTestById(id);
	    } catch (Exception e) {
	        // Handle errors and return a meaningful response
	        logger.error("deleteSlotByMedicalTestById Method Exception: {}", e.getMessage(), e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(-1, "Fail", e.getMessage()));
	    }
	}
	
//	@GetMapping("getMedicalTestSlotById")
//	public ResponseEntity<?> getMedicalTestSlotById(@RequestParam("id") Integer id, 
//	                                           @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
//	    try {
//	        return medicalTestConfigService.getMedicalTestSlotById(id, date);
//	    } catch (Exception e) {
//	        logger.error("getMedicalTestSlotById Method Exception: {}", e.getMessage(), e);
//	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//	                .body(new Response(-1, "Fail", e.getMessage()));
//	    }
//	}
}

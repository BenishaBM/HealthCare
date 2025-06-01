package com.annular.healthCare.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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

	@RequestMapping(path = "/register", method = RequestMethod.POST, consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	public ResponseEntity<?> userRegister(@ModelAttribute PatientDetailsWebModel userWebModel) {
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
	
	@RequestMapping(path = "/adminPatientRegister", method = RequestMethod.POST, consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	public ResponseEntity<?> adminPatientRegister(@ModelAttribute PatientDetailsWebModel userWebModel) {
		try {
			logger.info("User register controller start");
			return patientDetailService.adminPatientRegister(userWebModel);
		} catch (Exception e) {
			logger.error("userRegister Method Exception {}" + e);
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new Response(-1, "Fail", e.getMessage()));
		}

	}
	@PutMapping(path = "/updatePatientDetails", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	public ResponseEntity<?> updatePatientDetails(@ModelAttribute PatientDetailsWebModel userWebModel) {
	    try {
	        logger.info("User update controller start");
	        return patientDetailService.updatePatientDetails(userWebModel); // consider renaming to "update" in service
	    } catch (Exception e) {
	        logger.error("updateUser Method Exception: {}", e.getMessage(), e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(-1, "Fail", e.getMessage()));
	    }
	}


	@GetMapping("getAllPatientDetails")
	public ResponseEntity<?> getAllPatientDetails(@RequestParam("hospitalId") Integer hospitalId) {
		try {
			logger.info("getAllPatientDetails request for userType: {}", hospitalId);
			return patientDetailService.getAllPatientDetails(hospitalId);
		} catch (Exception e) {
			logger.error("getAllPatientDetails Method Exception: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new Response(-1, "Fail", e.getMessage()));
		}
	}
	
	@GetMapping("getPatientDetailsById")
	public ResponseEntity<?> getPatientDetailsById(@RequestParam("patientDetailsId") Integer patientDetailsID) {
		try {
			logger.info("getAllPatientDetails request for userType: {}", patientDetailsID);
			return patientDetailService.getPatientDetailsById(patientDetailsID);
		} catch (Exception e) {
			logger.error("getPatientDetailsById Method Exception: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new Response(-1, "Fail", e.getMessage()));
		}
	}
	
	
	@GetMapping("getDoctorListByHospitalId")
	public ResponseEntity<?> getDoctorListByHospitalId(@RequestParam("hospitalId") Integer hospitalId) {
		try {
			logger.info("getDoctorListByHospitalId request for userType: {}", hospitalId);
			return patientDetailService.getDoctorListByHospitalId(hospitalId);
		} catch (Exception e) {
			logger.error("getDoctorListByHospitalId Method Exception: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new Response(-1, "Fail", e.getMessage()));
		}
	}
	
//	  @DeleteMapping("/deleteMediaFilesById")
//	    public ResponseEntity<?> deleteMediaFilesById(@RequestParam("fileId")Integer fileId) {
//	        try {
//	            boolean isDeleted = patientDetailService.deleteMediaFilesById(fileId);
//	            if (isDeleted) {
//	                return ResponseEntity.ok(new Response(1, "Success","deleteMediaFilesById successfully."));
//	            } else {
//	                return ResponseEntity.badRequest().body(new Response(-1, "fail","Failed to delete post"));
//	            }
//	        } catch (Exception e) {
//	            logger.error("deleteQuestionById Method Exception -> {}", e.getMessage());
//	            e.printStackTrace();
//	            return ResponseEntity.internalServerError().body(new Response(-1, "Fail", e.getMessage()));
//	        }
//	    }
	  
		@PostMapping("patientAppoitmentByOffline")
		public ResponseEntity<?> patientAppoitmentByOffline(@RequestBody PatientDetailsWebModel patientDetailsWebModel) {
			try {
				logger.info("patientAppoitmentByOffline request for userType: {}");
				return patientDetailService.patientAppoitmentByOffline(patientDetailsWebModel);
			} catch (Exception e) {
				logger.error("patientAppoitmentByOffline Method Exception: {}", e.getMessage(), e);
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body(new Response(-1, "Fail", e.getMessage()));
			}
		}
		@GetMapping("getPatientDetailsByMobileNumberAndHospitalId")
		public ResponseEntity<?> getPatientDetailsByMobileNumberAndHospitalId(@RequestParam("phoneNumber") String phoneNumber,@RequestParam("hospitalId")Integer hospitalId) {
			try {
				logger.info("getPatientDetailsByMobileNumberAndHospitalId request for userType: {}", hospitalId);
				return patientDetailService.getPatientDetailsByMobileNumberAndHospitalId(phoneNumber,hospitalId);
			} catch (Exception e) {
				logger.error("getPatientDetailsByMobileNumberAndHospitalId Method Exception: {}", e.getMessage(), e);
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body(new Response(-1, "Fail", e.getMessage()));
			}
		}
		
		@GetMapping("getPatientDetailsByMobileNumber")
		public ResponseEntity<?> getPatientDetailsByMobileNumber(@RequestParam("mobileNumber") String mobileNumber) {
			try {
				logger.info("getPatientDetailsByMobileNumber request for userType: {}", mobileNumber);
				return patientDetailService.getPatientDetailsByMobileNumber(mobileNumber);
			} catch (Exception e) {
				logger.error("getPatientDetailsByMobileNumber Method Exception: {}", e.getMessage(), e);
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body(new Response(-1, "Fail", e.getMessage()));
			}
		}
		
		@PostMapping("patientAppoitmentByOnline")
		public ResponseEntity<?> patientAppoitmentByOnline(@RequestBody PatientDetailsWebModel patientDetailsWebModel) {
			try {
				logger.info("patientAppoitmentByOnline request for userType: {}");
				return patientDetailService.patientAppoitmentByOnline(patientDetailsWebModel);
			} catch (Exception e) {
				logger.error("patientAppoitmentByOnline Method Exception: {}", e.getMessage(), e);
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body(new Response(-1, "Fail", e.getMessage()));
			}
		}
		
		@RequestMapping(path = "/patientSubChildRegister", method = RequestMethod.POST, consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
		public ResponseEntity<?> patientSubChildRegister(@ModelAttribute PatientDetailsWebModel userWebModel) {
			try {
				logger.info("patientSubChildRegister register controller start");
				return patientDetailService.patientSubChildRegister(userWebModel);
			} catch (Exception e) {
				logger.error("patientSubChildRegister Method Exception {}" + e);
				e.printStackTrace();
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body(new Response(-1, "Fail", e.getMessage()));
			}

		}

		@GetMapping("getPatientRelationShipDetails")
		public ResponseEntity<?> getPatientRelationShipDetailsgetPatientRelationShipDetails(@RequestParam("patientDetailsId") Integer patientDetailsId,@RequestParam("relationShipType")String relationshipType) {
			try {
				logger.info("getPatientRelationShipDetails request for userType: {}", patientDetailsId,relationshipType);
				return patientDetailService.getPatientRelationShipDetails(patientDetailsId,relationshipType);
			} catch (Exception e) {
				logger.error("getPatientRelationShipDetails Method Exception: {}", e.getMessage(), e);
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body(new Response(-1, "Fail", e.getMessage()));
			}
		}
		
		@GetMapping("getPatientMappedDetailsById")
		public ResponseEntity<?> getPatientMappedDetailsById(@RequestParam("patientId") Integer patientDetailsId,@RequestParam("hospitalId")Integer hospitalId) {
			try {
				logger.info("getPatientMappedDetailsById request for userType: {}", patientDetailsId,hospitalId);
				return patientDetailService.getPatientMappedDetailsById(patientDetailsId,hospitalId);
			} catch (Exception e) {
				logger.error("getPatientMappedDetailsById Method Exception: {}", e.getMessage(), e);
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body(new Response(-1, "Fail", e.getMessage()));
			}
		}
		
		@GetMapping("getDoctorfeesById")
		public ResponseEntity<?> getDoctorfeesById(@RequestParam("userId") Integer userId) {
			try {
				logger.info("getDoctorfeesById request for userType: {}", userId);
				return patientDetailService.getDoctorfeesById(userId);
			} catch (Exception e) {
				logger.error("getDoctorfeesById Method Exception: {}", e.getMessage(), e);
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body(new Response(-1, "Fail", e.getMessage()));
			}
		}
		
		@GetMapping("getAllAddressData")
		public ResponseEntity<?> getAllAddressData() {
			try {
				logger.info("getAllAddressData request for userType: {}");
				return patientDetailService.getAllAddressData();
			} catch (Exception e) {
				logger.error("getAllAddressData Method Exception: {}", e.getMessage(), e);
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body(new Response(-1, "Fail", e.getMessage()));
			}
		}
		@GetMapping("getDropDownByLabList")
		public ResponseEntity<?> getDropDownByLabList() {
			try {
				logger.info("getDropDownByLabList request for userType: {}");
				return patientDetailService.getDropDownByLabList();
			} catch (Exception e) {
				logger.error("getDropDownByLabList Method Exception: {}", e.getMessage(), e);
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body(new Response(-1, "Fail", e.getMessage()));
			}
		}
		
		@GetMapping("getDropDownBySupportStaffList")
		public ResponseEntity<?> getDropDownBySupportStaffList() {
			try {
				logger.info("getDropDownBySupportStaffList request for userType: {}");
				return patientDetailService.getDropDownBySupportStaffList();
			} catch (Exception e) {
				logger.error("getDropDownBySupportStaffList Method Exception: {}", e.getMessage(), e);
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body(new Response(-1, "Fail", e.getMessage()));
			}
		}
		
}

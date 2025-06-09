package com.annular.healthCare.controller;

import java.util.Date;

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
	

	@GetMapping("getAllPatientAppointment") // Corrected spelling in endpoint
	public ResponseEntity<?> getAllPatientAppointment(
	        @RequestParam("appointmentDate") String  appointmentDate,@RequestParam("doctorId")Integer doctorId) {
	    try {
	        logger.info("getAllPatientAppointment request for createdOn: {}, appointmentType: {}", appointmentDate);
	        
	       
	        return doctorAppoitmentService.getAllPatientAppointment(appointmentDate, doctorId);
	    } catch (Exception e) {
	        logger.error("getAllPatientAppointment Method Exception: {}", e.getMessage(), e);
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
	
	@GetMapping("getAllPatientPharamcyByHospitalIdAndDate") // Corrected spelling in endpoint
	public ResponseEntity<?> getAllPatientPharamcyByHospitalIdAndDate(
	        @RequestParam("hospitalId") Integer  hospitalId,@RequestParam("currentDate")String currentDatae) {
	    try {
	        logger.info("getAllPatientPharamcyByHospitalIdAndDate request for createdOn: {}, appointmentType: {}", hospitalId);
	        
	       
	        return doctorAppoitmentService.getAllPatientPharamcyByHospitalIdAndDate(hospitalId,currentDatae);
	    } catch (Exception e) {
	        logger.error("getAllPatientPharamcyByHospitalIdAndDate Method Exception: {}", e.getMessage(), e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(-1, "Fail", e.getMessage()));
	    }
	}
	@GetMapping("getAllPatientPharamcyBypatientIdAndDate") // Corrected spelling in endpoint
	public ResponseEntity<?> getAllPatientPharamcyBypatientIdAndDate(
	        @RequestParam("patientId") Integer  patientId,@RequestParam("currentDate")String currentDatae) {
	    try {
	        logger.info("getAllPatientPharamcyBypatientIdAndDate request for createdOn: {}, appointmentType: {}", patientId);
	        
	       
	        return doctorAppoitmentService.getAllPatientPharamcyBypatientIdAndDate(patientId,currentDatae);
	    } catch (Exception e) {
	        logger.error("getAllPatientPharamcyBypatientIdAndDate Method Exception: {}", e.getMessage(), e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(-1, "Fail", e.getMessage()));
	    }
	}
	
	@PostMapping("saveMedicineDetailByPharamacy")
	public ResponseEntity<?> saveMedicineDetailByPharamacy(@RequestBody HospitalDataListWebModel userWebModel) {
	    try {
	        // Call the service to perform the update
	        return doctorAppoitmentService.saveMedicineDetailByPharamacy(userWebModel);
	    } catch (Exception e) {
	        // Handle errors and return a meaningful response
	        logger.error("saveMedicineDetailByPharamacy Method Exception: {}", e.getMessage(), e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(-1, "Fail", e.getMessage()));
	    }
	}
	
	@GetMapping("getAllPatientMedicalTestByHospitalIdAndDate") // Corrected spelling in endpoint
	public ResponseEntity<?> getAllPatientMedicalTestByHospitalIdAndDate(
	        @RequestParam("hospitalId") Integer  hospitalId,@RequestParam("currentDate")String currentDatae) {
	    try {
	        logger.info("getAllPatientMedicalTestByHospitalIdAndDate request for createdOn: {}, appointmentType: {}", hospitalId);
	        
	       
	        return doctorAppoitmentService.getAllPatientMedicalTestByHospitalIdAndDate(hospitalId,currentDatae);
	    } catch (Exception e) {
	        logger.error("getAllPatientMedicalTestByHospitalIdAndDate Method Exception: {}", e.getMessage(), e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(-1, "Fail", e.getMessage()));
	    }
	}
	
	@GetMapping("getAllPatientMedicalTestBypatientIdAndDate") // Corrected spelling in endpoint
	public ResponseEntity<?> getAllPatientMedicalTestBypatientIdAndDate(
	        @RequestParam("patientId") Integer  patientId,@RequestParam("currentDate")String currentDatae) {
	    try {
	        logger.info("getAllPatientMedicalTestBypatientIdAndDate request for createdOn: {}, appointmentType: {}", patientId);
	        
	       
	        return doctorAppoitmentService.getAllPatientMedicalTestBypatientIdAndDate(patientId,currentDatae);
	    } catch (Exception e) {
	        logger.error("getAllPatientMedicalTestBypatientIdAndDate Method Exception: {}", e.getMessage(), e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(-1, "Fail", e.getMessage()));
	    }
	}
	@GetMapping("getAllPatientAppointmentDetails") // Corrected spelling in endpoint
	public ResponseEntity<?> getAllPatientAppointmentDetails(
	        @RequestParam("patientId") Integer  patientId,@RequestParam("appointmentDate")String currentDatae) {
	    try {
	        logger.info("getAllPatientAppointmentDetails request for createdOn: {}, appointmentType: {}", patientId);
	        
	       
	        return doctorAppoitmentService.getAllPatientAppointmentDetails(patientId,currentDatae);
	    } catch (Exception e) {
	        logger.error("getAllPatientMedicalTestBypatientIdAndDate Method Exception: {}", e.getMessage(), e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(-1, "Fail", e.getMessage()));
	    }
	}
	
	@DeleteMapping("deleteParticularSpiltSlot")
	public ResponseEntity<?> deleteParticularSpiltSlot(@RequestParam("id")Integer id) {
	    try {
	        // Call the service to perform the update
	        return doctorAppoitmentService.deleteParticularSpiltSlot(id);
	    } catch (Exception e) {
	        // Handle errors and return a meaningful response
	        logger.error("deleteParticularSpiltSlote Method Exception: {}", e.getMessage(), e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(-1, "Fail", e.getMessage()));
	    }
	}
	
	@PostMapping("saveDoctorFees")
	public ResponseEntity<?> saveDoctorFees(@RequestBody HospitalDataListWebModel userWebModel) {
	    try {
	        // Call the service to perform the update
	        return doctorAppoitmentService.saveDoctorFees(userWebModel);
	    } catch (Exception e) {
	        // Handle errors and return a meaningful response
	        logger.error("saveDoctorFees Method Exception: {}", e.getMessage(), e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(-1, "Fail", e.getMessage()));
	    }
	}
	
	@PostMapping("updateFeesStatus")
	public ResponseEntity<?> updateFeesStatus(@RequestBody HospitalDataListWebModel userWebModel) {
	    try {
	        // Call the service to perform the update
	        return doctorAppoitmentService.updateFeesStatus(userWebModel);
	    } catch (Exception e) {
	        // Handle errors and return a meaningful response
	        logger.error("updateFeesStatus Method Exception: {}", e.getMessage(), e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(-1, "Fail", e.getMessage()));
	    }
	}
	
	@GetMapping("getAllDoctorFilterByLocation")
	public ResponseEntity<?> getAllDoctorFilterByLocation(@RequestParam("location")String location) {
	    try {
	        // Call the service to perform the update
	        return doctorAppoitmentService.getAllDoctorFilterByLocation(location);
	    } catch (Exception e) {
	        // Handle errors and return a meaningful response
	        logger.error("getAllDoctorFilterByLocation Method Exception: {}", e.getMessage(), e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(-1, "Fail", e.getMessage()));
	    }
	}
	
	@PostMapping("cancelAppointmentOnlineAndOffline")
	public ResponseEntity<?> cancelAppointmentOnlineAndOffline(@RequestBody HospitalDataListWebModel userWebModel) {
	    try {
	        // Call the service to perform the update
	        return doctorAppoitmentService.cancelAppointmentOnlineAndOffline(userWebModel);
	    } catch (Exception e) {
	        // Handle errors and return a meaningful response
	        logger.error("cancelAppointmentOnlineAndOffline Method Exception: {}", e.getMessage(), e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(-1, "Fail", e.getMessage()));
	    }
	}
}




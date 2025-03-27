package com.annular.healthCare.service.serviceImpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.annular.healthCare.Response;
import com.annular.healthCare.Util.Utility;
import com.annular.healthCare.model.MediaFileCategory;
import com.annular.healthCare.model.PatientAppointmentTable;
import com.annular.healthCare.model.PatientDetails;
import com.annular.healthCare.model.User;
import com.annular.healthCare.repository.PatientAppoitmentTablerepository;
import com.annular.healthCare.repository.PatientDetailsRepository;
import com.annular.healthCare.repository.UserRepository;
import com.annular.healthCare.service.MediaFileService;
import com.annular.healthCare.service.PatientDetailsService;
import com.annular.healthCare.webModel.FileInputWebModel;
import com.annular.healthCare.webModel.FileOutputWebModel;
import com.annular.healthCare.webModel.PatientDetailsWebModel;

@Service
public class PatientDetailsServiceImpl implements PatientDetailsService{
	
	
	public static final Logger logger = LoggerFactory.getLogger(PatientDetailsServiceImpl.class);
	
	@Autowired
	PatientDetailsRepository patientDetailsRepository;
	
	@Autowired
	MediaFileService mediaFilesService;
	
	@Autowired
	PatientAppoitmentTablerepository patientAppointmentRepository;
	
	@Autowired
	UserRepository userRepository;

	@Override
	public ResponseEntity<?> register(PatientDetailsWebModel userWebModel) {
	    try {
	        logger.info("Registering patient: {}", userWebModel.getPatientName());
	        
	        // Validate required fields
	        if (userWebModel.getPatientName() == null || userWebModel.getMobileNumber() == null) {
	            return ResponseEntity.badRequest().body(
	                new Response(0, "Fail", "Patient name and mobile number are required")
	            );
	        }
	        

            // **Check if slot is available before booking an appointment**
            if (userWebModel.getDoctorId() != null && userWebModel.getAppointmentDate() != null) {
                boolean isSlotBooked = checkIfSlotIsBooked(
                        userWebModel.getDoctorSlotId(),
                        userWebModel.getDaySlotId(),
                        userWebModel.getTimeSlotId()
                );

                if (isSlotBooked) {
                    logger.warn("Slot is already booked for date: {}", userWebModel.getAppointmentDate());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new Response(0, "Fail", "The selected slot on " 
                                    + userWebModel.getAppointmentDate() + " is already booked."));
                }
            }
	        // Create patient details
	        PatientDetails savedPatient = createPatientDetails(userWebModel);
	        
	        // Book appointment if appointment details are provided
	        PatientAppointmentTable appointment = bookAppointment(userWebModel, savedPatient);
	        
	        // Save media files if any
	        savePatientMediaFiles(userWebModel, savedPatient);

	        return ResponseEntity.ok(new Response(1, "Success", "Patient registered successfully"));
	    } catch (Exception e) {
	        logger.error("Registration failed", e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(0, "Fail", "Something went wrong during registration"));
	    }
	}
	public boolean checkIfSlotIsBooked(Integer doctorSlotId, Integer daySlotId, Integer timeSlotId) {
        try {
            boolean isBooked = patientAppointmentRepository.isSlotBooked(doctorSlotId, daySlotId, timeSlotId);
            logger.info("Slot availability check - DoctorSlotId: {}, DaySlotId: {}, TimeSlotId: {} - Status: {}", 
                        doctorSlotId, daySlotId, timeSlotId, isBooked);
            return isBooked;
        } catch (Exception e) {
            logger.error("Error checking slot availability: {}", e.getMessage(), e);
            return false; // Assume slot is not booked if query fails
        }
    }
	private PatientDetails createPatientDetails(PatientDetailsWebModel userWebModel) {
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
	    
	    return patientDetailsRepository.save(newPatient);
	}

	private PatientAppointmentTable bookAppointment(PatientDetailsWebModel userWebModel, PatientDetails savedPatient) {
	    // Check if appointment details are provided
	    if (userWebModel.getDoctorId() == null || 
	        userWebModel.getAppointmentDate() == null || 
	        userWebModel.getSlotStartTime() == null) {
	        return null;
	    }

	    // Fetch required entities
	    User doctor = userRepository.findById(userWebModel.getDoctorId())
	            .orElseThrow(() -> new RuntimeException("Doctor not found"));
	    User patient = userRepository.findById(savedPatient.getPatientDetailsId())
	            .orElseThrow(() -> new RuntimeException("Patient user not found"));

	    // Create appointment
	    PatientAppointmentTable appointment = PatientAppointmentTable.builder()
	            .doctor(doctor)
	            .patient(patient)
	            .doctorSlotId(userWebModel.getDoctorSlotId())
	            .daySlotId(userWebModel.getDaySlotId())
	            .timeSlotId(userWebModel.getTimeSlotId())
	            .appointmentDate(userWebModel.getAppointmentDate())
	            .slotStartTime(userWebModel.getSlotStartTime())
	            .slotEndTime(userWebModel.getSlotEndTime())
	            .slotTime(userWebModel.getSlotTime())
	            .isActive(true)
	            .createdBy(userWebModel.getCreatedBy())
	            .appointmentStatus("SCHEDULED")
	            .patientNotes(userWebModel.getPatientNotes())
	            .build();

	    return patientAppointmentRepository.save(appointment);
	}

	private void savePatientMediaFiles(PatientDetailsWebModel userWebModel, PatientDetails savedPatient) {
	    if (!Utility.isNullOrEmptyList(userWebModel.getFiles())) {
	        FileInputWebModel fileInput = FileInputWebModel.builder()
	                .category(MediaFileCategory.patientDocument)
	                .categoryRefId(savedPatient.getPatientDetailsId())
	                .files(userWebModel.getFiles())
	                .build();
	        
	        User userFromDB = userRepository.findById(userWebModel.getCreatedBy())
	                .orElse(null);
	        
	        mediaFilesService.saveMediaFiles(fileInput, userFromDB);
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
	@Override
	public ResponseEntity<?> updatePatientDetails(PatientDetailsWebModel userWebModel) {
	    try {
	        logger.info("Updating patient details for ID: {}", userWebModel.getPatientDetailsId());

	        if (userWebModel.getPatientDetailsId() == null) {
	            return ResponseEntity.badRequest().body(
	                new Response(0, "Fail", "Patient ID is required for update")
	            );
	        }

	        Optional<PatientDetails> optionalPatient = patientDetailsRepository.findById(userWebModel.getPatientDetailsId());

	        if (optionalPatient.isEmpty()) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                    .body(new Response(0, "Fail", "Patient not found with ID: " + userWebModel.getPatientDetailsId()));
	        }

	        PatientDetails patient = optionalPatient.get();

	        // Only update fields if they are non-null
	        if (userWebModel.getPatientName() != null) patient.setPatientName(userWebModel.getPatientName());
	        if (userWebModel.getDob() != null) patient.setDob(userWebModel.getDob());
	        if (userWebModel.getAge() != null) patient.setAge(userWebModel.getAge());
	        if (userWebModel.getGender() != null) patient.setGender(userWebModel.getGender());
	        if (userWebModel.getBloodGroup() != null) patient.setBloodGroup(userWebModel.getBloodGroup());
	        if (userWebModel.getMobileNumber() != null) patient.setMobileNumber(userWebModel.getMobileNumber());
	        if (userWebModel.getEmailId() != null) patient.setEmailId(userWebModel.getEmailId());
	        if (userWebModel.getAddress() != null) patient.setAddress(userWebModel.getAddress());
	        if (userWebModel.getCurrentAddress() != null) patient.setCurrentAddress(userWebModel.getCurrentAddress());
	        if (userWebModel.getEmergencyContact() != null) patient.setEmergencyContact(userWebModel.getEmergencyContact());
	        if (userWebModel.getHospitalId() != null) patient.setHospitalId(userWebModel.getHospitalId());
	        if (userWebModel.getPurposeOfVisit() != null) patient.setPurposeOfVisit(userWebModel.getPurposeOfVisit());
	        if (userWebModel.getDoctorId() != null) patient.setDoctorId(userWebModel.getDoctorId());
	        if (userWebModel.getPreviousMedicalHistory() != null) patient.setPreviousMedicalHistory(userWebModel.getPreviousMedicalHistory());
	        if (userWebModel.getInsuranceDetails() != null) patient.setInsuranceDetails(userWebModel.getInsuranceDetails());
	        if (userWebModel.getInsurerName() != null) patient.setInsurerName(userWebModel.getInsurerName());
	        if (userWebModel.getInsuranceProvider() != null) patient.setInsuranceProvider(userWebModel.getInsuranceProvider());
	        if (userWebModel.getPolicyNumber() != null) patient.setPolicyNumber(userWebModel.getPolicyNumber());
	        if (userWebModel.getDisability() != null) patient.setDisability(userWebModel.getDisability());

	        patient.setUserUpdatedOn(new Date());
	        if (userWebModel.getUserUpdatedBy() != null)
	            patient.setUserUpdatedBy(userWebModel.getUserUpdatedBy());

	        // Save updated patient
	        patientDetailsRepository.save(patient);

	        // Save or update media files (if passed)
	        if (!Utility.isNullOrEmptyList(userWebModel.getFiles())) {
	            FileInputWebModel fileInput = FileInputWebModel.builder()
	                    .category(MediaFileCategory.patientDocument)
	                    .categoryRefId(patient.getPatientDetailsId())
	                    .files(userWebModel.getFiles())
	                    .build();

	            User userFromDB = userRepository.findById(userWebModel.getUserUpdatedBy()).orElse(null);
	            mediaFilesService.saveMediaFiles(fileInput, userFromDB);
	        }

	        return ResponseEntity.ok(new Response(1, "Success", "Patient updated successfully"));

	    } catch (Exception e) {
	        logger.error("Patient update failed", e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(0, "Fail", "Something went wrong during update"));
	    }
	}



	@Override
	public ResponseEntity<?> getPatientDetailsById(Integer patientDetailsID) {
	    try {
	        if (patientDetailsID == null) {
	            return ResponseEntity.badRequest().body(new Response(0, "Fail", "Patient ID is required"));
	        }

	        Optional<PatientDetails> optionalPatient = patientDetailsRepository.findById(patientDetailsID);

	        if (optionalPatient.isEmpty()) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                    .body(new Response(0, "Fail", "Patient not found with ID: " + patientDetailsID));
	        }

	        PatientDetails patient = optionalPatient.get();

	        // Convert to WebModel
	        PatientDetailsWebModel webModel = new PatientDetailsWebModel();
	        webModel.setPatientDetailsId(patient.getPatientDetailsId());
	        webModel.setPatientName(patient.getPatientName());
	        webModel.setDob(patient.getDob());
	        webModel.setAge(patient.getAge());
	        webModel.setGender(patient.getGender());
	        webModel.setBloodGroup(patient.getBloodGroup());
	        webModel.setMobileNumber(patient.getMobileNumber());
	        webModel.setEmailId(patient.getEmailId());
	        webModel.setAddress(patient.getAddress());
	        webModel.setCurrentAddress(patient.getCurrentAddress());
	        webModel.setEmergencyContact(patient.getEmergencyContact());
	        webModel.setHospitalId(patient.getHospitalId());
	        webModel.setPurposeOfVisit(patient.getPurposeOfVisit());
	        webModel.setDoctorId(patient.getDoctorId());
	        webModel.setPreviousMedicalHistory(patient.getPreviousMedicalHistory());
	        webModel.setInsuranceDetails(patient.getInsuranceDetails());
	        webModel.setInsurerName(patient.getInsurerName());
	        webModel.setInsuranceProvider(patient.getInsuranceProvider());
	        webModel.setPolicyNumber(patient.getPolicyNumber());
	        webModel.setDisability(patient.getDisability());
	        webModel.setUserUpdatedBy(patient.getUserUpdatedBy());

	        // Fetch media files
	        List<FileOutputWebModel> mediaFiles = mediaFilesService.getMediaFilesByCategoryAndRefId(
	                MediaFileCategory.patientDocument, patient.getPatientDetailsId());
	        webModel.setFiless(mediaFiles);

	        return ResponseEntity.ok(new Response(1, "Success", webModel));

	    } catch (Exception e) {
	        logger.error("Failed to fetch patient details", e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(0, "Fail", "Something went wrong while retrieving patient details"));
	    }
	}

	@Override
	public ResponseEntity<?> getDoctorListByHospitalId(Integer hospitalId) {
	    try {
	        if (hospitalId == null) {
	            return ResponseEntity.badRequest().body(new Response(0, "Fail", "Hospital ID is required"));
	        }

	        // Assuming userType "doctor" is a String. Adjust if it's an enum or different value.
	        List<User> doctors = userRepository.findByHospitalIdAndUserType(hospitalId, "Doctor");

	        // Transforming to only return id and name
	        List<Map<String, Object>> doctorList = doctors.stream().map(doctor -> {
	            Map<String, Object> map = new HashMap<>();
	            map.put("id", doctor.getUserId());
	            map.put("firstname", doctor.getFirstName());
	            map.put("lastName", doctor.getLastName());
	            return map;
	        }).collect(Collectors.toList());

	        return ResponseEntity.ok(new Response(1, "Success", doctorList));

	    } catch (Exception e) {
	        logger.error("Error fetching doctor list", e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(0, "Fail", "Something went wrong while fetching doctor list"));
	    }
	}
	@Override
	public boolean deleteMediaFilesById(Integer fileId) {
	    return mediaFilesService.deleteMediaFilesByUserIdAndCategoryAndRefIds(
	            MediaFileCategory.patientDocument, fileId
	    ); 
	}
	

}

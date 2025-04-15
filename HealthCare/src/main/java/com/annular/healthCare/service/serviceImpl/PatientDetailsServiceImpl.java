package com.annular.healthCare.service.serviceImpl;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.persistence.Column;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.annular.healthCare.Response;
import com.annular.healthCare.Util.Base64FileUpload;
import com.annular.healthCare.Util.Utility;
import com.annular.healthCare.model.DoctorRole;
import com.annular.healthCare.model.DoctorSlotSpiltTime;
import com.annular.healthCare.model.DoctorSlotTime;
import com.annular.healthCare.model.DoctorSpecialty;
import com.annular.healthCare.model.MediaFile;
import com.annular.healthCare.model.MediaFileCategory;
import com.annular.healthCare.model.PatientAppointmentTable;
import com.annular.healthCare.model.PatientDetails;
import com.annular.healthCare.model.PatientMappedHospitalId;
import com.annular.healthCare.model.PatientSubChildDetails;
import com.annular.healthCare.model.User;
import com.annular.healthCare.repository.DoctorSlotSpiltTimeRepository;
import com.annular.healthCare.repository.DoctorSlotTimeRepository;
import com.annular.healthCare.repository.DoctorSpecialityRepository;
import com.annular.healthCare.repository.PatientAppoitmentTablerepository;
import com.annular.healthCare.repository.PatientDetailsRepository;
import com.annular.healthCare.repository.PatientMappedHospitalIdRepository;
import com.annular.healthCare.repository.PatientSubChildDetailsRepository;
import com.annular.healthCare.repository.UserRepository;
import com.annular.healthCare.service.MediaFileService;
import com.annular.healthCare.service.PatientDetailsService;
import com.annular.healthCare.webModel.FileInputWebModel;
import com.annular.healthCare.webModel.FileOutputWebModel;
import com.annular.healthCare.webModel.PatientAppointmentWebModel;
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
	
	@Autowired
	DoctorSlotTimeRepository doctorSlotTimeRepository;
	
	@Autowired
	DoctorSlotSpiltTimeRepository doctorSlotSplitTimeRepository;
	
	@Autowired
	PatientMappedHospitalIdRepository patientMappedHospitalIdRepository;
	
	@Autowired
	DoctorSpecialityRepository doctorSpecialtyRepository;
	
	@Autowired
	PatientSubChildDetailsRepository patientSubChildDetailsRepository;
	
	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");


	@Transactional
	@Override
	public ResponseEntity<?> register(PatientDetailsWebModel userWebModel) {
	    try {
	        logger.info("Registering patient: {}", userWebModel.getPatientName());

	        // Validate required fields
	        if (userWebModel.getPatientName() == null || userWebModel.getMobileNumber() == null) {
	            return ResponseEntity.badRequest().body(new Response(0, "Fail", "Patient name and mobile number are required"));
	        }

	        // Check if email already exists in the database
	        Optional<PatientDetails> existingUsers = patientDetailsRepository.findByEmailId(userWebModel.getEmailId());
	        if (existingUsers.isPresent()) {
	            return ResponseEntity.badRequest().body(new Response(0, "Fail", "Email already exists"));
	        }

	        
	        // Check if the patient already exists
	        Optional<PatientDetails> existingUser = patientDetailsRepository.findByMobileNumberAndHospitalId(
	            userWebModel.getMobileNumber());
	        if (existingUser.isPresent()) {
	        	return ResponseEntity.badRequest().body(new Response(0, "Fail", "Mobile number is already registered for this hospital."));
	        }

	        // Check if slot is available before booking an appointment
	        if (userWebModel.getDoctorId() != null 
	                && userWebModel.getAppointmentDate() != null 
	                && userWebModel.getDoctorSlotId() != null 
	                && userWebModel.getDaySlotId() != null 
	                && userWebModel.getSlotStartTime() != null 
	                && userWebModel.getSlotEndTime() != null) {  // Fixed duplicate check

	            boolean isSlotBooked = checkIfSlotIsBooked(
	                userWebModel.getDoctorSlotId(),
	                userWebModel.getDaySlotId(),
	                userWebModel.getSlotStartTime(),  // Pass correct start time
	                userWebModel.getSlotEndTime()     // Pass correct end time
	            );

	            if (isSlotBooked) {
	                logger.warn("Slot is already booked for doctorId: {}, date: {}, time: {} - {}",
	                        userWebModel.getDoctorId(),
	                        userWebModel.getAppointmentDate(),
	                        userWebModel.getSlotStartTime(),
	                        userWebModel.getSlotEndTime()
	                );
	                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                        .body(new Response(0, "Fail", "The selected slot on " 
	                                + userWebModel.getAppointmentDate() + " is already booked."));
	            }
	        }

	        // Create patient details
	        PatientDetails savedPatient = createPatientDetails(userWebModel);
	        if (savedPatient == null) {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                    .body(new Response(0, "Fail", "Failed to create patient details"));
	        }

	        // Book appointment if appointment details are provided
	        PatientAppointmentTable appointment = bookAppointment(userWebModel, savedPatient);  // Fixed return type
	        if (appointment == null) {  // No need to check for isEmpty() since it's not a list
	            return ResponseEntity.badRequest()
	                    .body(new Response(0, "Fail", "slot Already booked"));
	        }
	        if (userWebModel.getDoctorSlotSpiltTimeId() != null) {
                Optional<DoctorSlotSpiltTime> optionalSlot = doctorSlotSplitTimeRepository.findById(userWebModel.getDoctorSlotSpiltTimeId());
                if (optionalSlot.isPresent()) {
                    DoctorSlotSpiltTime slot = optionalSlot.get();
                    slot.setSlotStatus("Booked");
                    slot.setUpdatedBy(userWebModel.getCreatedBy());
                    slot.setUpdatedOn(new Date());
                    doctorSlotSplitTimeRepository.save(slot);
                    logger.info("Marked doctorSlotSplitTimeId {} as Booked", userWebModel.getDoctorSlotSpiltTimeId());
                } else {
                    logger.warn("doctorSlotSplitTimeId {} not found for updating status", userWebModel.getDoctorSlotSpiltTimeId());
                }
            }

	        // Save media files if any
	        savePatientMediaFiles(userWebModel, savedPatient);
	     // Save patient-hospital mapping



	        return ResponseEntity.ok(new Response(1, "Success", "Patient registered successfully"));
	    } catch (Exception e) {
	        logger.error("Registration failed: {}", e.getMessage(), e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(0, "Fail", "Something went wrong during registration"));
	    }
	}

   public boolean checkIfSlotIsBooked(Integer doctorSlotId, Integer daySlotId, String slotStartTime, String slotEndTime) {
	        try {
	            boolean isBooked = patientAppointmentRepository.isSlotBooked(doctorSlotId, daySlotId, slotStartTime, slotEndTime);

	            logger.info("Slot availability check - DoctorSlotId: {}, DaySlotId: {}, StartTime: {}, EndTime: {} - Status: {}", 
	                        doctorSlotId, daySlotId, slotStartTime, slotEndTime, isBooked);
	            return isBooked;
	        } catch (Exception e) {
	            logger.error("Error checking slot availability: {}", e.getMessage(), e);
	            return false; // Assume slot is available if query fails
	        }
	    }

   private PatientDetails createPatientDetails(PatientDetailsWebModel userWebModel) {

	    // Save patient details first
	    PatientDetails savedPatient = patientDetailsRepository.save(
	        PatientDetails.builder()
	            .patientName(userWebModel.getPatientName())
	            .dob(userWebModel.getDob())
	            .age(userWebModel.getAge())
	            .otp(100)
	            .gender(userWebModel.getGender())
	            .bloodGroup(userWebModel.getBloodGroup())
	            .mobileNumber(userWebModel.getMobileNumber())
	            .emailId(userWebModel.getEmailId())
	            .address(userWebModel.getAddress())
	            .currentAddress(userWebModel.getCurrentAddress())
	            .emergencyContact(userWebModel.getEmergencyContact())
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
	            .build()
	    );

	    // Now save patient-hospital mapping
	    PatientMappedHospitalId mapped = PatientMappedHospitalId.builder()
	        .createdBy(userWebModel.getCreatedBy())
	        .userIsActive(true)
	        .userUpdatedBy(userWebModel.getCreatedBy())
	        .patientId(savedPatient.getPatientDetailsId())
	        .hospitalId(userWebModel.getHospitalId())
	        .medicalHistoryStatus(true)
	        .personalDataStatus(true)
	        .build();

	    patientMappedHospitalIdRepository.save(mapped);

	    // Return saved patient at the end
	    return savedPatient;
	}

	    
	 // Helper method to format LocalTime to "hh:mm a" format (e.g., "07:45 PM")
	    private String formatTime(LocalTime time) {
	        return time.format(TIME_FORMATTER);
	    }
	    private PatientAppointmentTable bookAppointment(PatientDetailsWebModel userWebModel, PatientDetails savedPatient) {
//	        if (userWebModel.getDoctorId() == null || userWebModel.getAppointmentDate() == null) {
//	            throw new IllegalArgumentException("Doctor ID and Appointment Date are required.");
//	        }

	        // Fetch required entities
	        User doctor = userRepository.findById(userWebModel.getDoctorId())
	                .orElseThrow(() -> new RuntimeException("Doctor not found"));
	        PatientDetails patient = patientDetailsRepository.findById(savedPatient.getPatientDetailsId())
	                .orElseThrow(() -> new RuntimeException("Patient user not found"));

	        DoctorSlotTime doctorSlotTime = doctorSlotTimeRepository.findById(userWebModel.getTimeSlotId())
	                .orElseThrow(() -> new RuntimeException("Doctor slot time not found"));

	        // Validate input slot times
	        String inputSlotStartTime = userWebModel.getSlotStartTime();
	        String inputSlotEndTime = userWebModel.getSlotEndTime();

	        // First, check if the exact input slot is already booked
	        boolean isSlotAlreadyBooked = patientAppointmentRepository.isSlotBooked(
	                userWebModel.getDoctorSlotId(),
	                userWebModel.getDaySlotId(),
	                inputSlotStartTime,
	                inputSlotEndTime
	        );

	        if (isSlotAlreadyBooked) {
	            logger.warn("Slot is already booked: DoctorSlotId: {}, DaySlotId: {}, StartTime: {}, EndTime: {}",
	                    userWebModel.getDoctorSlotId(), 
	                    userWebModel.getDaySlotId(), 
	                    inputSlotStartTime, 
	                    inputSlotEndTime);
	            
	            return null; // Slot is already booked
	        }
	     // Get the appointment type from the input
	        String appointmentType = userWebModel.getAppointmentType();

	        // Count existing appointments for the doctor on the same date with the given appointment type
	        int newToken = patientAppointmentRepository.countByAppointmentDateAndDoctorIdAndAppointmentType(
	                userWebModel.getAppointmentDate(),
	                doctor.getUserId(),
	                appointmentType
	        ) + 1;

	        // If not booked, proceed with creating the appointment
	        PatientAppointmentTable appointment = PatientAppointmentTable.builder()
	                .doctor(doctor)
	                .age(userWebModel.getAge())
	                .dateOfBirth(userWebModel.getDateOfBirth())
	                .patientName(userWebModel.getPatientName())
	                .relationShipType(userWebModel.getRelationshipType())
	                .patient(patient)
	                .doctorSlotId(userWebModel.getDoctorSlotId())
	                .token(String.valueOf(newToken)) // Assigning token sequentially
	                .daySlotId(userWebModel.getDaySlotId())
	                .timeSlotId(userWebModel.getTimeSlotId())
	                .appointmentDate(userWebModel.getAppointmentDate())
	                .slotStartTime(inputSlotStartTime)
	                .slotEndTime(inputSlotEndTime)
	                .slotTime(userWebModel.getSlotTime())
	                .isActive(true)
	                .doctorSlotStartTime(doctorSlotTime.getSlotStartTime())
	                .doctorSlotEndTime(doctorSlotTime.getSlotEndTime())
	                .createdBy(userWebModel.getCreatedBy())
	                .appointmentStatus("SCHEDULED")
	                .appointmentType(userWebModel.getAppointmentType())
	                .patientNotes(userWebModel.getPatientNotes())
	                .build();

	        return patientAppointmentRepository.save(appointment);
	    }

	    private LocalTime parseTime(String timeString) {
	        if (timeString == null || timeString.trim().isEmpty()) {
	            logger.error("Null or empty time string provided");
	            throw new IllegalArgumentException("Time string cannot be null or empty");
	        }

	        // Preprocess the time string
	        timeString = preprocessTimeString(timeString);

	        // List of potential formatters
	        List<DateTimeFormatter> formatters = new ArrayList<>();
	        formatters.add(DateTimeFormatter.ofPattern("h:mm a"));     // 6:45 PM
	        formatters.add(DateTimeFormatter.ofPattern("hh:mm a"));    // 06:45 PM
	        formatters.add(DateTimeFormatter.ofPattern("H:mm"));       // 18:45
	        formatters.add(DateTimeFormatter.ofPattern("HH:mm"));      // 18:45
	        formatters.add(DateTimeFormatter.ISO_LOCAL_TIME);

	        // Try each formatter
	        for (DateTimeFormatter formatter : formatters) {
	            try {
	                LocalTime parsedTime = LocalTime.parse(timeString, formatter);
	                logger.info("Successfully parsed time: {} using format: {}", 
	                            timeString, formatter.toString());
	                return parsedTime;
	            } catch (DateTimeParseException e) {
	                logger.debug("Failed to parse '{}' with formatter {}: {}", 
	                             timeString, formatter, e.getMessage());
	            }
	        }

	        // If standard parsing fails, try custom parsing
	        try {
	            LocalTime customParsedTime = customTimeParse(timeString);
	            if (customParsedTime != null) {
	                return customParsedTime;
	            }
	        } catch (Exception e) {
	            logger.debug("Custom parsing failed: {}", e.getMessage());
	        }

	        // Final error logging
	        logger.error("Unable to parse time string: {}", timeString);
	        throw new IllegalArgumentException("Unable to parse time: " + timeString + 
	            ". Supported formats include: h:mm a, HH:mm, etc.");
	    }

	    private String preprocessTimeString(String timeString) {
	        // Trim and standardize
	        timeString = timeString.trim().toUpperCase();

	        // Remove any extra spaces around AM/PM
	        timeString = timeString.replaceAll("\\s*([AP]M)\\s*", "$1");

	        // Normalize separator (replace multiple spaces with single space)
	        timeString = timeString.replaceAll("\\s+", " ");

	        return timeString;
	    }

	    private LocalTime customTimeParse(String timeString) {
	        // Regex patterns for various time formats
	        List<Pattern> patterns = new ArrayList<>();
	        patterns.add(Pattern.compile("(\\d{1,2}):(\\d{2})\\s*([AP]M)"));  // 6:45 PM
	        patterns.add(Pattern.compile("(\\d{1,2})\\s*([AP]M)"));  // 6 PM
	        patterns.add(Pattern.compile("(\\d{1,2}):(\\d{2})"));    // 18:45

	        for (Pattern pattern : patterns) {
	            Matcher matcher = pattern.matcher(timeString);
	            if (matcher.matches()) {
	                try {
	                    int hour, minute = 0;
	                    boolean isPM = false;

	                    if (matcher.groupCount() == 3) {
	                        // Format with hours, minutes, and AM/PM
	                        hour = Integer.parseInt(matcher.group(1));
	                        minute = Integer.parseInt(matcher.group(2));
	                        isPM = matcher.group(3).equals("PM");
	                    } else if (matcher.groupCount() == 2) {
	                        // Format with hours and minutes
	                        hour = Integer.parseInt(matcher.group(1));
	                        minute = matcher.groupCount() > 1 ? Integer.parseInt(matcher.group(2)) : 0;
	                    } else {
	                        // Hours only or other formats
	                        hour = Integer.parseInt(matcher.group(1));
	                    }

	                    // Adjust hour for 12-hour clock
	                    if (isPM && hour < 12) {
	                        hour += 12;
	                    } else if (!isPM && hour == 12) {
	                        hour = 0;
	                    }

	                    LocalTime parsedTime = LocalTime.of(hour, minute);
	                    logger.info("Custom parsed time: {} from input: {}", parsedTime, timeString);
	                    return parsedTime;
	                } catch (Exception e) {
	                    logger.debug("Custom parsing failed for pattern {}: {}", pattern, e.getMessage());
	                }
	            }
	        }

	        return null;
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
	            // Step 1: Get all mapped patients for hospital
	            List<PatientMappedHospitalId> mappings = patientMappedHospitalIdRepository.findByHospitalId(hospitalId);

	            if (mappings.isEmpty()) {
	                response.put("status", 0);
	                response.put("message", "No patients found for the given hospital.");
	                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	            }

	            // Step 2: Extract patient IDs
	            List<Integer> patientIds = mappings.stream()
	                    .map(PatientMappedHospitalId::getPatientId)
	                    .collect(Collectors.toList());

	            // Step 3: Fetch patient details
	            List<PatientDetails> patients = patientDetailsRepository.findAllById(patientIds);

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
	                patientData.put("hospitalId", hospitalId); // from method param
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
	        webModel.setPurposeOfVisit(patient.getPurposeOfVisit());
	        webModel.setDoctorId(patient.getDoctorId());
	        webModel.setPreviousMedicalHistory(patient.getPreviousMedicalHistory());
	        webModel.setInsuranceDetails(patient.getInsuranceDetails());
	        webModel.setInsurerName(patient.getInsurerName());
	        webModel.setInsuranceProvider(patient.getInsuranceProvider());
	        webModel.setPolicyNumber(patient.getPolicyNumber());
	        webModel.setDisability(patient.getDisability());
	        webModel.setUserUpdatedBy(patient.getUserUpdatedBy());

//	        // Override hospitalId if mapping exists in patient_mapped_hospital_id table
//	        Optional<PatientMappedHospitalId> optionalMapping =
//	                patientMappedHospitalIdRepository.findByPatientId(patient.getPatientDetailsId());
//
//	        optionalMapping.ifPresent(mapping ->
//	                webModel.setHospitalId(mapping.getHospitalId())
//	        );

	        // Fetch media files
	        List<FileOutputWebModel> mediaFiles = mediaFilesService.getMediaFilesByCategoryAndRefId(
	                MediaFileCategory.patientDocument, patient.getPatientDetailsId());
	        webModel.setFiless(mediaFiles);

	        // Fetch patient appointments
	        List<PatientAppointmentTable> appointments = patientAppointmentRepository.findByPatient_PatientDetailsId(patientDetailsID);

	        // Convert appointment details to a list of WebModels
	        List<PatientAppointmentWebModel> appointmentWebModels = appointments.stream().map(appointment -> {
	            PatientAppointmentWebModel appointmentModel = new PatientAppointmentWebModel();
	            appointmentModel.setAppointmentId(appointment.getAppointmentId());
	            appointmentModel.setDoctorId(appointment.getDoctor().getUserId());
	            appointmentModel.setDoctorSlotId(appointment.getDoctorSlotId());
	            appointmentModel.setDaySlotId(appointment.getDaySlotId());
	            appointmentModel.setTimeSlotId(appointment.getTimeSlotId());
	            appointmentModel.setAppointmentDate(appointment.getAppointmentDate());
	            appointmentModel.setSlotStartTime(appointment.getSlotStartTime());
	            appointmentModel.setSlotEndTime(appointment.getSlotEndTime());
	            appointmentModel.setSlotTime(appointment.getSlotTime());
	            appointmentModel.setIsActive(appointment.getIsActive());
	            appointmentModel.setCreatedBy(appointment.getCreatedBy());
	            appointmentModel.setCreatedOn(appointment.getCreatedOn());
	            appointmentModel.setUpdatedBy(appointment.getUpdatedBy());
	            appointmentModel.setUpdatedOn(appointment.getUpdatedOn());
	            appointmentModel.setAppointmentStatus(appointment.getAppointmentStatus());
	            appointmentModel.setPatientNotes(appointment.getPatientNotes());

	            return appointmentModel;
	        }).collect(Collectors.toList());

	        // Add appointments to response model
	        webModel.setAppointments(appointmentWebModels);

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
	            // Extract role IDs
	            List<Integer> roleIds = doctor.getDoctorRoles().stream()
	                    .map((DoctorRole role) -> role.getRoleId()) // Explicitly defining type
	                    .collect(Collectors.toList());


	            List<String> specialties = roleIds.stream()
	                    .map((Integer roleId) -> {
	                        DoctorSpecialty specialty = doctorSpecialtyRepository.findById(roleId).orElse(null);
	                        return (specialty != null) ? specialty.getSpecialtyName() : null;
	                    })
	                    .filter(Objects::nonNull)
	                    .collect(Collectors.toList());


	           
	            map.put("specialties", specialties);
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


	@Override
	public ResponseEntity<?> adminPatientRegister(PatientDetailsWebModel userWebModel) {

		    try {
		        logger.info("Registering patient: {}", userWebModel.getPatientName());

		        if (userWebModel.getPatientName() == null || userWebModel.getMobileNumber() == null) {
		            return ResponseEntity.badRequest().body(
		                new Response(0, "Fail", "Patient name and mobile number are required")
		            );
		        }
		        // Check if the patient already exists
		        Optional<PatientDetails> existingUser = patientDetailsRepository.findByMobileNumberAndHospitalId(
		            userWebModel.getMobileNumber());
		        if (existingUser.isPresent()) {
		        	return ResponseEntity.badRequest().body(new Response(0, "Fail", "Mobile number is already registered for this hospital."));
		        }

		        PatientDetails newPatient = PatientDetails.builder()
		                .patientName(userWebModel.getPatientName())
		                .dob(userWebModel.getDob())
		                .age(userWebModel.getAge())
		                .otp(100)
		                .gender(userWebModel.getGender())
		                .bloodGroup(userWebModel.getBloodGroup())
		                .mobileNumber(userWebModel.getMobileNumber())
		                .emailId(userWebModel.getEmailId())
		                .address(userWebModel.getAddress())
		                .currentAddress(userWebModel.getCurrentAddress())
		                .emergencyContact(userWebModel.getEmergencyContact())
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
		        
		        PatientMappedHospitalId mapped = PatientMappedHospitalId.builder()
			            .createdBy(userWebModel.getCreatedBy())
			            .userIsActive(true)
			            .userUpdatedBy(userWebModel.getCreatedBy())
			            .patientId(savedPatient.getPatientDetailsId())
			            .hospitalId(userWebModel.getHospitalId())
			            .medicalHistoryStatus(true)
			            .personalDataStatus(true)
			            .build();
		        patientMappedHospitalIdRepository.save(mapped);

		        // Save media files if any
		        if (!Utility.isNullOrEmptyList(userWebModel.getFiles())) {
		            FileInputWebModel fileInput = FileInputWebModel.builder()
		                    .category(MediaFileCategory.patientDocument) // Define a suitable enum value
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
	    public ResponseEntity<?> patientAppoitmentByOffline(PatientDetailsWebModel patientDetailsWebModel) {
	        try {
	            // Validate patient and doctor
	            Optional<User> doctor = userRepository.findById(patientDetailsWebModel.getDoctorId());
	            Optional<PatientDetails> patient = patientDetailsRepository.findById(patientDetailsWebModel.getPatientDetailsId());

	            if (doctor.isEmpty() || patient.isEmpty()) {
	                return ResponseEntity.badRequest().body("Doctor or Patient not found.");
	            }
	            // Count existing appointments for the doctor on the same date with "OFFLINE" type
	            int newToken = patientAppointmentRepository.countByAppointmentDateAndDoctorIdAndAppointmentType(
	                    patientDetailsWebModel.getAppointmentDate(), 
	                    patientDetailsWebModel.getDoctorId(),
	                    "OFFLINE"
	            ) + 1; 

	            // Create new appointment entry
	            PatientAppointmentTable appointment = PatientAppointmentTable.builder()
	                .doctor(doctor.get())
	                .patient(patient.get())
	                
	                .appointmentDate(patientDetailsWebModel.getAppointmentDate())
	                .slotStartTime(patientDetailsWebModel.getSlotStartTime())
	                .slotEndTime(patientDetailsWebModel.getSlotEndTime())
	                .slotTime(patientDetailsWebModel.getSlotTime())
	                .isActive(true)
	                .createdBy(patientDetailsWebModel.getCreatedBy())
	                .appointmentStatus("SCHEDULED")
	                .patientNotes(patientDetailsWebModel.getPatientNotes())
	                .appointmentType("OFFLINE")
	                .token(String.valueOf(newToken)) // Assigning token sequentially
	                .build();

	            // Save the appointment
	            patientAppointmentRepository.save(appointment);

	            return ResponseEntity.ok(new Response(1, "Success","Offline appointment booked successfully."));
	        } catch (Exception e) {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred: " + e.getMessage());
	        }
	    }


        @Override
        public ResponseEntity<?> getPatientDetailsByMobileNumberAndHospitalId(String phoneNumber, Integer hospitalId) {
            try {
                // Step 1: Get patient by mobile number
                Optional<PatientDetails> patientDetailsOptional = patientDetailsRepository.findByMobileNumber(phoneNumber);

                if (patientDetailsOptional.isPresent()) {
                    PatientDetails patient = patientDetailsOptional.get();

                    // Step 2: Check if mapping exists for patientId and hospitalId
                    Optional<PatientMappedHospitalId> mappingOptional = 
                        patientMappedHospitalIdRepository.findByPatientIdAndHospitalId(patient.getPatientDetailsId(), hospitalId);

                    if (mappingOptional.isPresent()) {
                        // Step 3: Fetch Appointments
                        List<PatientAppointmentTable> appointments = 
                            patientAppointmentRepository.findByPatientDetailsId(patient.getPatientDetailsId());

                        // Step 4: Prepare response
                        Map<String, Object> responseMap = new HashMap<>();
                        responseMap.put("patientDetailsId", patient.getPatientDetailsId());
                        responseMap.put("patientName", patient.getPatientName());
                        responseMap.put("dob", patient.getDob());
                        responseMap.put("gender", patient.getGender());
                        responseMap.put("bloodGroup", patient.getBloodGroup());
                        responseMap.put("mobileNumber", patient.getMobileNumber());
                        responseMap.put("emailId", patient.getEmailId());
                        responseMap.put("address", patient.getAddress());
                        responseMap.put("emergencyContact", patient.getEmergencyContact());
                        responseMap.put("doctorId", patient.getDoctorId());
                        responseMap.put("userIsActive", patient.getUserIsActive());

                        // Step 5: Transform appointments
                        List<Map<String, Object>> appointmentList = new ArrayList<>();
                        for (PatientAppointmentTable appointment : appointments) {
                            Map<String, Object> appointmentMap = new HashMap<>();
                            appointmentMap.put("appointmentId", appointment.getAppointmentId());
                            appointmentMap.put("doctorId", appointment.getDoctor().getUserId());
                            appointmentMap.put("patientId", appointment.getPatient().getPatientDetailsId());
                            appointmentMap.put("doctorSlotId", appointment.getDoctorSlotId());
                            appointmentMap.put("daySlotId", appointment.getDaySlotId());
                            appointmentMap.put("timeSlotId", appointment.getTimeSlotId());
                            appointmentMap.put("appointmentDate", appointment.getAppointmentDate());
                            appointmentMap.put("slotStartTime", appointment.getSlotStartTime());
                            appointmentMap.put("slotEndTime", appointment.getSlotEndTime());
                            appointmentMap.put("slotTime", appointment.getSlotTime());
                            appointmentMap.put("isActive", appointment.getIsActive());
                            appointmentMap.put("appointmentStatus", appointment.getAppointmentStatus());
                            appointmentMap.put("patientNotes", appointment.getPatientNotes());
                            appointmentMap.put("doctorSlotStartTime", appointment.getDoctorSlotStartTime());
                            appointmentMap.put("doctorSlotEndTime", appointment.getDoctorSlotEndTime());
                            appointmentMap.put("age", appointment.getAge());
                            appointmentMap.put("dateOfBirth", appointment.getDateOfBirth());
                            appointmentMap.put("relationShipType", appointment.getRelationShipType());
                            appointmentMap.put("patientName", appointment.getPatientName());
                            appointmentMap.put("appointmentType", appointment.getAppointmentType());

                            appointmentList.add(appointmentMap);
                        }

                        responseMap.put("appointments", appointmentList);
                        return ResponseEntity.ok(responseMap);

                    } else {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(Collections.singletonMap("message", "No hospital mapping found for the given patient."));
                    }
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(Collections.singletonMap("message", "Patient not found with the given mobile number."));
                }
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Collections.singletonMap("error", "Error fetching patient details: " + e.getMessage()));
            }
        }


        @Override
        public ResponseEntity<?> getPatientDetailsByMobileNumber(String mobileNumber) {
            Map<String, Object> response = new HashMap<>();

            Optional<PatientDetails> optionalPatient = patientDetailsRepository.findByMobileNumber(mobileNumber);

            if (optionalPatient.isPresent()) {
                PatientDetails patient = optionalPatient.get();

                // Fetch appointments using patientDetailsId
                List<PatientAppointmentTable> appointments = patientAppointmentRepository.findByPatientId(patient.getPatientDetailsId());

                // Prepare patient details
                Map<String, Object> patientData = new HashMap<>();
                patientData.put("patientName", patient.getPatientName());
                patientData.put("dob", patient.getDob());
                patientData.put("gender", patient.getGender());
                patientData.put("bloodGroup", patient.getBloodGroup());
                patientData.put("mobileNumber", patient.getMobileNumber());
                patientData.put("emailId", patient.getEmailId());
                patientData.put("address", patient.getAddress());
                patientData.put("emergencyContact", patient.getEmergencyContact());
              //  patientData.put("hospitalId", patient.getHospitalId());
                patientData.put("purposeOfVisit", patient.getPurposeOfVisit());
                patientData.put("doctorId", patient.getDoctorId());
                patientData.put("age", patient.getAge());

                // Prepare appointment details
                List<Map<String, Object>> appointmentDataList = new ArrayList<>();
                for (PatientAppointmentTable appointment : appointments) {
                    Map<String, Object> appointmentData = new HashMap<>();
                    appointmentData.put("appointmentId", appointment.getAppointmentId());
                    appointmentData.put("doctorId", appointment.getDoctor().getUserId());
                    appointmentData.put("doctorSlotId", appointment.getDoctorSlotId());
                    appointmentData.put("daySlotId", appointment.getDaySlotId());
                    appointmentData.put("timeSlotId", appointment.getTimeSlotId());
                    appointmentData.put("appointmentDate", appointment.getAppointmentDate());
                    appointmentData.put("slotStartTime", appointment.getSlotStartTime());
                    appointmentData.put("slotEndTime", appointment.getSlotEndTime());
                    appointmentData.put("slotTime", appointment.getSlotTime());
                    appointmentData.put("appointmentStatus", appointment.getAppointmentStatus());
                    appointmentData.put("patientNotes", appointment.getPatientNotes());
                    appointmentData.put("appointmentType", appointment.getAppointmentType());
                    appointmentData.put("age", appointment.getAge());
                    appointmentData.put("dateOfBirth", appointment.getDateOfBirth());
                    appointmentData.put("relationShipType", appointment.getRelationShipType());
                    appointmentData.put("patientName", appointment.getPatientName());

                    appointmentDataList.add(appointmentData);
                }

                response.put("status", 1);
                response.put("message", "Success");
                response.put("patientDetails", patientData);
                response.put("appointments", appointmentDataList);

                return ResponseEntity.ok(response);
            } else {
                response.put("status", 0);
                response.put("message", "Patient not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        }


        @Override
        public ResponseEntity<?> patientAppoitmentByOnline(PatientDetailsWebModel userWebModel) {
            try {
                logger.info("Registering patient: {}", userWebModel.getPatientName());

                if (userWebModel.getDoctorId() != null 
                        && userWebModel.getAppointmentDate() != null 
                        && userWebModel.getDoctorSlotId() != null 
                        && userWebModel.getDaySlotId() != null 
                        && userWebModel.getSlotStartTime() != null 
                        && userWebModel.getSlotEndTime() != null) {

                    boolean isSlotBooked = checkIfSlotIsBooked(
                        userWebModel.getDoctorSlotId(),
                        userWebModel.getDaySlotId(),
                        userWebModel.getSlotStartTime(),
                        userWebModel.getSlotEndTime()
                    );

                    if (isSlotBooked) {
                        logger.warn("Slot is already booked for doctorId: {}, date: {}, time: {} - {}",
                                userWebModel.getDoctorId(),
                                userWebModel.getAppointmentDate(),
                                userWebModel.getSlotStartTime(),
                                userWebModel.getSlotEndTime()
                        );
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(new Response(0, "Fail", "The selected slot on " 
                                        + userWebModel.getAppointmentDate() + " is already booked."));
                    }
                }

                // Fetch patient record
                PatientDetails db = patientDetailsRepository.findByIds(userWebModel.getPatientDetailsId());

                // Book appointment
                PatientAppointmentTable appointment = bookAppointment(userWebModel, db);
                if (appointment == null) {
                    return ResponseEntity.badRequest()
                            .body(new Response(0, "Fail", "Slot already booked"));
                }

                // ✅ Update slot status in doctorSlotSplitTime
                if (userWebModel.getDoctorSlotSpiltTimeId() != null) {
                    Optional<DoctorSlotSpiltTime> optionalSlot = doctorSlotSplitTimeRepository.findById(userWebModel.getDoctorSlotSpiltTimeId());
                    if (optionalSlot.isPresent()) {
                        DoctorSlotSpiltTime slot = optionalSlot.get();
                        slot.setSlotStatus("Booked");
                        slot.setUpdatedBy(userWebModel.getCreatedBy());
                        slot.setUpdatedOn(new Date());
                        doctorSlotSplitTimeRepository.save(slot);
                        logger.info("Marked doctorSlotSplitTimeId {} as Booked", userWebModel.getDoctorSlotSpiltTimeId());
                    } else {
                        logger.warn("doctorSlotSplitTimeId {} not found for updating status", userWebModel.getDoctorSlotSpiltTimeId());
                    }
                }

                return ResponseEntity.ok(new Response(1, "Success", "Online registered successfully"));

            } catch (Exception e) {
                logger.error("Registration failed: {}", e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new Response(0, "Fail", "Something went wrong during registration"));
            }
        }

        @Override
        public ResponseEntity<?> patientSubChildRegister(PatientDetailsWebModel userWebModel) {
            try {
                if (userWebModel == null) {
                    return ResponseEntity.badRequest().body(
                        new Response(0, "Invalid request data", null));
                }

                // Check if sub-child already exists
                Optional<PatientSubChildDetails> existingPatient = 
                    patientSubChildDetailsRepository.findByPatientDetailsIdAndPatientName(
                        userWebModel.getPatientDetailsId(), 
                        userWebModel.getPatientName()
                    );

                if (existingPatient.isPresent()) {
                    return ResponseEntity.ok(
                        new Response(0, "Sub-child patient already exists", null));
                }

                // Build new entity
                PatientSubChildDetails patientSubChild = PatientSubChildDetails.builder()
                    .patientName(userWebModel.getPatientName())
                    .dob(userWebModel.getDob())
                    .gender(userWebModel.getGender())
                    .bloodGroup(userWebModel.getBloodGroup())
                    .relationshipType(userWebModel.getRelationshipType())
                    .patientDetailsId(userWebModel.getPatientDetailsId())
                    .address(userWebModel.getAddress())
                    .currentAddress(userWebModel.getCurrentAddress())
                    .emergencyContact(userWebModel.getEmergencyContact())
                    .purposeOfVisit(userWebModel.getPurposeOfVisit())
                    .doctorId(userWebModel.getDoctorId())
                    .previousMedicalHistory(userWebModel.getPreviousMedicalHistory())
                    .insuranceDetails(userWebModel.getInsuranceDetails())
                    .insurerName(userWebModel.getInsurerName())
                    .insuranceProvider(userWebModel.getInsuranceProvider())
                    .policyNumber(userWebModel.getPolicyNumber())
                    .disability(userWebModel.getDisability())
                    .age(userWebModel.getAge())
                    .userIsActive(true)
                    .createdBy(userWebModel.getCreatedBy() != null ? userWebModel.getCreatedBy() : 1)
                    .userCreatedOn(new Date())
                    .build();

                // Save patient entity
                PatientSubChildDetails savedPatient = patientSubChildDetailsRepository.save(patientSubChild);

                // Save any associated media files
                savePatientMediaFiless(userWebModel, savedPatient);

                return ResponseEntity.ok(new Response(1, "success", "Sub-child patient registered successfully"));

            } catch (Exception e) {
                logger.error("Error in patientSubChildRegister: {}", e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response(0, "Error registering sub-child patient: " + e.getMessage(), null));
            }
        }

        private void savePatientMediaFiless(PatientDetailsWebModel userWebModel, PatientSubChildDetails savedPatient) {
            if (!Utility.isNullOrEmptyList(userWebModel.getFiles())) {
                FileInputWebModel fileInput = FileInputWebModel.builder()
                    .category(MediaFileCategory.patientSubChildDocument)
                    .categoryRefId(savedPatient.getPatientSubChildDetailsId()) // Use getId() of sub-child, not patientDetailsId
                    .files(userWebModel.getFiles())
                    .build();

                User createdByUser = userRepository.findById(userWebModel.getCreatedBy())
                    .orElse(null);

                mediaFilesService.saveMediaFiles(fileInput, createdByUser);
            }
        }

}


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
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import com.annular.healthCare.model.DoctorSlotTime;
import com.annular.healthCare.model.MediaFileCategory;
import com.annular.healthCare.model.PatientAppointmentTable;
import com.annular.healthCare.model.PatientDetails;
import com.annular.healthCare.model.User;
import com.annular.healthCare.repository.DoctorSlotTimeRepository;
import com.annular.healthCare.repository.PatientAppoitmentTablerepository;
import com.annular.healthCare.repository.PatientDetailsRepository;
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
	
	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");


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

	        // Save media files if any
	        savePatientMediaFiles(userWebModel, savedPatient);

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

//	    private PatientAppointmentTable bookAppointment(PatientDetailsWebModel userWebModel, PatientDetails savedPatient) {
//	        if (userWebModel.getDoctorId() == null || userWebModel.getAppointmentDate() == null) {
//	            throw new IllegalArgumentException("Doctor ID and Appointment Date are required.");
//	        }
//
//	        // Fetch required entities
//	        User doctor = userRepository.findById(userWebModel.getDoctorId())
//	                .orElseThrow(() -> new RuntimeException("Doctor not found"));
//	        User patient = userRepository.findById(savedPatient.getPatientDetailsId())
//	                .orElseThrow(() -> new RuntimeException("Patient user not found"));
//
//	        DoctorSlotTime doctorSlotTime = doctorSlotTimeRepository.findById(userWebModel.getTimeSlotId())
//	                .orElseThrow(() -> new RuntimeException("Doctor slot time not found"));
//
//	        // Parse time slot details
//	        LocalTime startTime = parseTime(doctorSlotTime.getSlotStartTime());
//	        LocalTime endTime = parseTime(doctorSlotTime.getSlotEndTime());
//
//	        int slotDuration;
//	        try {
//	            slotDuration = Integer.parseInt(userWebModel.getSlotTime().replaceAll("[^0-9]", ""));
//	        } catch (NumberFormatException e) {
//	            logger.error("Invalid slot time format: {}", userWebModel.getSlotTime(), e);
//	            throw new IllegalArgumentException("Invalid slot time format. Please provide a numeric value (e.g., '15').");
//	        }
//
//	        while (startTime.isBefore(endTime)) {
//	            LocalTime nextSlotEndTime = startTime.plusMinutes(slotDuration);
//	            if (nextSlotEndTime.isAfter(endTime)) {
//	                break;
//	            }
//
//	            boolean isBooked = patientAppointmentRepository.isSlotBooked(
//	                    userWebModel.getDoctorSlotId(),
//	                    userWebModel.getDaySlotId(),
//	                    startTime.toString(),
//	                    nextSlotEndTime.toString()
//	            );
//
//	            if (!isBooked) {
//	                // Create and return the appointment immediately
//	                PatientAppointmentTable appointment = PatientAppointmentTable.builder()
//	                        .doctor(doctor)
//	                        .patient(patient)
//	                        .doctorSlotId(userWebModel.getDoctorSlotId())
//	                        .daySlotId(userWebModel.getDaySlotId())
//	                        .timeSlotId(userWebModel.getTimeSlotId())
//	                        .appointmentDate(userWebModel.getAppointmentDate())
//	                        .slotStartTime(startTime.toString())
//	                        .slotEndTime(nextSlotEndTime.toString())
//	                        .slotTime(userWebModel.getSlotTime())
//	                        .isActive(true)
//	                        .doctorSlotStartTime(doctorSlotTime.getSlotStartTime())
//	                        .doctorSlotEndTime(doctorSlotTime.getSlotEndTime())
//	                        .createdBy(userWebModel.getCreatedBy())
//	                        .appointmentStatus("SCHEDULED")
//	                        .patientNotes(userWebModel.getPatientNotes())
//	                        .build();
//
//	                return patientAppointmentRepository.save(appointment);
//	            } else {
//	                logger.warn("Slot already booked: {} - {}", startTime, nextSlotEndTime);
//	            }
//
//	            startTime = nextSlotEndTime; // Move to next slot
//	        }
//
//	        return null; // No available slot found
//	    }
	    
	 // Helper method to format LocalTime to "hh:mm a" format (e.g., "07:45 PM")
	    private String formatTime(LocalTime time) {
	        return time.format(TIME_FORMATTER);
	    }
	    private PatientAppointmentTable bookAppointment(PatientDetailsWebModel userWebModel, PatientDetails savedPatient) {
	        if (userWebModel.getDoctorId() == null || userWebModel.getAppointmentDate() == null) {
	            throw new IllegalArgumentException("Doctor ID and Appointment Date are required.");
	        }

	        // Fetch required entities
	        User doctor = userRepository.findById(userWebModel.getDoctorId())
	                .orElseThrow(() -> new RuntimeException("Doctor not found"));
	        User patient = userRepository.findById(savedPatient.getPatientDetailsId())
	                .orElseThrow(() -> new RuntimeException("Patient user not found"));

	        DoctorSlotTime doctorSlotTime = doctorSlotTimeRepository.findById(userWebModel.getTimeSlotId())
	                .orElseThrow(() -> new RuntimeException("Doctor slot time not found"));

	        // Parse time slot details
	        LocalTime startTime = parseTime(doctorSlotTime.getSlotStartTime());
	        LocalTime endTime = parseTime(doctorSlotTime.getSlotEndTime());

	        int slotDuration;
	        try {
	            slotDuration = Integer.parseInt(userWebModel.getSlotTime().replaceAll("[^0-9]", ""));
	        } catch (NumberFormatException e) {
	            logger.error("Invalid slot time format: {}", userWebModel.getSlotTime(), e);
	            throw new IllegalArgumentException("Invalid slot time format. Please provide a numeric value (e.g., '15').");
	        }

	        while (startTime.isBefore(endTime)) {
	            LocalTime nextSlotEndTime = startTime.plusMinutes(slotDuration);
	            if (nextSlotEndTime.isAfter(endTime)) {
	                break;
	            }

	            boolean isBooked = patientAppointmentRepository.isSlotBooked(
	                    userWebModel.getDoctorSlotId(),
	                    userWebModel.getDaySlotId(),
	                    formatTime(startTime),  // Save formatted time
	                    formatTime(nextSlotEndTime)  // Save formatted time
	            );

	            if (!isBooked) {
	                // Format times to "hh:mm a"
	                String formattedStartTime = formatTime(startTime);
	                String formattedEndTime = formatTime(nextSlotEndTime);

	                // Create and return the appointment immediately
	                PatientAppointmentTable appointment = PatientAppointmentTable.builder()
	                        .doctor(doctor)
	                        .patient(patient)
	                        .doctorSlotId(userWebModel.getDoctorSlotId())
	                        .daySlotId(userWebModel.getDaySlotId())
	                        .timeSlotId(userWebModel.getTimeSlotId())
	                        .appointmentDate(userWebModel.getAppointmentDate())
	                        .slotStartTime(formattedStartTime)  // Saving formatted start time
	                        .slotEndTime(formattedEndTime)  // Saving formatted end time
	                        .slotTime(userWebModel.getSlotTime())
	                        .isActive(true)
	                        .doctorSlotStartTime(formatTime(parseTime(doctorSlotTime.getSlotStartTime())))
	                        .doctorSlotEndTime(formatTime(parseTime(doctorSlotTime.getSlotEndTime())))
	                        .createdBy(userWebModel.getCreatedBy())
	                        .appointmentStatus("SCHEDULED")
	                        .patientNotes(userWebModel.getPatientNotes())
	                        .build();

	                return patientAppointmentRepository.save(appointment);
	            } else {
	                logger.warn("Slot already booked: {} - {}", formatTime(startTime), formatTime(nextSlotEndTime));
	            }

	            startTime = nextSlotEndTime; // Move to next slot
	        }

	        return null; // No available slot found
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

	        // Fetch patient appointments
	        List<PatientAppointmentTable> appointments = patientAppointmentRepository.findByPatient_UserId(patientDetailsID);

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

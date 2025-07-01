
package com.annular.healthCare.service.serviceImpl;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.persistence.Column;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.annular.healthCare.Response;
import com.annular.healthCare.Util.Base64FileUpload;
import com.annular.healthCare.Util.HealthCareConstant;
import com.annular.healthCare.Util.Utility;
import com.annular.healthCare.model.AddressData;
import com.annular.healthCare.model.DoctorRole;
import com.annular.healthCare.model.DoctorSlotSpiltTime;
import com.annular.healthCare.model.DoctorSlotTime;
import com.annular.healthCare.model.DoctorSpecialty;
import com.annular.healthCare.model.HospitalDataList;
import com.annular.healthCare.model.LabMasterData;
import com.annular.healthCare.model.MediaFile;
import com.annular.healthCare.model.MediaFileCategory;
import com.annular.healthCare.model.PatientAppointmentTable;
import com.annular.healthCare.model.PatientDetails;
import com.annular.healthCare.model.PatientMappedHospitalId;
import com.annular.healthCare.model.PatientSubChildDetails;
import com.annular.healthCare.model.SupportStaffMasterData;
import com.annular.healthCare.model.User;
import com.annular.healthCare.repository.AddressDataRepository;
import com.annular.healthCare.repository.DoctorSlotSpiltTimeRepository;
import com.annular.healthCare.repository.DoctorSlotTimeRepository;
import com.annular.healthCare.repository.DoctorSpecialityRepository;
import com.annular.healthCare.repository.HospitalDataListRepository;
import com.annular.healthCare.repository.LabMasterDataRepository;
import com.annular.healthCare.repository.MediaFileRepository;
import com.annular.healthCare.repository.PatientAppoitmentTablerepository;
import com.annular.healthCare.repository.PatientDetailsRepository;
import com.annular.healthCare.repository.PatientMappedHospitalIdRepository;
import com.annular.healthCare.repository.PatientSubChildDetailsRepository;
import com.annular.healthCare.repository.SupportStaffMasterDataRepository;
import com.annular.healthCare.repository.UserRepository;
//import com.annular.healthCare.service.MediaFileService;
import com.annular.healthCare.service.PatientDetailsService;
import com.annular.healthCare.service.SmsService;
import com.annular.healthCare.webModel.FileInputWebModel;
import com.annular.healthCare.webModel.FileOutputWebModel;
import com.annular.healthCare.webModel.PatientAppointmentWebModel;
import com.annular.healthCare.webModel.PatientDetailsWebModel;
import com.annular.healthCare.webModel.PatientSubChildDetailsWebModel;

@Service
public class PatientDetailsServiceImpl implements PatientDetailsService {

	public static final Logger logger = LoggerFactory.getLogger(PatientDetailsServiceImpl.class);

	@Autowired
	PatientDetailsRepository patientDetailsRepository;

//	@Autowired
//	MediaFileService mediaFilesService;

	@Autowired
	LabMasterDataRepository labMasterDataRepository;

	@Autowired
	PatientAppoitmentTablerepository patientAppointmentRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	DoctorSlotTimeRepository doctorSlotTimeRepository;

	@Autowired
	private SupportStaffMasterDataRepository supportStaffMasterDataRepository;

	@Autowired
	DoctorSlotSpiltTimeRepository doctorSlotSplitTimeRepository;

	@Autowired
	PatientMappedHospitalIdRepository patientMappedHospitalIdRepository;

	@Autowired
	DoctorSpecialityRepository doctorSpecialtyRepository;

	@Autowired
	PatientSubChildDetailsRepository patientSubChildDetailsRepository;

	@Autowired
	AddressDataRepository addressDataRepository;
	
	@Autowired
	MediaFileRepository mediaFileRepository;
	
	@Autowired
	HospitalDataListRepository hospitalDataListRepository;
	
	@Autowired
	private SmsService smsService;
	
	@Value("${annular.app.imageLocation}")
	private String imageLocation;

	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");


	@Transactional
	@Override
	public ResponseEntity<?> register(PatientDetailsWebModel userWebModel) {
	    try {
	        logger.info("Registering patient: {}", userWebModel.getPatientName());

	        // Validate required fields
	        if (userWebModel.getMobileNumber() == null) {
	            return ResponseEntity.badRequest()
	                    .body(new Response(0, "Fail", "mobile number are required"));
	        }

	        // Check if email already exists in the database
	        Optional<PatientDetails> existingUsers = patientDetailsRepository.findByEmailId(userWebModel.getEmailId());
	        if (existingUsers.isPresent()) {
	            return ResponseEntity.badRequest().body(new Response(0, "Fail", "Email already exists"));
	        }

	        // Check if the patient already exists
	        Optional<PatientDetails> existingUser = patientDetailsRepository
	                .findByMobileNumberAndHospitalId(userWebModel.getMobileNumber());
	        if (existingUser.isPresent()) {
	            return ResponseEntity.badRequest()
	                    .body(new Response(0, "Fail", "Mobile number is already registered for this hospital."));
	        }

	        // Check slot availability only if appointment details are provided
	        boolean hasAppointmentDetails = userWebModel.getDoctorId() != null && 
	                                      userWebModel.getAppointmentDate() != null &&
	                                      userWebModel.getDoctorSlotId() != null && 
	                                      userWebModel.getDaySlotId() != null &&
	                                      userWebModel.getSlotStartTime() != null && 
	                                      userWebModel.getSlotEndTime() != null;

	        if (hasAppointmentDetails) {
	            boolean isSlotBooked = checkIfSlotIsBooked(userWebModel.getDoctorSlotId(), userWebModel.getDaySlotId(),
	                    userWebModel.getSlotStartTime(),
	                    userWebModel.getSlotEndTime()
	            );

	            if (isSlotBooked) {
	                logger.warn("Slot is already booked for doctorId: {}, date: {}, time: {} - {}",
	                        userWebModel.getDoctorId(), userWebModel.getAppointmentDate(),
	                        userWebModel.getSlotStartTime(), userWebModel.getSlotEndTime());
	                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(0, "Fail",
	                        "The selected slot on " + userWebModel.getAppointmentDate() + " is already booked."));
	            }
	        }

	        // Create patient details (this will happen regardless of appointment details)
	        PatientDetails savedPatient = createPatientDetails(userWebModel);
	        if (savedPatient == null) {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                    .body(new Response(0, "Fail", "Failed to create patient details"));
	        }

	        // Book appointment only if appointment details are provided
	        PatientAppointmentTable appointment = null;
	        if (hasAppointmentDetails) {
	            appointment = bookAppointment(userWebModel, savedPatient);
	            if (appointment == null) {
	                // Patient is already created, but appointment booking failed
	                logger.warn("Patient created successfully but appointment booking failed for patient: {}", 
	                           savedPatient.getPatientName());
	                return ResponseEntity.badRequest().body(new Response(0, "Fail", "Patient registered but slot booking failed - slot may be already booked"));
	            }
	            
	            // Update slot status if appointment was successfully booked
	            if (userWebModel.getDoctorSlotSpiltTimeId() != null) {
	                Optional<DoctorSlotSpiltTime> optionalSlot = doctorSlotSplitTimeRepository
	                        .findById(userWebModel.getDoctorSlotSpiltTimeId());
	                if (optionalSlot.isPresent()) {
	                    DoctorSlotSpiltTime slot = optionalSlot.get();
	                    slot.setSlotStatus("Booked");
	                    slot.setUpdatedBy(userWebModel.getCreatedBy());
	                    slot.setUpdatedOn(new Date());
	                    doctorSlotSplitTimeRepository.save(slot);
	                    logger.info("Marked doctorSlotSplitTimeId {} as Booked", userWebModel.getDoctorSlotSpiltTimeId());
	                } else {
	                    logger.warn("doctorSlotSplitTimeId {} not found for updating status",
	                            userWebModel.getDoctorSlotSpiltTimeId());
	                }
	            }
	        }

	        // Save media files if any
	        savePatientMediaFiles(userWebModel, savedPatient);

	        // Send registration SMS (always sent for successful patient registration)
	        try {
	            String phoneNumber = userWebModel.getMobileNumber();
	            String smsMessage = "You have successfully registered with Aegle Healthcare. Your health is important to us! Stay in touch.";
	            logger.info("Sending registration SMS to: {}", phoneNumber);
	            smsService.sendSms(phoneNumber, smsMessage);
	            logger.info("Registration SMS sent successfully to: {}", phoneNumber);
	        } catch (Exception e) {
	            logger.error("Registration SMS sending failed: {}", e.getMessage(), e);
	        }

	        // Send appointment SMS only if appointment was successfully booked
	        if (appointment != null) {
	            try {
	                // Get all necessary data with safeguards
	                String phoneNumber = userWebModel.getMobileNumber();
	                String appointmentDate = userWebModel.getAppointmentDate() != null ? 
	                    String.valueOf(userWebModel.getAppointmentDate()) : "scheduled date";
	                String startTime = userWebModel.getSlotStartTime() != null ? 
	                    userWebModel.getSlotStartTime() : "scheduled time";
	                String endTime = userWebModel.getSlotEndTime() != null ? 
	                    userWebModel.getSlotEndTime() : "end time";
	                
	                // Declare variables outside of Optional chains
	                String doctorName = "Your doctor";
	                String hospitalName = "our hospital";
	                
	                // Get doctor name safely
	                if (userWebModel.getDoctorId() != null) {
	                    Optional<User> doctorOpt = userRepository.findById(userWebModel.getDoctorId());
	                    if (doctorOpt.isPresent()) {
	                        User doctor = doctorOpt.get();
	                        if (doctor.getUserName() != null && !doctor.getUserName().isEmpty()) {
	                            doctorName = doctor.getUserName();
	                        }
	                    }
	                }
	                
	                // Get hospital name safely
	                if (userWebModel.getHospitalId() != null) {
	                    Optional<HospitalDataList> hospitalOpt = hospitalDataListRepository.findById(userWebModel.getHospitalId());
	                    if (hospitalOpt.isPresent()) {
	                        HospitalDataList hospital = hospitalOpt.get();
	                        if (hospital.getHospitalName() != null && !hospital.getHospitalName().isEmpty()) {
	                            hospitalName = hospital.getHospitalName();
	                        }
	                    }
	                }
	                
	                // Build the message
	                StringBuilder smsBuilder = new StringBuilder();
	                smsBuilder.append("Your appointment is confirmed on ");
	                smsBuilder.append(appointmentDate);
	                smsBuilder.append(" (");
	                smsBuilder.append(startTime);
	                smsBuilder.append(" - ");
	                smsBuilder.append(endTime);
	                smsBuilder.append(") with Dr. ");
	                smsBuilder.append(doctorName);
	                smsBuilder.append(" at ");
	                smsBuilder.append(hospitalName);
	                smsBuilder.append(". Thank you!");
	                
	                String smsMessage = smsBuilder.toString();
	                
	                // Send the SMS
	                logger.info("About to send appointment SMS to: " + phoneNumber);
	                logger.info("SMS content: " + smsMessage);
	                
	                smsService.sendSms(phoneNumber, smsMessage);
	                logger.info("Appointment SMS sent successfully");
	                
	            } catch (Exception e) {
	                logger.error("Failed to send appointment SMS: " + e.getMessage());
	                e.printStackTrace();
	                // Continue execution - don't let SMS failure stop the process
	            }
	        }
	        
	        // Return success message based on whether appointment was also booked
	        String successMessage = hasAppointmentDetails && appointment != null ? 
	            "Patient registered and appointment booked successfully" : 
	            "Patient registered successfully";
	            
	        return ResponseEntity.ok(new Response(1, "Success", successMessage));
	        
	    } catch (Exception e) {
	        logger.error("Registration failed: {}", e.getMessage(), e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(0, "Fail", "Something went wrong during registration"));
	    }
	}
	public boolean checkIfSlotIsBooked(Integer doctorSlotId, Integer daySlotId, String slotStartTime,
			String slotEndTime) {
		try {
			boolean isBooked = patientAppointmentRepository.isSlotBooked(doctorSlotId, daySlotId, slotStartTime,
					slotEndTime);

			logger.info(
					"Slot availability check - DoctorSlotId: {}, DaySlotId: {}, StartTime: {}, EndTime: {} - Status: {}",
					doctorSlotId, daySlotId, slotStartTime, slotEndTime, isBooked);
			return isBooked;
		} catch (Exception e) {
			logger.error("Error checking slot availability: {}", e.getMessage(), e);
			return false; // Assume slot is available if query fails
		}
	}

	private PatientDetails createPatientDetails(PatientDetailsWebModel userWebModel) {

		// Save patient details first
		PatientDetails savedPatient = patientDetailsRepository.save(PatientDetails.builder()
				.firstName(userWebModel.getFirstName()).lastName(userWebModel.getLastNmae())
				.patientName((userWebModel.getFirstName() + " " + userWebModel.getLastNmae()).trim())
                .dob(userWebModel.getDob()).age(userWebModel.getAge())
				.otp(100).gender(userWebModel.getGender()).bloodGroup(userWebModel.getBloodGroup())
				.countryCode(userWebModel.getCountryCode())
				.emerCountryCode(userWebModel.getEmerCountryCode())
				.mobileNumber(userWebModel.getMobileNumber()).emailId(userWebModel.getEmailId())
				.address(userWebModel.getAddress()).currentAddress(userWebModel.getCurrentAddress())
				.emergencyName(userWebModel.getEmergencyName()).emergencyRelationship(userWebModel.getEmergencyRelationship())
				.emergencyContact(userWebModel.getEmergencyContact()).purposeOfVisit(userWebModel.getPurposeOfVisit())
				.doctorId(userWebModel.getDoctorId()).userIsActive(true).createdBy(userWebModel.getCreatedBy())
				.userCreatedOn(new Date()).previousMedicalHistory(userWebModel.getPreviousMedicalHistory())
				.insuranceDetails(userWebModel.getInsuranceDetails()).insurerName(userWebModel.getInsurerName())
				.insuranceProvider(userWebModel.getInsuranceProvider()).policyNumber(userWebModel.getPolicyNumber())
				.disability(userWebModel.getDisability()).build());

		// Now save patient-hospital mapping
		PatientMappedHospitalId mapped = PatientMappedHospitalId.builder().createdBy(userWebModel.getCreatedBy())
				.userIsActive(true).userUpdatedBy(userWebModel.getCreatedBy())
				.patientId(savedPatient.getPatientDetailsId()).hospitalId(userWebModel.getHospitalId())
				.medicalHistoryStatus(true).personalDataStatus(true).build();

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
		boolean isSlotAlreadyBooked = patientAppointmentRepository.isSlotBooked(userWebModel.getDoctorSlotId(),
				userWebModel.getDaySlotId(), inputSlotStartTime, inputSlotEndTime);

		if (isSlotAlreadyBooked) {
			logger.warn("Slot is already booked: DoctorSlotId: {}, DaySlotId: {}, StartTime: {}, EndTime: {}",
					userWebModel.getDoctorSlotId(), userWebModel.getDaySlotId(), inputSlotStartTime, inputSlotEndTime);

			return null; // Slot is already booked
		}
		// Get the appointment type from the input
		String appointmentType = userWebModel.getAppointmentType();

		// Count existing appointments for the doctor on the same date with the given
		// appointment type
		int newToken = patientAppointmentRepository.countByAppointmentDateAndDoctorIdAndAppointmentType(
				userWebModel.getAppointmentDate(), doctor.getUserId(), appointmentType) + 1;

		// If not booked, proceed with creating the appointment
		PatientAppointmentTable appointment = PatientAppointmentTable.builder().doctor(doctor)
				.age(userWebModel.getAge()).dateOfBirth(userWebModel.getDateOfBirth())
				.patientName(userWebModel.getPatientName()).relationShipType(userWebModel.getRelationshipType())
				.patient(patient).doctorSlotId(userWebModel.getDoctorSlotId()).token(String.valueOf(newToken)) // Assigning
																												// token
	             .doctorSlotSpiltTimeId(userWebModel.getDoctorSlotSpiltTimeId())																	// sequentially
				.daySlotId(userWebModel.getDaySlotId()).timeSlotId(userWebModel.getTimeSlotId())
				.appointmentDate(userWebModel.getAppointmentDate()).slotStartTime(inputSlotStartTime)
				.slotEndTime(inputSlotEndTime).slotTime(userWebModel.getSlotTime()).isActive(true)
				.doctorSlotStartTime(doctorSlotTime.getSlotStartTime())
				.doctorSlotEndTime(doctorSlotTime.getSlotEndTime()).createdBy(userWebModel.getCreatedBy())
				.appointmentStatus("SCHEDULED").appointmentType(userWebModel.getAppointmentType())
				.patientNotes(userWebModel.getPatientNotes()).build();

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
		formatters.add(DateTimeFormatter.ofPattern("h:mm a")); // 6:45 PM
		formatters.add(DateTimeFormatter.ofPattern("hh:mm a")); // 06:45 PM
		formatters.add(DateTimeFormatter.ofPattern("H:mm")); // 18:45
		formatters.add(DateTimeFormatter.ofPattern("HH:mm")); // 18:45
		formatters.add(DateTimeFormatter.ISO_LOCAL_TIME);

		// Try each formatter
		for (DateTimeFormatter formatter : formatters) {
			try {
				LocalTime parsedTime = LocalTime.parse(timeString, formatter);
				logger.info("Successfully parsed time: {} using format: {}", timeString, formatter.toString());
				return parsedTime;
			} catch (DateTimeParseException e) {
				logger.debug("Failed to parse '{}' with formatter {}: {}", timeString, formatter, e.getMessage());
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
		throw new IllegalArgumentException(
				"Unable to parse time: " + timeString + ". Supported formats include: h:mm a, HH:mm, etc.");
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
		patterns.add(Pattern.compile("(\\d{1,2}):(\\d{2})\\s*([AP]M)")); // 6:45 PM
		patterns.add(Pattern.compile("(\\d{1,2})\\s*([AP]M)")); // 6 PM
		patterns.add(Pattern.compile("(\\d{1,2}):(\\d{2})")); // 18:45

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
	        List<MultipartFile> files = userWebModel.getFiles();
	        List<MediaFile> filesList = new ArrayList<>();

	        for (MultipartFile file : files) {
	            try {
	                byte[] fileBytes = file.getBytes();
	                String base64Data = Base64.getEncoder().encodeToString(fileBytes);
	                String fileName = UUID.randomUUID().toString();

	                MediaFile mediaFile = new MediaFile();
	                mediaFile.setFileName(fileName);
	                mediaFile.setFileOriginalName(file.getOriginalFilename());
	                
	                mediaFile.setFileType(file.getContentType());
	                mediaFile.setCategory(MediaFileCategory.patientDocument);
	                mediaFile.setFileDomainId(HealthCareConstant.patientDocument);
	                mediaFile.setFileDomainReferenceId(savedPatient.getPatientDetailsId());
	                mediaFile.setFileIsActive(true);
	                mediaFile.setFileCreatedBy(userWebModel.getCreatedBy());

	                mediaFile = mediaFileRepository.save(mediaFile);
	                filesList.add(mediaFile);

	                // Save file to filesystem
	                Base64FileUpload.saveFile(imageLocation + "/patientDocument", base64Data, fileName);

	            } catch (IOException e) {
	                e.printStackTrace(); // Use proper logging in production
	            }
	        }
	    }
	}

	@Override
	public ResponseEntity<?> getAllPatientDetails(Integer hospitalId, Integer pageNo, Integer pageSize) {
	    Map<String, Object> response = new HashMap<>();
	    try {
	        // Validate pageNo and pageSize
	        if (pageNo == null || pageNo < 1) pageNo = 1;
	        if (pageSize == null || pageSize < 1) pageSize = 10;

	        // Step 1: Get all mapped patients for hospital
	        List<PatientMappedHospitalId> mappings = patientMappedHospitalIdRepository.findByHospitalId(hospitalId);

	        if (mappings.isEmpty()) {
	            response.put("status", 0);
	            response.put("message", "No patients found for the given hospital.");
	            return ResponseEntity.ok(response);
	        }

	        // Step 2: Extract patient IDs
	        List<Integer> patientIds = mappings.stream()
	                .map(PatientMappedHospitalId::getPatientId)
	                .collect(Collectors.toList());

	        // Step 3: Fetch and sort patients by created date (newest first)
	        List<PatientDetails> sortedPatients = patientDetailsRepository.findAllById(patientIds).stream()
	                .sorted(Comparator.comparing(PatientDetails::getUserCreatedOn).reversed())
	                .collect(Collectors.toList());

	        // Step 4: Pagination logic (1-based)
	        int totalElements = sortedPatients.size();
	        int fromIndex = (pageNo - 1) * pageSize;
	        int toIndex = Math.min(fromIndex + pageSize, totalElements);

	        if (fromIndex >= totalElements) {
	            response.put("status", 0);
	            response.put("message", "Page number out of range");
	            response.put("data", new ArrayList<>());
	            return ResponseEntity.ok(response);
	        }

	        List<PatientDetails> paginatedList = sortedPatients.subList(fromIndex, toIndex);

	        // Step 5: Build response data
	        List<Map<String, Object>> patientList = new ArrayList<>();
	        for (PatientDetails patient : paginatedList) {
	            Map<String, Object> patientData = new HashMap<>();
	            patientData.put("patientDetailsId", patient.getPatientDetailsId());
	            patientData.put("firstName", patient.getFirstName());
	            patientData.put("lastName", patient.getLastName());
	            patientData.put("patientName", patient.getPatientName());
	            patientData.put("dob", patient.getDob());
	            patientData.put("gender", patient.getGender());
	            patientData.put("bloodGroup", patient.getBloodGroup());
	            patientData.put("mobileNumber", patient.getMobileNumber());
	            patientData.put("emailId", patient.getEmailId());
	            patientData.put("address", patient.getAddress());
	            patientData.put("currentAddress", patient.getCurrentAddress());
	            patientData.put("emergencyContact", patient.getEmergencyContact());
	            patientData.put("hospitalId", hospitalId);
	            patientData.put("purposeOfVisit", patient.getPurposeOfVisit());
	            patientData.put("emergencyName", patient.getEmergencyName());
	            patientData.put("emergencyRelationship", patient.getEmergencyRelationship());
	            patientData.put("doctorId", patient.getDoctorId());
	            patientData.put("userIsActive", patient.getUserIsActive());
	            patientData.put("countryCode", patient.getCountryCode());
	            patientData.put("emerCountryCode", patient.getCountryCode()); // adjust if different
	            patientData.put("createdBy", patient.getCreatedBy());
	            patientData.put("userCreatedOn", patient.getUserCreatedOn());
	            patientData.put("previousMedicalHistory", patient.getPreviousMedicalHistory());
	            patientData.put("insuranceDetails", patient.getInsuranceDetails());
	            patientData.put("insurerName", patient.getInsurerName());
	            patientData.put("insuranceProvider", patient.getInsuranceProvider());
	            patientData.put("policyNumber", patient.getPolicyNumber());

	            patientList.add(patientData);
	        }

	        // Step 6: Response
	        response.put("status", 1);
	        response.put("message", "Success");
	        response.put("data", patientList);
	        response.put("totalElements", totalElements);
	        response.put("pageNo", pageNo);
	        response.put("pageSize", pageSize);
	        response.put("fetchedRecords", paginatedList.size());

	        return ResponseEntity.ok(response);

	    } catch (Exception e) {
	        response.put("status", 0);
	        response.put("message", "Error retrieving patient details");
	        response.put("error", e.getMessage());
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	    }
	}
	@Transactional
	@Override
	public ResponseEntity<?> updatePatientDetails(PatientDetailsWebModel userWebModel) {
	    try {
	        logger.info("Updating patient details for ID: {}", userWebModel.getPatientDetailsId());
	        
	        

	        if (userWebModel.getPatientDetailsId() == null) {
	            return ResponseEntity.badRequest().body(new Response(0, "Fail", "Patient ID is required for update"));
	        }

	        Optional<PatientDetails> optionalPatient = patientDetailsRepository.findById(userWebModel.getPatientDetailsId());
	        if (optionalPatient.isEmpty()) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                    .body(new Response(0, "Fail", "Patient not found with ID: " + userWebModel.getPatientDetailsId()));
	        }

	        PatientDetails patient = optionalPatient.get();
	        
	        // ✅ Mobile number uniqueness check
	        if (userWebModel.getMobileNumber() != null &&
	            !userWebModel.getMobileNumber().equals(patient.getMobileNumber())) {
	            
	            Optional<PatientDetails> existingWithSameMobile = patientDetailsRepository.findByMobileNumber(userWebModel.getMobileNumber());
	            if (existingWithSameMobile.isPresent() &&
	                !existingWithSameMobile.get().getPatientDetailsId().equals(patient.getPatientDetailsId())) {
	                
	                return ResponseEntity.badRequest().body(new Response(0, "Fail", "Mobile number already exists for another patient."));
	            }
	        }

	        // Update patient fields
	        Optional.ofNullable(userWebModel.getFirstName()).ifPresent(patient::setFirstName);
	        Optional.ofNullable(userWebModel.getLastNmae()).ifPresent(patient::setLastName);

	        String fullName = (Optional.ofNullable(userWebModel.getFirstName()).orElse("") + " " +
	                Optional.ofNullable(userWebModel.getLastNmae()).orElse("")).trim();
	        if (!fullName.isBlank()) patient.setPatientName(fullName);

	        Optional.ofNullable(userWebModel.getDob()).ifPresent(patient::setDob);
	        Optional.ofNullable(userWebModel.getAge()).ifPresent(patient::setAge);
	        Optional.ofNullable(userWebModel.getGender()).ifPresent(patient::setGender);
	        Optional.ofNullable(userWebModel.getBloodGroup()).ifPresent(patient::setBloodGroup);
	        Optional.ofNullable(userWebModel.getMobileNumber()).ifPresent(patient::setMobileNumber);
	        Optional.ofNullable(userWebModel.getEmailId()).ifPresent(patient::setEmailId);
	        Optional.ofNullable(userWebModel.getAddress()).ifPresent(patient::setAddress);
	        Optional.ofNullable(userWebModel.getCurrentAddress()).ifPresent(patient::setCurrentAddress);
	        Optional.ofNullable(userWebModel.getEmergencyContact()).ifPresent(patient::setEmergencyContact);
	        Optional.ofNullable(userWebModel.getEmergencyName()).ifPresent(patient::setEmergencyName);
	        Optional.ofNullable(userWebModel.getEmergencyRelationship()).ifPresent(patient::setEmergencyRelationship);
	        Optional.ofNullable(userWebModel.getPurposeOfVisit()).ifPresent(patient::setPurposeOfVisit);
	        Optional.ofNullable(userWebModel.getDoctorId()).ifPresent(patient::setDoctorId);
	        Optional.ofNullable(userWebModel.getPreviousMedicalHistory()).ifPresent(patient::setPreviousMedicalHistory);
	        Optional.ofNullable(userWebModel.getInsuranceDetails()).ifPresent(patient::setInsuranceDetails);
	        Optional.ofNullable(userWebModel.getInsurerName()).ifPresent(patient::setInsurerName);
	        Optional.ofNullable(userWebModel.getInsuranceProvider()).ifPresent(patient::setInsuranceProvider);
	        Optional.ofNullable(userWebModel.getPolicyNumber()).ifPresent(patient::setPolicyNumber);
	        Optional.ofNullable(userWebModel.getDisability()).ifPresent(patient::setDisability);
	        Optional.ofNullable(userWebModel.getCountryCode()).ifPresent(patient::setCountryCode);
	        Optional.ofNullable(userWebModel.getEmerCountryCode()).ifPresent(patient::setEmerCountryCode);

	        patient.setUserUpdatedOn(new Date());
	        if (userWebModel.getUserUpdatedBy() != null) {
	            patient.setUserUpdatedBy(userWebModel.getUserUpdatedBy());
	        }

	        patientDetailsRepository.save(patient);

	        // Handle parent files
	        if (!Utility.isNullOrEmptyList(userWebModel.getFiles())) {
	            mediaFileRepository.markFilesInactiveByCategoryAndRefId(
	                    MediaFileCategory.patientDocument,
	                    patient.getPatientDetailsId()
	            );
	            savePatientMediaFiless(userWebModel, patient, MediaFileCategory.patientDocument, patient.getPatientDetailsId());
	        }

	        // Update sub-child details
	        if (!Utility.isNullOrEmptyList(userWebModel.getChildDetailsList())) {
	            for (PatientSubChildDetails child : userWebModel.getChildDetailsList()) {
	                PatientSubChildDetailsWebModel childWebModel = new PatientSubChildDetailsWebModel();
	                BeanUtils.copyProperties(child, childWebModel);

	                if (child.getPatientSubChildDetailsId() != null) {
	                    Optional<PatientSubChildDetails> existingChildOpt =
	                            patientSubChildDetailsRepository.findById(child.getPatientSubChildDetailsId());

	                    if (existingChildOpt.isPresent()) {
	                        PatientSubChildDetails existingChild = existingChildOpt.get();

	                        Optional.ofNullable(childWebModel.getPatientName()).ifPresent(existingChild::setPatientName);
	                        Optional.ofNullable(childWebModel.getDob()).ifPresent(existingChild::setDob);
	                        Optional.ofNullable(childWebModel.getGender()).ifPresent(existingChild::setGender);
	                        Optional.ofNullable(childWebModel.getBloodGroup()).ifPresent(existingChild::setBloodGroup);
	                        Optional.ofNullable(childWebModel.getAddress()).ifPresent(existingChild::setAddress);
	                        Optional.ofNullable(childWebModel.getEmergencyContact()).ifPresent(existingChild::setEmergencyContact);
	                        Optional.ofNullable(childWebModel.getPurposeOfVisit()).ifPresent(existingChild::setPurposeOfVisit);
	                        Optional.ofNullable(childWebModel.getDoctorId()).ifPresent(existingChild::setDoctorId);
	                        Optional.ofNullable(childWebModel.getUserIsActive()).ifPresent(existingChild::setUserIsActive);
	                        Optional.ofNullable(childWebModel.getCurrentAddress()).ifPresent(existingChild::setCurrentAddress);
	                        Optional.ofNullable(childWebModel.getPreviousMedicalHistory()).ifPresent(existingChild::setPreviousMedicalHistory);
	                        Optional.ofNullable(childWebModel.getInsuranceDetails()).ifPresent(existingChild::setInsuranceDetails);
	                        Optional.ofNullable(childWebModel.getInsurerName()).ifPresent(existingChild::setInsurerName);
	                        Optional.ofNullable(childWebModel.getInsuranceProvider()).ifPresent(existingChild::setInsuranceProvider);
	                        Optional.ofNullable(childWebModel.getPolicyNumber()).ifPresent(existingChild::setPolicyNumber);
	                        Optional.ofNullable(childWebModel.getDisability()).ifPresent(existingChild::setDisability);
	                        Optional.ofNullable(childWebModel.getAge()).ifPresent(existingChild::setAge);
	                        Optional.ofNullable(childWebModel.getRelationshipType()).ifPresent(existingChild::setRelationshipType);

	                        existingChild.setUserUpdatedOn(new Date());
	                        if (userWebModel.getUserUpdatedBy() != null) {
	                            existingChild.setUserUpdatedBy(userWebModel.getUserUpdatedBy());
	                        }

	                        patientSubChildDetailsRepository.save(existingChild);


	            	        if (!Utility.isNullOrEmptyList(userWebModel.getFiles())) {
	            	            mediaFileRepository.markFilesInactiveByCategoryAndRefId(
	            	                    MediaFileCategory.patientSubChildDocument,
	            	                    existingChild.getPatientSubChildDetailsId()
	            	            );
	            	            savePatientMediaFiless(userWebModel, patient, MediaFileCategory.patientSubChildDocument, existingChild.getPatientSubChildDetailsId());
	            	        }
	                    }
	                }
	            }
	        }

	        return ResponseEntity.ok(new Response(1, "Success", "Patient updated successfully"));
	    } catch (Exception e) {
	        logger.error("Patient update failed", e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(0, "Fail", "Something went wrong during update"));
	    }
	}


	private void savePatientMediaFiless(
		    PatientDetailsWebModel userWebModel,
		    PatientDetails savedPatient,
		    MediaFileCategory category,
		    Integer categoryRefId) {

		    if (!Utility.isNullOrEmptyList(userWebModel.getFiles())) {
		        List<MultipartFile> files = userWebModel.getFiles();

		        for (MultipartFile file : files) {
		            try {
		                byte[] fileBytes = file.getBytes();
		                String base64Data = Base64.getEncoder().encodeToString(fileBytes);
		                String fileName = UUID.randomUUID().toString();

		                MediaFile mediaFile = new MediaFile();
		                mediaFile.setFileName(fileName);
		                mediaFile.setFileOriginalName(file.getOriginalFilename());
		                mediaFile.setFileType(file.getContentType());
		                mediaFile.setFileSize(String.valueOf(file.getSize()));

		                mediaFile.setCategory(category);

		                if (category == MediaFileCategory.patientDocument) {
		                    mediaFile.setFileDomainId(HealthCareConstant.patientDocument);
		                } else if (category == MediaFileCategory.patientSubChildDocument) {
		                    mediaFile.setFileDomainId(HealthCareConstant.patientSubChildDocument);
		                }

		                mediaFile.setFileDomainReferenceId(categoryRefId);
		                mediaFile.setFileIsActive(true);
		                mediaFile.setFileCreatedBy(userWebModel.getCreatedBy());

		                mediaFileRepository.save(mediaFile);

		                // Save to file system
		                Base64FileUpload.saveFile(imageLocation + "/" + category.name(), base64Data, fileName);

		            } catch (IOException e) {
		                logger.error("Error saving file: {}", e.getMessage(), e);
		            }
		        }
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
			webModel.setEmergencyName(patient.getEmergencyName());
			webModel.setEmergencyRelationship(patient.getEmergencyRelationship());
			webModel.setCountryCode(patient.getCountryCode());
			webModel.setEmerCountryCode(patient.getEmerCountryCode());

			// ✅ Retrieve media files (Profile Photo)
			List<MediaFile> files = mediaFileRepository.findByFileDomainIdAndFileDomainReferenceId(
					HealthCareConstant.patientDocument, patient.getPatientDetailsId());

			ArrayList<FileInputWebModel> filesInputWebModel = new ArrayList<>();
			for (MediaFile mediaFile : files) {
				FileInputWebModel filesInput = new FileInputWebModel();
				filesInput.setFileName(mediaFile.getFileOriginalName());
				filesInput.setFileId(mediaFile.getFileId());
				filesInput.setFileSize(mediaFile.getFileSize());
				filesInput.setFileType(mediaFile.getFileType());

				String fileData = Base64FileUpload.encodeToBase64String(
						imageLocation + "/patientDocument", mediaFile.getFileName());
				System.out.println("fileData---------->"+fileData);
				filesInput.setFileData(fileData);

				filesInputWebModel.add(filesInput);
			}

			// ✅ Set media files to response model
			webModel.setMediaFiles(filesInputWebModel);

			// ✅ Fetch appointments
			List<PatientAppointmentTable> appointments = patientAppointmentRepository
					.findByPatient_PatientDetailsId(patientDetailsID);

			List<PatientAppointmentWebModel> appointmentWebModels = appointments.stream().map(appointment -> {
				PatientAppointmentWebModel model = new PatientAppointmentWebModel();
				model.setAppointmentId(appointment.getAppointmentId());
				model.setDoctorId(appointment.getDoctor().getUserId());
				model.setDoctorSlotId(appointment.getDoctorSlotId());
				model.setDaySlotId(appointment.getDaySlotId());
				model.setTimeSlotId(appointment.getTimeSlotId());
				model.setAppointmentDate(appointment.getAppointmentDate());
				model.setSlotStartTime(appointment.getSlotStartTime());
				model.setSlotEndTime(appointment.getSlotEndTime());
				model.setSlotTime(appointment.getSlotTime());
				model.setIsActive(appointment.getIsActive());
				model.setCreatedBy(appointment.getCreatedBy());
				model.setCreatedOn(appointment.getCreatedOn());
				model.setUpdatedBy(appointment.getUpdatedBy());
				model.setUpdatedOn(appointment.getUpdatedOn());
				model.setAppointmentStatus(appointment.getAppointmentStatus());
				model.setPatientNotes(appointment.getPatientNotes());
				return model;
			}).collect(Collectors.toList());
			webModel.setAppointments(appointmentWebModels);

			// ✅ Fetch sub-child patient details
			List<PatientSubChildDetails> subChildren = patientSubChildDetailsRepository
					.findByPatientDetailsId(patientDetailsID);
			List<PatientSubChildDetailsWebModel> subChildWebModels = subChildren.stream().map(child -> {
				PatientSubChildDetailsWebModel model = new PatientSubChildDetailsWebModel();
				model.setPatientSubChildDetailsId(child.getPatientSubChildDetailsId());
				model.setPatientName(child.getPatientName());
				model.setDob(child.getDob());
				model.setGender(child.getGender());
				model.setBloodGroup(child.getBloodGroup());
				model.setAddress(child.getAddress());
				model.setEmergencyContact(child.getEmergencyContact());
				model.setPurposeOfVisit(child.getPurposeOfVisit());
				model.setDoctorId(child.getDoctorId());
				model.setUserIsActive(child.getUserIsActive());
				model.setCurrentAddress(child.getCurrentAddress());
				model.setCreatedBy(child.getCreatedBy());
				model.setUserCreatedOn(child.getUserCreatedOn());
				model.setUserUpdatedBy(child.getUserUpdatedBy());
				model.setUserUpdatedOn(child.getUserUpdatedOn());
				model.setPreviousMedicalHistory(child.getPreviousMedicalHistory());
				model.setInsuranceDetails(child.getInsuranceDetails());
				model.setInsurerName(child.getInsurerName());
				model.setInsuranceProvider(child.getInsuranceProvider());
				model.setPolicyNumber(child.getPolicyNumber());
				model.setDisability(child.getDisability());
				model.setAge(child.getAge());
				model.setRelationshipType(child.getRelationshipType());

				// ✅ Retrieve media files for sub-child
				List<MediaFile> mediaFiles = mediaFileRepository.findByFileDomainIdAndFileDomainReferenceId(
						HealthCareConstant.patientSubChildDocument, child.getPatientSubChildDetailsId());

				List<FileInputWebModel> fileInputWebModels = new ArrayList<>();
				for (MediaFile mediaFile : mediaFiles) {
					FileInputWebModel filesInput = new FileInputWebModel();
					filesInput.setFileName(mediaFile.getFileOriginalName());
					filesInput.setFileId(mediaFile.getFileId());
					filesInput.setFileSize(mediaFile.getFileSize());
					filesInput.setFileType(mediaFile.getFileType());

					String fileData = null;
					try {
						fileData = Base64FileUpload.encodeToBase64String(
								imageLocation + "/patientSubChildDocument", mediaFile.getFileName());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.println("fileData---------->" + fileData);
					filesInput.setFileData(fileData);

					fileInputWebModels.add(filesInput);
				}

				// ✅ Set media files to sub-child model
				model.setMediaFiless(fileInputWebModels);

				return model;
			}).collect(Collectors.toList());
			webModel.setSubChildDetails(subChildWebModels);

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

			// Assuming userType "doctor" is a String. Adjust if it's an enum or different
			// value.
			List<User> doctors = userRepository.findByHospitalIdAndUserType(hospitalId, "Doctor");

			// Transforming to only return id and name
			List<Map<String, Object>> doctorList = doctors.stream().map(doctor -> {
				Map<String, Object> map = new HashMap<>();
				map.put("id", doctor.getUserId());
				map.put("firstname", doctor.getFirstName());
				map.put("lastName", doctor.getLastName());
				// Extract role IDs
				List<Integer> roleIds = doctor.getDoctorRoles().stream().map((DoctorRole role) -> role.getRoleId()) // Explicitly
																													// defining
																													// type
						.collect(Collectors.toList());
				List<String> specialties = doctor.getDoctorRoles().stream()
					    .map(DoctorRole::getRoleId)
					    .filter(Objects::nonNull) // <- ignore null roleIds
					    .map(roleId -> {
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

//	@Override
//	public boolean deleteMediaFilesById(Integer fileId) {
//		return mediaFilesService.deleteMediaFilesByUserIdAndCategoryAndRefIds(MediaFileCategory.patientDocument,
//				fileId);
//	}

	@Override
	public ResponseEntity<?> adminPatientRegister(PatientDetailsWebModel userWebModel) {

		try {
			logger.info("Registering patient: {}", userWebModel.getPatientName());

			if (userWebModel.getMobileNumber() == null) {
				return ResponseEntity.badRequest()
						.body(new Response(0, "Fail","mobile number are required"));
			}
			// Check if the patient already exists
			Optional<PatientDetails> existingUser = patientDetailsRepository
					.findByMobileNumberAndHospitalId(userWebModel.getMobileNumber());
			if (existingUser.isPresent()) {
				return ResponseEntity.badRequest()
						.body(new Response(0, "Fail", "Mobile number is already registered for this hospital."));
			}
			String firstName = Optional.ofNullable(userWebModel.getFirstName()).orElse("");
			String lastName = Optional.ofNullable(userWebModel.getLastNmae()).orElse("");
			String fullName = (firstName + " " + lastName).trim();
			PatientDetails newPatient = PatientDetails.builder().firstName(userWebModel.getFirstName()).lastName(userWebModel.getLastNmae())
					.dob(userWebModel.getDob()).age(userWebModel.getAge()).otp(100).gender(userWebModel.getGender())
					.patientName(fullName)
					.bloodGroup(userWebModel.getBloodGroup()).mobileNumber(userWebModel.getMobileNumber())
					.emailId(userWebModel.getEmailId()).address(userWebModel.getAddress())
					.currentAddress(userWebModel.getCurrentAddress())
					.countryCode(userWebModel.getCountryCode())
					.emergencyContact(userWebModel.getEmergencyContact())
					.purposeOfVisit(userWebModel.getPurposeOfVisit()).doctorId(userWebModel.getDoctorId())
					.userIsActive(true).createdBy(userWebModel.getCreatedBy()).userCreatedOn(new Date())
					.previousMedicalHistory(userWebModel.getPreviousMedicalHistory())
					.insuranceDetails(userWebModel.getInsuranceDetails()).insurerName(userWebModel.getInsurerName())
					.insuranceProvider(userWebModel.getInsuranceProvider()).policyNumber(userWebModel.getPolicyNumber())
					.disability(userWebModel.getDisability()).build();

			// Save patient first to generate ID
			PatientDetails savedPatient = patientDetailsRepository.save(newPatient);

			PatientMappedHospitalId mapped = PatientMappedHospitalId.builder().createdBy(userWebModel.getCreatedBy())
					.userIsActive(true).userUpdatedBy(userWebModel.getCreatedBy())
					.patientId(savedPatient.getPatientDetailsId()).hospitalId(userWebModel.getHospitalId())
					.medicalHistoryStatus(true).personalDataStatus(true).build();
			patientMappedHospitalIdRepository.save(mapped);

			// Save media files if any
			if (!Utility.isNullOrEmptyList(userWebModel.getFiles())) {
				FileInputWebModel fileInput = FileInputWebModel.builder().category(MediaFileCategory.patientDocument) // Define
																														// a
																														// suitable
																														// enum
																														// value
						.categoryRefId(savedPatient.getPatientDetailsId()).files(userWebModel.getFiles()).build();

				User userFromDB = userRepository.findById(userWebModel.getCreatedBy()).orElse(null); // Or handle
																										// accordingly

			//	mediaFilesService.saveMediaFiles(fileInput, userFromDB);
				// Save media files if any
		        savePatientMediaFiles(userWebModel, savedPatient);

			}
	        // Send SMS
	        boolean smsSent = true;
	        String mobile = userWebModel.getMobileNumber();
	        String message = "You have successfully registered with Aegle Healthcare. Your health is important to us! Stay in touch.";

	        try {
	            smsService.sendSms(mobile, message);
	        } catch (Exception smsEx) {
	            smsSent = false;
	            logger.error("SMS failed for mobile: " + mobile, smsEx);
	        }

	        if (!smsSent) {
	            return ResponseEntity.ok(new Response(1, "Success", "Patient registered successfully, but SMS not sent"));
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
			Optional<PatientDetails> patient = patientDetailsRepository
					.findById(patientDetailsWebModel.getPatientDetailsId());

			if (doctor.isEmpty() || patient.isEmpty()) {
				return ResponseEntity.badRequest().body("Doctor or Patient not found.");
			}
			// Count existing appointments for the doctor on the same date with "OFFLINE"
			// type
			int newToken = patientAppointmentRepository.countByAppointmentDateAndDoctorIdAndAppointmentType(
					patientDetailsWebModel.getAppointmentDate(), patientDetailsWebModel.getDoctorId(), "OFFLINE") + 1;

			// Create new appointment entry
			PatientAppointmentTable appointment = PatientAppointmentTable.builder().doctor(doctor.get())
					.patient(patient.get())

					.appointmentDate(patientDetailsWebModel.getAppointmentDate())
					.slotStartTime(patientDetailsWebModel.getSlotStartTime())
					.slotEndTime(patientDetailsWebModel.getSlotEndTime()).slotTime(patientDetailsWebModel.getSlotTime())
					.isActive(true).createdBy(patientDetailsWebModel.getCreatedBy()).appointmentStatus("SCHEDULED")
					.patientNotes(patientDetailsWebModel.getPatientNotes()).appointmentType("OFFLINE")
					.token(String.valueOf(newToken)) // Assigning token sequentially
					.build();

			// Save the appointment
			patientAppointmentRepository.save(appointment);

			return ResponseEntity.ok(new Response(1, "Success", "Offline appointment booked successfully."));
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
				Optional<PatientMappedHospitalId> mappingOptional = patientMappedHospitalIdRepository
						.findByPatientIdAndHospitalId(patient.getPatientDetailsId(), hospitalId);

				if (mappingOptional.isPresent()) {
					// Step 3: Fetch Appointments
					List<PatientAppointmentTable> appointments = patientAppointmentRepository
							.findByPatientDetailsId(patient.getPatientDetailsId());

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
					responseMap.put("countryCode", patient.getCountryCode());
					responseMap.put("EmerCountryCode", patient.getEmerCountryCode());
					responseMap.put("countryCode", patient.getCountryCode());
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
					return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
							Collections.singletonMap("message", "No hospital mapping found for the given patient."));
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
			List<PatientAppointmentTable> appointments = patientAppointmentRepository
					.findByPatientId(patient.getPatientDetailsId());

			// Prepare patient details
			Map<String, Object> patientData = new HashMap<>();
			patientData.put("patientName", patient.getPatientName());
			patientData.put("firstName", patient.getFirstName());
			patientData.put("lastName", patient.getLastName());
			patientData.put("dob", patient.getDob());
			patientData.put("gender", patient.getGender());
			patientData.put("bloodGroup", patient.getBloodGroup());
			patientData.put("mobileNumber", patient.getMobileNumber());
			patientData.put("emailId", patient.getEmailId());
			patientData.put("countryCode", patient.getCountryCode());
			patientData.put("EmercountryCode", patient.getEmerCountryCode());
			patientData.put("address", patient.getAddress());
			patientData.put("emergencyContact", patient.getEmergencyContact());
			// patientData.put("hospitalId", patient.getHospitalId());
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
				appointmentData.put("doctorSlotSpiltTimeId", appointment.getDoctorSlotSpiltTimeId());
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

			if (userWebModel.getDoctorId() != null && userWebModel.getAppointmentDate() != null
					&& userWebModel.getDoctorSlotId() != null && userWebModel.getDaySlotId() != null
					&& userWebModel.getSlotStartTime() != null && userWebModel.getSlotEndTime() != null) {

				boolean isSlotBooked = checkIfSlotIsBooked(userWebModel.getDoctorSlotId(), userWebModel.getDaySlotId(),
						userWebModel.getSlotStartTime(), userWebModel.getSlotEndTime());

				if (isSlotBooked) {
					logger.warn("Slot is already booked for doctorId: {}, date: {}, time: {} - {}",
							userWebModel.getDoctorId(), userWebModel.getAppointmentDate(),
							userWebModel.getSlotStartTime(), userWebModel.getSlotEndTime());
					return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(0, "Fail",
							"The selected slot on " + userWebModel.getAppointmentDate() + " is already booked."));
				}
			}

			// Fetch patient record
			PatientDetails db = patientDetailsRepository.findByIds(userWebModel.getPatientDetailsId());

			// Book appointment
			PatientAppointmentTable appointment = bookAppointment(userWebModel, db);
			if (appointment == null) {
				return ResponseEntity.badRequest().body(new Response(0, "Fail", "Slot already booked"));
			}

			// ✅ Update slot status in doctorSlotSplitTime
			if (userWebModel.getDoctorSlotSpiltTimeId() != null) {
				Optional<DoctorSlotSpiltTime> optionalSlot = doctorSlotSplitTimeRepository
						.findById(userWebModel.getDoctorSlotSpiltTimeId());
				if (optionalSlot.isPresent()) {
					DoctorSlotSpiltTime slot = optionalSlot.get();
					slot.setSlotStatus("Booked");
					slot.setUpdatedBy(userWebModel.getCreatedBy());
					slot.setUpdatedOn(new Date());
					doctorSlotSplitTimeRepository.save(slot);
					logger.info("Marked doctorSlotSplitTimeId {} as Booked", userWebModel.getDoctorSlotSpiltTimeId());
				} else {
					logger.warn("doctorSlotSplitTimeId {} not found for updating status",
							userWebModel.getDoctorSlotSpiltTimeId());
				}
			}
			  // Send appointment SMS confirmation (but don't let SMS failure affect the response)
	        boolean smsSent = sendAppointmentConfirmationSMS(userWebModel, appointment);
	        
	        String responseMessage = "Online registered successfully";
	        if (!smsSent) {
	            // Adding a note about SMS in logs only, not changing the success status
	            logger.warn("Appointment booked successfully but SMS notification failed");
	            // We still return 200 OK as the core functionality (booking) succeeded
	        }

			return ResponseEntity.ok(new Response(1, "Success", "Online registered successfully"));

		} catch (Exception e) {
			logger.error("Registration failed: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new Response(0, "Fail", "Something went wrong during registration"));
		}
	}
	
	/**
	 * Send SMS confirmation for the booked appointment
	 * @param userWebModel The patient details model
	 * @param appointment The booked appointment (can be null)
	 * @return boolean indicating whether the SMS was sent successfully
	 */
	private boolean sendAppointmentConfirmationSMS(PatientDetailsWebModel userWebModel, PatientAppointmentTable appointment) {
	    // Send appointment SMS separately - with explicit variable declarations to avoid NPE
	    if (appointment != null) {
	        // Wrap in try-catch to prevent any exceptions from breaking the flow
	        try {
	            // Get all necessary data with safeguards
	            String phoneNumber = userWebModel.getMobileNumber();
	            String appointmentDate = userWebModel.getAppointmentDate() != null ? 
	                String.valueOf(userWebModel.getAppointmentDate()) : "scheduled date";
	            String startTime = userWebModel.getSlotStartTime() != null ? 
	                userWebModel.getSlotStartTime() : "scheduled time";
	            String endTime = userWebModel.getSlotEndTime() != null ? 
	                userWebModel.getSlotEndTime() : "end time";
	            
	            // Declare variables outside of Optional chains
	            String doctorName = "Your doctor";
	            String hospitalName = "our hospital";
	            
	            // Get doctor name safely
	            if (userWebModel.getDoctorId() != null) {
	                Optional<User> doctorOpt = userRepository.findById(userWebModel.getDoctorId());
	                if (doctorOpt.isPresent()) {
	                    User doctor = doctorOpt.get();
	                    if (doctor.getUserName() != null && !doctor.getUserName().isEmpty()) {
	                        doctorName = doctor.getUserName();
	                    }
	                }
	            }
	            
	            // Get hospital name safely
	            if (userWebModel.getHospitalId() != null) {
	                Optional<HospitalDataList> hospitalOpt = hospitalDataListRepository.findById(userWebModel.getHospitalId());
	                if (hospitalOpt.isPresent()) {
	                    HospitalDataList hospital = hospitalOpt.get();
	                    if (hospital.getHospitalName() != null && !hospital.getHospitalName().isEmpty()) {
	                        hospitalName = hospital.getHospitalName();
	                    }
	                }
	            }
	            
	            // Build the message
	            StringBuilder smsBuilder = new StringBuilder();
	            smsBuilder.append("Your appointment is confirmed on ");
	            smsBuilder.append(appointmentDate);
	            smsBuilder.append(" (");
	            smsBuilder.append(startTime);
	            smsBuilder.append(" - ");
	            smsBuilder.append(endTime);
	            smsBuilder.append(") with Dr. ");
	            smsBuilder.append(doctorName);
	            smsBuilder.append(" at ");
	            smsBuilder.append(hospitalName);
	            smsBuilder.append(". Thank you!");
	            String smsMessage = smsBuilder.toString();
	            
	            // Send the SMS
	            logger.info("About to send appointment SMS to: " + phoneNumber);
	            logger.info("SMS content: " + smsMessage);
	            
	            // Directly call SMS service
	            smsService.sendSms(phoneNumber, smsMessage);
	            logger.info("Appointment SMS sent successfully");
	            return true;
	        } catch (Exception e) {
	            logger.error("Failed to send appointment SMS: " + e.getMessage());
	            e.printStackTrace(); // Add stack trace for debugging
	            // Continue execution - don't let SMS failure stop the process
	            return false;
	        }
	    }
	    // If appointment is null, no SMS was sent
	    return false;
	}

	@Override
	public ResponseEntity<?> patientSubChildRegister(PatientDetailsWebModel userWebModel) {
		try {
			if (userWebModel == null) {
				return ResponseEntity.badRequest().body(new Response(0, "Invalid request data", null));
			}

			// Check if sub-child already exists
			Optional<PatientSubChildDetails> existingPatient = patientSubChildDetailsRepository
					.findByPatientDetailsIdAndPatientName(userWebModel.getPatientDetailsId(),
							userWebModel.getPatientName());

			if (existingPatient.isPresent()) {
				return ResponseEntity.ok(new Response(0, "Sub-child patient already exists", null));
			}

			// Build new entity
			PatientSubChildDetails patientSubChild = PatientSubChildDetails.builder()
					.patientName(userWebModel.getPatientName()).dob(userWebModel.getDob())
					.gender(userWebModel.getGender()).bloodGroup(userWebModel.getBloodGroup())
					.relationshipType(userWebModel.getRelationshipType())
					.patientDetailsId(userWebModel.getPatientDetailsId()).address(userWebModel.getAddress())
					.currentAddress(userWebModel.getCurrentAddress())
					.emergencyContact(userWebModel.getEmergencyContact())
					.purposeOfVisit(userWebModel.getPurposeOfVisit()).doctorId(userWebModel.getDoctorId())
					.previousMedicalHistory(userWebModel.getPreviousMedicalHistory())
					.insuranceDetails(userWebModel.getInsuranceDetails()).insurerName(userWebModel.getInsurerName())
					.insuranceProvider(userWebModel.getInsuranceProvider()).policyNumber(userWebModel.getPolicyNumber())
					.disability(userWebModel.getDisability()).age(userWebModel.getAge()).userIsActive(true)
					.createdBy(userWebModel.getCreatedBy() != null ? userWebModel.getCreatedBy() : 1)
					.userCreatedOn(new Date()).build();

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
	        List<MultipartFile> files = userWebModel.getFiles();
	        List<MediaFile> filesList = new ArrayList<>();

	        for (MultipartFile file : files) {
	            try {
	                byte[] fileBytes = file.getBytes();
	                String base64Data = Base64.getEncoder().encodeToString(fileBytes);
	                String fileName = UUID.randomUUID().toString();

	                MediaFile mediaFile = new MediaFile();
	                mediaFile.setFileName(fileName);
	                mediaFile.setFileOriginalName(file.getOriginalFilename());
	                
	                mediaFile.setFileType(file.getContentType());
	                mediaFile.setCategory(MediaFileCategory.patientSubChildDocument);
	                mediaFile.setFileDomainId(HealthCareConstant.patientSubChildDocument);
	                mediaFile.setFileDomainReferenceId(savedPatient.getPatientSubChildDetailsId());
	                mediaFile.setFileIsActive(true);
	                mediaFile.setFileCreatedBy(userWebModel.getCreatedBy());

	                mediaFile = mediaFileRepository.save(mediaFile);
	                filesList.add(mediaFile);

	                // Save file to filesystem
	                Base64FileUpload.saveFile(imageLocation + "/patientSubChildDocument", base64Data, fileName);

	            } catch (IOException e) {
	                e.printStackTrace(); // Use proper logging in production
	            }
	        }
	    }
	}

	@Override
	public ResponseEntity<?> getPatientRelationShipDetails(Integer patientDetailsId, String relationshipType) {
		List<Object[]> resultList = patientSubChildDetailsRepository
				.findIdAndNameByPatientDetailsIdAndRelationshipType(patientDetailsId, relationshipType);

		List<Map<String, Object>> patients = new ArrayList<>();

		for (Object[] row : resultList) {
			Map<String, Object> map = new HashMap<>();
			map.put("id", row[0]);
			map.put("name", row[1]);
			patients.add(map);
		}

		Map<String, Object> response = new HashMap<>();
		response.put("count", patients.size());
		response.put("patients", patients);

		return ResponseEntity.ok(response);
	}

	@Override
	public ResponseEntity<?> getPatientMappedDetailsById(Integer patientDetailsId, Integer hospitalId) {
		Optional<PatientMappedHospitalId> mappedOpt = patientMappedHospitalIdRepository.findMappedData(patientDetailsId,
				hospitalId);

		System.out.println("mappedOpt------------->");

		if (mappedOpt.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Mapping not found");
		}

		PatientMappedHospitalId mapping = mappedOpt.get();

		// If both flags are false or null, return 403
		if (Boolean.FALSE.equals(mapping.getMedicalHistoryStatus())
				&& Boolean.FALSE.equals(mapping.getPersonalDataStatus())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No data shared");
		}

		Optional<PatientDetails> patientOpt = patientDetailsRepository.findById(patientDetailsId);

		if (patientOpt.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Patient not found");
		}

		PatientDetails patient = patientOpt.get();
		Map<String, Object> response = new HashMap<>();

		// Add personal data if allowed
		if (Boolean.TRUE.equals(mapping.getPersonalDataStatus())) {
			response.put("patientName", patient.getPatientName());
			response.put("dob", patient.getDob());
			response.put("gender", patient.getGender());
			response.put("bloodGroup", patient.getBloodGroup());
			response.put("mobileNumber", patient.getMobileNumber());
			response.put("emailId", patient.getEmailId());
			response.put("address", patient.getAddress());
			response.put("emergencyContact", patient.getEmergencyContact());
		}

		// Add medical history if allowed
		if (Boolean.TRUE.equals(mapping.getMedicalHistoryStatus())) {
			response.put("previousMedicalHistory", patient.getPreviousMedicalHistory());
			response.put("insuranceDetails", patient.getInsuranceDetails());
			response.put("insurerName", patient.getInsurerName());
			response.put("insuranceProvider", patient.getInsuranceProvider());
			response.put("policyNumber", patient.getPolicyNumber());
			response.put("disability", patient.getDisability());
		}

		return ResponseEntity.ok(response);
	}

	@Override
	public ResponseEntity<?> getDoctorfeesById(Integer userId) {
		Optional<User> optionalUser = userRepository.findByUserId(userId);

		if (optionalUser.isPresent()) {
			Integer doctorFees = optionalUser.get().getDoctorFees();
			Map<String, Object> response = new HashMap<>();
			response.put("userId", userId);
			response.put("doctorFees", doctorFees);
			return ResponseEntity.ok(response);
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Collections.singletonMap("error", "User not found with ID: " + userId));
		}
	}

	@Override
	public ResponseEntity<?> getAllAddressData() {
		try {
			List<AddressData> activeAddresses = addressDataRepository.findByUserIsActiveTrue();

			List<Map<String, Object>> result = new ArrayList<>();

			for (AddressData address : activeAddresses) {
				Map<String, Object> map = new HashMap<>();
				map.put("addressDataId", address.getAddressDataId());
				map.put("addressName", address.getAddressName());
				result.add(map);
			}

			return ResponseEntity.ok(result);

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new Response(0, "error", "Failed to fetch address data."));
		}
	}

	public ResponseEntity<?> getDropDownByLabList() {
		// Fetch all active lab master data records from the database
		List<LabMasterData> labList = labMasterDataRepository.findByIsActiveTrue();

		// Create a response map to store response data
		Map<String, Object> response = new HashMap<>();

		// If the list is empty, return a message indicating no active labs with 200 OK
		if (labList.isEmpty()) {
			response.put("message", "No active labs found");
		} else {
			// Create a list of lab objects containing both id and name
			List<Map<String, Object>> labDropdown = labList.stream().map(lab -> {
				Map<String, Object> labData = new HashMap<>();
				labData.put("id", lab.getId());
				labData.put("name", lab.getName());
				return labData;
			}).collect(Collectors.toList());

			// Add the labDropdown list to the response
			response.put("labs", labDropdown);
		}

		return ResponseEntity.ok(response);
	}

	@Override
	public ResponseEntity<?> getDropDownBySupportStaffList() {
		// Fetch all active support staff records from the database
		List<SupportStaffMasterData> supportStaffList = supportStaffMasterDataRepository.findByIsActiveTrue();

		// Create a response map to store response data
		Map<String, Object> response = new HashMap<>();

		// If the list is empty, return a message indicating no active support staff
		// with 200 OK
		if (supportStaffList.isEmpty()) {
			response.put("message", "No active support staff found");
		} else {
			// Create a list of support staff objects containing both id and name
			List<Map<String, Object>> supportStaffDropdown = supportStaffList.stream().map(staff -> {
				Map<String, Object> staffData = new HashMap<>();
				staffData.put("id", staff.getId());
				staffData.put("name", staff.getName());
				return staffData;
			}).collect(Collectors.toList());

			// Add the supportStaffDropdown list to the response
			response.put("staff", supportStaffDropdown);
		}

		return ResponseEntity.ok(response);
	}
}

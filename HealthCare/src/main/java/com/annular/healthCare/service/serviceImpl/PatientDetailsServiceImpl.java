package com.annular.healthCare.service.serviceImpl;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import com.annular.healthCare.repository.PatientAppoitmentTablerepository;
import com.annular.healthCare.repository.PatientDetailsRepository;
import com.annular.healthCare.repository.PatientMappedHospitalIdRepository;
import com.annular.healthCare.repository.PatientSubChildDetailsRepository;
import com.annular.healthCare.repository.SupportStaffMasterDataRepository;
import com.annular.healthCare.repository.UserRepository;
import com.annular.healthCare.service.MediaFileService;
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

	@Autowired
	MediaFileService mediaFilesService;

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
	HospitalDataListRepository hospitalDataListRepository;
	
	@Autowired
	private SmsService smsService;

	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");

	@Transactional
	@Override
	public ResponseEntity<?> register(PatientDetailsWebModel userWebModel) {
		try {
			logger.info("Registering patient: {}", userWebModel.getPatientName());

			// Validate required fields
			if (userWebModel.getPatientName() == null || userWebModel.getMobileNumber() == null) {
				return ResponseEntity.badRequest()
						.body(new Response(0, "Fail", "Patient name and mobile number are required"));
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

			// Check if slot is available before booking an appointment
			if (userWebModel.getDoctorId() != null && userWebModel.getAppointmentDate() != null
					&& userWebModel.getDoctorSlotId() != null && userWebModel.getDaySlotId() != null
					&& userWebModel.getSlotStartTime() != null && userWebModel.getSlotEndTime() != null) { // Fixed
																											// duplicate
																											// check

				boolean isSlotBooked = checkIfSlotIsBooked(userWebModel.getDoctorSlotId(), userWebModel.getDaySlotId(),
						userWebModel.getSlotStartTime(), // Pass correct start time
						userWebModel.getSlotEndTime() // Pass correct end time
				);

				if (isSlotBooked) {
					logger.warn("Slot is already booked for doctorId: {}, date: {}, time: {} - {}",
							userWebModel.getDoctorId(), userWebModel.getAppointmentDate(),
							userWebModel.getSlotStartTime(), userWebModel.getSlotEndTime());
					return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(0, "Fail",
							"The selected slot on " + userWebModel.getAppointmentDate() + " is already booked."));
				}
			}

			// Create patient details
			PatientDetails savedPatient = createPatientDetails(userWebModel);
			if (savedPatient == null) {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body(new Response(0, "Fail", "Failed to create patient details"));
			}

			// Book appointment if appointment details are provided
			PatientAppointmentTable appointment = bookAppointment(userWebModel, savedPatient); // Fixed return type
			if (appointment == null) { // No need to check for isEmpty() since it's not a list
				return ResponseEntity.badRequest().body(new Response(0, "Fail", "slot Already booked"));
			}
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

			// Save media files if any
			savePatientMediaFiles(userWebModel, savedPatient);
			// Save patient-hospital mapping

			try {
			    String phoneNumber = userWebModel.getMobileNumber();
			    String smsMessage = "You have successfully registered with Aegle Healthcare. Your health is important to us! Stay in touch.";
			    logger.info("Sending registration SMS to: {}", phoneNumber);
			    smsService.sendSms(phoneNumber, smsMessage);
			    logger.info("SMS sent successfully to: {}", phoneNumber);
			} catch (Exception e) {
			    logger.error("SMS sending failed: {}", e.getMessage(), e);
			    // Do not throw or block response, just log the failure
			}

	        // Send SMS for appointment confirmation
	        try {
	            String phoneNumber = userWebModel.getMobileNumber();
	            String appointmentDate = String.valueOf(userWebModel.getAppointmentDate());
	            String startTime = userWebModel.getSlotStartTime();
	            String endTime = userWebModel.getSlotEndTime();

	            String doctorName = ""; // Default
	            String hospitalName = ""; // Default

	            // Fetch doctor name
	            if (userWebModel.getDoctorId() != null) {
	                Optional<User> doctor = userRepository.findById(userWebModel.getDoctorId());
	                doctorName = doctor.map(User::getUserName).orElse("Your doctor");
	            }

	            // Fetch hospital name
	            if (userWebModel.getHospitalId() != null) {
	                Optional<HospitalDataList> hospital = hospitalDataListRepository.findById(userWebModel.getHospitalId());
	                hospitalName = hospital.map(HospitalDataList::getHospitalName).orElse("Your hospital");
	            }

	            String smsMessage = String.format(
	                "Your appointment is confirmed on %s (%s - %s) with Dr. %s at %s. Thank you!",
	                appointmentDate, startTime, endTime, doctorName, hospitalName
	            );

	            logger.info("Sending appointment SMS to: {}", phoneNumber);
	            smsService.sendSms(phoneNumber, smsMessage);
	            logger.info("SMS sent successfully to: {}", phoneNumber);
	        } catch (Exception e) {
	            logger.error("SMS sending failed: {}", e.getMessage(), e);
	        }
			
			return ResponseEntity.ok(new Response(1, "Success", "Patient registered successfully"));
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
				.patientName(userWebModel.getPatientName()).dob(userWebModel.getDob()).age(userWebModel.getAge())
				.otp(100).gender(userWebModel.getGender()).bloodGroup(userWebModel.getBloodGroup())
				.mobileNumber(userWebModel.getMobileNumber()).emailId(userWebModel.getEmailId())
				.address(userWebModel.getAddress()).currentAddress(userWebModel.getCurrentAddress())
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
																												// sequentially
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
			FileInputWebModel fileInput = FileInputWebModel.builder().category(MediaFileCategory.patientDocument)
					.categoryRefId(savedPatient.getPatientDetailsId()).files(userWebModel.getFiles()).build();

			User userFromDB = userRepository.findById(userWebModel.getCreatedBy()).orElse(null);

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
			List<Integer> patientIds = mappings.stream().map(PatientMappedHospitalId::getPatientId)
					.collect(Collectors.toList());

			// Step 3: Fetch patient details
			//List<PatientDetails> patients = patientDetailsRepository.findAllById(patientIds);
			List<PatientDetails> patients = patientDetailsRepository.findAllById(patientIds)
				    .stream()
				    .sorted(Comparator.comparing(PatientDetails::getUserCreatedOn).reversed()) // Sort by latest created
				    .collect(Collectors.toList());


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
				return ResponseEntity.badRequest().body(new Response(0, "Fail", "Patient ID is required for update"));
			}

			Optional<PatientDetails> optionalPatient = patientDetailsRepository
					.findById(userWebModel.getPatientDetailsId());

			if (optionalPatient.isEmpty()) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
						new Response(0, "Fail", "Patient not found with ID: " + userWebModel.getPatientDetailsId()));
			}

			PatientDetails patient = optionalPatient.get();

			// Only update fields if they are non-null
			if (userWebModel.getPatientName() != null)
				patient.setPatientName(userWebModel.getPatientName());
			if (userWebModel.getDob() != null)
				patient.setDob(userWebModel.getDob());
			if (userWebModel.getAge() != null)
				patient.setAge(userWebModel.getAge());
			if (userWebModel.getGender() != null)
				patient.setGender(userWebModel.getGender());
			if (userWebModel.getBloodGroup() != null)
				patient.setBloodGroup(userWebModel.getBloodGroup());
			if (userWebModel.getMobileNumber() != null)
				patient.setMobileNumber(userWebModel.getMobileNumber());
			if (userWebModel.getEmailId() != null)
				patient.setEmailId(userWebModel.getEmailId());
			if (userWebModel.getAddress() != null)
				patient.setAddress(userWebModel.getAddress());
			if (userWebModel.getCurrentAddress() != null)
				patient.setCurrentAddress(userWebModel.getCurrentAddress());
			if (userWebModel.getEmergencyContact() != null)
				patient.setEmergencyContact(userWebModel.getEmergencyContact());
			if (userWebModel.getPurposeOfVisit() != null)
				patient.setPurposeOfVisit(userWebModel.getPurposeOfVisit());
			if (userWebModel.getDoctorId() != null)
				patient.setDoctorId(userWebModel.getDoctorId());
			if (userWebModel.getPreviousMedicalHistory() != null)
				patient.setPreviousMedicalHistory(userWebModel.getPreviousMedicalHistory());
			if (userWebModel.getInsuranceDetails() != null)
				patient.setInsuranceDetails(userWebModel.getInsuranceDetails());
			if (userWebModel.getInsurerName() != null)
				patient.setInsurerName(userWebModel.getInsurerName());
			if (userWebModel.getInsuranceProvider() != null)
				patient.setInsuranceProvider(userWebModel.getInsuranceProvider());
			if (userWebModel.getPolicyNumber() != null)
				patient.setPolicyNumber(userWebModel.getPolicyNumber());
			if (userWebModel.getDisability() != null)
				patient.setDisability(userWebModel.getDisability());

			patient.setUserUpdatedOn(new Date());
			if (userWebModel.getUserUpdatedBy() != null)
				patient.setUserUpdatedBy(userWebModel.getUserUpdatedBy());

			// Save updated patient
			patientDetailsRepository.save(patient);

			// Save or update media files (if passed)
			if (!Utility.isNullOrEmptyList(userWebModel.getFiles())) {
				FileInputWebModel fileInput = FileInputWebModel.builder().category(MediaFileCategory.patientDocument)
						.categoryRefId(patient.getPatientDetailsId()).files(userWebModel.getFiles()).build();

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

			// Fetch media files
			List<FileOutputWebModel> mediaFiles = mediaFilesService
					.getMediaFilesByCategoryAndRefId(MediaFileCategory.patientDocument, patient.getPatientDetailsId());
			webModel.setFiless(mediaFiles);

			// Fetch appointments
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

				List<String> specialties = roleIds.stream().map((Integer roleId) -> {
					DoctorSpecialty specialty = doctorSpecialtyRepository.findById(roleId).orElse(null);
					return (specialty != null) ? specialty.getSpecialtyName() : null;
				}).filter(Objects::nonNull).collect(Collectors.toList());

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
		return mediaFilesService.deleteMediaFilesByUserIdAndCategoryAndRefIds(MediaFileCategory.patientDocument,
				fileId);
	}

	@Override
	public ResponseEntity<?> adminPatientRegister(PatientDetailsWebModel userWebModel) {

		try {
			logger.info("Registering patient: {}", userWebModel.getPatientName());

			if (userWebModel.getPatientName() == null || userWebModel.getMobileNumber() == null) {
				return ResponseEntity.badRequest()
						.body(new Response(0, "Fail", "Patient name and mobile number are required"));
			}
			// Check if the patient already exists
			Optional<PatientDetails> existingUser = patientDetailsRepository
					.findByMobileNumberAndHospitalId(userWebModel.getMobileNumber());
			if (existingUser.isPresent()) {
				return ResponseEntity.badRequest()
						.body(new Response(0, "Fail", "Mobile number is already registered for this hospital."));
			}

			PatientDetails newPatient = PatientDetails.builder().patientName(userWebModel.getPatientName())
					.dob(userWebModel.getDob()).age(userWebModel.getAge()).otp(100).gender(userWebModel.getGender())
					.bloodGroup(userWebModel.getBloodGroup()).mobileNumber(userWebModel.getMobileNumber())
					.emailId(userWebModel.getEmailId()).address(userWebModel.getAddress())
					.currentAddress(userWebModel.getCurrentAddress())
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

				mediaFilesService.saveMediaFiles(fileInput, userFromDB);

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
			patientData.put("dob", patient.getDob());
			patientData.put("gender", patient.getGender());
			patientData.put("bloodGroup", patient.getBloodGroup());
			patientData.put("mobileNumber", patient.getMobileNumber());
			patientData.put("emailId", patient.getEmailId());
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
			FileInputWebModel fileInput = FileInputWebModel.builder()
					.category(MediaFileCategory.patientSubChildDocument)
					.categoryRefId(savedPatient.getPatientSubChildDetailsId()) // Use getId() of sub-child, not
																				// patientDetailsId
					.files(userWebModel.getFiles()).build();

			User createdByUser = userRepository.findById(userWebModel.getCreatedBy()).orElse(null);

			mediaFilesService.saveMediaFiles(fileInput, createdByUser);
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

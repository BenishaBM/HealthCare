package com.annular.healthCare.service.serviceImpl;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.annular.healthCare.Response;
import com.annular.healthCare.Util.Base64FileUpload;
import com.annular.healthCare.Util.HealthCareConstant;
import com.annular.healthCare.model.BookingDemo;
import com.annular.healthCare.model.DoctorDaySlot;
import com.annular.healthCare.model.DoctorLeaveList;
import com.annular.healthCare.model.DoctorRole;
import com.annular.healthCare.model.DoctorSlot;
import com.annular.healthCare.model.DoctorSlotDate;
import com.annular.healthCare.model.DoctorSlotSpiltTime;
import com.annular.healthCare.model.DoctorSlotTime;
import com.annular.healthCare.model.HospitalAdmin;
import com.annular.healthCare.model.HospitalDataList;
import com.annular.healthCare.model.LabMasterData;
import com.annular.healthCare.model.MediaFile;
import com.annular.healthCare.model.MediaFileCategory;
import com.annular.healthCare.model.PatientDetails;
import com.annular.healthCare.model.PatientMappedHospitalId;
import com.annular.healthCare.model.RefreshToken;
import com.annular.healthCare.model.SupportStaffMasterData;
import com.annular.healthCare.model.User;
import com.annular.healthCare.repository.BookingDemoRepository;
import com.annular.healthCare.repository.DoctorDaySlotRepository;
import com.annular.healthCare.repository.DoctorLeaveListRepository;
import com.annular.healthCare.repository.DoctorRoleRepository;
import com.annular.healthCare.repository.DoctorSlotDateRepository;
import com.annular.healthCare.repository.DoctorSlotRepository;
import com.annular.healthCare.repository.DoctorSlotSpiltTimeRepository;
import com.annular.healthCare.repository.DoctorSlotTimeOverrideRepository;
import com.annular.healthCare.repository.DoctorSlotTimeRepository;
import com.annular.healthCare.repository.DoctorSpecialityRepository;
import com.annular.healthCare.repository.HospitalAdminRepository;
import com.annular.healthCare.repository.HospitalDataListRepository;
import com.annular.healthCare.repository.LabMasterDataRepository;
import com.annular.healthCare.repository.MediaFileRepository;
import com.annular.healthCare.repository.PatientAppoitmentTablerepository;
import com.annular.healthCare.repository.PatientDetailsRepository;
import com.annular.healthCare.repository.PatientMappedHospitalIdRepository;
import com.annular.healthCare.repository.PatientSubChildDetailsRepository;
import com.annular.healthCare.repository.RefreshTokenRepository;
import com.annular.healthCare.repository.SupportStaffMasterDataRepository;
import com.annular.healthCare.repository.UserRepository;
import com.annular.healthCare.service.AuthService;
import com.annular.healthCare.service.SmsService;
import com.annular.healthCare.webModel.DoctorDaySlotWebModel;
import com.annular.healthCare.webModel.DoctorLeaveListWebModel;
import com.annular.healthCare.webModel.DoctorSlotTimeWebModel;
import com.annular.healthCare.webModel.FileInputWebModel;
import com.annular.healthCare.webModel.HospitalDataListWebModel;
import com.annular.healthCare.webModel.UserWebModel;

@Service
public class AuthServiceImpl implements AuthService {

	public static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

	@Autowired
	UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private PatientSubChildDetailsRepository patientSubChildDetailsRepository;

	@Autowired
	RefreshTokenRepository refreshTokenRepository;

	@Autowired
	MediaFileRepository mediaFileRepository;

	@Autowired
	DoctorSlotTimeRepository doctorSlotTimeRepository;

	@Autowired
	HospitalDataListRepository hospitalDataListRepository;

	@Autowired
	DoctorRoleRepository doctorRoleRepository;

	@Autowired
	DoctorSlotRepository doctorSlotRepository;
	
	@Autowired
	LabMasterDataRepository labMasterDataRepository;
	
	@Autowired
	private SupportStaffMasterDataRepository supportStaffMasterDataRepository;


	@Autowired
	DoctorDaySlotRepository doctorDaySlotRepository;

	@Autowired
	HospitalAdminRepository hospitalAdminRepository;

	@Autowired
	DoctorLeaveListRepository doctorLeaveListRepository;

	@Autowired
	DoctorSlotTimeOverrideRepository doctorSlotTimeOverrideRepository;

	@Autowired
	DoctorSpecialityRepository doctorSpecialtyRepository;

	@Autowired
	PatientAppoitmentTablerepository patientAppoitnmentRepository;
	
	@Autowired
	PatientMappedHospitalIdRepository patientMappedHospitalIdRepository;
	
	@Autowired
	PatientDetailsRepository patientDetailsRepository;
	
	@Autowired
	BookingDemoRepository bookingDemoRepository;
	
	@Autowired
	DoctorSlotSpiltTimeRepository doctorSlotSplitTimeRepository;
	
	@Autowired
	private SmsService smsService;

	
	@Autowired
	DoctorSlotDateRepository doctorSlotDateRepository;

	@Value("${annular.app.imageLocation}")
	private String imageLocation;
	
@Override
public ResponseEntity<?> register(UserWebModel userWebModel) {
    HashMap<String, Object> response = new HashMap<>();
    try {
        logger.info("Register method start");

        // Check if a user with the same emailId already exists
        Optional<User> existingUser = userRepository.findByEmailIdss(userWebModel.getEmailId());
        
        if (existingUser.isPresent()) {
            response.put("message", "User with this email already exists");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Validate phone number format if provided
        if (userWebModel.getPhoneNumber() != null && !isValidPhoneNumber(userWebModel.getPhoneNumber())) {
            response.put("message", "Invalid phone number format");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Validate doctor slots if user is a doctor
        if (userWebModel.getUserType().equalsIgnoreCase("DOCTOR") && userWebModel.getDoctorDaySlots() != null) {
            // Check for slot time overlaps and validity
            String validationError = validateDoctorSlots(userWebModel.getDoctorDaySlots());
            if (validationError != null) {
                response.put("message", validationError);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        }

        // Create new user entity
        User savedUser = createAndSaveUser(userWebModel);

        // Handle file uploads (e.g., profile photos)
        if (userWebModel.getFilesInputWebModel() != null && !userWebModel.getFilesInputWebModel().isEmpty()) {
            handleFileUploads(savedUser, userWebModel.getFilesInputWebModel());
        }

        // Save user roles if provided
        saveUserRoles(savedUser, userWebModel.getRoleIds());

        // Process doctor-specific data if user is a doctor
        if (userWebModel.getUserType().equalsIgnoreCase("DOCTOR")) {
            processDoctorData(savedUser, userWebModel);
        }

        // Send SMS based on userType
        boolean smsSent = sendRegistrationSMS(userWebModel);

        String message = smsSent ? "User registered successfully" : "User registered successfully, but SMS not sent";
        return ResponseEntity.ok(new Response(1, "success", message));
        
    } catch (IllegalArgumentException e) {
        logger.error("Validation error during registration: " + e.getMessage(), e);
        response.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    } catch (Exception e) {
        logger.error("Error registering user: " + e.getMessage(), e);
        response.put("message", "Registration failed: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

/**
 * Validates phone number format
 */
private boolean isValidPhoneNumber(String phoneNumber) {
    if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
        return false;
    }
    // Basic validation: should be 10 digits for Indian numbers
    return phoneNumber.matches("^[0-9]{10}$");
}

/**
 * Sends registration SMS to user
 */
private boolean sendRegistrationSMS(UserWebModel userWebModel) {
    if (userWebModel.getPhoneNumber() == null || userWebModel.getPhoneNumber().trim().isEmpty()) {
        return true; // No phone number provided, consider it successful
    }

    String smsMessage;
    if (userWebModel.getUserType().equalsIgnoreCase("ADMIN")) {
        smsMessage = "Welcome to Aegle Healthcare Application. Wishing you all the very best!";
    } else {
        smsMessage = "Hi " + userWebModel.getFirstName() + ", you have been successfully registered!";
    }

    try {
        smsService.sendSms(userWebModel.getPhoneNumber(), smsMessage);
        return true;
    } catch (Exception smsEx) {
        logger.error("SMS could not be sent to user: " + userWebModel.getPhoneNumber(), smsEx);
        return false;
    }
}

/**
 * Creates and saves a new user based on the provided web model
 */
private User createAndSaveUser(UserWebModel userWebModel) {
    // Validate required fields
    if (userWebModel.getEmailId() == null || userWebModel.getEmailId().trim().isEmpty()) {
        throw new IllegalArgumentException("Email ID is required");
    }
    if (userWebModel.getFirstName() == null || userWebModel.getFirstName().trim().isEmpty()) {
        throw new IllegalArgumentException("First name is required");
    }
    if (userWebModel.getPassword() == null || userWebModel.getPassword().trim().isEmpty()) {
        throw new IllegalArgumentException("Password is required");
    }

    User newUser = User.builder()
            .emailId(userWebModel.getEmailId().trim())
            .firstName(userWebModel.getFirstName().trim())
            .lastName(userWebModel.getLastName() != null ? userWebModel.getLastName().trim() : "")
            .password(passwordEncoder.encode(userWebModel.getPassword()))
            .yearOfExperiences(userWebModel.getYearOfExperiences())
            .userType(userWebModel.getUserType())
            .countryCode(userWebModel.getCountryCode())
            .dob(userWebModel.getDob())
            .supportStaffId(userWebModel.getSupportStaffId())
            .labMasterDataId(userWebModel.getLabMasterDataId())
            .phoneNumber(userWebModel.getPhoneNumber())
            .doctorFees(userWebModel.getDoctorfees())
            .userIsActive(true)
            .currentAddress(userWebModel.getCurrentAddress())
            .empId(userWebModel.getEmpId())
            .gender(userWebModel.getGender())
            .createdBy(userWebModel.getCreatedBy() != null ? userWebModel.getCreatedBy() : 1)
            .userName((userWebModel.getFirstName() + " " + 
                     (userWebModel.getLastName() != null ? userWebModel.getLastName() : "")).trim())
            .hospitalId(userWebModel.getHospitalId())
            .build();

    return userRepository.save(newUser);
}

/**
 * Saves user roles if provided
 */
private void saveUserRoles(User savedUser, List<Integer> roleIds) {
    if (roleIds != null && !roleIds.isEmpty()) {
        for (Integer roleId : roleIds) {
            if (roleId != null) {
                DoctorRole doctorRole = DoctorRole.builder()
                        .user(savedUser)
                        .roleId(roleId)
                        .createdBy(savedUser.getCreatedBy())
                        .userIsActive(true)
                        .build();
                doctorRoleRepository.save(doctorRole);
            }
        }
    }
}

/**
 * Processes doctor-specific data including slots and leaves
 */
private void processDoctorData(User savedUser, UserWebModel userWebModel) {
    try {
        // Create and save doctor slot
        DoctorSlot doctorSlot = createAndSaveDoctorSlot(savedUser);
        
        // Process doctor day slots if provided
        if (userWebModel.getDoctorDaySlots() != null && !userWebModel.getDoctorDaySlots().isEmpty()) {
            processDoctorDaySlots(savedUser, doctorSlot, userWebModel.getDoctorDaySlots());
        }
        
        // Save doctor leaves if provided
        saveDoctorLeaves(savedUser, userWebModel.getDoctorLeaveList());
        
    } catch (Exception e) {
        logger.error("Error processing doctor data for user: " + savedUser.getEmailId(), e);
        throw new RuntimeException("Failed to process doctor data: " + e.getMessage(), e);
    }
}

/**
 * Creates and saves a doctor slot
 */
private DoctorSlot createAndSaveDoctorSlot(User savedUser) {
    DoctorSlot doctorSlot = DoctorSlot.builder()
            .user(savedUser)
            .createdBy(savedUser.getCreatedBy())
            .isActive(true)
            .build();
    return doctorSlotRepository.save(doctorSlot);
}

/**
 * Processes doctor day slots with improved error handling
 */
private void processDoctorDaySlots(User savedUser, DoctorSlot doctorSlot, List<DoctorDaySlotWebModel> doctorDaySlots) {
    for (DoctorDaySlotWebModel daySlotModel : doctorDaySlots) {
        try {
            // Validate day slot dates
            if (daySlotModel.getStartSlotDate() == null || daySlotModel.getEndSlotDate() == null) {
                logger.warn("Skipping day slot with null dates for day: " + daySlotModel.getDay());
                continue;
            }

            // Debug date matching
            debugDateMatching(daySlotModel.getStartSlotDate(), daySlotModel.getEndSlotDate(), daySlotModel.getDay());

            // Create and save doctor day slot
            DoctorDaySlot doctorDaySlot = createAndSaveDoctorDaySlot(doctorSlot, savedUser, daySlotModel);
            
            // Process slot times if provided
            if (daySlotModel.getDoctorSlotTimes() != null && !daySlotModel.getDoctorSlotTimes().isEmpty()) {
                processDoctorSlotTimes(savedUser, doctorSlot, doctorDaySlot, daySlotModel);
            }
            
        } catch (Exception e) {
            logger.error("Error processing day slot for day: " + daySlotModel.getDay(), e);
            throw new RuntimeException("Failed to process day slot for " + daySlotModel.getDay() + ": " + e.getMessage(), e);
        }
    }
}

/**
 * Creates and saves a doctor day slot
 */
private DoctorDaySlot createAndSaveDoctorDaySlot(DoctorSlot doctorSlot, User savedUser, DoctorDaySlotWebModel daySlotModel) {
    DoctorDaySlot doctorDaySlot = DoctorDaySlot.builder()
            .doctorSlot(doctorSlot)
            .day(daySlotModel.getDay())
            .startSlotDate(daySlotModel.getStartSlotDate())
            .endSlotDate(daySlotModel.getEndSlotDate())
            .createdBy(savedUser.getCreatedBy())
            .isActive(true)
            .build();
    return doctorDaySlotRepository.save(doctorDaySlot);
}

/**
 * Processes doctor slot times with improved validation
 */
private void processDoctorSlotTimes(User savedUser, DoctorSlot doctorSlot, DoctorDaySlot doctorDaySlot, DoctorDaySlotWebModel daySlotModel) {
    for (DoctorSlotTimeWebModel slotTimeModel : daySlotModel.getDoctorSlotTimes()) {
        try {
            // Validate slot times
            if (slotTimeModel.getSlotStartTime() == null || slotTimeModel.getSlotEndTime() == null) {
                logger.warn("Skipping slot time with null values for day: " + daySlotModel.getDay());
                continue;
            }

            // Create and save doctor slot time
            DoctorSlotTime doctorSlotTime = createAndSaveDoctorSlotTime(doctorDaySlot, savedUser, slotTimeModel);
            
            // Get matching dates for the selected day using fixed timezone method
            List<LocalDate> matchingDates = getMatchingDatesByDay(
                    daySlotModel.getStartSlotDate(),
                    daySlotModel.getEndSlotDate(),
                    daySlotModel.getDay()
            );
            
            // Process each matching date
            if (!matchingDates.isEmpty()) {
                processMatchingDates(savedUser, doctorSlot, doctorDaySlot, doctorSlotTime, matchingDates, slotTimeModel);
            } else {
                logger.warn("No matching dates found for day: " + daySlotModel.getDay() + 
                           " between " + daySlotModel.getStartSlotDate() + " and " + daySlotModel.getEndSlotDate());
            }
            
        } catch (Exception e) {
            logger.error("Error processing slot time for day: " + daySlotModel.getDay(), e);
            throw new RuntimeException("Failed to process slot time: " + e.getMessage(), e);
        }
    }
}

/**
 * Creates and saves a doctor slot time
 */
private DoctorSlotTime createAndSaveDoctorSlotTime(DoctorDaySlot doctorDaySlot, User savedUser, DoctorSlotTimeWebModel slotTimeModel) {
    DoctorSlotTime doctorSlotTime = DoctorSlotTime.builder()
            .doctorDaySlot(doctorDaySlot)
            .slotStartTime(slotTimeModel.getSlotStartTime())
            .slotEndTime(slotTimeModel.getSlotEndTime())
            .slotTime(slotTimeModel.getSlotTime())
            .createdBy(savedUser.getCreatedBy())
            .isActive(true)
            .build();
    return doctorSlotTimeRepository.save(doctorSlotTime);
}

/**
 * Processes matching dates for doctor slots
 */
private void processMatchingDates(User savedUser, DoctorSlot doctorSlot, DoctorDaySlot doctorDaySlot,
                                  DoctorSlotTime doctorSlotTime, List<LocalDate> matchingDates,
                                  DoctorSlotTimeWebModel slotTimeModel) {
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);
    
    for (LocalDate date : matchingDates) {
        try {
            // Save DoctorSlotDate
            DoctorSlotDate doctorSlotDate = createAndSaveDoctorSlotDate(savedUser, doctorSlot, doctorDaySlot, doctorSlotTime, date);
            
            // Split slot into intervals and save
            createDoctorSlotSplitTimes(savedUser, doctorSlotDate, slotTimeModel, timeFormatter);
            
        } catch (Exception e) {
            logger.error("Error processing date: " + date + " for slot time", e);
            throw new RuntimeException("Failed to process date " + date + ": " + e.getMessage(), e);
        }
    }
}

/**
 * Creates and saves a doctor slot date
 */
private DoctorSlotDate createAndSaveDoctorSlotDate(User savedUser, DoctorSlot doctorSlot, DoctorDaySlot doctorDaySlot,
                                                   DoctorSlotTime doctorSlotTime, LocalDate date) {
    DoctorSlotDate doctorSlotDate = DoctorSlotDate.builder()
            .doctorSlotId(doctorSlot.getDoctorSlotId())
            .doctorDaySlotId(doctorDaySlot.getDoctorDaySlotId())
            .doctorSlotTimeId(doctorSlotTime.getDoctorSlotTimeId())
            .date(date.toString())
            .createdBy(savedUser.getCreatedBy())
            .isActive(true)
            .build();
    return doctorSlotDateRepository.save(doctorSlotDate);
}

/**
 * Creates doctor slot split times with improved error handling and validation
 */
private void createDoctorSlotSplitTimes(User savedUser, DoctorSlotDate doctorSlotDate,
                                        DoctorSlotTimeWebModel slotTimeModel, DateTimeFormatter timeFormatter) {
    try {
        // Parse start and end times with better error handling
        LocalTime start = parseTimeWithValidation(slotTimeModel.getSlotStartTime(), timeFormatter);
        LocalTime end = parseTimeWithValidation(slotTimeModel.getSlotEndTime(), timeFormatter);

        // Validate that start time is before end time
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("Start time (" + slotTimeModel.getSlotStartTime() + 
                                             ") must be before end time (" + slotTimeModel.getSlotEndTime() + ")");
        }

        // Parse duration with validation
        int duration = parseDurationWithValidation(slotTimeModel.getSlotTime());

        logger.info("Creating split times from {} to {} with {} minute intervals for date {}",
                   slotTimeModel.getSlotStartTime(), slotTimeModel.getSlotEndTime(),
                   duration, doctorSlotDate.getDate());

        // Create split times
        int slotCount = 0;
        LocalTime currentStart = start;

        while (currentStart.plusMinutes(duration).compareTo(end) <= 0) {
            LocalTime currentEnd = currentStart.plusMinutes(duration);

            // Check if the slot already exists to avoid duplicates
            boolean exists = doctorSlotSplitTimeRepository
                    .existsBySlotStartTimeAndSlotEndTimeAndDoctorSlotDateId(
                            currentStart.format(timeFormatter),
                            currentEnd.format(timeFormatter),
                            doctorSlotDate.getDoctorSlotDateId());

            if (!exists) {
                DoctorSlotSpiltTime splitTime = DoctorSlotSpiltTime.builder()
                        .slotStartTime(currentStart.format(timeFormatter))
                        .slotEndTime(currentEnd.format(timeFormatter))
                        .slotStatus("Available")
                        .createdBy(savedUser.getCreatedBy())
                        .doctorSlotDateId(doctorSlotDate.getDoctorSlotDateId())
                        .isActive(true)
                        .build();

                doctorSlotSplitTimeRepository.save(splitTime);
                slotCount++;
            }

            currentStart = currentEnd;
        }

        logger.info("Created {} split time slots for doctor slot date ID: {}",
                   slotCount, doctorSlotDate.getDoctorSlotDateId());

    } catch (Exception e) {
        logger.error("Error creating doctor slot split times: {}", e.getMessage(), e);
        throw new RuntimeException("Failed to create doctor slot split times: " + e.getMessage(), e);
    }
}

/**
 * Parses time string with better validation
 */
private LocalTime parseTimeWithValidation(String timeString, DateTimeFormatter timeFormatter) {
    if (timeString == null || timeString.trim().isEmpty()) {
        throw new IllegalArgumentException("Time string cannot be null or empty");
    }
    
    try {
        timeString = timeString.trim();
        return LocalTime.parse(timeString, timeFormatter);
    } catch (DateTimeParseException e) {
        logger.error("Failed to parse time: '" + timeString + "'", e);
        throw new IllegalArgumentException("Invalid time format: '" + timeString + "'. Expected format: 'h:mm AM/PM'", e);
    }
}

/**
 * Parses duration string with validation
 */
private int parseDurationWithValidation(String durationString) {
    if (durationString == null || durationString.trim().isEmpty()) {
        throw new IllegalArgumentException("Duration string cannot be null or empty");
    }
    
    try {
        // Extract numeric part from duration string
        String durationStr = durationString.replaceAll("[^0-9]", "").trim();
        if (durationStr.isEmpty()) {
            throw new IllegalArgumentException("No numeric value found in duration: " + durationString);
        }
        
        int duration = Integer.parseInt(durationStr);
        if (duration <= 0) {
            throw new IllegalArgumentException("Duration must be positive: " + duration);
        }
        
        return duration;
    } catch (NumberFormatException e) {
        throw new IllegalArgumentException("Invalid duration format: " + durationString, e);
    }
}

/**
 * Saves doctor leaves if provided
 */
private void saveDoctorLeaves(User savedUser, List<DoctorLeaveListWebModel> doctorLeaveList) {
    if (doctorLeaveList != null && !doctorLeaveList.isEmpty()) {
        for (DoctorLeaveListWebModel leaveModel : doctorLeaveList) {
            if (leaveModel.getDoctorLeaveDate() != null) {
                try {
                    DoctorLeaveList doctorLeave = DoctorLeaveList.builder()
                            .user(savedUser)
                            .doctorLeaveDate(leaveModel.getDoctorLeaveDate())
                            .createdBy(savedUser.getCreatedBy())
                            .userIsActive(true)
                            .build();
                    doctorLeaveListRepository.save(doctorLeave);
                } catch (Exception e) {
                    logger.error("Error saving doctor leave for date: " + leaveModel.getDoctorLeaveDate(), e);
                    // Continue with other leaves instead of failing completely
                }
            }
        }
    }
}

/**
 * FIXED: Returns a list of dates that match the specified day of week within the date range
 * Fixed timezone issue by using India timezone (UTC+5:30)
 */
private List<LocalDate> getMatchingDatesByDay(Date startDate, Date endDate, String dayOfWeek) {
    try {
        if (startDate == null || endDate == null || dayOfWeek == null) {
            logger.warn("Null parameters provided to getMatchingDatesByDay");
            return new ArrayList<>();
        }

        // Use India timezone (UTC+5:30) to match your system timezone
        ZoneId indiaZone = ZoneId.of("Asia/Kolkata");
        LocalDate start = startDate.toInstant().atZone(indiaZone).toLocalDate();
        LocalDate end = endDate.toInstant().atZone(indiaZone).toLocalDate();
        
        logger.info("Converting dates using India timezone - Start: {} End: {} Day: {}", 
                   start, end, dayOfWeek);

        if (start.isAfter(end)) {
            logger.warn("Start date {} is after end date {}", start, end);
            return new ArrayList<>();
        }

        DayOfWeek targetDay;
        try {
            targetDay = DayOfWeek.valueOf(dayOfWeek.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid day of week: {}", dayOfWeek);
            return new ArrayList<>();
        }

        List<LocalDate> dates = new ArrayList<>();
        LocalDate current = start;
        
        logger.info("Looking for {} between {} and {}", targetDay, start, end);
        
        while (!current.isAfter(end)) {
            if (current.getDayOfWeek() == targetDay) {
                dates.add(current);
                logger.info("Found matching date: {} ({})", current, current.getDayOfWeek());
            }
            current = current.plusDays(1);
        }
        
        logger.info("Found {} matching dates for {}: {}", dates.size(), dayOfWeek, dates);
        return dates;
        
    } catch (Exception e) {
        logger.error("Error getting matching dates for day: " + dayOfWeek, e);
        return new ArrayList<>();
    }
}

/**
 * Debug method to check date conversion and matching
 */
private void debugDateMatching(Date startDate, Date endDate, String dayOfWeek) {
    logger.info("=== DEBUG DATE MATCHING ===");
    logger.info("Input - Start: {}, End: {}, Day: {}", startDate, endDate, dayOfWeek);
    
    // Check different timezone conversions
    ZoneId utcZone = ZoneOffset.UTC;
    ZoneId systemZone = ZoneId.systemDefault();
    ZoneId indiaZone = ZoneId.of("Asia/Kolkata");
    
    LocalDate utcStart = startDate.toInstant().atZone(utcZone).toLocalDate();
    LocalDate utcEnd = endDate.toInstant().atZone(utcZone).toLocalDate();
    
    LocalDate systemStart = startDate.toInstant().atZone(systemZone).toLocalDate();
    LocalDate systemEnd = endDate.toInstant().atZone(systemZone).toLocalDate();
    
    LocalDate indiaStart = startDate.toInstant().atZone(indiaZone).toLocalDate();
    LocalDate indiaEnd = endDate.toInstant().atZone(indiaZone).toLocalDate();
    
    logger.info("UTC: {} to {}", utcStart, utcEnd);
    logger.info("System ({}): {} to {}", systemZone, systemStart, systemEnd);
    logger.info("India: {} to {}", indiaStart, indiaEnd);
    
    // Check what day of week each date is
    LocalDate current = indiaStart;
    while (!current.isAfter(indiaEnd)) {
        logger.info("Date: {} is {}", current, current.getDayOfWeek());
        current = current.plusDays(1);
    }
    logger.info("=== END DEBUG ===");
}

/**
 * Enhanced validation for doctor time slots
 */
private String validateDoctorSlots(List<DoctorDaySlotWebModel> doctorDaySlots) {
    if (doctorDaySlots == null || doctorDaySlots.isEmpty()) {
        return null; // No slots to validate
    }

    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);

    // Group slots by day
    Map<String, List<DoctorDaySlotWebModel>> slotsByDay = doctorDaySlots.stream()
            .collect(Collectors.groupingBy(DoctorDaySlotWebModel::getDay));

    // Check each day's slots for overlaps and validity
    for (Map.Entry<String, List<DoctorDaySlotWebModel>> entry : slotsByDay.entrySet()) {
        String day = entry.getKey();
        List<DoctorDaySlotWebModel> daySlots = entry.getValue();

        for (DoctorDaySlotWebModel daySlot : daySlots) {
            // Validate dates
            if (daySlot.getStartSlotDate() == null || daySlot.getEndSlotDate() == null) {
                return "Start date and end date are required for day: " + day;
            }

            // Validate that start date is not after end date
            if (daySlot.getStartSlotDate().after(daySlot.getEndSlotDate())) {
                return "Start date cannot be after end date for day: " + day;
            }

            List<DoctorSlotTimeWebModel> timeSlots = daySlot.getDoctorSlotTimes();
            if (timeSlots == null || timeSlots.isEmpty()) {
                continue;
            }

            // Validate individual time slots
            for (DoctorSlotTimeWebModel timeSlot : timeSlots) {
                if (timeSlot.getSlotStartTime() == null || timeSlot.getSlotEndTime() == null) {
                    return "Start time and end time are required for all slots on day: " + day;
                }

                // Validate time format and logic
                try {
                    LocalTime start = parseTimeWithValidation(timeSlot.getSlotStartTime(), timeFormatter);
                    LocalTime end = parseTimeWithValidation(timeSlot.getSlotEndTime(), timeFormatter);
                    
                    if (!start.isBefore(end)) {
                        return "Start time must be before end time for day: " + day + 
                               " (Start: " + timeSlot.getSlotStartTime() + ", End: " + timeSlot.getSlotEndTime() + ")";
                    }
                } catch (Exception e) {
                    return "Invalid time format for day: " + day + " - " + e.getMessage();
                }

                // Validate duration
                if (timeSlot.getSlotTime() == null || timeSlot.getSlotTime().trim().isEmpty()) {
                    return "Slot duration is required for day: " + day;
                }

                try {
                    parseDurationWithValidation(timeSlot.getSlotTime());
                } catch (Exception e) {
                    return "Invalid duration format for day: " + day + " - " + e.getMessage();
                }
            }

            // Check for overlaps within the same day
            String overlapError = checkTimeSlotOverlaps(timeSlots, day, timeFormatter);
            if (overlapError != null) {
                return overlapError;
            }
        }
    }
    
    return null; // All validations passed
}

/**
 * Checks for time slot overlaps within a day
 */
private String checkTimeSlotOverlaps(List<DoctorSlotTimeWebModel> timeSlots, String day, DateTimeFormatter timeFormatter) {
    if (timeSlots.size() < 2) {
        return null; // No overlaps possible with less than 2 slots
    }

    // Sort slots by start time for easier comparison
    List<DoctorSlotTimeWebModel> sortedTimeSlots = new ArrayList<>(timeSlots);
    try {
        sortedTimeSlots.sort(Comparator.comparing(slot -> parseTimeWithValidation(slot.getSlotStartTime(), timeFormatter)));
    } catch (Exception e) {
        return "Error sorting time slots for day: " + day + " - " + e.getMessage();
    }

    // Check for overlaps
    for (int i = 0; i < sortedTimeSlots.size() - 1; i++) {
        try {
            LocalTime currentEnd = parseTimeWithValidation(sortedTimeSlots.get(i).getSlotEndTime(), timeFormatter);
            LocalTime nextStart = parseTimeWithValidation(sortedTimeSlots.get(i + 1).getSlotStartTime(), timeFormatter);

            if (currentEnd.isAfter(nextStart)) {
                return "Slot overlap detected for day: " + day + " between " +
                       sortedTimeSlots.get(i).getSlotStartTime() + "-" + sortedTimeSlots.get(i).getSlotEndTime() +
                       " and " + sortedTimeSlots.get(i + 1).getSlotStartTime() + "-" + sortedTimeSlots.get(i + 1).getSlotEndTime();
            }
        } catch (Exception e) {
            return "Error validating slot times for day: " + day + " - " + e.getMessage();
        }
    }
    
    return null; // No overlaps found
}


	/**
	 * Enhanced file upload handling with better error management
	 */
	public void handleFileUploads(User user, List<FileInputWebModel> filesInputWebModel) throws IOException {
	    if (filesInputWebModel == null || filesInputWebModel.isEmpty()) {
	        return; // No files to upload
	    }

	    List<MediaFile> filesList = new ArrayList<>();
	    
	    for (FileInputWebModel fileInput : filesInputWebModel) {
	        if (fileInput.getFileData() != null && !fileInput.getFileData().trim().isEmpty()) {
	            try {
	                // Validate file input
	                if (fileInput.getFileName() == null || fileInput.getFileName().trim().isEmpty()) {
	                    logger.warn("Skipping file upload due to missing filename");
	                    continue;
	                }

	                // Create a new MediaFile instance for each file
	                MediaFile mediaFile = new MediaFile();
	                String fileName = UUID.randomUUID().toString();

	                // Set properties of the media file
	                mediaFile.setFileName(fileName);
	                mediaFile.setUser(user);
	                mediaFile.setFileOriginalName(fileInput.getFileName());
	                mediaFile.setFileSize(fileInput.getFileSize());
	                mediaFile.setFileType(fileInput.getFileType());
	                mediaFile.setCategory(MediaFileCategory.profilePic);
	                mediaFile.setFileDomainId(HealthCareConstant.ProfilePhoto);
	                mediaFile.setFileDomainReferenceId(user.getUserId());
	                mediaFile.setFileIsActive(true);
	                mediaFile.setFileCreatedBy(user.getCreatedBy());

	                // Save media file to the database
	                mediaFile = mediaFileRepository.save(mediaFile);
	                filesList.add(mediaFile);

	                // Save the file to the file system
	                Base64FileUpload.saveFile(imageLocation + "/profilePhoto", fileInput.getFileData(), fileName);
	                
	                logger.info("Successfully uploaded file: {} for user: {}", fileInput.getFileName(), user.getEmailId());
	                
	            } catch (Exception e) {
	                logger.error("Error uploading file: " + fileInput.getFileName() + " for user: " + user.getEmailId(), e);
	                // Continue with other files instead of failing completely
	            }
	        }
	    }
	}
	@Override
	public RefreshToken createRefreshToken(User user) {
		try {
			logger.info("createRefreshToken method start");

			// Find the user by username and userType
			Optional<User> checkUser = userRepository.findByEmailId(user.getEmailId(), user.getUserType());

			// Check if the user is present
			if (checkUser.isPresent()) {
				User users = checkUser.get(); // Get the actual user

				// Create and set refresh token details
				RefreshToken refreshToken = new RefreshToken();
				refreshToken.setUserId(users.getUserId()); // Set userId from the found user
				refreshToken.setToken(UUID.randomUUID().toString()); // Generate a random token
				// refreshToken.setExpiryToken(LocalTime.now().plusMinutes(45)); // Uncomment if
				// expiry is needed

				// Save the refresh token to the repository
				refreshToken = refreshTokenRepository.save(refreshToken);

				logger.info("createRefreshToken method end");
				return refreshToken;
			} else {
				logger.warn("User not found for username: " + user.getEmailId());
				return null; // Return null if user is not found
			}
		} catch (Exception e) {
			logger.error("Error in createRefreshToken method: ", e);
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Response verifyExpiration(RefreshToken refreshToken) {
		// TODO Auto-generated method stub
		return new Response(-1, "Fail", "RefreshToken expired");
	}
	@Override
	public ResponseEntity<Response> getUserDetailsByUserType(String userType, Integer pageNo, Integer pageSize) {
	    logger.info("Fetching user details for userType: {}, pageNo: {}, pageSize: {}", userType, pageNo, pageSize);

	    if (pageNo == null || pageNo < 1) {
	        return ResponseEntity.ok(new Response(0, "Page number must be at least 1", new ArrayList<>()));
	    }

	    if (pageSize == null || pageSize < 1) {
	        return ResponseEntity.ok(new Response(0, "Page size must be at least 1", new ArrayList<>()));
	    }

	    PageRequest pageRequest = PageRequest.of(pageNo - 1, pageSize, Sort.by("userCreatedOn").descending());
	    Page<User> usersPage = userRepository.findByUserType(userType, pageRequest);

	    // Even if page is empty, don't return "out of range" unless total is zero
	    if (usersPage.getTotalElements() == 0) {
	        return ResponseEntity.ok(new Response(0, "No users found for given userType", new ArrayList<>()));
	    }

	    List<Map<String, Object>> usersResponseList = usersPage.getContent().stream().map(user -> {
	        Map<String, Object> userMap = new HashMap<>();
	        userMap.put("userId", user.getUserId());
	        userMap.put("userName", user.getUserName());
	        userMap.put("emailId", user.getEmailId());
	        userMap.put("phoneNumber", user.getPhoneNumber());
	        userMap.put("countryCode", user.getCountryCode());
	        userMap.put("address", user.getCurrentAddress());
	        userMap.put("userType", user.getUserType());
	        userMap.put("userIsActive", user.getUserIsActive());
	        userMap.put("empId", user.getEmpId());
	        userMap.put("gender", user.getGender());
	        userMap.put("createdOn", user.getUserCreatedOn());
	        return userMap;
	    }).collect(Collectors.toList());

	    Map<String, Object> responseMap = new HashMap<>();
	    responseMap.put("users", usersResponseList);
	    responseMap.put("currentPage", pageNo);
	    responseMap.put("pageSize", pageSize);
	    responseMap.put("totalPages", usersPage.getTotalPages());
	    responseMap.put("totalElements", usersPage.getTotalElements());
	    responseMap.put("isFirst", usersPage.isFirst());
	    responseMap.put("isLast", usersPage.isLast());
	    responseMap.put("hasNext", usersPage.hasNext());
	    responseMap.put("hasPrevious", usersPage.hasPrevious());

	    return ResponseEntity.ok(new Response(1, "Users retrieved successfully", responseMap));
	}



	@Override
	public ResponseEntity<?> getDropDownByUserTypeByHospitalId() {
		try {
			// Fetch users with userType = "ADMIN" and hospitalId is null
			List<User> admins = userRepository.findByUserTypeAndHospitalIdIsNull("ADMIN");

			// Extract only userId and userName
			List<Map<String, Object>> adminList = admins.stream().map(admin -> {
				Map<String, Object> userMap = new HashMap<>();
				userMap.put("userId", admin.getUserId());
				userMap.put("userName", admin.getUserName());
				return userMap;
			}).collect(Collectors.toList());

			// Prepare response using HashMap
			Map<String, Object> response = new HashMap<>();
			// response.put("admins", adminList.isEmpty() ? new ArrayList<>() : adminList);
			// // Ensure empty array instead of null

			return ResponseEntity.ok(new Response(1, "success", adminList.isEmpty() ? new ArrayList<>() : adminList));
		} catch (Exception e) {
			Map<String, String> errorResponse = new HashMap<>();
			errorResponse.put("error", "An error occurred while fetching admins");
			return ResponseEntity.status(500).body(errorResponse);
		}

	}

	@Override
	public ResponseEntity<?> updateUserDetailsByUserId(HospitalDataListWebModel userWebModel) {
		HashMap<String, Object> response = new HashMap<>();
		try {
			logger.info("Updating user details for userId: " + userWebModel.getUserId());

			// Step 1: Retrieve the existing user
			Optional<User> userOptional = userRepository.findById(userWebModel.getUserId());
			if (!userOptional.isPresent()) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response(0, "Fail", "User not found"));
			}

			User existingUser = userOptional.get();

			// Step 2: Update user details (only non-null values)
			if (userWebModel.getEmailId() != null)
				existingUser.setEmailId(userWebModel.getEmailId());
			if (userWebModel.getFirstName() != null)
				existingUser.setFirstName(userWebModel.getFirstName());
			if (userWebModel.getPassword() != null && !userWebModel.getPassword().isEmpty()) {
				existingUser.setPassword(passwordEncoder.encode(userWebModel.getPassword()));
			}
			if(userWebModel.getEmpId() != null)
				existingUser.setEmpId(userWebModel.getEmpId());
			if (userWebModel.getLastName() != null)
				existingUser.setLastName(userWebModel.getLastName());
			if (userWebModel.getPhoneNumber() != null)
				existingUser.setPhoneNumber(userWebModel.getPhoneNumber());
			if (userWebModel.getCurrentAddress() != null)
				existingUser.setCurrentAddress(userWebModel.getCurrentAddress());
			if (userWebModel.getUserUpdatedBy() != null)
				existingUser.setUserUpdatedBy(userWebModel.getUserUpdatedBy());
			if (userWebModel.getYearOfExperiences() != null)
				existingUser.setYearOfExperiences(userWebModel.getYearOfExperiences());
			
			if (userWebModel.getDoctorFees() != null)
				existingUser.setDoctorFees(userWebModel.getDoctorFees());
			
			if(userWebModel.getCountryCode() != null)
				existingUser.setCountryCode(userWebModel.getCountryCode());
		
			if(userWebModel.getLabMasterDataId() != null)
				existingUser.setLabMasterDataId(userWebModel.getLabMasterDataId());
			
			if(userWebModel.getSupportStaffId() != null)
				existingUser.setSupportStaffId(userWebModel.getSupportStaffId());
			
			if(userWebModel.getDob() != null)
				existingUser.setDob(userWebModel.getDob());

			// Update timestamp
			existingUser.setUserUpdatedOn(new Date());

			// Merge firstName + lastName → userName
			String fullName = (existingUser.getFirstName() != null ? existingUser.getFirstName() : "") + " "
					+ (existingUser.getLastName() != null ? existingUser.getLastName() : "");
			existingUser.setUserName(fullName.trim());

			// Step 3: Save updated user
			User updatedUser = userRepository.save(existingUser);

//			// Step 5: Upload new media file (if provided)
//			if (userWebModel.getFilesInputWebModel() != null && !userWebModel.getFilesInputWebModel().isEmpty()) {
//			    // Only delete old media if new media is being uploaded
//			    List<MediaFile> oldMediaFiles = mediaFileRepository.findByUserId(HealthCareConstant.ProfilePhoto,
//			            updatedUser.getUserId());
//			    if (!oldMediaFiles.isEmpty()) {
//			        for (MediaFile oldMediaFile : oldMediaFiles) {
//			            Base64FileUpload.deleteFile(imageLocation + "/profilePhoto", oldMediaFile.getFileName());
//			            mediaFileRepository.deleteById(oldMediaFile.getFileId());
//			        }
//			    }
//
//			    // Then upload the new files
//			    handleFileUploads(updatedUser, userWebModel.getFilesInputWebModel());
//			}

			// Step 5: Handle profile photo update/removal
			List<MediaFile> oldMediaFiles = mediaFileRepository.findByUserId(
			        HealthCareConstant.ProfilePhoto, updatedUser.getUserId());

			// If filesInputWebModel is null or empty → only delete old files
			if (userWebModel.getFilesInputWebModel() == null || userWebModel.getFilesInputWebModel().isEmpty()) {
			    if (!oldMediaFiles.isEmpty()) {
			        for (MediaFile oldMediaFile : oldMediaFiles) {
			            Base64FileUpload.deleteFile(imageLocation + "/profilePhoto", oldMediaFile.getFileName());
			            mediaFileRepository.deleteById(oldMediaFile.getFileId());
			        }
			    }
			} else {
			    // If new files are provided → delete old and upload new
			    if (!oldMediaFiles.isEmpty()) {
			        for (MediaFile oldMediaFile : oldMediaFiles) {
			            Base64FileUpload.deleteFile(imageLocation + "/profilePhoto", oldMediaFile.getFileName());
			            mediaFileRepository.deleteById(oldMediaFile.getFileId());
			        }
			    }

			    // Upload new profile photo(s)
			    handleFileUploads(updatedUser, userWebModel.getFilesInputWebModel());
			}

			// Step 6: Update User Roles (remove all and reassign if provided)
			doctorRoleRepository.deactivateUser(updatedUser.getUserId()); // always remove all existing roles

			// Reassign only if new roles are provided
			if (userWebModel.getRoleIds() != null && !userWebModel.getRoleIds().isEmpty()) {
			    for (Integer roleId : userWebModel.getRoleIds()) {
			        DoctorRole doctorRole = DoctorRole.builder()
			                .user(updatedUser)
			                .roleId(roleId)
			                .createdBy(updatedUser.getCreatedBy())
			                .userIsActive(true)
			                .build();
			        doctorRoleRepository.save(doctorRole);
			    }
			}

//			// Save doctor leaves if provided
//			if (userWebModel.getDoctorLeaveList() != null) {
//				for (DoctorLeaveListWebModel leaveModel : userWebModel.getDoctorLeaveList()) {
//					DoctorLeaveList doctorLeave = DoctorLeaveList.builder().user(updatedUser)
//							.doctorLeaveDate(leaveModel.getDoctorLeaveDate()).createdBy(updatedUser.getCreatedBy())
//							.userIsActive(true).build();
//					doctorLeaveListRepository.save(doctorLeave);
//				}
//			}
			if (userWebModel.getDoctorLeaveList() != null) {
			    for (DoctorLeaveListWebModel leaveModel : userWebModel.getDoctorLeaveList()) {
			        Date leaveDate = leaveModel.getDoctorLeaveDate();
			        boolean leaveExists = doctorLeaveListRepository
			            .existsByUserUserIdAndDoctorLeaveDate(updatedUser.getUserId(), leaveDate);
			        
			        if (!leaveExists) {
			            DoctorLeaveList doctorLeave = DoctorLeaveList.builder()
			                .user(updatedUser)
			                .doctorLeaveDate(leaveDate)
			                .createdBy(updatedUser.getCreatedBy())
			                .userIsActive(true)
			                .build();
			            doctorLeaveListRepository.save(doctorLeave);
			        } else {
			            logger.info("Leave already exists for userId: " + updatedUser.getUserId() + " on date: " + leaveDate);
			        }
			    }
			}


			// Step 6: Return success response
			response.put("message", "User details updated successfully");
			response.put("data", updatedUser);

			return ResponseEntity.ok(new Response(1, "Success", "User details updated successfully"));

		} catch (Exception e) {
			logger.error("Error updating user details: " + e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new Response(0, "Fail", "Error updating user details"));
		}
	}

	@Override
	public ResponseEntity<?> deleteUserDetailsByUserId(HospitalDataListWebModel userWebModel) {
		HashMap<String, Object> response = new HashMap<>();
		try {
			// Log the incoming soft delete request
			logger.info("Soft deleting user details for userId: " + userWebModel.getUserId());

			// Step 1: Retrieve the existing user by userId
			Optional<User> userOptional = userRepository.findById(userWebModel.getUserId());

			// Step 2: Check if the user exists
			if (!userOptional.isPresent()) {
				response.put("message", "User not found");
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response(0, "Fail", "User not found"));
			}

			// Step 3: Get the existing user entity
			User existingUser = userOptional.get();

			// Step 4: Set userIsActive to false (soft delete)
			existingUser.setUserIsActive(false);

			// Update the 'userUpdatedOn' field to the current time
			existingUser.setUserUpdatedOn(new Date());

			// Step 5: Save the updated user entity back to the database
			User updatedUser = userRepository.save(existingUser);

			// Step 6: Soft delete associated HospitalAdmin entities (if any)
			List<HospitalAdmin> hospitalAdmins = hospitalAdminRepository.findByAdminUserId(userWebModel.getUserId());
			for (HospitalAdmin admin : hospitalAdmins) {
				admin.setUserIsActive(false); // Set admin as inactive
				admin.setUserUpdatedOn(new Date()); // Update the timestamp for admin
				hospitalAdminRepository.save(admin); // Save the updated admin record
			}

			// Step 7: Soft delete associated Doctor entities (if any)
			List<DoctorRole> doctors = doctorRoleRepository.findByDoctorUserId(userWebModel.getUserId());
			for (DoctorRole doctor : doctors) {
				doctor.setUserIsActive(false); // Set doctor as inactive
				doctor.setUserUpdatedOn(new Date()); // Update the timestamp for doctor
				doctorRoleRepository.save(doctor); // Save the updated doctor record
			}

			// Step 8: Return success response
			response.put("message", "User, HospitalAdmin, and Doctor soft deleted successfully");
			response.put("data", updatedUser);

			return ResponseEntity.ok(new Response(1, "success", "deleted successfully"));

		} catch (Exception e) {
			logger.error("Error soft deleting user, HospitalAdmin, and Doctor details: " + e.getMessage(), e);
			response.put("message", "Error soft deleting user, HospitalAdmin, and Doctor details");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new Response(0, "Fail", "Error soft deleting user, HospitalAdmin, and Doctor details"));
		}
	}

	@Override
	public ResponseEntity<?> deletehospitalAdminByUserId(Integer adminId) {
		HashMap<String, Object> response = new HashMap<>();
		try {
			// Log the incoming delete request
			logger.info("Soft deleting admin user with adminId: " + adminId);

			// Step 1: Retrieve the existing hospital admin by adminId (using Optional)
			Optional<HospitalAdmin> hospitalAdminOptional = hospitalAdminRepository.findById(adminId);

			// Step 2: Check if the admin exists
			if (!hospitalAdminOptional.isPresent()) {
				response.put("message", "Admin not found for the given adminId");
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new Response(0, "Fail", "Admin not found for the given adminId"));
			}

			// Step 3: Get the existing HospitalAdmin entity
			HospitalAdmin hospitalAdmin = hospitalAdminOptional.get();

			// Step 4: Set userIsActive to false to soft delete the admin
			hospitalAdmin.setUserIsActive(false); // Mark the admin as inactive
			hospitalAdmin.setUserUpdatedOn(new Date()); // Update the timestamp of when it was deactivated

			// Step 5: Save the updated HospitalAdmin entity back to the database
			hospitalAdminRepository.save(hospitalAdmin);

			return ResponseEntity.ok(new Response(1, "success", "Admin user deactivated successfully"));

		} catch (Exception e) {
			logger.error("Error soft deleting admin: " + e.getMessage(), e);
			response.put("message", "Error soft deleting admin");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new Response(0, "Fail", "Error soft deleting admin"));
		}
	}
//	@Override
//	public ResponseEntity<?> getUserDetailsByUserId(Integer userId) {
//	    try {
//	        Optional<User> userData = userRepository.findById(userId);
//
//	        if (!userData.isPresent()) {
//	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//	                    .body(Collections.singletonMap("message", "User not found"));
//	        }
//
//	        User user = userData.get();
//	        Map<String, Object> data = new HashMap<>();
//	        data.put("userId", user.getUserId());
//	        data.put("userName", user.getUserName());
//	        data.put("emailId", user.getEmailId());
//	        data.put("userType", user.getUserType());
//	        data.put("firstName", user.getFirstName());
//	        data.put("lastName", user.getLastName());
//	        data.put("empId", user.getEmpId());
//	        data.put("address", user.getCurrentAddress());
//	        data.put("doctorFees", user.getDoctorFees());
//	        data.put("phoneNumber", user.getPhoneNumber());
//	        data.put("gender", user.getGender());
//	        data.put("countryCode", user.getCountryCode());
//	        data.put("dob", user.getDob());
//	        data.put("yearOfExperience", user.getYearOfExperiences());
//	        Integer hospitalId = user.getHospitalId();
//	        data.put("hospitalId", hospitalId);
//	        Integer supportStaffId = user.getSupportStaffId(); // assuming this exists in the User entity
//	        data.put("supportStaffId", supportStaffId);
//
//	        if (supportStaffId != null) {
//	            try {
//	                Optional<SupportStaffMasterData> staffDataOpt = supportStaffMasterDataRepository.findById(supportStaffId);
//	                String staffName = staffDataOpt.map(SupportStaffMasterData::getName).orElse("N/A");
//	                data.put("supportStaffName", staffName);
//	            } catch (Exception e) {
//	                logger.error("Error retrieving support staff name for supportStaffId {}: {}", supportStaffId, e.getMessage());
//	                data.put("supportStaffName", "Error retrieving");
//	            }
//	        } else {
//	            data.put("supportStaffName", "N/A");
//	        }
//	        Integer labMasterDataId= user.getLabMasterDataId(); // assuming this exists in the User entity
//	        data.put("labMasterDataId", labMasterDataId);
//
//	        if (labMasterDataId != null) {
//	            try {
//	                Optional<LabMasterData> staffDataOpt = labMasterDataRepository.findById(labMasterDataId);
//	                String labStaffName = staffDataOpt.map(LabMasterData::getName).orElse("N/A");
//	                data.put("LabMasterDataName", labStaffName);
//	            } catch (Exception e) {
//	                logger.error("Error retrieving LabMasterDataname for labMasterDataId {}: {}", labMasterDataId, e.getMessage());
//	                data.put("LabMasterDataName", "Error retrieving");
//	            }
//	        } else {
//	            data.put("LabMasterDataName", "N/A");
//	        }
//
//
//	        
//	        if (hospitalId != null) {
//	            try {
//	                Optional<HospitalDataList> hospitalData = hospitalDataListRepository.findByHospitalId(hospitalId);
//	                data.put("hospitalName", hospitalData.map(HospitalDataList::getHospitalName).orElse("N/A"));
//	            } catch (Exception e) {
//	                logger.error("Error retrieving hospital name for hospitalId {}: {}", hospitalId, e.getMessage());
//	                data.put("hospitalName", "Error retrieving");
//	            }
//	        } else {
//	            data.put("hospitalName", "N/A");
//	        }
//
//	        data.put("userIsActive", user.getUserIsActive());
//	     // Roles
//	        List<Map<String, Object>> roleDetails = new ArrayList<>();
//	        if (user.getDoctorRoles() != null) {
//	            for (DoctorRole doctorRole : user.getDoctorRoles()) {
//	                // Only include active roles
//	                if (Boolean.TRUE.equals(doctorRole.getUserIsActive())) {
//	                    Map<String, Object> roleMap = new HashMap<>();
//	                    roleMap.put("roleId", doctorRole.getRoleId());
//	                    roleMap.put("isActive", doctorRole.getUserIsActive());
//	                    try {
//	                        String specialtyName = doctorSpecialtyRepository
//	                                .findSpecialtyNameByRoleId(doctorRole.getRoleId());
//	                        roleMap.put("specialtyName", specialtyName != null ? specialtyName : "N/A");
//	                    } catch (Exception e) {
//	                        logger.error("Error fetching specialty name for roleId {}: {}", doctorRole.getRoleId(), e.getMessage());
//	                        roleMap.put("specialtyName", "Error retrieving");
//	                    }
//	                    roleDetails.add(roleMap);
//	                }
//	            }
//	        }
//
//	        data.put("roles", roleDetails);
//
//
//	    	// Retrieve media files associated with the hospital data (Profile Photo)
//			List<MediaFile> files = mediaFileRepository.findByFileDomainIdAndFileDomainReferenceId(
//					HealthCareConstant.ProfilePhoto, user.getUserId());
//
//			// Prepare the list of FileInputWebModel from retrieved media files
//			ArrayList<FileInputWebModel> filesInputWebModel = new ArrayList<>();
//
//			for (MediaFile mediaFile : files) {
//				FileInputWebModel filesInput = new FileInputWebModel();
//				filesInput.setFileName(mediaFile.getFileOriginalName());
//				filesInput.setFileId(mediaFile.getFileId());
//				filesInput.setFileSize(mediaFile.getFileSize());
//				filesInput.setFileType(mediaFile.getFileType());
//
//				String fileData = Base64FileUpload.encodeToBase64String(imageLocation + "/profilePhoto",
//						mediaFile.getFileName());
//				filesInput.setFileData(fileData);
//
//				filesInputWebModel.add(filesInput);
//			}
//
//	        data.put("profilePhotos", filesInputWebModel);
//
//	        // Doctor-specific data
//	        if ("DOCTOR".equalsIgnoreCase(user.getUserType())) {
//	            // Doctor Slots
//	            List<Map<String, Object>> doctorSlotList = new ArrayList<>();
//	            List<DoctorSlot> doctorSlots = doctorSlotRepository.findByUser(user);
//	            for (DoctorSlot doctorSlot : doctorSlots) {
//	                Map<String, Object> slotData = new HashMap<>();
//	                slotData.put("slotId", doctorSlot.getDoctorSlotId());
//	                slotData.put("isActive", doctorSlot.getIsActive());
//
//	                // Day Slots
//	                List<Map<String, Object>> daySlotList = new ArrayList<>();
//	                List<DoctorDaySlot> doctorDaySlots = doctorDaySlotRepository.findByDoctorSlot(doctorSlot);
//	                for (DoctorDaySlot daySlot : doctorDaySlots) {
//	                    Map<String, Object> daySlotData = new HashMap<>();
//	                    daySlotData.put("daySlotId", daySlot.getDoctorDaySlotId());
//	                    daySlotData.put("day", daySlot.getDay());
//	                    daySlotData.put("startSlotDate", daySlot.getStartSlotDate());
//	                    daySlotData.put("endSlotDate", daySlot.getEndSlotDate());
//	                    daySlotData.put("isActive", daySlot.getIsActive());
//
//	                    // Time Slots
//	                    List<Map<String, Object>> timeSlotList = new ArrayList<>();
//	                    List<DoctorSlotTime> doctorSlotTimes = doctorSlotTimeRepository.findByDoctorDaySlot(daySlot);
//	                    for (DoctorSlotTime slotTime : doctorSlotTimes) {
//	                        Map<String, Object> timeSlotData = new HashMap<>();
//	                        timeSlotData.put("timeSlotId", slotTime.getDoctorSlotTimeId());
//	                        timeSlotData.put("slotStartTime", slotTime.getSlotStartTime());
//	                        timeSlotData.put("slotEndTime", slotTime.getSlotEndTime());
//	                        timeSlotData.put("slotTime", slotTime.getSlotTime());
//	                        timeSlotData.put("isActive", slotTime.getIsActive());
//
//	                        // Slot Dates
//	                        List<DoctorSlotDate> slotDates = doctorSlotDateRepository
//	                                .findByDoctorSlotIdAndDoctorDaySlotIdAndDoctorSlotTimeId(
//	                                        doctorSlot.getDoctorSlotId(),
//	                                        daySlot.getDoctorDaySlotId(),
//	                                        slotTime.getDoctorSlotTimeId());
//
//	                        List<Map<String, Object>> slotDatesWithSplit = new ArrayList<>();
//	                        for (DoctorSlotDate slotDate : slotDates) {
//	                            Map<String, Object> slotDateMap = new HashMap<>();
//	                            slotDateMap.put("date", slotDate.getDate());
//	                            slotDateMap.put("isActive", slotDate.getIsActive());
//
//	                            // Split Times
//	                            List<DoctorSlotSpiltTime> splitTimes = doctorSlotSplitTimeRepository
//	                                    .findByDoctorSlotDateId(slotDate.getDoctorSlotDateId());
//
//	                            List<Map<String, Object>> splitTimeList = new ArrayList<>();
//	                            for (DoctorSlotSpiltTime splitTime : splitTimes) {
//	                                Map<String, Object> splitMap = new HashMap<>();
//	                                splitMap.put("slotStartTime", splitTime.getSlotStartTime());
//	                                splitMap.put("slotEndTime", splitTime.getSlotEndTime());
//	                                splitMap.put("slotStatus", splitTime.getSlotStatus());
//	                                splitMap.put("isActive", splitTime.getIsActive());
//	                                splitMap.put("id", splitTime.getDoctorSlotSpiltTimeId());
//	                                splitTimeList.add(splitMap);
//	                            }
//
//	                            slotDateMap.put("splitTimes", splitTimeList);
//	                            slotDatesWithSplit.add(slotDateMap);
//	                        }
//
//	                        timeSlotData.put("dates", slotDatesWithSplit);
//	                        timeSlotList.add(timeSlotData);
//	                    }
//
//	                    daySlotData.put("timeSlots", timeSlotList);
//	                    daySlotList.add(daySlotData);
//	                }
//
//	                slotData.put("daySlots", daySlotList);
//	                doctorSlotList.add(slotData);
//	            }
//	            data.put("doctorSlots", doctorSlotList);
//
//	            // Doctor Leaves
//	            List<Map<String, Object>> doctorLeaveList = new ArrayList<>();
//	            List<DoctorLeaveList> doctorLeaves = doctorLeaveListRepository.findByUser(user);
//	            for (DoctorLeaveList doctorLeave : doctorLeaves) {
//	                Map<String, Object> leaveData = new HashMap<>();
//	                leaveData.put("leaveId", doctorLeave.getDoctorLeaveListId());
//	                leaveData.put("doctorLeaveDate", doctorLeave.getDoctorLeaveDate());
//	                leaveData.put("userIsActive", doctorLeave.getUserIsActive());
//	                doctorLeaveList.add(leaveData);
//	            }
//	            data.put("doctorLeaveList", doctorLeaveList);
//	        }
//
//	        return ResponseEntity.ok(data);
//	    } catch (Exception e) {
//	        logger.error("Exception while retrieving user details: ", e);
//	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//	                .body(Collections.singletonMap("message", "Error retrieving user details"));
//	    }
//	}

	//checking
	@Override
	public ResponseEntity<?> getUserDetailsByUserId(Integer userId) {
	    try {
	        Optional<User> userData = userRepository.findById(userId);

	        if (!userData.isPresent()) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                    .body(Collections.singletonMap("message", "User not found"));
	        }

	        User user = userData.get();
	        Map<String, Object> data = new HashMap<>();
	        data.put("userId", user.getUserId());
	        data.put("userName", user.getUserName());
	        data.put("emailId", user.getEmailId());
	        data.put("userType", user.getUserType());
	        data.put("firstName", user.getFirstName());
	        data.put("lastName", user.getLastName());
	        data.put("empId", user.getEmpId());
	        data.put("address", user.getCurrentAddress());
	        data.put("doctorFees", user.getDoctorFees());
	        data.put("phoneNumber", user.getPhoneNumber());
	        data.put("gender", user.getGender());
	        data.put("countryCode", user.getCountryCode());
	        data.put("dob", user.getDob());
	        data.put("yearOfExperience", user.getYearOfExperiences());
	        Integer hospitalId = user.getHospitalId();
	        data.put("hospitalId", hospitalId);
	        Integer supportStaffId = user.getSupportStaffId();
	        data.put("supportStaffId", supportStaffId);

	        if (supportStaffId != null) {
	            try {
	                Optional<SupportStaffMasterData> staffDataOpt = supportStaffMasterDataRepository.findById(supportStaffId);
	                String staffName = staffDataOpt.map(SupportStaffMasterData::getName).orElse("N/A");
	                data.put("supportStaffName", staffName);
	            } catch (Exception e) {
	                logger.error("Error retrieving support staff name for supportStaffId {}: {}", supportStaffId, e.getMessage());
	                data.put("supportStaffName", "Error retrieving");
	            }
	        } else {
	            data.put("supportStaffName", "N/A");
	        }
	        
	        Integer labMasterDataId = user.getLabMasterDataId();
	        data.put("labMasterDataId", labMasterDataId);

	        if (labMasterDataId != null) {
	            try {
	                Optional<LabMasterData> staffDataOpt = labMasterDataRepository.findById(labMasterDataId);
	                String labStaffName = staffDataOpt.map(LabMasterData::getName).orElse("N/A");
	                data.put("LabMasterDataName", labStaffName);
	            } catch (Exception e) {
	                logger.error("Error retrieving LabMasterDataname for labMasterDataId {}: {}", labMasterDataId, e.getMessage());
	                data.put("LabMasterDataName", "Error retrieving");
	            }
	        } else {
	            data.put("LabMasterDataName", "N/A");
	        }

	        if (hospitalId != null) {
	            try {
	                Optional<HospitalDataList> hospitalData = hospitalDataListRepository.findByHospitalId(hospitalId);
	                data.put("hospitalName", hospitalData.map(HospitalDataList::getHospitalName).orElse("N/A"));
	            } catch (Exception e) {
	                logger.error("Error retrieving hospital name for hospitalId {}: {}", hospitalId, e.getMessage());
	                data.put("hospitalName", "Error retrieving");
	            }
	        } else {
	            data.put("hospitalName", "N/A");
	        }

	        data.put("userIsActive", user.getUserIsActive());
	        
	        // Roles
	        List<Map<String, Object>> roleDetails = new ArrayList<>();
	        if (user.getDoctorRoles() != null) {
	            for (DoctorRole doctorRole : user.getDoctorRoles()) {
	                if (Boolean.TRUE.equals(doctorRole.getUserIsActive())) {
	                    Map<String, Object> roleMap = new HashMap<>();
	                    roleMap.put("roleId", doctorRole.getRoleId());
	                    roleMap.put("isActive", doctorRole.getUserIsActive());
	                    try {
	                        String specialtyName = doctorSpecialtyRepository
	                                .findSpecialtyNameByRoleId(doctorRole.getRoleId());
	                        roleMap.put("specialtyName", specialtyName != null ? specialtyName : "N/A");
	                    } catch (Exception e) {
	                        logger.error("Error fetching specialty name for roleId {}: {}", doctorRole.getRoleId(), e.getMessage());
	                        roleMap.put("specialtyName", "Error retrieving");
	                    }
	                    roleDetails.add(roleMap);
	                }
	            }
	        }
	        data.put("roles", roleDetails);

	        // Retrieve media files associated with the hospital data (Profile Photo)
	        List<MediaFile> files = mediaFileRepository.findByFileDomainIdAndFileDomainReferenceId(
	                HealthCareConstant.ProfilePhoto, user.getUserId());

	        // Prepare the list of FileInputWebModel from retrieved media files
	        ArrayList<FileInputWebModel> filesInputWebModel = new ArrayList<>();

	        for (MediaFile mediaFile : files) {
	            FileInputWebModel filesInput = new FileInputWebModel();
	            filesInput.setFileName(mediaFile.getFileOriginalName());
	            filesInput.setFileId(mediaFile.getFileId());
	            filesInput.setFileSize(mediaFile.getFileSize());
	            filesInput.setFileType(mediaFile.getFileType());

	            String fileData = Base64FileUpload.encodeToBase64String(imageLocation + "/profilePhoto",
	                    mediaFile.getFileName());
	            filesInput.setFileData(fileData);

	            filesInputWebModel.add(filesInput);
	        }

	        data.put("profilePhotos", filesInputWebModel);

	        // Doctor-specific data
	        if ("DOCTOR".equalsIgnoreCase(user.getUserType())) {
	            // Get doctor leaves and create a set of leave dates for quick lookup
	            List<DoctorLeaveList> doctorLeaves = doctorLeaveListRepository.findByUser(user);
	            Set<String> leaveDatesAsStrings = new HashSet<>();
	            
	            // FIX: Set timezone to IST for consistent date formatting
	            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	            dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata")); // IST timezone
	            
	            for (DoctorLeaveList doctorLeave : doctorLeaves) {
	                if (Boolean.TRUE.equals(doctorLeave.getUserIsActive())) {
	                    // Convert Date to string format for comparison with IST timezone
	                    String leaveDateStr = dateFormat.format(doctorLeave.getDoctorLeaveDate());
	                    leaveDatesAsStrings.add(leaveDateStr);
	                }
	            }

	            // Doctor Slots
	            List<Map<String, Object>> doctorSlotList = new ArrayList<>();
	            List<DoctorSlot> doctorSlots = doctorSlotRepository.findByUser(user);
	            for (DoctorSlot doctorSlot : doctorSlots) {
	                Map<String, Object> slotData = new HashMap<>();
	                slotData.put("slotId", doctorSlot.getDoctorSlotId());
	                slotData.put("isActive", doctorSlot.getIsActive());

	                // Day Slots
	                List<Map<String, Object>> daySlotList = new ArrayList<>();
	                List<DoctorDaySlot> doctorDaySlots = doctorDaySlotRepository.findByDoctorSlot(doctorSlot);
	                for (DoctorDaySlot daySlot : doctorDaySlots) {
	                    Map<String, Object> daySlotData = new HashMap<>();
	                    daySlotData.put("daySlotId", daySlot.getDoctorDaySlotId());
	                    daySlotData.put("day", daySlot.getDay());
	                    daySlotData.put("startSlotDate", daySlot.getStartSlotDate());
	                    daySlotData.put("endSlotDate", daySlot.getEndSlotDate());
	                    daySlotData.put("isActive", daySlot.getIsActive());

	                    // Time Slots
	                    List<Map<String, Object>> timeSlotList = new ArrayList<>();
	                    List<DoctorSlotTime> doctorSlotTimes = doctorSlotTimeRepository.findByDoctorDaySlot(daySlot);
	                    for (DoctorSlotTime slotTime : doctorSlotTimes) {
	                        Map<String, Object> timeSlotData = new HashMap<>();
	                        timeSlotData.put("timeSlotId", slotTime.getDoctorSlotTimeId());
	                        timeSlotData.put("slotStartTime", slotTime.getSlotStartTime());
	                        timeSlotData.put("slotEndTime", slotTime.getSlotEndTime());
	                        timeSlotData.put("slotTime", slotTime.getSlotTime());
	                        timeSlotData.put("isActive", slotTime.getIsActive());

	                        // Slot Dates
	                        List<DoctorSlotDate> slotDates = doctorSlotDateRepository
	                                .findByDoctorSlotIdAndDoctorDaySlotIdAndDoctorSlotTimeId(
	                                        doctorSlot.getDoctorSlotId(),
	                                        daySlot.getDoctorDaySlotId(),
	                                        slotTime.getDoctorSlotTimeId());

	                        List<Map<String, Object>> slotDatesWithSplit = new ArrayList<>();
	                        for (DoctorSlotDate slotDate : slotDates) {
	                            Map<String, Object> slotDateMap = new HashMap<>();
	                            slotDateMap.put("date", slotDate.getDate());
	                            slotDateMap.put("isActive", slotDate.getIsActive());

	                            // Check if doctor is on leave for this date
	                            String slotDateStr;
	                            if (slotDate.getDate() instanceof String) {
	                                slotDateStr = (String) slotDate.getDate();
	                            } else {
	                                // FIX: Use IST timezone for slot date formatting as well
	                                slotDateStr = dateFormat.format(slotDate.getDate());
	                            }
	                            
	                            boolean isOnLeave = leaveDatesAsStrings.contains(slotDateStr);
	                            if (isOnLeave) {
	                                slotDateMap.put("leaveMessage", "Doctor is on Leave");
	                                slotDateMap.put("isOnLeave", true);
	                                // You might want to set splitTimes to empty or add a leave indicator
	                                slotDateMap.put("splitTimes", new ArrayList<>());
	                            } else {
	                                slotDateMap.put("isOnLeave", false);
	                                
	                                // Split Times - only process if not on leave
	                                List<DoctorSlotSpiltTime> splitTimes = doctorSlotSplitTimeRepository
	                                        .findByDoctorSlotDateId(slotDate.getDoctorSlotDateId());

	                                List<Map<String, Object>> splitTimeList = new ArrayList<>();
	                                for (DoctorSlotSpiltTime splitTime : splitTimes) {
	                                    Map<String, Object> splitMap = new HashMap<>();
	                                    splitMap.put("slotStartTime", splitTime.getSlotStartTime());
	                                    splitMap.put("slotEndTime", splitTime.getSlotEndTime());
	                                    splitMap.put("slotStatus", splitTime.getSlotStatus());
	                                    splitMap.put("isActive", splitTime.getIsActive());
	                                    splitMap.put("id", splitTime.getDoctorSlotSpiltTimeId());
	                                    splitTimeList.add(splitMap);
	                                }
	                                slotDateMap.put("splitTimes", splitTimeList);
	                            }

	                            slotDatesWithSplit.add(slotDateMap);
	                        }

	                        timeSlotData.put("dates", slotDatesWithSplit);
	                        timeSlotList.add(timeSlotData);
	                    }

	                    daySlotData.put("timeSlots", timeSlotList);
	                    daySlotList.add(daySlotData);
	                }

	                slotData.put("daySlots", daySlotList);
	                doctorSlotList.add(slotData);
	            }
	            data.put("doctorSlots", doctorSlotList);

	            // Doctor Leaves (keeping the original structure as well)
	            List<Map<String, Object>> doctorLeaveList = new ArrayList<>();
	            for (DoctorLeaveList doctorLeave : doctorLeaves) {
	                Map<String, Object> leaveData = new HashMap<>();
	                leaveData.put("leaveId", doctorLeave.getDoctorLeaveListId());
	                leaveData.put("doctorLeaveDate", doctorLeave.getDoctorLeaveDate());
	                leaveData.put("userIsActive", doctorLeave.getUserIsActive());
	                doctorLeaveList.add(leaveData);
	            }
	            data.put("doctorLeaveList", doctorLeaveList);
	        }

	        return ResponseEntity.ok(data);
	    } catch (Exception e) {
	        logger.error("Exception while retrieving user details: ", e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(Collections.singletonMap("message", "Error retrieving user details"));
	    }
	}
	@Override
	public ResponseEntity<?> deleteDoctorRoleById(Integer doctorRoleId) {
		try {
			logger.info("Disabling doctor role with ID: {}", doctorRoleId);

			// Step 1: Retrieve the existing DoctorRole entity by doctorRoleId
			DoctorRole doctorRole = doctorRoleRepository.findById(doctorRoleId)
					.orElseThrow(() -> new RuntimeException("Doctor role not found"));

			// Step 2: Set userIsActive to false
			doctorRole.setUserIsActive(false);
			doctorRole.setUserUpdatedOn(new Date());

			// Step 3: Save the updated entity
			doctorRoleRepository.save(doctorRole);

			// Step 4: Return success response
			return ResponseEntity.ok(new Response(1, "Success", "Doctor role disabled successfully"));

		} catch (RuntimeException e) {
			logger.warn("Doctor role not found: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response(0, "Fail", "Doctor role not found"));
		} catch (Exception e) {
			logger.error("Error disabling doctor role: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new Response(0, "Fail", "Error disabling doctor role"));
		}
	}

	@Override
	public ResponseEntity<?> getDoctorSlotById(Integer userId, LocalDate requestDate) {
	    try {
	        if (userId == null || requestDate == null) {
	            return ResponseEntity.badRequest()
	                    .body(Collections.singletonMap("message", "Invalid user ID or request date"));
	        }

	        Optional<User> userData = userRepository.findById(userId);
	        if (userData.isEmpty()) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                    .body(Collections.singletonMap("message", "User not found"));
	        }

	        User user = userData.get();
	        Map<String, Object> response = new LinkedHashMap<>();
	        response.put("userId", user.getUserId());
	        response.put("userName", user.getUserName());

	        if (!"DOCTOR".equalsIgnoreCase(user.getUserType())) {
	            return ResponseEntity.ok(response);
	        }

	        if (checkDoctorLeave(user, requestDate)) {
	            response.put("doctorSlots", Collections.emptyList());
	            response.put("message", "Doctor is on leave for the requested date");
	            return ResponseEntity.ok(response);
	        }

	        List<Map<String, Object>> doctorSlotList = doctorDaySlotRepository.findByDoctorSlot_User(user).stream()
	                .filter(slot -> isValidSlot(slot, requestDate))
	                .map(slot -> mapDoctorSlot(slot, requestDate))
	                .filter(doctorSlotMap -> {
	                    @SuppressWarnings("unchecked")
	                    List<Map<String, Object>> daySlots = (List<Map<String, Object>>) doctorSlotMap.get("daySlots");
	                    return daySlots != null && !daySlots.isEmpty();
	                })
	                .distinct()
	                .collect(Collectors.toList());

	        response.put("doctorSlots", doctorSlotList);
	        return ResponseEntity.ok(response);

	    } catch (Exception e) {
	        logger.error("Error retrieving doctor slots for user {}: ", userId, e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(Collections.singletonMap("message", "Error retrieving doctor slots"));
	    }
	}

	private boolean checkDoctorLeave(User doctor, LocalDate requestDate) {
	    Date reqDate = Date.from(requestDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
	    return doctorLeaveListRepository.existsByUserAndDoctorLeaveDateAndUserIsActive(doctor, reqDate, true);
	}

	private boolean isValidSlot(DoctorDaySlot doctorSlot, LocalDate requestDate) {
	    if (doctorSlot == null || requestDate == null) return false;

	    LocalDate startDate = convertToLocalDate(doctorSlot.getStartSlotDate());
	    LocalDate endDate = convertToLocalDate(doctorSlot.getEndSlotDate());

	    return Boolean.TRUE.equals(doctorSlot.getIsActive())
	            && !requestDate.isBefore(startDate)
	            && !requestDate.isAfter(endDate);
	}

	private LocalDate convertToLocalDate(Date date) {
	    if (date == null) return null;
	    return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}

	private Map<String, Object> mapDoctorSlot(DoctorDaySlot daySlot, LocalDate requestDate) {
	    List<Map<String, Object>> daySlotList = doctorDaySlotRepository.findByDoctorSlot(daySlot.getDoctorSlot()).stream()
	            .filter(slot -> isValidDaySlot(slot, requestDate))
	            .map(slot -> mapDoctorDaySlot(slot, requestDate))
	            .filter(daySlotMap -> {
	                @SuppressWarnings("unchecked")
	                List<Map<String, Object>> timeSlots = (List<Map<String, Object>>) daySlotMap.get("slotTimes");
	                return timeSlots != null && !timeSlots.isEmpty();
	            })
	            .collect(Collectors.toList());

	    Map<String, Object> doctorSlotData = new LinkedHashMap<>();
	    doctorSlotData.put("doctorSlotId", daySlot.getDoctorSlot().getDoctorSlotId());
	    doctorSlotData.put("daySlots", daySlotList);
	    return doctorSlotData;
	}

	private boolean isValidDaySlot(DoctorDaySlot daySlot, LocalDate requestDate) {
	    if (daySlot == null || requestDate == null) return false;

	    LocalDate startDate = convertToLocalDate(daySlot.getStartSlotDate());
	    LocalDate endDate = convertToLocalDate(daySlot.getEndSlotDate());
	    
	    if (startDate == null || endDate == null) return false;
	    
	    // Fix: Use proper day comparison
	    String requestDay = requestDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
	    String slotDay = daySlot.getDay();

	    return Boolean.TRUE.equals(daySlot.getIsActive())
	            && !requestDate.isBefore(startDate)
	            && !requestDate.isAfter(endDate)
	            && requestDay.equalsIgnoreCase(slotDay);
	}

	private Map<String, Object> mapDoctorDaySlot(DoctorDaySlot daySlot, LocalDate requestDate) {
	    Map<String, Object> daySlotData = new LinkedHashMap<>();
	    daySlotData.put("daySlotId", daySlot.getDoctorDaySlotId());
	    daySlotData.put("day", daySlot.getDay());
	    daySlotData.put("startSlotDate", daySlot.getStartSlotDate());
	    daySlotData.put("endSlotDate", daySlot.getEndSlotDate());
	    daySlotData.put("isActive", daySlot.getIsActive());

	    List<Map<String, Object>> timeSlotList = doctorSlotTimeRepository.findByDoctorDaySlot(daySlot).stream()
	            .filter(slotTime -> Boolean.TRUE.equals(slotTime.getIsActive()))
	            .map(slotTime -> mapDoctorSlotTime(slotTime, requestDate))
	            .filter(timeSlotMap -> {
	                @SuppressWarnings("unchecked")
	                List<Map<String, Object>> splitTimes = (List<Map<String, Object>>) timeSlotMap.get("slotSplitTimes");
	                return splitTimes != null && !splitTimes.isEmpty();
	            })
	            .collect(Collectors.toList());

	    daySlotData.put("slotTimes", timeSlotList);
	    return daySlotData;
	}

	private Map<String, Object> mapDoctorSlotTime(DoctorSlotTime slotTime, LocalDate requestDate) {
	    Map<String, Object> timeSlotData = new LinkedHashMap<>();
	    timeSlotData.put("slotTimeId", slotTime.getDoctorSlotTimeId());
	    timeSlotData.put("startTime", slotTime.getSlotStartTime());
	    timeSlotData.put("endTime", slotTime.getSlotEndTime());
	    timeSlotData.put("isActive", slotTime.getIsActive());

	    // Fix: Convert LocalDate to proper string format for database query
	    String dateString = requestDate.toString(); // This gives YYYY-MM-DD format
	    
	    List<DoctorSlotDate> doctorSlotDates = doctorSlotDateRepository
	            .findByDoctorSlotTimeIdAndDoctorDaySlotIdAndDoctorSlotIdAndDateAndIsActive(
	                    slotTime.getDoctorSlotTimeId(),
	                    slotTime.getDoctorDaySlot().getDoctorDaySlotId(),
	                    slotTime.getDoctorDaySlot().getDoctorSlot().getDoctorSlotId(),
	                    dateString,
	                    true
	            );

	    // Get current date and time in Kolkata timezone for comparison
	    ZoneId kolkataZone = ZoneId.of("Asia/Kolkata");
	    LocalDate currentDate = LocalDate.now(kolkataZone);
	    LocalTime currentTime = LocalTime.now(kolkataZone);
	    boolean isToday = requestDate.equals(currentDate);
	    boolean isPastDate = requestDate.isBefore(currentDate);

	    List<Map<String, Object>> splitTimeList = doctorSlotDates.stream()
	    	    .flatMap((DoctorSlotDate slotDate) -> {
	    	        List<DoctorSlotSpiltTime> splitTimes = doctorSlotSplitTimeRepository
	    	            .findByDoctorSlotDateIdAndIsActive(slotDate.getDoctorSlotDateId(), true);
	    	        return splitTimes.stream()
	    	            .filter(split -> {
	    	                // Fix: Improved filtering logic
	    	                if (isPastDate) {
	    	                    // For past dates, only show non-Available slots (Booked, etc.)
	    	                    return !"Booked" .equalsIgnoreCase(split.getSlotStatus());
	    	                } else if (isToday) {
	    	                    // For today, always show Booked slots
//	    	                    if ("Booked".equalsIgnoreCase(split.getSlotStatus())) {
//	    	                        return true;
//	    	                    }
	    	                    // For today, check if Available slots have passed
	    	                	 if ("Booked".equalsIgnoreCase(split.getSlotStatus()) || "Available".equalsIgnoreCase(split.getSlotStatus()) || "OVERRIDDEN".equalsIgnoreCase(split.getSlotStatus())) {
	    	                        LocalTime slotStartTime = parseTimeString(split.getSlotStartTime());
	    	                        // Show slot if start time is in the future or current
	    	                        return slotStartTime != null && !slotStartTime.isBefore(currentTime);
	    	                    }
	    	                    // Show all other non-Available slots for today
	    	                    return true;
	    	                } else {
	    	                    // For future dates, show all slots
	    	                    return true;
	    	                }
	    	            })
	                    .map(split -> {
	                        Map<String, Object> splitData = new LinkedHashMap<>();
	                        splitData.put("slotSplitTimeId", split.getDoctorSlotSpiltTimeId());
	                        splitData.put("slotStartTime", split.getSlotStartTime());
	                        splitData.put("slotEndTime", split.getSlotEndTime());
	                      //  splitData.put("slotStatus", split.getSlotStatus());
	                        String originalStatus = split.getSlotStatus();
	                        String displayStatus = "OVERRIDDEN".equalsIgnoreCase(originalStatus) ? "Available" : originalStatus;
	                        splitData.put("slotStatus", displayStatus);
	                        return splitData;
	                    });
	            })
	            .collect(Collectors.toList());

	    timeSlotData.put("slotSplitTimes", splitTimeList);
	    return timeSlotData;
	}

	// Helper method to parse time string (e.g., "10:16 AM" -> LocalTime)
	private LocalTime parseTimeString(String timeString) {
	    if (timeString == null || timeString.trim().isEmpty()) {
	        return null;
	    }
	    
	    try {
	        // Handle different time formats
	        DateTimeFormatter formatter12Hour = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);
	        DateTimeFormatter formatter24Hour = DateTimeFormatter.ofPattern("H:mm");
	        
	        timeString = timeString.trim();
	        
	        if (timeString.contains("AM") || timeString.contains("PM")) {
	            return LocalTime.parse(timeString, formatter12Hour);
	        } else {
	            return LocalTime.parse(timeString, formatter24Hour);
	        }
	    } catch (Exception e) {
	        logger.warn("Failed to parse time string: {}", timeString, e);
	        return null;
	    }
	}
	@Override
	public ResponseEntity<?> deleteDoctorLeaveByLeaveId(Integer doctorLeaveListId) {
		try {
			logger.info("Disabling doctor role with ID: {}", doctorLeaveListId);

			// Step 1: Retrieve the existing DoctorRole entity by doctorRoleId
			DoctorLeaveList doctorRole = doctorLeaveListRepository.findById(doctorLeaveListId)
					.orElseThrow(() -> new RuntimeException("deleteDoctorLeaveByLeaveId"));

			// Step 2: Set userIsActive to false
			doctorRole.setUserIsActive(false);
			doctorRole.setUserUpdatedOn(new Date());

			// Step 3: Save the updated entity
			doctorLeaveListRepository.save(doctorRole);

			// Step 4: Return success response
			return ResponseEntity.ok(new Response(1, "Success", "deleteDoctorLeaveByLeaveId"));

		} catch (RuntimeException e) {
			logger.warn("Doctor role not found: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new Response(0, "Fail", "deleteDoctorLeaveByLeaveId"));
		} catch (Exception e) {
			logger.error("Error disabling doctor role: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new Response(0, "Fail", "Error disabling doctor role"));
		}
	}

	@Override
	public ResponseEntity<?> addTimeSlotByDoctor(HospitalDataListWebModel userWebModel) {
	    try {
	        DoctorSlot doctorSlot = doctorSlotRepository.findById(userWebModel.getDoctorSlotId()).orElseThrow(
	                () -> new RuntimeException("DoctorSlot not found with ID: " + userWebModel.getDoctorSlotId()));

	        List<DoctorDaySlot> existingDoctorDaySlots = doctorDaySlotRepository.findByDoctorSlot(doctorSlot);

	        if (!validateDoctorSlots(existingDoctorDaySlots, userWebModel.getDoctorDaySlots())) {
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
	                    new Response(0, "Error", "Doctor slot times overlap. Please ensure slot times don't conflict."));
	        }

	        if (userWebModel.getDoctorDaySlots() != null) {
	            for (DoctorDaySlotWebModel daySlotModel : userWebModel.getDoctorDaySlots()) {
	                DoctorDaySlot doctorDaySlot = DoctorDaySlot.builder()
	                        .doctorSlot(doctorSlot)
	                        .day(daySlotModel.getDay())
	                        .startSlotDate(daySlotModel.getStartSlotDate())
	                        .endSlotDate(daySlotModel.getEndSlotDate())
	                        .createdBy(userWebModel.getCreatedBy())
	                        .isActive(true)
	                        .build();

	                doctorDaySlot = doctorDaySlotRepository.save(doctorDaySlot);

	                if (daySlotModel.getDoctorSlotTimes() != null) {
	                    for (DoctorSlotTimeWebModel slotTimeModel : daySlotModel.getDoctorSlotTimes()) {
	                        DoctorSlotTime doctorSlotTime = DoctorSlotTime.builder()
	                                .doctorDaySlot(doctorDaySlot)
	                                .slotStartTime(slotTimeModel.getSlotStartTime())
	                                .slotEndTime(slotTimeModel.getSlotEndTime())
	                                .slotTime(slotTimeModel.getSlotTime())
	                                .createdBy(userWebModel.getCreatedBy())
	                                .isActive(true)
	                                .build();

	                        doctorSlotTime = doctorSlotTimeRepository.save(doctorSlotTime);

	                        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

	                        // Convert Date to LocalDate
	                        LocalDate startDate = convertToLocalDate(doctorDaySlot.getStartSlotDate());
	                        LocalDate endDate = convertToLocalDate(doctorDaySlot.getEndSlotDate());
	                        
	                        // Get the target day of week from the daySlotModel
	                        DayOfWeek targetDayOfWeek = getDayOfWeek(daySlotModel.getDay());
	                        
	                        // Adjust startDate to the first occurrence of the target day if needed
	                        while (startDate.getDayOfWeek() != targetDayOfWeek) {
	                            startDate = startDate.plusDays(1);
	                            // If we've gone past the end date, break out
	                            if (startDate.isAfter(endDate)) {
	                                break;
	                            }
	                        }

	                        // Process only the specific days of the week within the date range
	                        while (!startDate.isAfter(endDate)) {
	                            // Only process if it's the target day of week
	                            DoctorSlotDate doctorSlotDate = createAndSaveDoctorSlotDate(
	                                    userWebModel.getCreatedBy(), doctorSlot, doctorDaySlot, doctorSlotTime, startDate);

	                            createDoctorSlotSplitTimes(userWebModel.getCreatedBy(), doctorSlotDate, slotTimeModel, timeFormatter);

	                            // Move to the next occurrence of the same day (add 7 days)
	                            startDate = startDate.plusDays(7);
	                        }
	                    }
	                }
	            }
	        }

	        return ResponseEntity.ok(new Response(1, "Success", "Time slots and leaves added successfully"));
	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(0, "Error", "An error occurred: " + e.getMessage()));
	    }
	}

	/**
	 * Helper method to convert day name to DayOfWeek enum
	 */
	private DayOfWeek getDayOfWeek(String dayName) {
	    switch (dayName.toUpperCase()) {
	        case "MONDAY":
	            return DayOfWeek.MONDAY;
	        case "TUESDAY":
	            return DayOfWeek.TUESDAY;
	        case "WEDNESDAY":
	            return DayOfWeek.WEDNESDAY;
	        case "THURSDAY":
	            return DayOfWeek.THURSDAY;
	        case "FRIDAY":
	            return DayOfWeek.FRIDAY;
	        case "SATURDAY":
	            return DayOfWeek.SATURDAY;
	        case "SUNDAY":
	            return DayOfWeek.SUNDAY;
	        default:
	            throw new IllegalArgumentException("Invalid day name: " + dayName);
	    }
	}

	
	/**
	 * Creates and saves a doctor slot date.
	 */
	private DoctorSlotDate createAndSaveDoctorSlotDate(Integer createdBy, DoctorSlot doctorSlot,
	                                                   DoctorDaySlot doctorDaySlot,
	                                                   DoctorSlotTime doctorSlotTime, LocalDate date) {
	    DoctorSlotDate doctorSlotDate = DoctorSlotDate.builder()
	            .doctorSlotId(doctorSlot.getDoctorSlotId())
	            .doctorDaySlotId(doctorDaySlot.getDoctorDaySlotId())
	            .doctorSlotTimeId(doctorSlotTime.getDoctorSlotTimeId())
	            .date(date.toString())
	            .createdBy(createdBy)
	            .isActive(true)
	            .build();

	    return doctorSlotDateRepository.save(doctorSlotDate);
	}

	/**
	 * Creates doctor slot split times for a specific slot date with error handling.
	 */
	private void createDoctorSlotSplitTimes(Integer createdBy, DoctorSlotDate doctorSlotDate,
	                                        DoctorSlotTimeWebModel slotTimeModel, DateTimeFormatter timeFormatter) {
	    try {
	        DateTimeFormatter timeFormatter1 = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);

	        LocalTime start = LocalTime.parse(slotTimeModel.getSlotStartTime(), timeFormatter1);
	        LocalTime end = LocalTime.parse(slotTimeModel.getSlotEndTime(), timeFormatter1);

	        String durationStr = slotTimeModel.getSlotTime().replaceAll("[^0-9]", "").trim();
	        int duration = Integer.parseInt(durationStr);

	        logger.info("Creating split times from {} to {} with {} minute intervals for date {}",
	                slotTimeModel.getSlotStartTime(), slotTimeModel.getSlotEndTime(),
	                duration, doctorSlotDate.getDate());

	        int slotCount = 0;
	        LocalTime currentStart = start;

	        while (currentStart.plusMinutes(duration).compareTo(end) <= 0) {
	            LocalTime currentEnd = currentStart.plusMinutes(duration);

	            boolean exists = doctorSlotSplitTimeRepository
	                    .existsBySlotStartTimeAndSlotEndTimeAndDoctorSlotDateId(
	                            currentStart.format(timeFormatter1),
	                            currentEnd.format(timeFormatter1),
	                            doctorSlotDate.getDoctorSlotDateId());

	            if (!exists) {
	                DoctorSlotSpiltTime splitTime = DoctorSlotSpiltTime.builder()
	                        .slotStartTime(currentStart.format(timeFormatter1))
	                        .slotEndTime(currentEnd.format(timeFormatter1))
	                        .slotStatus("Available")
	                        .createdBy(createdBy)
	                        .doctorSlotDateId(doctorSlotDate.getDoctorSlotDateId())
	                        .isActive(true)
	                        .build();

	                doctorSlotSplitTimeRepository.save(splitTime);
	                slotCount++;
	            }

	            currentStart = currentEnd;
	        }

	        logger.info("Created {} split time slots for doctor slot date ID: {}",
	                slotCount, doctorSlotDate.getDoctorSlotDateId());

	    } catch (Exception e) {
	        logger.error("Error creating doctor slot split times: {}", e.getMessage(), e);
	        throw new RuntimeException("Failed to create doctor slot split times", e);
	    }
	}
	/**
	 * Helper method to convert java.util.Date to java.time.LocalDate
	 */
//	private LocalDate convertToLocalDate(Date date) {
//	    if (date == null) {
//	        return null;
//	    }
//	    return date.toInstant()
//	            .atZone(ZoneId.systemDefault())
//	            .toLocalDate();
//	}
	/**
	 * Validates if any new slots overlap with existing slots or among themselves.
	 */
	private boolean validateDoctorSlots(List<DoctorDaySlot> existingSlots, List<DoctorDaySlotWebModel> newSlots) {
		for (DoctorDaySlotWebModel newSlot : newSlots) {
			for (DoctorDaySlot existingSlot : existingSlots) {
				if (newSlot.getDay().equals(existingSlot.getDay()) && slotsOverlap(newSlot.getStartSlotDate(),
						newSlot.getEndSlotDate(), existingSlot.getStartSlotDate(), existingSlot.getEndSlotDate())) {
					return false;
				}
			}

			// Check for overlap among new slots themselves
			for (DoctorDaySlotWebModel otherNewSlot : newSlots) {
				if (newSlot != otherNewSlot && newSlot.getDay().equals(otherNewSlot.getDay())
						&& slotsOverlap(newSlot.getStartSlotDate(), newSlot.getEndSlotDate(),
								otherNewSlot.getStartSlotDate(), otherNewSlot.getEndSlotDate())) {
					return false;
				}
			}
		}
		return true;
	}
	

	/**
	 * Checks if two time slots overlap.
	 */
	private boolean slotsOverlap(LocalDateTime start1, LocalDateTime end1, LocalDateTime start2, LocalDateTime end2) {
		return start1.isBefore(end2) && start2.isBefore(end1);
	}

	private boolean slotsOverlap(Date start1, Date end1, Date start2, Date end2) {
		LocalDateTime startDateTime1 = convertToLocalDateTime(start1);
		LocalDateTime endDateTime1 = convertToLocalDateTime(end1);
		LocalDateTime startDateTime2 = convertToLocalDateTime(start2);
		LocalDateTime endDateTime2 = convertToLocalDateTime(end2);

		return startDateTime1.isBefore(endDateTime2) && startDateTime2.isBefore(endDateTime1);
	}

	/**
	 * Converts a Date to LocalDateTime.
	 */
	private LocalDateTime convertToLocalDateTime(Date date) {
		return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
	}

	@Transactional
	@Override
	public ResponseEntity<?> deleteTimeSlotById(Integer doctorDaySlotId) {
		try {
			// Fetch the DoctorDaySlot by ID
			DoctorDaySlot doctorDaySlot = doctorDaySlotRepository.findById(doctorDaySlotId)
					.orElseThrow(() -> new RuntimeException("DoctorDaySlot not found with ID: " + doctorDaySlotId));

			// Delete associated DoctorSlotTime records first
			doctorSlotTimeRepository.deleteByDoctorDaySlot(doctorDaySlot);

			// Delete the DoctorDaySlot
			doctorDaySlotRepository.delete(doctorDaySlot);

			return ResponseEntity.ok(new Response(1, "Success", "Time slot deleted successfully"));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new Response(0, "Error", "An error occurred: " + e.getMessage()));
		}
	}

	@Override
	@Transactional
	public ResponseEntity<?> doctorSlotById(Integer doctorSlotId) {
		return null;
//	    try {
//	        // Check if the DoctorDaySlot exists
//	        DoctorSlot doctorDaySlot = doctorSlotRepository.findById(doctorSlotId)
//	                .orElseThrow(() -> new RuntimeException("DoctorSlot not found with ID: " + doctorSlotId));
//
//	        // Get the associated DoctorSlot
//	       Integer doctorSlot = doctorDaySlot.getDoctorSlotId(); // Correct method to get DoctorSlot object
//
//	        // Delete associated DoctorSlotTime records first
//	        doctorSlotTimeRepository.deleteByDoctorDaySlot(doctorSlot);
//
//	        // Now delete the DoctorDaySlot
//	        doctorDaySlotRepository.delete(doctorDaySlot);
//
//	        // Check if there are any remaining DoctorDaySlots linked to this DoctorSlot
//	        boolean hasOtherDaySlots = doctorDaySlotRepository.existsByDoctorSlot(doctorSlot);
//
//	        // If no other DoctorDaySlots exist, delete the DoctorSlot
//	        if (!hasOtherDaySlots) {
//	            doctorSlotRepository.delete(doctorSlot);
//	        }
//
//	        return ResponseEntity.ok("DoctorSlot and associated records deleted successfully.");
//	    } catch (Exception e) {
//	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//	                .body("Error deleting DoctorSlot: " + e.getMessage());
//	    }
//	}

	}
	@Override
	public ResponseEntity<?> verifyMobileNumber(String mobileNumber, Integer hospitalId) {
	    Map<String, Object> response = new HashMap<>();

	    // 1. Find patient by mobile number
	    Optional<PatientDetails> patientOpt = patientDetailsRepository.findByMobileNumber(mobileNumber);

	    if (patientOpt.isPresent()) {
	        PatientDetails patient = patientOpt.get();

	        // 2. Check if patient is mapped to hospital
	        boolean isMapped = false;
	        if (hospitalId != null) {
	            isMapped = patientMappedHospitalIdRepository.existsByPatientIdAndHospitalId(patient.getPatientDetailsId(), hospitalId);
	        }

	        if (isMapped) {
	            response.put("statusCode", 2);
	            response.put("status", "success");
	            response.put("message", "Mobile number exists and is mapped to the hospital");
	        } else {
	            response.put("statusCode", 1);
	            response.put("status", "success");
	            response.put("message", "Mobile number exists but not mapped to the hospital");
	        }
	        response.put("patientId", patient.getPatientDetailsId());
	    } else {
	        response.put("statusCode", 0);
	        response.put("status", "failure");
	        response.put("message", "Mobile number not registered");
	    }

	    return ResponseEntity.ok(response);
	}
	
	
	@Override
	public ResponseEntity<?> verifyMobileNumberWithoutHospitalId(String mobileNumber) {
	    Map<String, Object> response = new HashMap<>();

	    // 1. Find patient by mobile number
	    Optional<PatientDetails> patientOpt = patientDetailsRepository.findByMobileNumber(mobileNumber);

	    if (patientOpt.isPresent()) {
	        PatientDetails patient = patientOpt.get();

	        response.put("statusCode", 1);
	        response.put("status", "success");
	        response.put("message", "Mobile number exists");
	        response.put("patientId", patient.getPatientDetailsId());
	    } else {
	        response.put("statusCode", 0);
	        response.put("status", "failure");
	        response.put("message", "Mobile number not registered");
	    }

	    return ResponseEntity.ok(response);
	}


	@Override
	public ResponseEntity<?> checkExistingUserOrNewUserByPatentientId(Integer patientId, Integer hospitalId) {
	    try {
	        boolean exists = patientMappedHospitalIdRepository.existsByPatientIdAndHospitalId(patientId, hospitalId);

	        Map<String, Object> responseMap = new HashMap<>();
	        responseMap.put("existingUser", exists);
	        responseMap.put("message", exists ? "Existing user in hospital" : "New user for hospital");

	        return ResponseEntity.ok(new Response(1, "success", responseMap));

	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	            .body(new Response(0, "error", "An error occurred while checking user existence."));
	    }
	}

	@Override
	public ResponseEntity<?> savePatientIdAndHospitalIdByExistingUser(UserWebModel userWebModel) {
	    try {
	        Integer patientId = userWebModel.getPatientId();
	        Integer hospitalId = userWebModel.getHospitalId();

	        // Validation
	        if (patientId == null || hospitalId == null) {
	            return ResponseEntity.badRequest()
	                .body(new Response(0, "Fail", "Patient ID and Hospital ID must not be null"));
	        }

	        // Check if mapping already exists
	        Optional<PatientMappedHospitalId> existingMapping =
	            patientMappedHospitalIdRepository.findByPatientIdAndHospitalId(patientId, hospitalId);

	        if (existingMapping.isPresent()) {
	            return ResponseEntity.ok(new Response(1, "Fail", "Mapping already exists"));
	        }

	        // Create new mapping
	        PatientMappedHospitalId mapping = PatientMappedHospitalId.builder()
	            .patientId(patientId)
	            .hospitalId(hospitalId)
	            .createdBy(userWebModel.getCreatedBy())
	            .userUpdatedBy(userWebModel.getCreatedBy())
	            .userIsActive(true)
	            .medicalHistoryStatus(userWebModel.getMedicalHistoryStatus())
	            .personalDataStatus(userWebModel.getPersonalDataStatus())
	            .build();

	        patientMappedHospitalIdRepository.save(mapping);

	        // Handle associated file uploads
	        if (userWebModel.getFilesInputWebModel() != null && !userWebModel.getFilesInputWebModel().isEmpty()) {
	            handleFileUploadss(userWebModel, userWebModel.getFilesInputWebModel());
	        }

	        return ResponseEntity.ok(new Response(1, "Success", "Patient-hospital mapping saved successfully"));

	    } catch (Exception e) {
	        logger.error("Error saving mapping: {}", e.getMessage(), e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	            .body(new Response(0, "Fail", "An error occurred while saving the mapping"));
	    }
	}

	// Helper method to handle file uploads
	public void handleFileUploadss(UserWebModel userWebModel, List<FileInputWebModel> filesInputWebModel) throws IOException {
	    if (filesInputWebModel == null || filesInputWebModel.isEmpty()) {
	        return;
	    }

	    for (FileInputWebModel fileInput : filesInputWebModel) {
	        if (fileInput.getFileData() != null) {
	            MediaFile mediaFile = new MediaFile();
	            String fileName = UUID.randomUUID().toString();

	            // Fetch the user (creator)
	            User hospitalUser = userRepository.findById(userWebModel.getCreatedBy())
	                .orElseThrow(() -> new RuntimeException("User not found"));

	            mediaFile.setFileName(fileName);
	            mediaFile.setUser(hospitalUser);
	            mediaFile.setFileOriginalName(fileInput.getFileName());
	            mediaFile.setFileSize(fileInput.getFileSize());
	            mediaFile.setFileType(fileInput.getFileType());
	            mediaFile.setCategory(MediaFileCategory.scanDocument);
	            mediaFile.setFileDomainId(HealthCareConstant.scanDocument);
	            mediaFile.setFileDomainReferenceId(userWebModel.getPatientId()); // Mapping to Patient ID
	            mediaFile.setFileIsActive(true);
	            mediaFile.setFileCreatedBy(userWebModel.getCreatedBy());

	            // Save to DB
	            mediaFileRepository.save(mediaFile);

	            // Save to filesystem
	            Base64FileUpload.saveFile(imageLocation + "/scanDocument", fileInput.getFileData(), fileName);
	        }
	    }
	}

	@Override
	public ResponseEntity<?> saveHospitalLink(HospitalDataListWebModel userWebModel) {
	    Optional<HospitalDataList> optionalPatient = hospitalDataListRepository.findById(userWebModel.getHospitalId());

	    if (optionalPatient.isPresent()) {
	    	HospitalDataList patient = optionalPatient.get();
	        patient.setHospitalLink(userWebModel.getHospitalLink());
	        patient.setLinkstatus(true);
	        hospitalDataListRepository.save(patient);
	        return ResponseEntity.ok("Hospital link updated successfully.");
	    } else {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Patient not found with ID: " + userWebModel.getPatientId());
	    }
	}

	@Override
	public ResponseEntity<?> bookingDemoRegister(UserWebModel userWebModel) {
	    try {
	        // Validate incoming data (optional but recommended)
	        if (userWebModel == null) {
	            return ResponseEntity.badRequest().body(new Response(0, "Invalid data", "Request body is empty"));
	        }

	        // Map fields from UserWebModel to BookingDemo
	        BookingDemo booking = BookingDemo.builder()
	                .name(userWebModel.getName())
	                .country(userWebModel.getCountry())
	                .businessName(userWebModel.getBusinessName())
	                .businessNameType(userWebModel.getBusinessNameType())
	                .remarks(userWebModel.getRemarks())
	                .websiteUrl(userWebModel.getWebsiteUrl())
	                .email(userWebModel.getEmail())
	                .city(userWebModel.getCity())
	                .mobileNumber(userWebModel.getMobileNumber())
	                .countryCode(userWebModel.getCountryCode())
	                .createdBy(userWebModel.getCreatedBy())
	                .isActive(true)
	                .build();

	        // Save to DB
	        bookingDemoRepository.save(booking);

	        // Return success response
	        return ResponseEntity.ok(new Response(1, "Success", "Booking registered successfully"));

	    } catch (Exception e) {
	        // Log the error (optional but helpful)
	        e.printStackTrace();
	        return ResponseEntity.internalServerError()
	                .body(new Response(0, "Error", "An error occurred while registering booking"));
	    }
	}

	@Override
	public ResponseEntity<?> getAllbookingDemo(Integer page, Integer size) {
	    try {
	        // Business logic for page/size validation
	        if (page == null || page < 1) {
	            page = 1; // Default to page 1 if null or invalid
	        }

	        if (size == null || size <= 0) {
	            size = 10; // Default page size if null or invalid
	        }

	        int adjustedPage = page - 1; // Spring Data uses 0-based indexing

	        Pageable pageable = PageRequest.of(adjustedPage, size, Sort.by("createdOn").descending());
	        Page<BookingDemo> bookingPage = bookingDemoRepository.findByIsActiveTrue(pageable);

	        Map<String, Object> response = new HashMap<>();
	        response.put("status", 1);
	        response.put("message", "Active bookings retrieved successfully");
	        response.put("data", bookingPage.getContent());
	        response.put("currentPage", page); // show 1-based page to client
	        response.put("totalItems", bookingPage.getTotalElements());
	        response.put("totalPages", bookingPage.getTotalPages());

	        return ResponseEntity.ok(response);
	    } catch (Exception e) {
	        e.printStackTrace();

	        Map<String, Object> errorResponse = new HashMap<>();
	        errorResponse.put("status", 0);
	        errorResponse.put("message", "Failed to retrieve booking data");
	        errorResponse.put("data", null);

	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
	    }
	}

	@Override
	public ResponseEntity<?> getAllHospitalListCount(String startDate, String endDate) {
	    try {
	        // Parse dates (assuming input is in "yyyy-MM-dd" format)
	        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	        Date start = sdf.parse(startDate);
	        Date end = sdf.parse(endDate);

	        // Get all hospitals regardless of date
	        Integer totalHospitalCount = hospitalDataListRepository.countAllHospitals();
	        Integer activeHospitalCount = hospitalDataListRepository.countByCreatedOnBetweenAndIsActive(start, end, true);

	        double activePercentage = 0;
	        if (totalHospitalCount != null && totalHospitalCount > 0) {
	            activePercentage = ((double) activeHospitalCount / totalHospitalCount) * 100;
	        }

	        Map<String, Object> responseMap = new HashMap<>();
	        responseMap.put("totalHospitals", totalHospitalCount);
	        responseMap.put("activeHospitals", activeHospitalCount);
	        responseMap.put("activePercentage", String.format("%.2f", activePercentage));

	        return ResponseEntity.ok(new Response(1, "Success", responseMap));
	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(0, "Error", "Failed to fetch hospital count."));
	    }
	}

	@Override
	public ResponseEntity<?> getAllEmployeeListCount(String startDate, String endDate, String userType) {
	    try {
	        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	        Date start = sdf.parse(startDate);
	        Date end = sdf.parse(endDate);

	        List<Map<String, Object>> resultList = new ArrayList<>();

	        // If userType is provided, process only that
	        if (userType != null && !userType.trim().isEmpty()) {
	            resultList.add(getUserTypeStats(userType, start, end));
	        } else {
	            // Handle all user types (e.g., assume you have a method or enum for valid types)
	            List<String> allUserTypes = userRepository.findAllDistinctUserTypes(); // or manually list them
	            for (String type : allUserTypes) {
	                resultList.add(getUserTypeStats(type, start, end));
	            }
	        }

	        return ResponseEntity.ok(new Response(1, "Success", resultList));
	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(0, "Error", "Failed to fetch employee list count."));
	    }
	}

	// Helper method to reduce duplication
	private Map<String, Object> getUserTypeStats(String userType, Date start, Date end) {
	    Integer totalUsers = userRepository.countByUserType(userType);
	    Integer activeUsers = userRepository.countActiveByUserTypeAndDateRange(userType, start, end);

	    double activePercentage = 0;
	    if (totalUsers != null && totalUsers > 0) {
	        activePercentage = ((double) activeUsers / totalUsers) * 100;
	    }

	    Map<String, Object> map = new HashMap<>();
	    map.put("userType", userType);
	    map.put("totalUsers", totalUsers);
	    map.put("activeUsers", activeUsers);
	    map.put("activePercentage", String.format("%.2f", activePercentage));
	    return map;
	}

	@Override
	public ResponseEntity<?> getAllPatientListCount(String startDate, String endDate) {
	    try {
	        // Convert dates
	        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	        Date start = sdf.parse(startDate);
	        Date end = sdf.parse(endDate);

	        // Total patients (no filter)
	        Integer totalPatients = patientDetailsRepository.countTotalPatients();

	        // Active patients within date range
	        Integer activePatients = patientDetailsRepository.countActivePatientsBetweenDates(start, end);

	        // Calculate percentage
	        double activePercentage = 0;
	        if (totalPatients != null && totalPatients > 0) {
	            activePercentage = ((double) activePatients / totalPatients) * 100;
	        }

	        // Prepare response
	        Map<String, Object> responseMap = new HashMap<>();
	        responseMap.put("totalPatients", totalPatients);
	        responseMap.put("activePatients", activePatients);
	        responseMap.put("activePercentage", String.format("%.2f", activePercentage));

	        return ResponseEntity.ok(new Response(1, "Success", responseMap));
	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(0, "Error", "Failed to fetch patient list count."));
	    }
	}

	@Override
	public ResponseEntity<?> getAllEmployeeListCountByHospitalId(String startDate, String endDate, Integer hospitalId, String userType) {
	    try {
	        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	        Date start = sdf.parse(startDate);
	        Date end = sdf.parse(endDate);

	        // Count all users for given hospital & user type
	        Integer totalUsers = userRepository.countByUserTypeAndHospitalId(userType, hospitalId);

	        // Count active users within date range
	        Integer activeUsers = userRepository.countActiveUsersByHospitalIdAndDateRange(userType, hospitalId, start, end);

	        double activePercentage = 0;
	        if (totalUsers != null && totalUsers > 0) {
	            activePercentage = ((double) activeUsers / totalUsers) * 100;
	        }

	        Map<String, Object> responseMap = new HashMap<>();
	        responseMap.put("userType", userType);
	        responseMap.put("hospitalId", hospitalId);
	        responseMap.put("totalUsers", totalUsers);
	        responseMap.put("activeUsers", activeUsers);
	        responseMap.put("activePercentage", String.format("%.2f", activePercentage));

	        return ResponseEntity.ok(new Response(1, "Success", responseMap));
	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(0, "Error", "Failed to fetch employee count by hospital ID."));
	    }
	}

	@Override
	public ResponseEntity<?> getAllPatientListCountByHospitalId(String startDate, String endDate, Integer hospitalId) {
	    try {
	        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	        Date start = sdf.parse(startDate);
	        Date end = sdf.parse(endDate);

	        // Total + active parent patients
	        Integer totalMainPatients = patientMappedHospitalIdRepository.countTotalPatientsByHospitalId(hospitalId);
	        Integer activeMainPatients = patientMappedHospitalIdRepository.countActivePatientsByHospitalIdAndDateRange(hospitalId, start, end);

	        // Total + active sub-patients (child/dependent)
	        Integer totalSubPatients = patientSubChildDetailsRepository.countTotalSubPatientsByHospitalId(hospitalId);
	        Integer activeSubPatients = patientSubChildDetailsRepository.countActiveSubPatientsByHospitalIdAndDateRange(hospitalId, start, end);

	        int totalPatients = (totalMainPatients != null ? totalMainPatients : 0) +
	                            (totalSubPatients != null ? totalSubPatients : 0);
	        int activePatients = (activeMainPatients != null ? activeMainPatients : 0) +
	                             (activeSubPatients != null ? activeSubPatients : 0);

	        double activePercentage = 0;
	        if (totalPatients > 0) {
	            activePercentage = ((double) activePatients / totalPatients) * 100;
	        }

	        Map<String, Object> responseMap = new HashMap<>();
	        responseMap.put("hospitalId", hospitalId);
	        responseMap.put("totalMainPatients", totalMainPatients);
	        responseMap.put("totalSubPatients", totalSubPatients);
	        responseMap.put("totalPatients", totalPatients);
	        responseMap.put("activePatients", activePatients);
	        responseMap.put("activePercentage", String.format("%.2f", activePercentage));

	        return ResponseEntity.ok(new Response(1, "Success", responseMap));
	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(0, "Error", "Failed to fetch patient count by hospital ID."));
	    }
	}

	}








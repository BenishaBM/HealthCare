package com.annular.healthCare.service.serviceImpl;

import java.math.BigDecimal;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.expression.ParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.annular.healthCare.Response;
import com.annular.healthCare.Util.Base64FileUpload;
import com.annular.healthCare.Util.HealthCareConstant;
import com.annular.healthCare.model.Department;
import com.annular.healthCare.model.DoctorDaySlot;
import com.annular.healthCare.model.DoctorRole;
import com.annular.healthCare.model.DoctorSlot;
import com.annular.healthCare.model.DoctorSlotDate;
import com.annular.healthCare.model.DoctorSlotSpiltTime;
import com.annular.healthCare.model.DoctorSlotTime;
import com.annular.healthCare.model.DoctorSlotTimeOverride;
import com.annular.healthCare.model.DoctorSpecialty;
import com.annular.healthCare.model.HospitalDataList;
import com.annular.healthCare.model.MediaFile;
import com.annular.healthCare.model.MediaFileCategory;
import com.annular.healthCare.model.MedicalTestConfig;
import com.annular.healthCare.model.MedicalTestDaySlot;
import com.annular.healthCare.model.MedicalTestSlot;
import com.annular.healthCare.model.MedicalTestSlotDate;
import com.annular.healthCare.model.MedicalTestSlotSpiltTime;
import com.annular.healthCare.model.MedicalTestSlotTime;
import com.annular.healthCare.model.MedicalTestSlotTimeOveride;
import com.annular.healthCare.model.PatientAppointmentTable;
import com.annular.healthCare.model.PatientDetails;
import com.annular.healthCare.model.User;
import com.annular.healthCare.repository.DepartmentRepository;
import com.annular.healthCare.repository.DoctorSpecialityRepository;
import com.annular.healthCare.repository.HospitalDataListRepository;
import com.annular.healthCare.repository.MediaFileRepository;
import com.annular.healthCare.repository.MedicalTestConfigRepository;
import com.annular.healthCare.repository.MedicalTestDaySlotRepository;
import com.annular.healthCare.repository.MedicalTestSlotDateRepository;
import com.annular.healthCare.repository.MedicalTestSlotRepository;
import com.annular.healthCare.repository.MedicalTestSlotSpiltTimeRepository;
import com.annular.healthCare.repository.MedicalTestSlotTimeOverideRepository;
import com.annular.healthCare.repository.MedicalTestSlotTimeRepository;
import com.annular.healthCare.repository.PatientAppoitmentTablerepository;
import com.annular.healthCare.repository.PatientDetailsRepository;
import com.annular.healthCare.repository.UserRepository;
import com.annular.healthCare.service.MedicalTestConfigService;
import com.annular.healthCare.webModel.DaySlotWebModel;
import com.annular.healthCare.webModel.DoctorDaySlotWebModel;
import com.annular.healthCare.webModel.DoctorSlotTimeWebModel;
import com.annular.healthCare.webModel.FileInputWebModel;
import com.annular.healthCare.webModel.MedicalTestConfigWebModel;
import com.annular.healthCare.webModel.MedicalTestDaySlotWebModel;
import com.annular.healthCare.webModel.MedicalTestDto;
import com.annular.healthCare.webModel.MedicalTestItem;
import com.annular.healthCare.webModel.MedicalTestSlotTimeWebModel;
import com.annular.healthCare.webModel.TimeSlotModel;

@Service
public class MedicalTestConfigServiceImpl implements MedicalTestConfigService {

	public static final Logger log = LoggerFactory.getLogger(MedicalTestConfigServiceImpl.class);

	@Autowired
	MedicalTestConfigRepository medicalTestConfigRepository;

	@Autowired
	DepartmentRepository departmentRepository;

	@Value("${annular.app.imageLocation}")
	private String imageLocation;

	@Autowired
	MedicalTestSlotRepository medicalTestSlotRepository;

	@Autowired
	PatientAppoitmentTablerepository patientAppointmentRepository;

	@Autowired
	PatientDetailsRepository patientDetailsRepository;

	@Autowired
	MediaFileRepository mediaFilesRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	MedicalTestDaySlotRepository medicalTestDaySlotRepository;

	@Autowired
	MedicalTestSlotTimeRepository medicalTestSlotTimeRepository;

	@Autowired
	MedicalTestSlotDateRepository medicalTestSlotDateRepository;

	@Autowired
	DoctorSpecialityRepository doctorSpecialtyRepository;

	@Autowired
	MedicalTestSlotSpiltTimeRepository medicalTestSlotSpiltTimeRepository;

	@Autowired
	MedicalTestSlotTimeOverideRepository medicalTestSlotTimeOverideRepository;

	@Autowired
	HospitalDataListRepository hospitalDataListRepository;

	@Override
	public ResponseEntity<?> saveDepartment(MedicalTestConfigWebModel request) {
		Optional<Department> existing = departmentRepository.findByName(request.getName());

		if (existing.isPresent()) {
			return ResponseEntity.badRequest().body(new Response(0, "fail","Department already exists."));
		}

		Department department = Department.builder().name(request.getName()).createdBy(request.getCreatedBy())
				.updatedBy(request.getUpdatedBy()).hospitalId(request.getHospitalId()).isActive(true).build();

		departmentRepository.save(department);

		return ResponseEntity.ok(new Response(1, "success", "Department saved successfully."));
	}

	@Override
	public ResponseEntity<?> saveMedicalTestName(MedicalTestConfigWebModel request) {

		Department department = departmentRepository.findById(request.getDepartmentId()).orElse(null);
		if (department == null) {
			return ResponseEntity.badRequest().body(new Response(0, "fail", "Department not found."));
		}

		List<String> successfulTests = new ArrayList<>();
		List<String> failedTests = new ArrayList<>();

		for (MedicalTestItem test : request.getMedicalTests()) {
			try {
				boolean exists = medicalTestConfigRepository.existsByHospitalIdAndMedicalTestNameAndDepartmentId(
						request.getHospitalId(), test.getMedicalTestName(), department.getId());

				if (exists) {
					failedTests.add(test.getMedicalTestName());
					continue;
				}

				MedicalTestConfig medicalTestConfig = MedicalTestConfig.builder().department(department)
						.medicalTestName(test.getMedicalTestName()).mrp(test.getMrp()).gst(test.getGst())
						.createdBy(request.getCreatedBy()).updatedBy(request.getUpdatedBy()).isActive(true)
						.hospitalId(request.getHospitalId()).build();

				medicalTestConfigRepository.save(medicalTestConfig);
				successfulTests.add(test.getMedicalTestName());
			} catch (Exception e) {
				failedTests.add(test.getMedicalTestName());
			}
		}

		String message;
		if (failedTests.isEmpty()) {
			message = "All medical tests saved successfully.";
		} else if (successfulTests.isEmpty()) {
			message = "Failed to save any medical tests: " + String.join(", ", failedTests);
		} else {
			message = "Partially successful. Saved: " + String.join(", ", successfulTests) + ". Failed: "
					+ String.join(", ", failedTests);
		}

		return ResponseEntity.ok(new Response(!successfulTests.isEmpty() ? 1 : 0,
				!successfulTests.isEmpty() ? "success" : "fail", message));
	}

	@Override
	public ResponseEntity<?> getAllMedicalTestNameByHospitalId(Integer hospitalId) {

		try {
			List<MedicalTestConfig> testConfigs = medicalTestConfigRepository
					.findByHospitalIdAndIsActiveTrue(hospitalId);
			if (testConfigs.isEmpty()) {
				return ResponseEntity.ok(new Response(0, "fail", "No medical tests found for the given hospital ID."));
			}

			// Group tests by department
			Map<Integer, Map<String, Object>> departmentMap = new HashMap<>();

			for (MedicalTestConfig config : testConfigs) {
				if (config.getDepartment() == null) {
					continue; // Skip tests without department
				}

				Integer departmentId = config.getDepartment().getId();

				// If we haven't seen this department yet, create a new entry
				if (!departmentMap.containsKey(departmentId)) {
					Map<String, Object> newDeptMap = new HashMap<>();
					newDeptMap.put("id", departmentId);
					newDeptMap.put("name", config.getDepartment().getName());
					newDeptMap.put("createdOn", config.getCreatedOn());
					newDeptMap.put("tests", new ArrayList<Map<String, Object>>());
					departmentMap.put(departmentId, newDeptMap);
				}

				// Add this test to the department's test list
				if (config.getMedicalTestName() != null) {
					Map<String, Object> testMap = new HashMap<>();
					testMap.put("id", config.getId());
					testMap.put("name", config.getMedicalTestName());
					testMap.put("mrp", config.getMrp());
					testMap.put("gst", config.getGst());
					testMap.put("createdOn", config.getCreatedOn());

					@SuppressWarnings("unchecked")
					List<Map<String, Object>> tests = (List<Map<String, Object>>) departmentMap.get(departmentId)
							.get("tests");
					tests.add(testMap);
				}
			}

			// Sort tests inside each department by 'createdOn' in descending order
			for (Map<String, Object> deptMap : departmentMap.values()) {
				List<Map<String, Object>> tests = (List<Map<String, Object>>) deptMap.get("tests");

				tests.sort((test1, test2) -> {
					Date createdOn1 = (Date) test1.get("createdOn");
					Date createdOn2 = (Date) test2.get("createdOn");

					if (createdOn1 == null && createdOn2 == null)
						return 0;
					if (createdOn1 == null)
						return 1;
					if (createdOn2 == null)
						return -1;

					return createdOn2.compareTo(createdOn1); // descending
				});
			}

			// Convert to a list for the response
			List<Map<String, Object>> responseList = new ArrayList<>(departmentMap.values());

			// Sort departments by 'createdOn' in descending order
			responseList.sort((dept1, dept2) -> {
				Date createdOn1 = (Date) dept1.get("createdOn");
				Date createdOn2 = (Date) dept2.get("createdOn");

				if (createdOn1 == null && createdOn2 == null)
					return 0;
				if (createdOn1 == null)
					return 1;
				if (createdOn2 == null)
					return -1;

				return createdOn2.compareTo(createdOn1); // descending
			});

			return ResponseEntity.ok(new Response(1, "success", responseList));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new Response(0, "error", "Something went wrong: " + e.getMessage()));
		}
	}

	@Override
	public ResponseEntity<?> getMedicalTestNameById(Integer departmentId) {
		try {
			Optional<Department> departmentOpt = departmentRepository.findById(departmentId);

			if (!departmentOpt.isPresent()) {
				return ResponseEntity.ok(new Response(0, "fail", "Department not found with ID: " + departmentId));
			}

			Department department = departmentOpt.get();

			List<Map<String, Object>> testsList = new ArrayList<>();
			for (MedicalTestConfig test : department.getMedicalTests()) {
				if (Boolean.TRUE.equals(test.getIsActive())) {
					Map<String, Object> testMap = new HashMap<>();
					testMap.put("id", test.getId());
					testMap.put("medicalTestName", test.getMedicalTestName());
					testMap.put("mrp", test.getMrp());
					testMap.put("gst", test.getGst());
					testMap.put("hospitalId", test.getHospitalId());
					testsList.add(testMap);
				}
			}

			Map<String, Object> responseMap = new HashMap<>();
			responseMap.put("departmentId", department.getId());
			responseMap.put("departmentName", department.getName());
			responseMap.put("hospitalId", department.getHospitalId());
			responseMap.put("medicalTests", testsList);

			return ResponseEntity.ok(new Response(1, "success", responseMap));

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new Response(0, "error", "Something went wrong: " + e.getMessage()));
		}
	}

	@Override
	public ResponseEntity<?> updateMedicalTestName(MedicalTestConfigWebModel request) {
		try {
			// Find department by name (assuming the name is unique for each department)
			Department department = departmentRepository.findById(request.getId())
					.orElseThrow(() -> new RuntimeException("Department not found"));

			// Iterate through the tests in the request
			List<String> successfulTests = new ArrayList<>();
			List<String> failedTests = new ArrayList<>();

			for (MedicalTestDto testDto : request.getTests()) {
				try {
					MedicalTestConfig medicalTestConfig;

					if (testDto.getMedicalId() != null) {
						// Update existing test by medicalId
						Optional<MedicalTestConfig> optionalConfig = medicalTestConfigRepository
								.findById(testDto.getMedicalId());

						if (optionalConfig.isPresent()) {
							medicalTestConfig = optionalConfig.get();
							// Update the fields
							medicalTestConfig.setMedicalTestName(testDto.getMedicalTestName());
							medicalTestConfig.setUpdatedBy(testDto.getUpdatedBy());
							medicalTestConfig.setUpdatedOn(new Date());
							medicalTestConfig.setHospitalId(testDto.getHospitalId());
							medicalTestConfig.setIsActive(true);
							medicalTestConfig.setGst(testDto.getGst());
							medicalTestConfig.setMrp(testDto.getMrp());

							medicalTestConfigRepository.save(medicalTestConfig); // Save updated test
							successfulTests.add(testDto.getMedicalTestName());
						} else {
							failedTests.add("Test ID " + testDto.getMedicalId() + " not found for update.");
						}
					} else {
						// Create new test
						medicalTestConfig = MedicalTestConfig.builder().department(department)
								.medicalTestName(testDto.getMedicalTestName()).hospitalId(testDto.getHospitalId())
								.createdBy(testDto.getUpdatedBy()).updatedBy(testDto.getUpdatedBy())
								.mrp(testDto.getMrp()).gst(testDto.getGst()).isActive(true).createdOn(new Date())
								.updatedOn(new Date()).build();

						medicalTestConfigRepository.save(medicalTestConfig); // Save new test
						successfulTests.add(testDto.getMedicalTestName());
					}
				} catch (Exception e) {
					failedTests.add(testDto.getMedicalTestName());
				}
			}

			// Return response with successful and failed tests
			String message = "";
			if (failedTests.isEmpty()) {
				message = "All medical tests processed successfully.";
			} else if (successfulTests.isEmpty()) {
				message = "Failed to process all medical tests. Tests that failed: " + String.join(", ", failedTests);
			} else {
				message = "Partially successful. Processed tests: " + String.join(", ", successfulTests)
						+ ". Failed tests: " + String.join(", ", failedTests);
			}

			return ResponseEntity.ok(new Response(1, "success", message));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new Response(0, "error", "Something went wrong: " + e.getMessage()));
		}
	}

	@Override
	public ResponseEntity<?> deleteMedicalTestNameById(Integer id) {
		try {
			Optional<MedicalTestConfig> optionalConfig = medicalTestConfigRepository.findById(id);

			if (!optionalConfig.isPresent()) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new Response(0, "fail", "Medical test not found with ID: " + id));
			}

			MedicalTestConfig config = optionalConfig.get();

			// Soft delete
			config.setIsActive(false);
			config.setUpdatedOn(new Date());

			medicalTestConfigRepository.save(config);

			return ResponseEntity.ok(new Response(1, "success", "Medical test deactivated successfully."));

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new Response(0, "error", "Something went wrong: " + e.getMessage()));
		}
	}

	@Override
	public ResponseEntity<?> deleteDepartmentById(Integer id) {
		try {
			// Check if the department exists
			Optional<Department> departmentOptional = departmentRepository.findById(id);

			if (!departmentOptional.isPresent()) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new Response(0, "fail", "Department not found with ID: " + id));
			}

			// Fetch the department
			Department department = departmentOptional.get();

			// Set isActive = false for all associated MedicalTestConfig records
			List<MedicalTestConfig> medicalTests = medicalTestConfigRepository.findByDepartmentId(id);
			for (MedicalTestConfig medicalTestConfig : medicalTests) {
				medicalTestConfig.setIsActive(false);
			}
			medicalTestConfigRepository.saveAll(medicalTests);

			// Set isActive = false for the department
			department.setIsActive(false);
			departmentRepository.save(department);

			// Return success message
			return ResponseEntity
					.ok(new Response(1, "success", "Department and associated tests set to inactive successfully"));

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new Response(0, "error", "Something went wrong: " + e.getMessage()));
		}
	}

	@Override
	public ResponseEntity<?> saveMedicalTestSlotByDepartmentId(MedicalTestConfigWebModel medicalTestConfigWebModel) {
		try {
			// Input validation
			if (medicalTestConfigWebModel == null) {
				return ResponseEntity.badRequest().body("Medical test configuration cannot be null");
			}

			if (medicalTestConfigWebModel.getId() == null) {
				return ResponseEntity.badRequest().body("Department ID cannot be null");
			}

			if (medicalTestConfigWebModel.getCreatedBy() == null) {
				return ResponseEntity.badRequest().body("Created by cannot be null");
			}

			if (medicalTestConfigWebModel.getMedicalTestDaySlots() == null
					|| medicalTestConfigWebModel.getMedicalTestDaySlots().isEmpty()) {
				return ResponseEntity.badRequest().body("At least one day slot must be provided");
			}

			// 1. Validate Department exists
			Department department = departmentRepository.findById(medicalTestConfigWebModel.getId()).orElseThrow(
					() -> new RuntimeException("Department not found with ID: " + medicalTestConfigWebModel.getId()));

			// 2. Validate day slots for overlaps and duplicates
			ValidationResult validationResult = validateDaySlots(medicalTestConfigWebModel.getMedicalTestDaySlots());
			if (!validationResult.isValid()) {
				return ResponseEntity.badRequest().body(validationResult.getErrorMessage());
			}

			// 3. Save MedicalTestSlot
			MedicalTestSlot slot = MedicalTestSlot.builder().department(department)
					.createdBy(medicalTestConfigWebModel.getCreatedBy()).isActive(true).build();
			slot = medicalTestSlotRepository.save(slot);

			// 4. Process each day slot
			for (MedicalTestDaySlotWebModel daySlotModel : medicalTestConfigWebModel.getMedicalTestDaySlots()) {
				processDaySlot(slot, daySlotModel, medicalTestConfigWebModel.getCreatedBy());
			}

			return ResponseEntity.ok("Medical Test Slot saved successfully.");

		} catch (ValidationException e) {
			log.error("Validation error while saving test slots: {}", e.getMessage());
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch (Exception e) {
			log.error("Error while saving test slots: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error while saving test slots: " + e.getMessage());
		}
	}

	/**
	 * Process a single day slot with validation
	 */
	private void processDaySlot(MedicalTestSlot slot, MedicalTestDaySlotWebModel daySlotModel, Integer createdBy) {
		// Validate day slot
		ValidationResult dayValidation = validateDaySlot(daySlotModel);
		if (!dayValidation.isValid()) {
			throw new ValidationException(dayValidation.getErrorMessage());
		}

		// Save day slot
		MedicalTestDaySlot daySlot = MedicalTestDaySlot.builder().medicalTestSlot(slot).day(daySlotModel.getDay())
				.startSlotDate(daySlotModel.getStartSlotDate()).endSlotDate(daySlotModel.getEndSlotDate())
				.createdBy(createdBy).isActive(true).build();
		daySlot = medicalTestDaySlotRepository.save(daySlot);

		// Process time slots for this day
		for (MedicalTestSlotTimeWebModel timeSlotModel : daySlotModel.getMedicalTestSlotTimes()) {
			processTimeSlot(slot, daySlot, timeSlotModel, daySlotModel, createdBy);
		}
	}

	/**
	 * Process a single time slot with validation
	 */
	private void processTimeSlot(MedicalTestSlot slot, MedicalTestDaySlot daySlot,
			MedicalTestSlotTimeWebModel timeSlotModel, MedicalTestDaySlotWebModel daySlotModel, Integer createdBy) {

		// Validate time slot
		ValidationResult timeValidation = validateTimeSlot(timeSlotModel);
		if (!timeValidation.isValid()) {
			throw new ValidationException(timeValidation.getErrorMessage());
		}

		// Save time slot
		MedicalTestSlotTime timeSlot = MedicalTestSlotTime.builder().medicalTestDaySlot(daySlot)
				.slotStartTime(timeSlotModel.getSlotStartTime()).slotEndTime(timeSlotModel.getSlotEndTime())
				.slotTime(timeSlotModel.getSlotTime()).createdBy(createdBy).isActive(true).build();
		MedicalTestSlotTime savedTimeSlot = medicalTestSlotTimeRepository.save(timeSlot);

		// Generate dates for this day of week
		List<String> datesToGenerate = generateDatesForDayOfWeek(daySlotModel.getDay(), daySlotModel.getStartSlotDate(),
				daySlotModel.getEndSlotDate());

		// Process each date
		for (String date : datesToGenerate) {
			processSlotDate(slot, daySlot, savedTimeSlot, date, timeSlotModel, createdBy);
		}
	}

	/**
	 * Process a single slot date with validation
	 */
	private void processSlotDate(MedicalTestSlot slot, MedicalTestDaySlot daySlot, MedicalTestSlotTime timeSlot,
			String date, MedicalTestSlotTimeWebModel timeSlotModel, Integer createdBy) {

		// Check for duplicate slot dates
		boolean duplicateExists = medicalTestSlotDateRepository
				.existsByMedicalTestSlotIdAndDateAndIsActiveTrue(slot.getMedicalTestSlotId(), date);

		if (duplicateExists) {
			log.warn("Duplicate slot date found for date: {} and slot ID: {}", date, slot.getMedicalTestSlotId());
			throw new ValidationException("Duplicate slot date found for date: " + date);
		}

		// Save slot date
		MedicalTestSlotDate slotDate = MedicalTestSlotDate.builder().medicalTestSlotId(slot.getMedicalTestSlotId())
				.medicalTestDaySlotId(daySlot.getMedicalTestDaySlotId())
				.medicalTestSlotTimeId(timeSlot.getMedicalTestSlotTimeId()).date(date).createdBy(createdBy)
				.isActive(true).build();

		MedicalTestSlotDate savedSlotDate = medicalTestSlotDateRepository.save(slotDate);
		log.debug("Saved slot date with ID: {}", savedSlotDate.getMedicalTestSlotDateId());

		// Split time slots and save
		processTimeSlotSplitting(savedSlotDate, timeSlotModel, createdBy, slot.getDepartment().getId());

	}

//	/**
//	 * Process time slot splitting with validation
//	 */
//	private void processTimeSlotSplitting(MedicalTestSlotDate savedSlotDate,
//            MedicalTestSlotTimeWebModel timeSlotModel,
//            Integer createdBy,
//            Integer departmentId)
//
//	{
//		try {
//			List<TimeSlotInterval> timeIntervals = splitTimeSlot(timeSlotModel.getSlotStartTime(),
//					timeSlotModel.getSlotEndTime(), parseSlotDuration(timeSlotModel.getSlotTime()));
//
//// Validate generated intervals for overlaps
//			validateTimeIntervals(timeIntervals);
//
//			for (TimeSlotInterval interval : timeIntervals) {
//
//				String dbStartTime = convert24To12Hour(interval.getStartTime());
//				String dbEndTime = convert24To12Hour(interval.getEndTime());
//
//				log.info("Checking overlap for deptId={}, date={}, start={}, end={}",
//				         departmentId, savedSlotDate.getDate(), dbStartTime, dbEndTime);
//
//				boolean exists = medicalTestSlotSpiltTimeRepository.existsOverlappingSlotForDepartment(
//				    departmentId,
//				    savedSlotDate.getDate(),
//				    dbStartTime,
//				    dbEndTime
//				);
//
//
//				if (exists) {
//					throw new ValidationException(
//							String.format("Time slot %s - %s overlaps with an existing slot on %s", dbStartTime,
//									dbEndTime, savedSlotDate.getDate()));
//				}
//
//// Save to DB
//				MedicalTestSlotSpiltTime spiltTime = MedicalTestSlotSpiltTime.builder().slotStartTime(dbStartTime)
//						.slotEndTime(dbEndTime).slotStatus("AVAILABLE").medicalTestSlotDate(savedSlotDate)
//						.createdBy(createdBy).isActive(true).build();
//
//				medicalTestSlotSpiltTimeRepository.save(spiltTime);
//			}
//
//		} catch (Exception e) {
//			log.error("Error processing time slots for date {}: {}", savedSlotDate.getDate(), e.getMessage(), e);
//			throw new RuntimeException("Error processing time slots: " + e.getMessage(), e);
//		}
//	}

	/**
	 * FIXED: Process time slot splitting with proper AM/PM handling
	 */
	private void processTimeSlotSplitting(MedicalTestSlotDate savedSlotDate,
	        MedicalTestSlotTimeWebModel timeSlotModel,
	        Integer createdBy,
	        Integer departmentId) {
	    try {
	        List<TimeSlotInterval> timeIntervals = splitTimeSlot(
	            timeSlotModel.getSlotStartTime(),
	            timeSlotModel.getSlotEndTime(), 
	            parseSlotDuration(timeSlotModel.getSlotTime())
	        );

	        // Validate generated intervals for overlaps
	        validateTimeIntervals(timeIntervals);

	        for (TimeSlotInterval interval : timeIntervals) {
	            // FIXED: Ensure proper AM/PM conversion
	            String dbStartTime = convertTo12HourFormat(interval.getStartTime());
	            String dbEndTime = convertTo12HourFormat(interval.getEndTime());

	            log.info("Processing time slot - deptId={}, date={}, start={}, end={}",
	                     departmentId, savedSlotDate.getDate(), dbStartTime, dbEndTime);

	            // Check for overlapping slots
	            boolean exists = medicalTestSlotSpiltTimeRepository.existsOverlappingSlotForDepartment(
	                departmentId,
	                savedSlotDate.getDate(),
	                dbStartTime,
	                dbEndTime
	            );

	            if (exists) {
	                throw new ValidationException(
	                    String.format("Time slot %s - %s overlaps with an existing slot on %s", 
	                        dbStartTime, dbEndTime, savedSlotDate.getDate())
	                );
	            }

	            // Save to DB with proper AM/PM format
	            MedicalTestSlotSpiltTime spiltTime = MedicalTestSlotSpiltTime.builder()
	                .slotStartTime(dbStartTime)
	                .slotEndTime(dbEndTime)
	                .slotStatus("AVAILABLE")
	                .medicalTestSlotDate(savedSlotDate)
	                .createdBy(createdBy)
	                .isActive(true)
	                .build();

	            medicalTestSlotSpiltTimeRepository.save(spiltTime);
	            log.info("Saved time slot: {} to {} for date: {}", dbStartTime, dbEndTime, savedSlotDate.getDate());
	        }

	    } catch (Exception e) {
	        log.error("Error processing time slots for date {}: {}", savedSlotDate.getDate(), e.getMessage(), e);
	        throw new RuntimeException("Error processing time slots: " + e.getMessage(), e);
	    }
	}
	
	/**
	 * FIXED: Improved 24-hour to 12-hour conversion with proper AM/PM handling
	 * @throws java.text.ParseException 
	 */
	private String convertTo12HourFormat(String time24OrAmPm) throws java.text.ParseException {
	    if (time24OrAmPm == null || time24OrAmPm.trim().isEmpty()) {
	        throw new IllegalArgumentException("Time cannot be null or empty");
	    }

	    String trimmedTime = time24OrAmPm.trim();
	    
	    // If already in AM/PM format, return as is
	    if (trimmedTime.toLowerCase().contains("am") || trimmedTime.toLowerCase().contains("pm")) {
	        return normalizeAmPmFormat(trimmedTime);
	    }

	    // Convert from 24-hour format
	    try {
	        TimeZone timeZone = TimeZone.getTimeZone("Asia/Kolkata");
	        
	        SimpleDateFormat inputFormat = new SimpleDateFormat("HH:mm");
	        inputFormat.setTimeZone(timeZone);
	        
	        SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
	        outputFormat.setTimeZone(timeZone);
	        
	        Date parsedTime = inputFormat.parse(trimmedTime);
	        String convertedTime = outputFormat.format(parsedTime);
	        
	        log.debug("Converted time {} to {}", trimmedTime, convertedTime);
	        return convertedTime;
	        
	    } catch (ParseException e) {
	        log.error("Error converting time format: {}", trimmedTime, e);
	        throw new RuntimeException("Invalid time format: " + trimmedTime, e);
	    }
	}


/**
 * FIXED: Normalize AM/PM format to ensure consistency
 */
private String normalizeAmPmFormat(String amPmTime) {
    if (amPmTime == null || amPmTime.trim().isEmpty()) {
        return amPmTime;
    }
    
    String normalized = amPmTime.trim()
        .replaceAll("(?i)\\s*am\\s*", " AM")
        .replaceAll("(?i)\\s*pm\\s*", " PM");
    
    // Ensure proper spacing
    normalized = normalized.replaceAll("\\s+", " ");
    
    return normalized;
}

	
	/**
	 * Validate all day slots for conflicts and overlaps
	 */
	private ValidationResult validateDaySlots(List<MedicalTestDaySlotWebModel> daySlots) {
		Set<String> seenDays = new HashSet<>();

		for (MedicalTestDaySlotWebModel daySlot : daySlots) {
			// Check for duplicate days
			if (seenDays.contains(daySlot.getDay())) {
				return new ValidationResult(false, "Duplicate day found: " + daySlot.getDay());
			}
			seenDays.add(daySlot.getDay());

			// Validate individual day slot
			ValidationResult dayValidation = validateDaySlot(daySlot);
			if (!dayValidation.isValid()) {
				return dayValidation;
			}

			// Validate time slots within the day
			ValidationResult timeValidation = validateTimeSlots(daySlot.getMedicalTestSlotTimes());
			if (!timeValidation.isValid()) {
				return timeValidation;
			}
		}

		return new ValidationResult(true, null);
	}

	private String convert24To12Hour(String time24) throws java.text.ParseException {
	    try {
	        TimeZone timeZone = TimeZone.getTimeZone("Asia/Kolkata");

	        SimpleDateFormat inputFormat = new SimpleDateFormat("HH:mm");
	        inputFormat.setTimeZone(timeZone);

	        SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
	        outputFormat.setTimeZone(timeZone);

	        return outputFormat.format(inputFormat.parse(time24));
	    } catch (ParseException e) {
	        log.error("Error parsing time: {}", time24);
	        throw new RuntimeException("Invalid time format: " + time24);
	    }
	}




	/**
	 * Validate a single day slot
	 */
	private ValidationResult validateDaySlot(MedicalTestDaySlotWebModel daySlot) {
		if (daySlot == null) {
			return new ValidationResult(false, "Day slot cannot be null");
		}

		if (daySlot.getDay() == null || daySlot.getDay().trim().isEmpty()) {
			return new ValidationResult(false, "Day cannot be null or empty");
		}

		if (daySlot.getStartSlotDate() == null) {
			return new ValidationResult(false, "Start slot date cannot be null");
		}

		if (daySlot.getEndSlotDate() == null) {
			return new ValidationResult(false, "End slot date cannot be null");
		}

		if (daySlot.getStartSlotDate().after(daySlot.getEndSlotDate())) {
			return new ValidationResult(false, "Start date cannot be after end date");
		}

		if (daySlot.getMedicalTestSlotTimes() == null || daySlot.getMedicalTestSlotTimes().isEmpty()) {
			return new ValidationResult(false, "At least one time slot must be provided for day: " + daySlot.getDay());
		}

		return new ValidationResult(true, null);
	}

	/**
	 * Validate time slots for overlaps within a day - FIXED VERSION
	 */
	private ValidationResult validateTimeSlots(List<MedicalTestSlotTimeWebModel> timeSlots) {
		if (timeSlots == null || timeSlots.size() < 2) {
			return new ValidationResult(true, null); // No overlaps possible with less than 2 slots
		}

		// First validate individual time slots
		for (MedicalTestSlotTimeWebModel timeSlot : timeSlots) {
			ValidationResult result = validateTimeSlot(timeSlot);
			if (!result.isValid()) {
				return result;
			}
		}

		// Then check for overlaps
		String overlapMessage = checkTimeSlotOverlaps(timeSlots);
		if (overlapMessage != null) {
			return new ValidationResult(false, overlapMessage);
		}

		return new ValidationResult(true, null);
	}

	/**
	 * FIXED: Improved method to check for time slot overlaps within a day
	 */
	private String checkTimeSlotOverlaps(List<MedicalTestSlotTimeWebModel> timeSlots) {
		if (timeSlots == null || timeSlots.size() < 2) {
			return null; // No overlaps possible with less than 2 slots
		}

		DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
		List<TimeSlotPair> parsedSlots = new ArrayList<>();

		// Parse all time slots first
		for (int i = 0; i < timeSlots.size(); i++) {
			MedicalTestSlotTimeWebModel slot = timeSlots.get(i);
			try {
				LocalTime startTime = LocalTime.parse(slot.getSlotStartTime().trim(), timeFormatter);
				LocalTime endTime = LocalTime.parse(slot.getSlotEndTime().trim(), timeFormatter);

				// Validate that start time is before end time
				if (!startTime.isBefore(endTime)) {
					return String.format("Invalid time slot at index %d: start time %s must be before end time %s", i,
							slot.getSlotStartTime(), slot.getSlotEndTime());
				}

				parsedSlots.add(new TimeSlotPair(startTime, endTime, i, slot));
			} catch (DateTimeParseException e) {
				return String.format("Invalid time format in slot at index %d: %s", i, e.getMessage());
			}
		}

		// Sort by start time
		parsedSlots.sort(Comparator.comparing(TimeSlotPair::getStartTime));

		// Check for overlaps
		for (int i = 0; i < parsedSlots.size() - 1; i++) {
			TimeSlotPair current = parsedSlots.get(i);
			TimeSlotPair next = parsedSlots.get(i + 1);

			// Check if current slot's end time is after next slot's start time
			if (current.getEndTime().isAfter(next.getStartTime())) {
				return String.format("Time slot overlap detected between slots: %s-%s and %s-%s",
						current.getOriginalSlot().getSlotStartTime(), current.getOriginalSlot().getSlotEndTime(),
						next.getOriginalSlot().getSlotStartTime(), next.getOriginalSlot().getSlotEndTime());
			}

			// Optional: Log adjacent slots for awareness
			if (current.getEndTime().equals(next.getStartTime())) {
				log.info("Adjacent time slots detected: {} ends at {} and {} starts at {}",
						current.getOriginalSlot().getSlotStartTime() + "-" + current.getOriginalSlot().getSlotEndTime(),
						current.getEndTime(),
						next.getOriginalSlot().getSlotStartTime() + "-" + next.getOriginalSlot().getSlotEndTime(),
						next.getStartTime());
			}
		}

		return null; // No overlaps found
	}

	/**
	 * Helper class to store parsed time slot information
	 */
	private static class TimeSlotPair {
		private final LocalTime startTime;
		private final LocalTime endTime;
		private final int originalIndex;
		private final MedicalTestSlotTimeWebModel originalSlot;

		public TimeSlotPair(LocalTime startTime, LocalTime endTime, int originalIndex,
				MedicalTestSlotTimeWebModel originalSlot) {
			this.startTime = startTime;
			this.endTime = endTime;
			this.originalIndex = originalIndex;
			this.originalSlot = originalSlot;
		}

		public LocalTime getStartTime() {
			return startTime;
		}

		public LocalTime getEndTime() {
			return endTime;
		}

		public int getOriginalIndex() {
			return originalIndex;
		}

		public MedicalTestSlotTimeWebModel getOriginalSlot() {
			return originalSlot;
		}
	}

	/**
	 * Enhanced validation for individual time slots
	 */
	private ValidationResult validateTimeSlot(MedicalTestSlotTimeWebModel timeSlot) {
		if (timeSlot == null) {
			return new ValidationResult(false, "Time slot cannot be null");
		}

		if (timeSlot.getSlotStartTime() == null || timeSlot.getSlotStartTime().trim().isEmpty()) {
			return new ValidationResult(false, "Slot start time cannot be null or empty");
		}

		if (timeSlot.getSlotEndTime() == null || timeSlot.getSlotEndTime().trim().isEmpty()) {
			return new ValidationResult(false, "Slot end time cannot be null or empty");
		}

		if (timeSlot.getSlotTime() == null || timeSlot.getSlotTime().trim().isEmpty()) {
			return new ValidationResult(false, "Slot duration cannot be null or empty");
		}

		// Validate time format using DateTimeFormatter for better error handling
		DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

		try {
			LocalTime startTime = LocalTime.parse(timeSlot.getSlotStartTime().trim(), timeFormatter);
			LocalTime endTime = LocalTime.parse(timeSlot.getSlotEndTime().trim(), timeFormatter);

			// Check if start time is before end time
			if (!startTime.isBefore(endTime)) {
				return new ValidationResult(false, String.format("Start time %s must be before end time %s",
						timeSlot.getSlotStartTime(), timeSlot.getSlotEndTime()));
			}

			// Optional: Validate minimum slot duration (e.g., at least 1 minute)
			if (Duration.between(startTime, endTime).toMinutes() < 1) {
				return new ValidationResult(false, "Time slot duration must be at least 1 minute");
			}

		} catch (DateTimeParseException e) {
			return new ValidationResult(false,
					String.format("Invalid time format. Use HH:mm format. Error: %s", e.getMessage()));
		}

		return new ValidationResult(true, null);
	}

	/**
	 * Validate time format (HH:mm) - keeping for backward compatibility
	 */
	private boolean isValidTimeFormat(String time) {
		if (time == null || time.trim().isEmpty()) {
			return false;
		}

		try {
			DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
			LocalTime.parse(time.trim(), timeFormatter);
			return true;
		} catch (DateTimeParseException e) {
			return false;
		}
	}

	/**
	 * Validate time intervals for overlaps - FIXED VERSION
	 */
	private void validateTimeIntervals(List<TimeSlotInterval> intervals) {
		if (intervals == null || intervals.size() < 2) {
			return; // No overlaps possible with less than 2 intervals
		}

		// Sort intervals by start time
		intervals.sort((a, b) -> {
			try {
				SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
				Date startA = timeFormat.parse(a.getStartTime());
				Date startB = timeFormat.parse(b.getStartTime());
				return startA.compareTo(startB);
			} catch (java.text.ParseException e) {
				log.error("Error parsing time for sorting: {}", e.getMessage());
				return 0;
			}
		});

		// Check for overlaps
		for (int i = 0; i < intervals.size() - 1; i++) {
			TimeSlotInterval current = intervals.get(i);
			TimeSlotInterval next = intervals.get(i + 1);

			if (intervalsOverlap(current, next)) {
				throw new ValidationException(String.format("Generated time intervals overlap: %s-%s and %s-%s",
						current.getStartTime(), current.getEndTime(), next.getStartTime(), next.getEndTime()));
			}
		}
	}

	/**
	 * Check if two time intervals overlap - FIXED VERSION
	 */
	private boolean intervalsOverlap(TimeSlotInterval interval1, TimeSlotInterval interval2) {
		try {
			SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);

			Date start1 = timeFormat.parse(interval1.getStartTime());
			Date end1 = timeFormat.parse(interval1.getEndTime());
			Date start2 = timeFormat.parse(interval2.getStartTime());
			Date end2 = timeFormat.parse(interval2.getEndTime());

			// Two intervals overlap if: start1 < end2 && start2 < end1
			return start1.before(end2) && start2.before(end1);

		} catch (java.text.ParseException e) {
			log.error("Error parsing time intervals for overlap check: {}", e.getMessage());
			return false;
		}
	}

	/**
	 * Validation result holder class
	 */
	private static class ValidationResult {
		private boolean valid;
		private String errorMessage;

		public ValidationResult(boolean valid, String errorMessage) {
			this.valid = valid;
			this.errorMessage = errorMessage;
		}

		public boolean isValid() {
			return valid;
		}

		public String getErrorMessage() {
			return errorMessage;
		}
	}

	/**
	 * Custom validation exception class
	 */
	private static class ValidationException extends RuntimeException {
		public ValidationException(String message) {
			super(message);
		}
	}

	/**
	 * Helper class to store time slot interval information
	 */
	private static class TimeSlotInterval {
		private String startTime;
		private String endTime;

		public TimeSlotInterval(String startTime, String endTime) {
			this.startTime = startTime;
			this.endTime = endTime;
		}

		public String getStartTime() {
			return startTime;
		}

		public String getEndTime() {
			return endTime;
		}
	}

	/**
	 * Generates a list of dates for a specific day of the week within a date range
	 */
	private List<String> generateDatesForDayOfWeek(String dayOfWeekStr, Date startDate, Date endDate) {
		List<String> dates = new ArrayList<>();

		try {
			int dayOfWeek;

			if (dayOfWeekStr.matches("\\d+")) {
				dayOfWeek = Integer.parseInt(dayOfWeekStr);
				if (dayOfWeek < 1 || dayOfWeek > 7) {
					throw new IllegalArgumentException("Day of week must be between 1 and 7, got: " + dayOfWeek);
				}
			} else {
				switch (dayOfWeekStr.trim().toLowerCase()) {
				case "monday":
					dayOfWeek = 1;
					break;
				case "tuesday":
					dayOfWeek = 2;
					break;
				case "wednesday":
					dayOfWeek = 3;
					break;
				case "thursday":
					dayOfWeek = 4;
					break;
				case "friday":
					dayOfWeek = 5;
					break;
				case "saturday":
					dayOfWeek = 6;
					break;
				case "sunday":
					dayOfWeek = 7;
					break;
				default:
					throw new IllegalArgumentException("Invalid day of week: " + dayOfWeekStr);
				}
			}

			TimeZone timeZone = TimeZone.getTimeZone("Asia/Kolkata");
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			dateFormat.setTimeZone(timeZone);

			Calendar startCal = Calendar.getInstance(timeZone);
			startCal.setTime(startDate);
			startCal.set(Calendar.HOUR_OF_DAY, 0);
			startCal.set(Calendar.MINUTE, 0);
			startCal.set(Calendar.SECOND, 0);
			startCal.set(Calendar.MILLISECOND, 0);

			Calendar endCal = Calendar.getInstance(timeZone);
			endCal.setTime(endDate);
			endCal.set(Calendar.HOUR_OF_DAY, 0);
			endCal.set(Calendar.MINUTE, 0);
			endCal.set(Calendar.SECOND, 0);
			endCal.set(Calendar.MILLISECOND, 0);

			int calendarDayOfWeek = (dayOfWeek == 7) ? Calendar.SUNDAY : dayOfWeek + 1;

			int currentDay = startCal.get(Calendar.DAY_OF_WEEK);
			int daysUntilTarget = (calendarDayOfWeek - currentDay + 7) % 7;
			if (daysUntilTarget != 0) {
				startCal.add(Calendar.DAY_OF_MONTH, daysUntilTarget);
			}

			while (!startCal.after(endCal)) {
				dates.add(dateFormat.format(startCal.getTime()));
				startCal.add(Calendar.DAY_OF_MONTH, 7);
			}

			log.debug("Generated {} dates for day {}", dates.size(), dayOfWeekStr);

		} catch (Exception e) {
			log.error("Error generating dates for day '{}': {}", dayOfWeekStr, e.getMessage(), e);
			throw new RuntimeException("Error generating dates: " + e.getMessage(), e);
		}

		return dates;
	}

	/**
	 * Parse slot duration from string format to minutes
	 */
	private Integer parseSlotDuration(String slotTimeStr) {
		if (slotTimeStr == null || slotTimeStr.trim().isEmpty()) {
			throw new IllegalArgumentException("Slot time cannot be null or empty");
		}

		String trimmedStr = slotTimeStr.trim().toLowerCase();

		try {
			String numericPart = trimmedStr.replaceAll("[^0-9]", "");

			if (numericPart.isEmpty()) {
				throw new IllegalArgumentException("No numeric value found in slot time: " + slotTimeStr);
			}

			int duration = Integer.parseInt(numericPart);

			if (trimmedStr.contains("hour") || trimmedStr.contains("hr")) {
				duration *= 60;
			}

			return duration;

		} catch (NumberFormatException e) {
			log.error("Error parsing slot duration '{}': {}", slotTimeStr, e.getMessage());
			throw new IllegalArgumentException("Invalid slot duration format: " + slotTimeStr, e);
		}
	}

//	/**
//	 * Splits a time slot into intervals based on slot duration and formats time
//	 * with AM/PM
//	 */
//	private List<TimeSlotInterval> splitTimeSlot(String startTime, String endTime, Integer slotDuration)
//	        throws java.text.ParseException {
//	    List<TimeSlotInterval> intervals = new ArrayList<>();
//
//	    TimeZone timeZone = TimeZone.getTimeZone("Asia/Kolkata");
//
//	    SimpleDateFormat inputFormat = new SimpleDateFormat("HH:mm");
//	    inputFormat.setTimeZone(timeZone);
//
//	    SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm a"); // Converts to AM/PM
//	    outputFormat.setTimeZone(timeZone);
//
//	    Calendar startCal = Calendar.getInstance(timeZone);
//	    startCal.setTime(inputFormat.parse(startTime));
//
//	    Calendar endCal = Calendar.getInstance(timeZone);
//	    endCal.setTime(inputFormat.parse(endTime));
//
//	    while (startCal.before(endCal)) {
//	        String intervalStart = outputFormat.format(startCal.getTime());
//
//	        startCal.add(Calendar.MINUTE, slotDuration);
//
//	        String intervalEnd = outputFormat.format(startCal.getTime());
//
//	        intervals.add(new TimeSlotInterval(intervalStart, intervalEnd));
//	    }
//
//	    return intervals;
//	}
	/**
	 * FIXED: Enhanced time slot splitting with proper AM/PM handling
	 * @throws java.text.ParseException 
	 */
	private List<TimeSlotInterval> splitTimeSlot(String startTime, String endTime, Integer slotDuration) throws java.text.ParseException {
	    List<TimeSlotInterval> intervals = new ArrayList<>();
	    
	    if (startTime == null || endTime == null || slotDuration == null || slotDuration <= 0) {
	        throw new IllegalArgumentException("Invalid parameters for time slot splitting");
	    }

	    try {
	        TimeZone timeZone = TimeZone.getTimeZone("Asia/Kolkata");
	        
	        SimpleDateFormat inputFormat = new SimpleDateFormat("HH:mm");
	        inputFormat.setTimeZone(timeZone);
	        
	        SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
	        outputFormat.setTimeZone(timeZone);

	        Calendar startCal = Calendar.getInstance(timeZone);
	        startCal.setTime(inputFormat.parse(startTime.trim()));

	        Calendar endCal = Calendar.getInstance(timeZone);
	        endCal.setTime(inputFormat.parse(endTime.trim()));

	        // Validate that start time is before end time
	        if (!startCal.before(endCal)) {
	            throw new IllegalArgumentException("Start time must be before end time");
	        }

	        while (startCal.before(endCal)) {
	            String intervalStart = outputFormat.format(startCal.getTime());
	            
	            // Add slot duration
	            startCal.add(Calendar.MINUTE, slotDuration);
	            
	            // Don't exceed end time
	            if (startCal.after(endCal)) {
	                break;
	            }
	            
	            String intervalEnd = outputFormat.format(startCal.getTime());
	            
	            intervals.add(new TimeSlotInterval(intervalStart, intervalEnd));
	            
	            log.debug("Created interval: {} - {}", intervalStart, intervalEnd);
	        }
	        
	        log.info("Generated {} time intervals with {} minute slots", intervals.size(), slotDuration);
	        
	    } catch (ParseException e) {
	        log.error("Error parsing time during slot splitting: start={}, end={}", startTime, endTime, e);
	        throw new RuntimeException("Error parsing time during slot splitting", e);
	    }
	    
	    return intervals;
	}


//	@Override
//	public ResponseEntity<?> saveMedicalTestSlotByDepartmentId(MedicalTestConfigWebModel medicalTestConfigWebModel) {
//	    try {
//	        // 1. Save MedicalTestSlot
//	        Department department = departmentRepository.findById(medicalTestConfigWebModel.getId())
//	            .orElseThrow(() -> new RuntimeException("Department not found with ID: " + medicalTestConfigWebModel.getId()));
//	            
//	        MedicalTestSlot slot = MedicalTestSlot.builder()
//	                .department(department)
//	                .createdBy(medicalTestConfigWebModel.getCreatedBy())
//	                .isActive(true)
//	                .build();
//	        slot = medicalTestSlotRepository.save(slot);
//
//	        // 2. Iterate DaySlots
//	        for (MedicalTestDaySlotWebModel daySlotModel : medicalTestConfigWebModel.getMedicalTestDaySlots()) {
//	            MedicalTestDaySlot daySlot = MedicalTestDaySlot.builder()
//	                    .medicalTestSlot(slot)
//	                    .day(daySlotModel.getDay())
//	                    .startSlotDate(daySlotModel.getStartSlotDate())
//	                    .endSlotDate(daySlotModel.getEndSlotDate())
//	                    .createdBy(medicalTestConfigWebModel.getCreatedBy())
//	                    .isActive(true)
//	                    .build();
//	            daySlot = medicalTestDaySlotRepository.save(daySlot);
//
//	            // 3. Save TimeSlots
//	            for (MedicalTestSlotTimeWebModel timeSlotModel : daySlotModel.getMedicalTestSlotTimes()) {
//	                MedicalTestSlotTime timeSlot = MedicalTestSlotTime.builder()
//	                        .medicalTestDaySlot(daySlot)
//	                        .slotStartTime(timeSlotModel.getSlotStartTime())
//	                        .slotEndTime(timeSlotModel.getSlotEndTime())
//	                        .slotTime(timeSlotModel.getSlotTime())
//	                        .createdBy(medicalTestConfigWebModel.getCreatedBy())
//	                        .isActive(true)
//	                        .build();
//	                MedicalTestSlotTime savedTimeSlot = medicalTestSlotTimeRepository.save(timeSlot);
//	                
//	                // 4. Generate dates based on day and date range
//	                List<String> datesToGenerate = generateDatesForDayOfWeek(
//	                        daySlotModel.getDay(),
//	                        daySlotModel.getStartSlotDate(),
//	                        daySlotModel.getEndSlotDate()
//	                );
//	                
//	                // 5. Create MedicalTestSlotDate for each date
//	                for (String date : datesToGenerate) {
//	                    MedicalTestSlotDate slotDate = MedicalTestSlotDate.builder()
//	                            .medicalTestSlotId(slot.getMedicalTestSlotId())
//	                            .medicalTestDaySlotId(daySlot.getMedicalTestDaySlotId())  
//	                            .medicalTestSlotTimeId(savedTimeSlot.getMedicalTestSlotTimeId())
//	                            .date(date)
//	                            .createdBy(medicalTestConfigWebModel.getCreatedBy())
//	                            .isActive(true)
//	                            .build();
//	                    
//	                    MedicalTestSlotDate savedSlotDate = medicalTestSlotDateRepository.save(slotDate);
//	                    
//	                    // Log the saved date to help with debugging
//	                    log.debug("Saved slot date with ID: {}", savedSlotDate.getMedicalTestSlotDateId());
//	                    
//	                    // 6. Split time slots into intervals and save MedicalTestSlotSpiltTime
//	                    try {
//	                        List<TimeSlotInterval> timeIntervals = splitTimeSlot(
//	                                timeSlotModel.getSlotStartTime(),
//	                                timeSlotModel.getSlotEndTime(),
//	                                parseSlotDuration(timeSlotModel.getSlotTime()) // Updated to use new method
//	                        );
//	                        
//	                        // Verify we have a valid medical_test_slot_date_id before saving split times
//	                        if (savedSlotDate.getMedicalTestSlotDateId() == null) {
//	                            throw new RuntimeException("Failed to generate valid medical_test_slot_date_id for slot date: " + date);
//	                        }
//	                        
//	                        Integer slotDateId = savedSlotDate.getMedicalTestSlotDateId();
//	                        for (TimeSlotInterval interval : timeIntervals) {
//	                            // Log before saving to verify ID is present
//	                            log.debug("Creating split time with medicalTestSlotDateId: {}", slotDateId);
//
//	                            // Create an instance of MedicalTestSlotDate with just the ID
//	                            MedicalTestSlotDate slotDate1 = new MedicalTestSlotDate();
//	                            slotDate1.setMedicalTestSlotDateId(slotDateId);
//
//	                            MedicalTestSlotSpiltTime spiltTime = MedicalTestSlotSpiltTime.builder()
//	                                    .slotStartTime(interval.getStartTime())
//	                                    .slotEndTime(interval.getEndTime())
//	                                    .slotStatus("AVAILABLE") // Default status
//	                                    .medicalTestSlotDate(slotDate1) // ✅ Pass the entity, not ID
//	                                    .createdBy(medicalTestConfigWebModel.getCreatedBy())
//	                                    .isActive(true)
//	                                    .build();
//
//	                            medicalTestSlotSpiltTimeRepository.save(spiltTime);
//	                        }
//
//	                    } catch (Exception e) {
//	                        log.error("Error processing time slots for date {}: {}", date, e.getMessage(), e);
//	                        throw new RuntimeException("Error processing time slots for date " + date + ": " + e.getMessage(), e);
//	                    }
//	                }
//	            }
//	        }
//
//	        return ResponseEntity.ok("Medical Test Slot saved successfully.");
//	    } catch (Exception e) {
//	        log.error("Error while saving test slots: {}", e.getMessage(), e);
//	        e.printStackTrace();
//	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//	            .body("Error while saving test slots: " + e.getMessage());
//	    }
//	}
//
//	/**
//	 * Helper class to store time slot interval information
//	 */
//	private static class TimeSlotInterval {
//	    private String startTime;
//	    private String endTime;
//	    
//	    public TimeSlotInterval(String startTime, String endTime) {
//	        this.startTime = startTime;
//	        this.endTime = endTime;
//	    }
//	    
//	    public String getStartTime() {
//	        return startTime;
//	    }
//	    
//	    public String getEndTime() {
//	        return endTime;
//	    }
//	}
//
//	/**
//	 * Generates a list of dates for a specific day of the week within a date range
//	 * 
//	 * @param dayOfWeekStr the day of week as String (can be either 1-7 or day names like "Monday", "Wednesday")
//	 * @param startDate the start date as java.util.Date
//	 * @param endDate the end date as java.util.Date
//	 * @return list of dates in "yyyy-MM-dd" format
//	 */
//
//	
//	private List<String> generateDatesForDayOfWeek(String dayOfWeekStr, Date startDate, Date endDate) {
//	    List<String> dates = new ArrayList<>();
//
//	    try {
//	        int dayOfWeek;
//
//	        if (dayOfWeekStr.matches("\\d+")) {
//	            dayOfWeek = Integer.parseInt(dayOfWeekStr);
//	            if (dayOfWeek < 1 || dayOfWeek > 7) {
//	                throw new IllegalArgumentException("Day of week must be between 1 and 7, got: " + dayOfWeek);
//	            }
//	        } else {
//	            switch (dayOfWeekStr.trim().toLowerCase()) {
//	                case "monday": dayOfWeek = 1; break;
//	                case "tuesday": dayOfWeek = 2; break;
//	                case "wednesday": dayOfWeek = 3; break;
//	                case "thursday": dayOfWeek = 4; break;
//	                case "friday": dayOfWeek = 5; break;
//	                case "saturday": dayOfWeek = 6; break;
//	                case "sunday": dayOfWeek = 7; break;
//	                default:
//	                    throw new IllegalArgumentException("Invalid day of week: " + dayOfWeekStr);
//	            }
//	        }
//
//	        TimeZone timeZone = TimeZone.getTimeZone("Asia/Kolkata");
//	        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//	        dateFormat.setTimeZone(timeZone);
//
//	        Calendar startCal = Calendar.getInstance(timeZone);
//	        startCal.setTime(startDate);
//	        // Clear time
//	        startCal.set(Calendar.HOUR_OF_DAY, 0);
//	        startCal.set(Calendar.MINUTE, 0);
//	        startCal.set(Calendar.SECOND, 0);
//	        startCal.set(Calendar.MILLISECOND, 0);
//
//	        Calendar endCal = Calendar.getInstance(timeZone);
//	        endCal.setTime(endDate);
//	        endCal.set(Calendar.HOUR_OF_DAY, 0);
//	        endCal.set(Calendar.MINUTE, 0);
//	        endCal.set(Calendar.SECOND, 0);
//	        endCal.set(Calendar.MILLISECOND, 0);
//
//	        int calendarDayOfWeek = (dayOfWeek == 7) ? Calendar.SUNDAY : dayOfWeek + 1;
//
//	        // Move forward to first match ON or AFTER startDate
//	        int currentDay = startCal.get(Calendar.DAY_OF_WEEK);
//	        int daysUntilTarget = (calendarDayOfWeek - currentDay + 7) % 7;
//	        if (daysUntilTarget != 0) {
//	            startCal.add(Calendar.DAY_OF_MONTH, daysUntilTarget);
//	        }
//
//	        // Now loop and add all matching weekdays
//	        while (!startCal.after(endCal)) {
//	            dates.add(dateFormat.format(startCal.getTime()));
//	            startCal.add(Calendar.DAY_OF_MONTH, 7);
//	        }
//
//	        log.debug("Generated {} dates for day {}", dates.size(), dayOfWeekStr);
//
//	    } catch (Exception e) {
//	        log.error("Error generating dates for day '{}': {}", dayOfWeekStr, e.getMessage(), e);
//	        throw new RuntimeException("Error generating dates: " + e.getMessage(), e);
//	    }
//
//	    return dates;
//	}
//
//
//	/**
//	 * Parses the slot time duration from a string format (e.g., "30 mins", "1 hour") to minutes as an integer
//	 * 
//	 * @param slotTimeStr the slot time as string (e.g., "30 mins", "1 hour")
//	 * @return the duration in minutes as an integer
//	 */
//	private Integer parseSlotDuration(String slotTimeStr) {
//	    if (slotTimeStr == null || slotTimeStr.trim().isEmpty()) {
//	        throw new IllegalArgumentException("Slot time cannot be null or empty");
//	    }
//	    
//	    String trimmedStr = slotTimeStr.trim().toLowerCase();
//	    
//	    try {
//	        // Extract just the numeric part
//	        String numericPart = trimmedStr.replaceAll("[^0-9]", "");
//	        
//	        if (numericPart.isEmpty()) {
//	            throw new IllegalArgumentException("No numeric value found in slot time: " + slotTimeStr);
//	        }
//	        
//	        int duration = Integer.parseInt(numericPart);
//	        
//	        // If it contains "hour" or "hr", convert to minutes
//	        if (trimmedStr.contains("hour") || trimmedStr.contains("hr")) {
//	            duration *= 60;
//	        }
//	        
//	        return duration;
//	        
//	    } catch (NumberFormatException e) {
//	        log.error("Error parsing slot duration '{}': {}", slotTimeStr, e.getMessage());
//	        throw new IllegalArgumentException("Invalid slot duration format: " + slotTimeStr, e);
//	    }
//	}
//
//	/**
//	 * Splits a time slot into intervals based on slot duration and formats time with AM/PM
//	 * 
//	 * @param startTime the start time in "HH:mm" format
//	 * @param endTime the end time in "HH:mm" format
//	 * @param slotDuration the duration of each slot in minutes
//	 * @return list of time slot intervals with times in AM/PM format
//	 * @throws java.text.ParseException if there's an error parsing the time strings
//	 */
//	private List<TimeSlotInterval> splitTimeSlot(String startTime, String endTime, Integer slotDuration) throws java.text.ParseException {
//	    List<TimeSlotInterval> intervals = new ArrayList<>();
//	    
//	    // Parse input times (which are in 24-hour format)
//	    SimpleDateFormat inputFormat = new SimpleDateFormat("HH:mm");
//	    
//	    // Format for output times with AM/PM
//	   // SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm a");
//	 // To this:
//	    SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
//	    outputFormat.getDateFormatSymbols().setAmPmStrings(new String[]{"AM", "PM"});
//	    
//	    Calendar startCal = Calendar.getInstance();
//	    startCal.setTime(inputFormat.parse(startTime));
//	    
//	    Calendar endCal = Calendar.getInstance();
//	    endCal.setTime(inputFormat.parse(endTime));
//	    
//	    // Generate time slots until we reach the end time
//	    while (startCal.getTime().before(endCal.getTime())) {
//	        // Format with AM/PM
//	        String intervalStart = outputFormat.format(startCal.getTime());
//	        
//	        // Add slot duration minutes to start time
//	        startCal.add(Calendar.MINUTE, slotDuration);
//	        
//	        // If exceeding end time, use end time as interval end
//	        if (startCal.getTime().after(endCal.getTime())) {
//	            String formattedEndTime = outputFormat.format(endCal.getTime());
//	            intervals.add(new TimeSlotInterval(intervalStart, formattedEndTime));
//	        } else {
//	            String intervalEnd = outputFormat.format(startCal.getTime());
//	            intervals.add(new TimeSlotInterval(intervalStart, intervalEnd));
//	        }
//	    }
//	    
//	    return intervals;
//	}
	@Override
	public ResponseEntity<?> getMedicalTestSlotByDepartmentId(Integer id) {

		// Fetch the medical test slots for the department
		List<MedicalTestSlot> slots = medicalTestSlotRepository.findByDepartmentIdAndIsActiveTrue(id);

		List<Map<String, Object>> responseList = new ArrayList<>();

		for (MedicalTestSlot slot : slots) {
			Map<String, Object> slotMap = new HashMap<>();
			Department department = slot.getDepartment();

			// Set fields from MedicalTestSlot
			slotMap.put("medicalTestSlotId", slot.getMedicalTestSlotId());
			slotMap.put("createdBy", slot.getCreatedBy());
			slotMap.put("createdOn", slot.getCreatedOn());
			slotMap.put("updatedBy", slot.getUpdatedBy());
			slotMap.put("updatedOn", slot.getUpdatedOn());
			slotMap.put("isActive", slot.getIsActive());

			if (department != null) {
				slotMap.put("departmentId", department.getId());
				slotMap.put("departmentName", department.getName());
				slotMap.put("hospitalId", department.getHospitalId());

				List<MedicalTestConfig> testConfigs = department.getMedicalTests();
				if (testConfigs != null && !testConfigs.isEmpty()) {
					List<Map<String, Object>> testDetails = new ArrayList<>();

					for (MedicalTestConfig test : testConfigs) {
						Map<String, Object> testMap = new HashMap<>();
						testMap.put("medicalTestId", test.getId());
						testMap.put("medicalTestName", test.getMedicalTestName());
						testMap.put("mrp", test.getMrp());
						testMap.put("gst", test.getGst());
						testMap.put("hospitalId", test.getHospitalId());
						testDetails.add(testMap);
					}

					slotMap.put("medicalTestDetails", testDetails);
				}
			}

			// Handle day slots
			List<MedicalTestDaySlot> daySlots = medicalTestDaySlotRepository
					.findByMedicalTestSlotMedicalTestSlotIdAndIsActiveTrue(slot.getMedicalTestSlotId());

			List<Map<String, Object>> daySlotModels = new ArrayList<>();

			for (MedicalTestDaySlot daySlot : daySlots) {
				Map<String, Object> daySlotMap = new HashMap<>();
				daySlotMap.put("medicalTestDaySlotId", daySlot.getMedicalTestDaySlotId());
				daySlotMap.put("day", daySlot.getDay());
				daySlotMap.put("startSlotDate", daySlot.getStartSlotDate());
				daySlotMap.put("endSlotDate", daySlot.getEndSlotDate());

				List<MedicalTestSlotTime> slotTimes = medicalTestSlotTimeRepository
						.findByMedicalTestDaySlotMedicalTestDaySlotIdAndIsActiveTrue(daySlot.getMedicalTestDaySlotId());

				List<Map<String, Object>> timeSlotModels = new ArrayList<>();

				for (MedicalTestSlotTime slotTime : slotTimes) {
					Map<String, Object> timeSlotMap = new HashMap<>();
					timeSlotMap.put("medicalTestSlotTimeId", slotTime.getMedicalTestSlotTimeId());
					timeSlotMap.put("slotStartTime", slotTime.getSlotStartTime());
					timeSlotMap.put("slotEndTime", slotTime.getSlotEndTime());
					timeSlotMap.put("slotTime", slotTime.getSlotTime());

					List<MedicalTestSlotDate> slotDates = medicalTestSlotDateRepository
							.findByMedicalTestSlotIdAndMedicalTestDaySlotIdAndMedicalTestSlotTimeId(
									slot.getMedicalTestSlotId(), daySlot.getMedicalTestDaySlotId(),
									slotTime.getMedicalTestSlotTimeId());

					List<Map<String, Object>> slotDatesWithSplit = new ArrayList<>();

					for (MedicalTestSlotDate slotDate : slotDates) {
						Map<String, Object> slotDateMap = new HashMap<>();
						slotDateMap.put("date", slotDate.getDate());
						slotDateMap.put("isActive", slotDate.getIsActive());
						slotDateMap.put("id", slotDate.getMedicalTestSlotDateId());

						List<MedicalTestSlotSpiltTime> splitTimes = medicalTestSlotSpiltTimeRepository
								.findByMedicalTestSlotDate_MedicalTestSlotDateIdAndIsActiveTrue(
										slotDate.getMedicalTestSlotDateId());

						List<Map<String, Object>> splitTimeList = new ArrayList<>();
						for (MedicalTestSlotSpiltTime splitTime : splitTimes) {
							Map<String, Object> splitMap = new HashMap<>();
							splitMap.put("slotStartTime", splitTime.getSlotStartTime());
							splitMap.put("slotEndTime", splitTime.getSlotEndTime());
							splitMap.put("slotStatus", splitTime.getSlotStatus());
							splitMap.put("overiddenStatus", splitTime.getOvverridenStatus());
							splitMap.put("isActive", splitTime.getIsActive());
							splitMap.put("id", splitTime.getMedicalTestSlotSpiltTimeId());
							splitTimeList.add(splitMap);
						}

						slotDateMap.put("splitTimes", splitTimeList);
						slotDatesWithSplit.add(slotDateMap);
					}

					timeSlotMap.put("slotDates", slotDatesWithSplit);
					timeSlotModels.add(timeSlotMap);
				}

				daySlotMap.put("timeSlots", timeSlotModels);
				daySlotModels.add(daySlotMap);
			}

			slotMap.put("daySlots", daySlotModels);
			responseList.add(slotMap);
		}

		return ResponseEntity.ok(responseList);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ResponseEntity<?> saveMedicalTestOvverride(MedicalTestConfigWebModel webModel) {
		try {
			if (webModel == null || webModel.getMedicalTestSlotTimeId() == null || webModel.getOverrideDate() == null
					|| StringUtils.isEmpty(webModel.getNewSlotTime())) {
				return ResponseEntity.badRequest().body(new Response(0, "error", "Missing required fields"));
			}

			Optional<MedicalTestSlotTime> doctorSlotTimeOpt = medicalTestSlotTimeRepository
					.findById(webModel.getMedicalTestSlotTimeId());
			if (!doctorSlotTimeOpt.isPresent()) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response(0, "error",
						"MedicalTestSlotTime not found with ID: " + webModel.getMedicalTestSlotTimeId()));
			}

			MedicalTestSlotTime slot = doctorSlotTimeOpt.get();

			// Create multiple formatters to handle different time formats
			DateTimeFormatter formatter12Hour = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);
			DateTimeFormatter formatter24Hour = DateTimeFormatter.ofPattern("H:mm");
			DateTimeFormatter formatter24HourWithColon = DateTimeFormatter.ofPattern("HH:mm");

			// Parse original slot times with flexible formatting
			LocalTime originalStart = parseTimeFlexibly(slot.getSlotStartTime(), formatter12Hour, formatter24Hour,
					formatter24HourWithColon);
			LocalTime originalEnd = parseTimeFlexibly(slot.getSlotEndTime(), formatter12Hour, formatter24Hour,
					formatter24HourWithColon);

			if (originalStart == null || originalEnd == null) {
				return ResponseEntity.badRequest().body(new Response(0, "error", "Invalid original slot time format"));
			}

			int durationMinutes = extractDurationInMinutes(webModel.getNewSlotTime());
			if (durationMinutes <= 0) {
				return ResponseEntity.badRequest().body(new Response(0, "error", "Invalid new slot time format."));
			}

			// Save override entry
			MedicalTestSlotTimeOveride override = MedicalTestSlotTimeOveride.builder().originalSlot(slot)
					.overrideDate(webModel.getOverrideDate()).newSlotTime(webModel.getNewSlotTime())
					.reason(webModel.getReason()).isActive(true).build();
			medicalTestSlotTimeOverideRepository.save(override);

			// Define IST timezone consistently
			ZoneId istZone = ZoneId.of("Asia/Kolkata");

			// Convert the Date to LocalDate in IST for database query
			DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			String dateString = webModel.getOverrideDate().toInstant().atZone(ZoneId.of("UTC"))
					.withZoneSameInstant(istZone).toLocalDate().format(dateFormatter);

			Optional<MedicalTestSlotDate> doctorSlotDateOpt = medicalTestSlotDateRepository
					.findByDateAndMedicalTestSlotTimeIdAndIsActive(dateString, slot.getMedicalTestSlotTimeId(), true);

			if (doctorSlotDateOpt.isPresent()) {
				Integer doctorSlotDateId = doctorSlotDateOpt.get().getMedicalTestSlotDateId();
				List<MedicalTestSlotSpiltTime> existingSplitTimes = medicalTestSlotSpiltTimeRepository
						.findByMedicalTestSlotDate_MedicalTestSlotDateIdAndIsActive(doctorSlotDateId, true);

				Optional<MedicalTestSlotDate> db = medicalTestSlotDateRepository.findById(doctorSlotDateId);
				if (db.isPresent()) {
					MedicalTestSlotDate slotDate = db.get();

					// Get current time and override date in IST
					LocalDate today = LocalDate.now(istZone);
					LocalTime now = LocalTime.now(istZone);
					LocalDate overrideLocalDate = webModel.getOverrideDate().toInstant().atZone(istZone).toLocalDate();

					// Update only future split times
					int overriddenSlots = 0;
					for (MedicalTestSlotSpiltTime splitTime : existingSplitTimes) {
						// Parse split time start with flexible formatting
						LocalTime slotStart = parseTimeFlexibly(splitTime.getSlotStartTime(), formatter12Hour,
								formatter24Hour, formatter24HourWithColon);
						LocalTime slotEnd = parseTimeFlexibly(splitTime.getSlotEndTime(), formatter12Hour,
								formatter24Hour, formatter24HourWithColon);

						if (slotStart == null || slotEnd == null) {
							continue; // Skip invalid time entries
						}

						boolean shouldOverride = true;

						if (overrideLocalDate.isEqual(today)) {
							if (!slotStart.isAfter(now)) {
								shouldOverride = false;
							}
						}

						if (shouldOverride) {
							// Deactivate current
							splitTime.setIsActive(false);
							medicalTestSlotSpiltTimeRepository.save(splitTime);

							// Calculate new start/end with override offset
							LocalTime newStart = slotStart.plusMinutes(durationMinutes);
							LocalTime newEnd = slotEnd.plusMinutes(durationMinutes);

							// Format back to 12-hour format for consistency
							MedicalTestSlotSpiltTime newSplit = MedicalTestSlotSpiltTime.builder()
									.slotStartTime(newStart.format(formatter12Hour))
									.slotEndTime(newEnd.format(formatter12Hour)).slotStatus(splitTime.getSlotStatus())
									.ovverridenStatus("OVERRIDDEN").medicalTestSlotDate(slotDate).isActive(true)
									.createdBy(webModel.getUpdatedBy()).createdOn(new Date()).build();

							medicalTestSlotSpiltTimeRepository.save(newSplit);
							overriddenSlots++;
						}
					}
				}
			}

			return ResponseEntity.ok(
					new Response(1, "success", "Override saved. Updated appointments and overridden future slots."));

		} catch (DateTimeParseException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(new Response(0, "error", "Invalid time format: " + e.getMessage()));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new Response(0, "error", "Error while saving override: " + e.getMessage()));
		}
	}

	// Helper method to parse time with multiple formats
	private LocalTime parseTimeFlexibly(String timeString, DateTimeFormatter... formatters) {
		if (timeString == null || timeString.trim().isEmpty()) {
			return null;
		}

		String trimmedTime = timeString.trim();

		for (DateTimeFormatter formatter : formatters) {
			try {
				return LocalTime.parse(trimmedTime, formatter);
			} catch (DateTimeParseException e) {
				// Try next formatter
			}
		}

		// If none of the formatters work, return null
		return null;
	}

	private int extractDurationInMinutes(String newSlotTime) {
		try {
			return Integer.parseInt(newSlotTime.trim().split(" ")[0]);
		} catch (Exception e) {
			return 0;
		}

	}

	@Override
	public ResponseEntity<?> addTimeSlotByMedicalTest(MedicalTestConfigWebModel medicalTestConfigWebModel) {
		// TODO Auto-generated method stub
		try {
			MedicalTestSlot doctorSlot = medicalTestSlotRepository
					.findById(medicalTestConfigWebModel.getMedicalTestSlotId()).orElseThrow(() -> new RuntimeException(
							"medicalTestSlot not found with ID: " + medicalTestConfigWebModel.getMedicalTestSlotId()));

			List<MedicalTestDaySlot> existingDoctorDaySlots = medicalTestDaySlotRepository
					.findByMedicalTestSlot(doctorSlot);

			if (!validateMedicalTestSlots(existingDoctorDaySlots, medicalTestConfigWebModel.getMedicalTestDaySlots())) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(0, "Error",
						"Doctor slot times overlap. Please ensure slot times don't conflict."));
			}

			if (medicalTestConfigWebModel.getMedicalTestDaySlots() != null) {
				for (MedicalTestDaySlotWebModel daySlotModel : medicalTestConfigWebModel.getMedicalTestDaySlots()) {
					MedicalTestDaySlot doctorDaySlot = MedicalTestDaySlot.builder().medicalTestSlot(doctorSlot)
							.day(daySlotModel.getDay()).startSlotDate(daySlotModel.getStartSlotDate())
							.endSlotDate(daySlotModel.getEndSlotDate())
							.createdBy(medicalTestConfigWebModel.getCreatedBy()).isActive(true).build();

					doctorDaySlot = medicalTestDaySlotRepository.save(doctorDaySlot);

					if (daySlotModel.getMedicalTestSlotTimes() != null) {
						for (MedicalTestSlotTimeWebModel slotTimeModel : daySlotModel.getMedicalTestSlotTimes()) {
							MedicalTestSlotTime doctorSlotTime = MedicalTestSlotTime.builder()
									.medicalTestDaySlot(doctorDaySlot).slotStartTime(slotTimeModel.getSlotStartTime())
									.slotEndTime(slotTimeModel.getSlotEndTime()).slotTime(slotTimeModel.getSlotTime())
									.createdBy(medicalTestConfigWebModel.getCreatedBy()).isActive(true).build();

							doctorSlotTime = medicalTestSlotTimeRepository.save(doctorSlotTime);

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
								MedicalTestSlotDate doctorSlotDate = createAndSaveMedicalTestSlotDate(
										medicalTestConfigWebModel.getCreatedBy(), doctorSlot, doctorDaySlot,
										doctorSlotTime, startDate);

								createMedicalTestSlotSplitTimes(medicalTestConfigWebModel.getCreatedBy(),
										doctorSlotDate, slotTimeModel, timeFormatter);

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

	private LocalDate convertToLocalDate(Date date) {
		return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
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
	private MedicalTestSlotDate createAndSaveMedicalTestSlotDate(Integer createdBy, MedicalTestSlot doctorSlot,
			MedicalTestDaySlot doctorDaySlot, MedicalTestSlotTime doctorSlotTime, LocalDate date) {
		MedicalTestSlotDate doctorSlotDate = MedicalTestSlotDate.builder()
				.medicalTestSlotId(doctorSlot.getMedicalTestSlotId())
				.medicalTestDaySlotId(doctorDaySlot.getMedicalTestDaySlotId())
				.medicalTestSlotTimeId(doctorSlotTime.getMedicalTestSlotTimeId()).date(date.toString())
				.createdBy(createdBy).isActive(true).build();

		return medicalTestSlotDateRepository.save(doctorSlotDate);
	}

	/**
	 * Creates doctor slot split times for a specific slot date with error handling.
	 */
	private void createMedicalTestSlotSplitTimes(Integer createdBy, MedicalTestSlotDate doctorSlotDate,
			MedicalTestSlotTimeWebModel slotTimeModel, DateTimeFormatter timeFormatter) {
		try {
			DateTimeFormatter timeFormatter1 = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);

			LocalTime start = LocalTime.parse(slotTimeModel.getSlotStartTime(), timeFormatter1);
			LocalTime end = LocalTime.parse(slotTimeModel.getSlotEndTime(), timeFormatter1);

			String durationStr = slotTimeModel.getSlotTime().replaceAll("[^0-9]", "").trim();
			int duration = Integer.parseInt(durationStr);

			log.info("Creating split times from {} to {} with {} minute intervals for date {}",
					slotTimeModel.getSlotStartTime(), slotTimeModel.getSlotEndTime(), duration,
					doctorSlotDate.getDate());

			int slotCount = 0;
			LocalTime currentStart = start;

			while (currentStart.plusMinutes(duration).compareTo(end) <= 0) {
				LocalTime currentEnd = currentStart.plusMinutes(duration);

				boolean exists = medicalTestSlotSpiltTimeRepository
						.existsBySlotStartTimeAndSlotEndTimeAndMedicalTestSlotDate_MedicalTestSlotDateId(
								currentStart.format(timeFormatter1), currentEnd.format(timeFormatter1),
								doctorSlotDate.getMedicalTestSlotDateId());

				if (!exists) {
					MedicalTestSlotSpiltTime splitTime = MedicalTestSlotSpiltTime.builder()
							.slotStartTime(currentStart.format(timeFormatter1))
							.slotEndTime(currentEnd.format(timeFormatter1)).slotStatus("Available").createdBy(createdBy)
							.medicalTestSlotDate(doctorSlotDate).isActive(true).build();

					medicalTestSlotSpiltTimeRepository.save(splitTime);
					slotCount++;
				}

				currentStart = currentEnd;
			}

			log.info("Created {} split time slots for doctor slot date ID: {}", slotCount,
					doctorSlotDate.getMedicalTestSlotDateId());

		} catch (Exception e) {
			log.error("Error creating doctor slot split times: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to create doctor slot split times", e);
		}
	}

	/**
	 * Helper method to convert java.util.Date to java.time.LocalDate
	 */
//			private LocalDate convertToLocalDate(Date date) {
//			    if (date == null) {
//			        return null;
//			    }
//			    return date.toInstant()
//			            .atZone(ZoneId.systemDefault())
//			            .toLocalDate();
//			}
	/**
	 * Validates if any new slots overlap with existing slots or among themselves.
	 */
	private boolean validateMedicalTestSlots(List<MedicalTestDaySlot> existingSlots,
			List<MedicalTestDaySlotWebModel> newSlots) {
		for (MedicalTestDaySlotWebModel newSlot : newSlots) {
			for (MedicalTestDaySlot existingSlot : existingSlots) {
				if (newSlot.getDay().equals(existingSlot.getDay()) && slotsOverlap(newSlot.getStartSlotDate(),
						newSlot.getEndSlotDate(), existingSlot.getStartSlotDate(), existingSlot.getEndSlotDate())) {
					return false;
				}
			}

			// Check for overlap among new slots themselves
			for (MedicalTestDaySlotWebModel otherNewSlot : newSlots) {
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

	@Override
	@Transactional
	public ResponseEntity<?> deleteSlotByMedicalTestById(Integer id) {
		if (id == null) {
			return ResponseEntity.badRequest().body(new Response(0, "error", "Invalid ID"));
		}

		Optional<MedicalTestSlotSpiltTime> slotOpt = medicalTestSlotSpiltTimeRepository.findById(id);
		if (!slotOpt.isPresent()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new Response(0, "error", "Slot not found with ID: " + id));
		}

		MedicalTestSlotSpiltTime slot = slotOpt.get();
		if (Boolean.FALSE.equals(slot.getIsActive())) {
			return ResponseEntity.ok(new Response(1, "success", "Slot already inactive"));
		}

		slot.setIsActive(false);
		slot.setUpdatedOn(new Date()); // optional if you want to track the update timestamp
		medicalTestSlotSpiltTimeRepository.save(slot);

		return ResponseEntity.ok(new Response(1, "success", "Slot deactivated successfully"));
	}

	/**
	 * Improved implementation for retrieving medical test slots by department
	 */
	@Override
	public ResponseEntity<?> getMedicalTestSlotById(Integer id, LocalDate date) {
		try {
			if (id == null || date == null) {
				return ResponseEntity.badRequest()
						.body(Collections.singletonMap("message", "Invalid department ID or request date"));
			}

			Optional<Department> departmentData = departmentRepository.findById(id);
			if (departmentData.isEmpty()) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(Collections.singletonMap("message", "Department not found"));
			}

			Department department = departmentData.get();
			Map<String, Object> response = new LinkedHashMap<>();
			response.put("departmentId", department.getId());
			response.put("name", department.getName());

			List<Map<String, Object>> testSlotList = medicalTestDaySlotRepository
					.findByMedicalTestSlot_Department(department).stream().filter(slot -> isValidSlot(slot, date))
					.map(slot -> buildMedicalTestSlot(slot, date)).filter(Objects::nonNull).distinct()
					.collect(Collectors.toList());

			response.put("medicalTestSlots", testSlotList);
			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.error("Error retrieving medical test slots for department {}: ", id, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.singletonMap("message", "Error retrieving medical test slots"));
		}
	}

	/**
	 * Converts Date object to LocalDate
	 */
	private LocalDate convertToLocalDates(Date dateToConvert) {
		if (dateToConvert == null) {
			return null;
		}
		return dateToConvert.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}

	/**
	 * Checks if the medical test slot is valid for the given date
	 */
	private boolean isValidSlot(MedicalTestDaySlot testDaySlot, LocalDate requestDate) {
		if (testDaySlot == null || requestDate == null)
			return false;

		LocalDate startDate = convertToLocalDates(testDaySlot.getStartSlotDate());
		LocalDate endDate = convertToLocalDates(testDaySlot.getEndSlotDate());

		return Boolean.TRUE.equals(testDaySlot.getIsActive()) && !requestDate.isBefore(startDate)
				&& !requestDate.isAfter(endDate);
	}

	/**
	 * Builds medical test slot data structure with all related information
	 */
	private Map<String, Object> buildMedicalTestSlot(MedicalTestDaySlot daySlot, LocalDate requestDate) {
		List<Map<String, Object>> daySlotList = medicalTestDaySlotRepository
				.findByMedicalTestSlot(daySlot.getMedicalTestSlot()).stream()
				.filter(slot -> isValidDaySlot(slot, requestDate)).map(slot -> buildDaySlot(slot, requestDate))
				.collect(Collectors.toList());

		Map<String, Object> testSlotData = new LinkedHashMap<>();
		testSlotData.put("medicalTestSlotId", daySlot.getMedicalTestSlot().getMedicalTestSlotId());
		testSlotData.put("daySlots", daySlotList);
		return testSlotData;
	}

	/**
	 * Checks if day slot is valid for the requested date
	 */
	private boolean isValidDaySlot(MedicalTestDaySlot daySlot, LocalDate requestDate) {
		if (daySlot == null || requestDate == null)
			return false;

		LocalDate startDate = convertToLocalDate(daySlot.getStartSlotDate());
		LocalDate endDate = convertToLocalDate(daySlot.getEndSlotDate());
		String requestDay = requestDate.getDayOfWeek().toString();

		return Boolean.TRUE.equals(daySlot.getIsActive()) && !requestDate.isBefore(startDate)
				&& !requestDate.isAfter(endDate) && requestDay.equalsIgnoreCase(daySlot.getDay());
	}

	/**
	 * Builds day slot data structure with times and availability
	 */
	private Map<String, Object> buildDaySlot(MedicalTestDaySlot daySlot, LocalDate requestDate) {
		Map<String, Object> daySlotData = new LinkedHashMap<>();
		daySlotData.put("daySlotId", daySlot.getMedicalTestDaySlotId());
		daySlotData.put("day", daySlot.getDay());
		daySlotData.put("startSlotDate", daySlot.getStartSlotDate());
		daySlotData.put("endSlotDate", daySlot.getEndSlotDate());
		daySlotData.put("isActive", daySlot.getIsActive());

		// Use the proper relationship defined in MedicalTestDaySlot to get slot times
		List<Map<String, Object>> timeSlotList = medicalTestSlotTimeRepository.findByMedicalTestDaySlot(daySlot)
				.stream().filter(slotTime -> Boolean.TRUE.equals(slotTime.getIsActive()))
				.map(slotTime -> buildSlotTime(slotTime, requestDate)).collect(Collectors.toList());

		daySlotData.put("slotTimes", timeSlotList);
		return daySlotData;
	}

	/**
	 * Builds time slot data with split times information
	 */
	private Map<String, Object> buildSlotTime(MedicalTestSlotTime slotTime, LocalDate requestDate) {
		Map<String, Object> timeSlotData = new LinkedHashMap<>();
		timeSlotData.put("slotTimeId", slotTime.getMedicalTestSlotTimeId());
		timeSlotData.put("startTime", slotTime.getSlotStartTime());
		timeSlotData.put("endTime", slotTime.getSlotEndTime());
		timeSlotData.put("isActive", slotTime.getIsActive());
		if (slotTime.getSlotTime() != null) {
			timeSlotData.put("slotTime", slotTime.getSlotTime());
		}

		// Query for slot dates with proper parameters
		List<MedicalTestSlotDate> testSlotDates = medicalTestSlotDateRepository
				.findByMedicalTestSlotTimeIdAndMedicalTestDaySlotIdAndMedicalTestSlotIdAndDateAndIsActive(
						slotTime.getMedicalTestSlotTimeId(), slotTime.getMedicalTestDaySlot().getMedicalTestDaySlotId(),
						slotTime.getMedicalTestDaySlot().getMedicalTestSlot().getMedicalTestSlotId(),
						requestDate.toString(), true);

		// Map split times for this time slot
		List<Map<String, Object>> splitTimeList = testSlotDates.stream().flatMap(slotDate -> {
			// Use the updated repository method with proper path traversal
			List<MedicalTestSlotSpiltTime> splitTimes = medicalTestSlotSpiltTimeRepository
					.findByMedicalTestSlotDate_MedicalTestSlotDateIdAndIsActive(slotDate.getMedicalTestSlotDateId(),
							true);

			return splitTimes.stream().map(split -> {
				Map<String, Object> splitData = new LinkedHashMap<>();
				splitData.put("slotSplitTimeId", split.getMedicalTestSlotSpiltTimeId());
				splitData.put("slotStartTime", split.getSlotStartTime());
				splitData.put("slotEndTime", split.getSlotEndTime());
				splitData.put("slotStatus", split.getSlotStatus());
				return splitData;
			});
		}).collect(Collectors.toList());

		timeSlotData.put("slotSplitTimes", splitTimeList);
		return timeSlotData;
	}

	@Override
	public ResponseEntity<?> getAllDepartmentByhospitalId(Integer hospitalId) {
		List<Department> departments = departmentRepository.findByHospitalIdAndIsActiveTrue(hospitalId);

		List<Map<String, Object>> result = new ArrayList<>();

		for (Department dept : departments) {
			Map<String, Object> map = new HashMap<>();
			map.put("id", dept.getId());
			map.put("name", dept.getName());
			result.add(map);
		}

		return ResponseEntity.ok(result);
	}

	@Override
	public ResponseEntity<?> getAllDoctorList() {
		List<User> doctors = userRepository.findByUserTypeIgnoreCaseAndUserIsActiveTrue("DOCTOR");

		List<Map<String, Object>> doctorList = new ArrayList<>();

		for (User user : doctors) {
			Map<String, Object> doctorMap = new HashMap<>();

			// Compose name
			String name = (user.getFirstName() != null ? user.getFirstName() : "") + " "
					+ (user.getLastName() != null ? user.getLastName() : "");
			if (name.trim().isEmpty()) {
				name = user.getUserName();
			}

			// Get specialties from DoctorSpecialty table
			List<DoctorSpecialty> specialties = doctorSpecialtyRepository.findSpecialtiesByUserId(user.getUserId());
			List<String> specialtyNames = new ArrayList<>();
			for (DoctorSpecialty specialty : specialties) {
				if (specialty.getSpecialtyName() != null) {
					specialtyNames.add(specialty.getSpecialtyName());
				}
			}

			// Get hospital name from HospitalDetail table using hospitalId
			Optional<HospitalDataList> hospitalDetailOpt = hospitalDataListRepository.findById(user.getHospitalId());
			String hospitalName = hospitalDetailOpt.map(HospitalDataList::getHospitalName).orElse("Unknown Hospital");

			// Add data to map
			doctorMap.put("userId", user.getUserId());
			doctorMap.put("hospitalId", user.getHospitalId());
			doctorMap.put("userName", name.trim());
			doctorMap.put("hospitalName", hospitalName);
			doctorMap.put("specialties", specialtyNames);

			doctorList.add(doctorMap);
		}

		return ResponseEntity.ok(doctorList);
	}

//	@Override
//	public ResponseEntity<?> saveResultByAppoitmentId(MedicalTestConfigWebModel medicalTestConfigWebModel) {
//		Map<String, Object> response = new HashMap<>();
//		try {
//			ArrayList<FileInputWebModel> filesInputWebModel = medicalTestConfigWebModel.getFilesInputWebModel();
//			ArrayList<MediaFile> uploadedDocuments = new ArrayList<>();
//			Optional<User> userOptional = userRepository.findById(medicalTestConfigWebModel.getCreatedBy());
//			if (!userOptional.isPresent()) {
//				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(-1, "User not found", null));
//			}
//
//			User user = userOptional.get();
//
//			if (filesInputWebModel != null && !filesInputWebModel.isEmpty()) {
//				for (FileInputWebModel fileInput : filesInputWebModel) {
//					if (fileInput.getFileData() != null) {
//						MediaFile mediaFiles = new MediaFile();
//
//						String fileName = UUID.randomUUID().toString();
//
//						mediaFiles.setFileName(fileName);
//						mediaFiles.setFileOriginalName(fileInput.getFileName());
//						mediaFiles.setFileSize(fileInput.getFileSize());
//						mediaFiles.setFileType(fileInput.getFileType());
//						mediaFiles.setFileDomainId(HealthCareConstant.resultDocument);
//						mediaFiles.setUser(user);
//
//						// Assuming restaurant object is already defined and represents the appointment
//						mediaFiles.setFileDomainReferenceId(medicalTestConfigWebModel.getId());
//						mediaFiles.setFileIsActive(true);
//						mediaFiles.setCategory(MediaFileCategory.resutDocument);
//
//						mediaFiles.setFileCreatedBy(medicalTestConfigWebModel.getCreatedBy());
//
//						mediaFiles = mediaFilesRepository.save(mediaFiles);
//						uploadedDocuments.add(mediaFiles);
//
//						// Save the file to disk
//						Base64FileUpload.saveFile(imageLocation + "/healthCare", fileInput.getFileData(), fileName);
//					}
//				}
//			}
//
//			response.put("uploadedDocuments", uploadedDocuments);
//			return ResponseEntity.ok(new Response(1, "Files saved successfully", response));
//
//		} catch (Exception e) {
//			log.error("Error at saveResultByAppoitmentId() -> {}", e.getMessage());
//			e.printStackTrace();
//			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//					.body(new Response(-1, "Error occurred while saving results", null));
//		}
//	}
	@Override
	public ResponseEntity<?> saveResultByAppoitmentId(MedicalTestConfigWebModel medicalTestConfigWebModel) {
	    Map<String, Object> response = new HashMap<>();
	    try {
	        ArrayList<FileInputWebModel> filesInputWebModel = medicalTestConfigWebModel.getFilesInputWebModel();
	        ArrayList<MediaFile> uploadedDocuments = new ArrayList<>();
	        Optional<User> userOptional = userRepository.findById(medicalTestConfigWebModel.getCreatedBy());

	        if (!userOptional.isPresent()) {
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(-1, "User not found", null));
	        }

	        User user = userOptional.get();

	        // Step 1: Set existing documents to inactive
	        List<MediaFile> existingFiles = mediaFilesRepository
	            .findByFileDomainReferenceIdAndFileDomainIdAndFileIsActiveTrue(
	                medicalTestConfigWebModel.getId(), HealthCareConstant.resultDocument);

	        for (MediaFile existing : existingFiles) {
	            existing.setFileIsActive(false);
	        }
	        mediaFilesRepository.saveAll(existingFiles);

	        // Step 2: Save new files
	        if (filesInputWebModel != null && !filesInputWebModel.isEmpty()) {
	            for (FileInputWebModel fileInput : filesInputWebModel) {
	                if (fileInput.getFileData() != null) {
	                    MediaFile mediaFile = new MediaFile();

	                    String fileName = UUID.randomUUID().toString();
	                    mediaFile.setFileName(fileName);
	                    mediaFile.setFileOriginalName(fileInput.getFileName());
	                    mediaFile.setFileSize(fileInput.getFileSize());
	                    mediaFile.setFileType(fileInput.getFileType());
	                    mediaFile.setFileDomainId(HealthCareConstant.resultDocument);
	                    mediaFile.setUser(user);
	                    mediaFile.setFileDomainReferenceId(medicalTestConfigWebModel.getId());
	                    mediaFile.setFileIsActive(true);
	                    mediaFile.setCategory(MediaFileCategory.resutDocument);
	                    mediaFile.setFileCreatedBy(medicalTestConfigWebModel.getCreatedBy());

	                    mediaFile = mediaFilesRepository.save(mediaFile);
	                    uploadedDocuments.add(mediaFile);

	                    // Save the file to disk
	                    Base64FileUpload.saveFile(imageLocation + "/healthCare", fileInput.getFileData(), fileName);
	                }
	            }
	        }

	        response.put("uploadedDocuments", uploadedDocuments);
	        return ResponseEntity.ok(new Response(1, "Files saved successfully", response));

	    } catch (Exception e) {
	        log.error("Error at saveResultByAppoitmentId() -> {}", e.getMessage());
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(-1, "Error occurred while saving results", null));
	    }
	}


	@Override
	public ResponseEntity<?> getResultByAppoitmentId(MedicalTestConfigWebModel medicalTestConfigWebModel) {
		Map<String, Object> response = new HashMap<>();
		try {
			Integer appointmentId = medicalTestConfigWebModel.getId();

			// Fetch only active media files related to the appointment with category
			// 'resutDocument'
			List<MediaFile> resultFiles = mediaFilesRepository
					.findByFileDomainReferenceIdAndCategoryAndFileIsActiveTrue(appointmentId,
							MediaFileCategory.resutDocument);

			ArrayList<FileInputWebModel> filesInputWebModelList = new ArrayList<>();

			if (resultFiles != null && !resultFiles.isEmpty()) {
				for (MediaFile mediaFile : resultFiles) {
					FileInputWebModel fileInput = new FileInputWebModel();
					fileInput.setFileName(mediaFile.getFileOriginalName());
					fileInput.setFileSize(mediaFile.getFileSize());
					fileInput.setFileType(mediaFile.getFileType());
					fileInput.setFileId(mediaFile.getFileId());
					fileInput.setFilePath(mediaFile.getFilePath());
					fileInput.setType(mediaFile.getFileType());
					fileInput.setUserId(mediaFile.getUser().getUserId());

					// Convert file to Base64 string
					String fileData = Base64FileUpload.encodeToBase64String(imageLocation + "/healthCare",
							mediaFile.getFileName());
					fileInput.setFileData(fileData);

					filesInputWebModelList.add(fileInput);
				}
			}

			response.put("appointmentId", appointmentId);
			response.put("resultDocuments", filesInputWebModelList);

			return ResponseEntity.ok(new Response(1, "Result documents fetched successfully", response));

		} catch (Exception e) {
			log.error("Error at getResultByAppoitmentId() -> {}", e.getMessage());
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new Response(-1, "Failed to fetch result documents", null));
		}
	}

	@Override
	public ResponseEntity<?> getAllPatientAppointmentByReceptionistLogin(Integer hospitalId) {
		try {
			// List<PatientAppointmentTable> appointments =
			// patientAppointmentRepository.findAppointmentsByFilter(appointmentDate,
			// appointmentType, doctorId);
			List<PatientAppointmentTable> appointments = patientAppointmentRepository
					.findAppointmentsByDoctorHospitalId(hospitalId) // ✅ Filtering by doctor's hospital
					.stream().sorted(Comparator.comparing(PatientAppointmentTable::getCreatedOn).reversed())
					.collect(Collectors.toList());

			if (appointments.isEmpty()) {
				return ResponseEntity.ok(new Response(0, "No Appointments Found", null));
			}

			List<Map<String, Object>> appointmentDetails = new ArrayList<>();

			for (PatientAppointmentTable appointment : appointments) {
				Map<String, Object> details = new HashMap<>();
				// Appointment details
				// Appointment details
				details.put("appointmentId", appointment.getAppointmentId());
				details.put("appointmentDate", appointment.getAppointmentDate());
				details.put("appointmentType", appointment.getAppointmentType());
				details.put("appointmentStatus", appointment.getAppointmentStatus());
				details.put("slotStartTime", appointment.getSlotStartTime());
				details.put("slotEndTime", appointment.getSlotEndTime());
				details.put("doctorSlotSpiltTimeId", appointment.getDoctorSlotSpiltTimeId());
				details.put("slotTime", appointment.getSlotTime());
				details.put("isActive", appointment.getIsActive());
				details.put("createdBy", appointment.getCreatedBy());
				details.put("createdOn", appointment.getCreatedOn());
				details.put("updatedBy", appointment.getUpdatedBy());
				details.put("updatedOn", appointment.getUpdatedOn());
				details.put("patientNotes", appointment.getPatientNotes());
				details.put("doctorSlotId", appointment.getDoctorSlotId());
				details.put("daySlotId", appointment.getDaySlotId());
				details.put("timeSlotId", appointment.getTimeSlotId());
				details.put("age", appointment.getAge());
				details.put("dob", appointment.getDateOfBirth());
				details.put("relationshipType", appointment.getRelationShipType());
				details.put("patientName", appointment.getPatientName());
				details.put("token", appointment.getToken());

				// Doctor details
				details.put("doctorId", appointment.getDoctor().getUserId());
				details.put("doctorName", appointment.getDoctor().getUserName());

				// Fetching Patient Details
				Optional<PatientDetails> patientDetailsOpt = patientDetailsRepository
						.findById(appointment.getPatient().getPatientDetailsId());
				if (patientDetailsOpt.isPresent()) {
					PatientDetails patient = patientDetailsOpt.get();
					details.put("patientId", patient.getPatientDetailsId());
					details.put("patientName", patient.getPatientName());
					details.put("patientAge", patient.getAge());
					details.put("patientDOB", patient.getDob());
					details.put("gender", patient.getGender());
					details.put("bloodGroup", patient.getBloodGroup());
					details.put("mobileNumber", patient.getMobileNumber());
					details.put("emailId", patient.getEmailId());
					details.put("address", patient.getAddress());
					details.put("emergencyContact", patient.getEmergencyContact());

					details.put("purposeOfVisit", patient.getPurposeOfVisit());
					details.put("policyNumber", patient.getPolicyNumber());
					details.put("disability", patient.getDisability());
					details.put("previousMedicalHistory", patient.getPreviousMedicalHistory());
				}
				appointmentDetails.add(details);
			}

			return ResponseEntity.ok(new Response(1, "Appointments Retrieved", appointmentDetails));

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new Response(-1, "Failed to retrieve appointments", e.getMessage()));
		}
	}

	@Override
	public ResponseEntity<?> getAllPatientAppointmentByPharmacyPendingList(Integer hospitalId, String userType) {
		// TODO Auto-generated method stub
		return null;
	}

}

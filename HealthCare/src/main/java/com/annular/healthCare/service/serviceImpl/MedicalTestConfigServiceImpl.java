package com.annular.healthCare.service.serviceImpl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
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
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.ParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.annular.healthCare.Response;
import com.annular.healthCare.model.Department;
import com.annular.healthCare.model.DoctorDaySlot;
import com.annular.healthCare.model.DoctorRole;
import com.annular.healthCare.model.DoctorSlot;
import com.annular.healthCare.model.DoctorSlotDate;
import com.annular.healthCare.model.DoctorSlotSpiltTime;
import com.annular.healthCare.model.DoctorSlotTime;
import com.annular.healthCare.model.DoctorSlotTimeOverride;
import com.annular.healthCare.model.DoctorSpecialty;
import com.annular.healthCare.model.MedicalTestConfig;
import com.annular.healthCare.model.MedicalTestDaySlot;
import com.annular.healthCare.model.MedicalTestSlot;
import com.annular.healthCare.model.MedicalTestSlotDate;
import com.annular.healthCare.model.MedicalTestSlotSpiltTime;
import com.annular.healthCare.model.MedicalTestSlotTime;
import com.annular.healthCare.model.MedicalTestSlotTimeOveride;
import com.annular.healthCare.model.PatientAppointmentTable;
import com.annular.healthCare.model.User;
import com.annular.healthCare.repository.DepartmentRepository;
import com.annular.healthCare.repository.DoctorSpecialityRepository;
import com.annular.healthCare.repository.MedicalTestConfigRepository;
import com.annular.healthCare.repository.MedicalTestDaySlotRepository;
import com.annular.healthCare.repository.MedicalTestSlotDateRepository;
import com.annular.healthCare.repository.MedicalTestSlotRepository;
import com.annular.healthCare.repository.MedicalTestSlotSpiltTimeRepository;
import com.annular.healthCare.repository.MedicalTestSlotTimeOverideRepository;
import com.annular.healthCare.repository.MedicalTestSlotTimeRepository;
import com.annular.healthCare.repository.UserRepository;
import com.annular.healthCare.service.MedicalTestConfigService;
import com.annular.healthCare.webModel.DaySlotWebModel;
import com.annular.healthCare.webModel.DoctorDaySlotWebModel;
import com.annular.healthCare.webModel.DoctorSlotTimeWebModel;
import com.annular.healthCare.webModel.MedicalTestConfigWebModel;
import com.annular.healthCare.webModel.MedicalTestDaySlotWebModel;
import com.annular.healthCare.webModel.MedicalTestDto;
import com.annular.healthCare.webModel.MedicalTestItem;
import com.annular.healthCare.webModel.MedicalTestSlotTimeWebModel;
import com.annular.healthCare.webModel.TimeSlotModel;

@Service
public class MedicalTestConfigServiceImpl implements MedicalTestConfigService{
	
	public static final Logger log = LoggerFactory.getLogger(MediaFilesServiceImpl.class);
	
	@Autowired
	MedicalTestConfigRepository medicalTestConfigRepository;
	
	@Autowired
	DepartmentRepository departmentRepository;
	
	@Autowired
	MedicalTestSlotRepository medicalTestSlotRepository;
	
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
	
	
	@Override
	public ResponseEntity<?> saveDepartment(MedicalTestConfigWebModel request) {
		Optional<Department> existing = departmentRepository.findByName(request.getName());

	    if (existing.isPresent()) {
	        return ResponseEntity.ok(new Response(0, "fail", "Department already exists."));
	    }

	    Department department = Department.builder()
	            .name(request.getName())
	            .createdBy(request.getCreatedBy())
	            .updatedBy(request.getUpdatedBy())
	            .hospitalId(request.getHospitalId())
	            .isActive(true)
	            .build();

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
		                    request.getHospitalId(),
		                    test.getMedicalTestName(),
		                    department.getId()
		            );

		            if (exists) {
		                failedTests.add(test.getMedicalTestName());
		                continue;
		            }

		            MedicalTestConfig medicalTestConfig = MedicalTestConfig.builder()
		                    .department(department)
		                    .medicalTestName(test.getMedicalTestName())
		                    .mrp(test.getMrp())
		                    .gst(test.getGst())
		                    .createdBy(request.getCreatedBy())
		                    .updatedBy(request.getUpdatedBy())
		                    .isActive(true)
		                    .hospitalId(request.getHospitalId())
		                    .build();

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
		        message = "Partially successful. Saved: " + String.join(", ", successfulTests)
		                + ". Failed: " + String.join(", ", failedTests);
		    }

		    return ResponseEntity.ok(new Response(
		            !successfulTests.isEmpty() ? 1 : 0,
		            !successfulTests.isEmpty() ? "success" : "fail",
		            message
		    ));
		}

	

	@Override
	public ResponseEntity<?> getAllMedicalTestNameByHospitalId(Integer hospitalId) {

		    try {
		        List<MedicalTestConfig> testConfigs = medicalTestConfigRepository.findByHospitalIdAndIsActiveTrue(hospitalId);
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
		                
		                @SuppressWarnings("unchecked")
		                List<Map<String, Object>> tests = (List<Map<String, Object>>) departmentMap.get(departmentId).get("tests");
		                tests.add(testMap);
		            }
		        }
		        
		        // Convert to a list for the response
		        List<Map<String, Object>> responseList = new ArrayList<>(departmentMap.values());
		        
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
	                    Optional<MedicalTestConfig> optionalConfig = medicalTestConfigRepository.findById(testDto.getMedicalId());

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
	                        

	                        medicalTestConfigRepository.save(medicalTestConfig);  // Save updated test
	                        successfulTests.add(testDto.getMedicalTestName());
	                    } else {
	                        failedTests.add("Test ID " + testDto.getMedicalId() + " not found for update.");
	                    }
	                } else {
	                    // Create new test
	                    medicalTestConfig = MedicalTestConfig.builder()
	                            .department(department)
	                            .medicalTestName(testDto.getMedicalTestName())
	                            .hospitalId(testDto.getHospitalId())
	                            .createdBy(testDto.getUpdatedBy())
	                            .updatedBy(testDto.getUpdatedBy())
	                            .mrp(testDto.getMrp())
	                            .gst(testDto.getGst())
	                            .isActive(true)
	                            .createdOn(new Date())
	                            .updatedOn(new Date())
	                            .build();

	                    medicalTestConfigRepository.save(medicalTestConfig);  // Save new test
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
	            message = "Partially successful. Processed tests: " + String.join(", ", successfulTests) + ". Failed tests: " + String.join(", ", failedTests);
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
	        return ResponseEntity.ok(new Response(1, "success", "Department and associated tests set to inactive successfully"));

	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(0, "error", "Something went wrong: " + e.getMessage()));
	    }
	}
	@Override
	public ResponseEntity<?> saveMedicalTestSlotByDepartmentId(MedicalTestConfigWebModel medicalTestConfigWebModel) {
	    try {
	        // 1. Save MedicalTestSlot
	        Department department = departmentRepository.findById(medicalTestConfigWebModel.getId())
	            .orElseThrow(() -> new RuntimeException("Department not found with ID: " + medicalTestConfigWebModel.getId()));
	            
	        MedicalTestSlot slot = MedicalTestSlot.builder()
	                .department(department)
	                .createdBy(medicalTestConfigWebModel.getCreatedBy())
	                .isActive(true)
	                .build();
	        slot = medicalTestSlotRepository.save(slot);

	        // 2. Iterate DaySlots
	        for (MedicalTestDaySlotWebModel daySlotModel : medicalTestConfigWebModel.getMedicalTestDaySlots()) {
	            MedicalTestDaySlot daySlot = MedicalTestDaySlot.builder()
	                    .medicalTestSlot(slot)
	                    .day(daySlotModel.getDay())
	                    .startSlotDate(daySlotModel.getStartSlotDate())
	                    .endSlotDate(daySlotModel.getEndSlotDate())
	                    .createdBy(medicalTestConfigWebModel.getCreatedBy())
	                    .isActive(true)
	                    .build();
	            daySlot = medicalTestDaySlotRepository.save(daySlot);

	            // 3. Save TimeSlots
	            for (MedicalTestSlotTimeWebModel timeSlotModel : daySlotModel.getMedicalTestSlotTimes()) {
	                MedicalTestSlotTime timeSlot = MedicalTestSlotTime.builder()
	                        .medicalTestDaySlot(daySlot)
	                        .slotStartTime(timeSlotModel.getSlotStartTime())
	                        .slotEndTime(timeSlotModel.getSlotEndTime())
	                        .slotTime(timeSlotModel.getSlotTime())
	                        .createdBy(medicalTestConfigWebModel.getCreatedBy())
	                        .isActive(true)
	                        .build();
	                MedicalTestSlotTime savedTimeSlot = medicalTestSlotTimeRepository.save(timeSlot);
	                
	                // 4. Generate dates based on day and date range
	                List<String> datesToGenerate = generateDatesForDayOfWeek(
	                        daySlotModel.getDay(),
	                        daySlotModel.getStartSlotDate(),
	                        daySlotModel.getEndSlotDate()
	                );
	                
	                // 5. Create MedicalTestSlotDate for each date
	                for (String date : datesToGenerate) {
	                    MedicalTestSlotDate slotDate = MedicalTestSlotDate.builder()
	                            .medicalTestSlotId(slot.getMedicalTestSlotId())
	                            .medicalTestDaySlotId(daySlot.getMedicalTestDaySlotId())  
	                            .medicalTestSlotTimeId(savedTimeSlot.getMedicalTestSlotTimeId())
	                            .date(date)
	                            .createdBy(medicalTestConfigWebModel.getCreatedBy())
	                            .isActive(true)
	                            .build();
	                    
	                    MedicalTestSlotDate savedSlotDate = medicalTestSlotDateRepository.save(slotDate);
	                    
	                    // Log the saved date to help with debugging
	                    log.debug("Saved slot date with ID: {}", savedSlotDate.getMedicalTestSlotDateId());
	                    
	                    // 6. Split time slots into intervals and save MedicalTestSlotSpiltTime
	                    try {
	                        List<TimeSlotInterval> timeIntervals = splitTimeSlot(
	                                timeSlotModel.getSlotStartTime(),
	                                timeSlotModel.getSlotEndTime(),
	                                parseSlotDuration(timeSlotModel.getSlotTime()) // Updated to use new method
	                        );
	                        
	                        // Verify we have a valid medical_test_slot_date_id before saving split times
	                        if (savedSlotDate.getMedicalTestSlotDateId() == null) {
	                            throw new RuntimeException("Failed to generate valid medical_test_slot_date_id for slot date: " + date);
	                        }
	                        
	                        Integer slotDateId = savedSlotDate.getMedicalTestSlotDateId();
	                        for (TimeSlotInterval interval : timeIntervals) {
	                            // Log before saving to verify ID is present
	                            log.debug("Creating split time with medicalTestSlotDateId: {}", slotDateId);

	                            // Create an instance of MedicalTestSlotDate with just the ID
	                            MedicalTestSlotDate slotDate1 = new MedicalTestSlotDate();
	                            slotDate1.setMedicalTestSlotDateId(slotDateId);

	                            MedicalTestSlotSpiltTime spiltTime = MedicalTestSlotSpiltTime.builder()
	                                    .slotStartTime(interval.getStartTime())
	                                    .slotEndTime(interval.getEndTime())
	                                    .slotStatus("AVAILABLE") // Default status
	                                    .medicalTestSlotDate(slotDate1) // ✅ Pass the entity, not ID
	                                    .createdBy(medicalTestConfigWebModel.getCreatedBy())
	                                    .isActive(true)
	                                    .build();

	                            medicalTestSlotSpiltTimeRepository.save(spiltTime);
	                        }

	                    } catch (Exception e) {
	                        log.error("Error processing time slots for date {}: {}", date, e.getMessage(), e);
	                        throw new RuntimeException("Error processing time slots for date " + date + ": " + e.getMessage(), e);
	                    }
	                }
	            }
	        }

	        return ResponseEntity.ok("Medical Test Slot saved successfully.");
	    } catch (Exception e) {
	        log.error("Error while saving test slots: {}", e.getMessage(), e);
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	            .body("Error while saving test slots: " + e.getMessage());
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
	 * 
	 * @param dayOfWeekStr the day of week as String (can be either 1-7 or day names like "Monday", "Wednesday")
	 * @param startDate the start date as java.util.Date
	 * @param endDate the end date as java.util.Date
	 * @return list of dates in "yyyy-MM-dd" format
	 */
	private List<String> generateDatesForDayOfWeek(String dayOfWeekStr, Date startDate, Date endDate) {
	    List<String> dates = new ArrayList<>();
	    
	    try {
	        int dayOfWeek;
	        
	        // Check if the input is a number or a day name
	        if (dayOfWeekStr.matches("\\d+")) {
	            // It's a number, parse it directly
	            dayOfWeek = Integer.parseInt(dayOfWeekStr);
	            if (dayOfWeek < 1 || dayOfWeek > 7) {
	                throw new IllegalArgumentException("Day of week must be between 1 and 7, got: " + dayOfWeek);
	            }
	        } else {
	            // It's a day name, convert to a number (1=Monday, 7=Sunday)
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
	        
	        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	        Calendar startCal = Calendar.getInstance();
	        startCal.setTime(startDate);
	        
	        Calendar endCal = Calendar.getInstance();
	        endCal.setTime(endDate);
	        
	        // Convert from Monday-Sunday (1-7) to Calendar's Sunday-Saturday (1-7)
	        int calendarDayOfWeek;
	        if (dayOfWeek == 7) {
	            calendarDayOfWeek = Calendar.SUNDAY; // 1
	        } else {
	            calendarDayOfWeek = dayOfWeek + 1; // Add 1 to convert
	        }
	        
	        // Find the first occurrence of the requested day
	        while (startCal.get(Calendar.DAY_OF_WEEK) != calendarDayOfWeek) {
	            startCal.add(Calendar.DAY_OF_MONTH, 1);
	        }
	        
	        // Generate all dates for the specified day of week within the range
	        while (!startCal.after(endCal)) {
	            dates.add(dateFormat.format(startCal.getTime()));
	            startCal.add(Calendar.DATE, 7); // Move to next week
	        }
	        
	        log.debug("Generated {} dates for day {}", dates.size(), dayOfWeekStr);
	        
	    } catch (Exception e) {
	        log.error("Error generating dates for day '{}': {}", dayOfWeekStr, e.getMessage(), e);
	        throw new RuntimeException("Error generating dates for day '" + dayOfWeekStr + "': " + e.getMessage(), e);
	    }
	    
	    return dates;
	}

	/**
	 * Parses the slot time duration from a string format (e.g., "30 mins", "1 hour") to minutes as an integer
	 * 
	 * @param slotTimeStr the slot time as string (e.g., "30 mins", "1 hour")
	 * @return the duration in minutes as an integer
	 */
	private Integer parseSlotDuration(String slotTimeStr) {
	    if (slotTimeStr == null || slotTimeStr.trim().isEmpty()) {
	        throw new IllegalArgumentException("Slot time cannot be null or empty");
	    }
	    
	    String trimmedStr = slotTimeStr.trim().toLowerCase();
	    
	    try {
	        // Extract just the numeric part
	        String numericPart = trimmedStr.replaceAll("[^0-9]", "");
	        
	        if (numericPart.isEmpty()) {
	            throw new IllegalArgumentException("No numeric value found in slot time: " + slotTimeStr);
	        }
	        
	        int duration = Integer.parseInt(numericPart);
	        
	        // If it contains "hour" or "hr", convert to minutes
	        if (trimmedStr.contains("hour") || trimmedStr.contains("hr")) {
	            duration *= 60;
	        }
	        
	        return duration;
	        
	    } catch (NumberFormatException e) {
	        log.error("Error parsing slot duration '{}': {}", slotTimeStr, e.getMessage());
	        throw new IllegalArgumentException("Invalid slot duration format: " + slotTimeStr, e);
	    }
	}

	/**
	 * Splits a time slot into intervals based on slot duration and formats time with AM/PM
	 * 
	 * @param startTime the start time in "HH:mm" format
	 * @param endTime the end time in "HH:mm" format
	 * @param slotDuration the duration of each slot in minutes
	 * @return list of time slot intervals with times in AM/PM format
	 * @throws java.text.ParseException if there's an error parsing the time strings
	 */
	private List<TimeSlotInterval> splitTimeSlot(String startTime, String endTime, Integer slotDuration) throws java.text.ParseException {
	    List<TimeSlotInterval> intervals = new ArrayList<>();
	    
	    // Parse input times (which are in 24-hour format)
	    SimpleDateFormat inputFormat = new SimpleDateFormat("HH:mm");
	    
	    // Format for output times with AM/PM
	   // SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm a");
	 // To this:
	    SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
	    outputFormat.getDateFormatSymbols().setAmPmStrings(new String[]{"AM", "PM"});
	    
	    Calendar startCal = Calendar.getInstance();
	    startCal.setTime(inputFormat.parse(startTime));
	    
	    Calendar endCal = Calendar.getInstance();
	    endCal.setTime(inputFormat.parse(endTime));
	    
	    // Generate time slots until we reach the end time
	    while (startCal.getTime().before(endCal.getTime())) {
	        // Format with AM/PM
	        String intervalStart = outputFormat.format(startCal.getTime());
	        
	        // Add slot duration minutes to start time
	        startCal.add(Calendar.MINUTE, slotDuration);
	        
	        // If exceeding end time, use end time as interval end
	        if (startCal.getTime().after(endCal.getTime())) {
	            String formattedEndTime = outputFormat.format(endCal.getTime());
	            intervals.add(new TimeSlotInterval(intervalStart, formattedEndTime));
	        } else {
	            String intervalEnd = outputFormat.format(startCal.getTime());
	            intervals.add(new TimeSlotInterval(intervalStart, intervalEnd));
	        }
	    }
	    
	    return intervals;
	}
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
	                                slot.getMedicalTestSlotId(),
	                                daySlot.getMedicalTestDaySlotId(),
	                                slotTime.getMedicalTestSlotTimeId());

	                List<Map<String, Object>> slotDatesWithSplit = new ArrayList<>();

	                for (MedicalTestSlotDate slotDate : slotDates) {
	                    Map<String, Object> slotDateMap = new HashMap<>();
	                    slotDateMap.put("date", slotDate.getDate());
	                    slotDateMap.put("isActive", slotDate.getIsActive());
	                    slotDateMap.put("id", slotDate.getMedicalTestSlotDateId());

	                    List<MedicalTestSlotSpiltTime> splitTimes = medicalTestSlotSpiltTimeRepository
	                            .findByMedicalTestSlotDate_MedicalTestSlotDateId(slotDate.getMedicalTestSlotDateId());

	                    List<Map<String, Object>> splitTimeList = new ArrayList<>();
	                    for (MedicalTestSlotSpiltTime splitTime : splitTimes) {
	                        Map<String, Object> splitMap = new HashMap<>();
	                        splitMap.put("slotStartTime", splitTime.getSlotStartTime());
	                        splitMap.put("slotEndTime", splitTime.getSlotEndTime());
	                        splitMap.put("slotStatus", splitTime.getSlotStatus());
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
	@Transactional
	public ResponseEntity<?> saveMedicalTestOvverride(MedicalTestConfigWebModel webModel) {
		 try {
		        if (webModel == null || webModel.getMedicalTestSlotTimeId() == null ||
		                webModel.getOverrideDate() == null || StringUtils.isEmpty(webModel.getNewSlotTime())) {
		            return ResponseEntity.badRequest().body(new Response(0, "error", "Missing required fields"));
		        }

		        Optional<MedicalTestSlotTime> doctorSlotTimeOpt = medicalTestSlotTimeRepository.findById(webModel.getMedicalTestSlotTimeId());
		        if (!doctorSlotTimeOpt.isPresent()) {
		            return ResponseEntity.status(HttpStatus.NOT_FOUND)
		                    .body(new Response(0, "error", "MedicalTestSlotTime not found with ID: " + webModel.getMedicalTestSlotTimeId()));
		        }

		        MedicalTestSlotTime slot = doctorSlotTimeOpt.get();
		        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);
		        LocalTime originalStart = LocalTime.parse(slot.getSlotStartTime(), formatter);
		        LocalTime originalEnd = LocalTime.parse(slot.getSlotEndTime(), formatter);

		        int durationMinutes = extractDurationInMinutes(webModel.getNewSlotTime());
		        if (durationMinutes <= 0) {
		            return ResponseEntity.badRequest().body(new Response(0, "error", "Invalid new slot time format."));
		        }

		        // Save override entry
		        MedicalTestSlotTimeOveride override = MedicalTestSlotTimeOveride.builder()
		                .originalSlot(slot)
		                .overrideDate(webModel.getOverrideDate())
		                .newSlotTime(webModel.getNewSlotTime())
		                .reason(webModel.getReason())
		                .isActive(true)
		                .build();
		        medicalTestSlotTimeOverideRepository.save(override);

		        // Find DoctorSlotDate
		        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		        String dateString = sdf.format(webModel.getOverrideDate());
		        Optional<MedicalTestSlotDate> doctorSlotDateOpt = medicalTestSlotDateRepository
		                .findByDateAndMedicalTestSlotTimeIdAndIsActive(dateString, slot.getMedicalTestSlotTimeId(), true);

		        if (doctorSlotDateOpt.isPresent()) {
		            Integer doctorSlotDateId = doctorSlotDateOpt.get().getMedicalTestSlotDateId();
		            List<MedicalTestSlotSpiltTime> existingSplitTimes = medicalTestSlotSpiltTimeRepository
		                    .findByMedicalTestSlotDate_MedicalTestSlotDateIdAndIsActive(doctorSlotDateId, true);
		            
		            Optional<MedicalTestSlotDate> db = medicalTestSlotDateRepository.findById(doctorSlotDateId);
		            if (db.isPresent()) {
		                MedicalTestSlotDate slotDate = db.get();

		            // Get current time and override date
		            LocalDate today = LocalDate.now();
		            LocalTime now = LocalTime.now();
		            LocalDate overrideLocalDate = webModel.getOverrideDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

		            // Update only future split times
		            int overriddenSlots = 0;
		            for (MedicalTestSlotSpiltTime splitTime : existingSplitTimes) {
		                LocalTime slotStart = LocalTime.parse(splitTime.getSlotStartTime(), formatter);

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
		                    LocalTime newEnd = LocalTime.parse(splitTime.getSlotEndTime(), formatter).plusMinutes(durationMinutes);

		                    MedicalTestSlotSpiltTime newSplit = MedicalTestSlotSpiltTime.builder()
		                            .slotStartTime(newStart.format(formatter))
		                            .slotEndTime(newEnd.format(formatter))
		                            
		                            .ovverridenStatus("OVERRIDDEN")
		                            .medicalTestSlotDate(slotDate)
		                            .isActive(true)
		                            .createdBy(webModel.getUpdatedBy())
		                            .createdOn(new Date())
		                            .build();

		                    medicalTestSlotSpiltTimeRepository.save(newSplit);
		                    overriddenSlots++;
		                }
		            }
		        }
		        }

//		        // Update appointments
//		        List<PatientAppointmentTable> appointments = patientAppoinmentRepository
//		                .findByAppointmentDateAndDoctorSlotTimeId(dateString, slot.getDoctorSlotTimeId());
//
//		        LocalDate today = LocalDate.now();
//		        LocalTime now = LocalTime.now();
//		        LocalDate overrideDate = webModel.getOverrideDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
//
//		        int updatedAppointments = 0;
//
//		        for (PatientAppointmentTable appointment : appointments) {
//		            LocalTime apptStart = LocalTime.parse(appointment.getSlotStartTime(), formatter);
//		            LocalTime apptEnd = LocalTime.parse(appointment.getSlotEndTime(), formatter);
//
//		            boolean shouldUpdate = true;
//
//		            if (overrideDate.isEqual(today)) {
//		                if (!apptStart.isAfter(now)) {
//		                    shouldUpdate = false;
//		                }
//		            }
//
//		            if (shouldUpdate) {
//		                appointment.setSlotStartTime(apptStart.plusMinutes(durationMinutes).format(formatter));
//		                appointment.setSlotEndTime(apptEnd.plusMinutes(durationMinutes).format(formatter));
//		                updatedAppointments++;
//		            }
//		        }
//
//		        patientAppoinmentRepository.saveAll(appointments);
//
		        return ResponseEntity.ok(new Response(1, "success",
		                "Override saved. Updated " + " appointments and " +
		                        "overridden future slots."));

		    } catch (DateTimeParseException e) {
		        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
		                .body(new Response(0, "error", "Invalid time format: " + e.getMessage()));
		    } catch (Exception e) {
		        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
		                .body(new Response(0, "error", "Error while saving override: " + e.getMessage()));
		    }
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
			        MedicalTestSlot doctorSlot = medicalTestSlotRepository.findById(medicalTestConfigWebModel.getMedicalTestSlotId()).orElseThrow(
			                () -> new RuntimeException("medicalTestSlot not found with ID: " + medicalTestConfigWebModel.getMedicalTestSlotId()));

			        List<MedicalTestDaySlot> existingDoctorDaySlots = medicalTestDaySlotRepository.findByMedicalTestSlot(doctorSlot);

			        if (!validateMedicalTestSlots(existingDoctorDaySlots, medicalTestConfigWebModel.getMedicalTestDaySlots())) {
			            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
			                    new Response(0, "Error", "Doctor slot times overlap. Please ensure slot times don't conflict."));
			        }

			        if (medicalTestConfigWebModel.getMedicalTestDaySlots() != null) {
			            for (MedicalTestDaySlotWebModel daySlotModel : medicalTestConfigWebModel.getMedicalTestDaySlots()) {
			                MedicalTestDaySlot doctorDaySlot = MedicalTestDaySlot.builder()
			                        .medicalTestSlot(doctorSlot)
			                        .day(daySlotModel.getDay())
			                        .startSlotDate(daySlotModel.getStartSlotDate())
			                        .endSlotDate(daySlotModel.getEndSlotDate())
			                        .createdBy(medicalTestConfigWebModel.getCreatedBy())
			                        .isActive(true)
			                        .build();

			                doctorDaySlot = medicalTestDaySlotRepository.save(doctorDaySlot);

			                if (daySlotModel.getMedicalTestSlotTimes() != null) {
			                    for (MedicalTestSlotTimeWebModel slotTimeModel : daySlotModel.getMedicalTestSlotTimes()) {
			                        MedicalTestSlotTime doctorSlotTime = MedicalTestSlotTime.builder()
			                                .medicalTestDaySlot(doctorDaySlot)
			                                .slotStartTime(slotTimeModel.getSlotStartTime())
			                                .slotEndTime(slotTimeModel.getSlotEndTime())
			                                .slotTime(slotTimeModel.getSlotTime())
			                                .createdBy(medicalTestConfigWebModel.getCreatedBy())
			                                .isActive(true)
			                                .build();

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
			                            		medicalTestConfigWebModel.getCreatedBy(), doctorSlot, doctorDaySlot, doctorSlotTime, startDate);

			                            createMedicalTestSlotSplitTimes(medicalTestConfigWebModel.getCreatedBy(), doctorSlotDate, slotTimeModel, timeFormatter);

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
			                                                   MedicalTestDaySlot doctorDaySlot,
			                                                   MedicalTestSlotTime doctorSlotTime, LocalDate date) {
			    MedicalTestSlotDate doctorSlotDate = MedicalTestSlotDate.builder()
			            .medicalTestSlotId(doctorSlot.getMedicalTestSlotId())
			            .medicalTestDaySlotId(doctorDaySlot.getMedicalTestDaySlotId())
			            .medicalTestSlotTimeId(doctorSlotTime.getMedicalTestSlotTimeId())
			            .date(date.toString())
			            .createdBy(createdBy)
			            .isActive(true)
			            .build();

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
			                slotTimeModel.getSlotStartTime(), slotTimeModel.getSlotEndTime(),
			                duration, doctorSlotDate.getDate());

			        int slotCount = 0;
			        LocalTime currentStart = start;

			        while (currentStart.plusMinutes(duration).compareTo(end) <= 0) {
			            LocalTime currentEnd = currentStart.plusMinutes(duration);

			            boolean exists = medicalTestSlotSpiltTimeRepository
			                    .existsBySlotStartTimeAndSlotEndTimeAndMedicalTestSlotDate_MedicalTestSlotDateId(
			                            currentStart.format(timeFormatter1),
			                            currentEnd.format(timeFormatter1),
			                            doctorSlotDate.getMedicalTestSlotDateId());

			            if (!exists) {
			                MedicalTestSlotSpiltTime splitTime = 	MedicalTestSlotSpiltTime.builder()
			                        .slotStartTime(currentStart.format(timeFormatter1))
			                        .slotEndTime(currentEnd.format(timeFormatter1))
			                        .slotStatus("Available")
			                        .createdBy(createdBy)
			                        .medicalTestSlotDate(doctorSlotDate)
			                        .isActive(true)
			                        .build();

			                medicalTestSlotSpiltTimeRepository.save(splitTime);
			                slotCount++;
			            }

			            currentStart = currentEnd;
			        }

			        log.info("Created {} split time slots for doctor slot date ID: {}",
			                slotCount, doctorSlotDate.getMedicalTestSlotDateId());

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
			private boolean validateMedicalTestSlots(List<MedicalTestDaySlot> existingSlots, List<MedicalTestDaySlotWebModel> newSlots) {
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
			        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response(0, "error", "Slot not found with ID: " + id));
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
			                .findByMedicalTestSlot_Department(department)
			                .stream()
			                .filter(slot -> isValidSlot(slot, date))
			                .map(slot -> buildMedicalTestSlot(slot, date))
			                .filter(Objects::nonNull)
			                .distinct()
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
			    return dateToConvert.toInstant()
			            .atZone(ZoneId.systemDefault())
			            .toLocalDate();
			}

			/**
			 * Checks if the medical test slot is valid for the given date
			 */
			private boolean isValidSlot(MedicalTestDaySlot testDaySlot, LocalDate requestDate) {
			    if (testDaySlot == null || requestDate == null) return false;

			    LocalDate startDate = convertToLocalDates(testDaySlot.getStartSlotDate());
			    LocalDate endDate = convertToLocalDates(testDaySlot.getEndSlotDate());

			    return Boolean.TRUE.equals(testDaySlot.getIsActive())
			            && !requestDate.isBefore(startDate)
			            && !requestDate.isAfter(endDate);
			}

			/**
			 * Builds medical test slot data structure with all related information
			 */
			private Map<String, Object> buildMedicalTestSlot(MedicalTestDaySlot daySlot, LocalDate requestDate) {
			    List<Map<String, Object>> daySlotList = medicalTestDaySlotRepository
			            .findByMedicalTestSlot(daySlot.getMedicalTestSlot())
			            .stream()
			            .filter(slot -> isValidDaySlot(slot, requestDate))
			            .map(slot -> buildDaySlot(slot, requestDate))
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
			    if (daySlot == null || requestDate == null) return false;

			    LocalDate startDate = convertToLocalDate(daySlot.getStartSlotDate());
			    LocalDate endDate = convertToLocalDate(daySlot.getEndSlotDate());
			    String requestDay = requestDate.getDayOfWeek().toString();

			    return Boolean.TRUE.equals(daySlot.getIsActive())
			            && !requestDate.isBefore(startDate)
			            && !requestDate.isAfter(endDate)
			            && requestDay.equalsIgnoreCase(daySlot.getDay());
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
			    List<Map<String, Object>> timeSlotList = medicalTestSlotTimeRepository
			            .findByMedicalTestDaySlot(daySlot)
			            .stream()
			            .filter(slotTime -> Boolean.TRUE.equals(slotTime.getIsActive()))
			            .map(slotTime -> buildSlotTime(slotTime, requestDate))
			            .collect(Collectors.toList());

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
			                    slotTime.getMedicalTestSlotTimeId(),
			                    slotTime.getMedicalTestDaySlot().getMedicalTestDaySlotId(),
			                    slotTime.getMedicalTestDaySlot().getMedicalTestSlot().getMedicalTestSlotId(),
			                    requestDate.toString(),
			                    true
			            );

			    // Map split times for this time slot
			    List<Map<String, Object>> splitTimeList = testSlotDates.stream()
			            .flatMap(slotDate -> {
			                // Use the updated repository method with proper path traversal
			                List<MedicalTestSlotSpiltTime> splitTimes = medicalTestSlotSpiltTimeRepository
			                        .findByMedicalTestSlotDate_MedicalTestSlotDateIdAndIsActive(
			                            slotDate.getMedicalTestSlotDateId(), 
			                            true
			                        );

			                return splitTimes.stream().map(split -> {
			                    Map<String, Object> splitData = new LinkedHashMap<>();
			                    splitData.put("slotSplitTimeId", split.getMedicalTestSlotSpiltTimeId());
			                    splitData.put("slotStartTime", split.getSlotStartTime());
			                    splitData.put("slotEndTime", split.getSlotEndTime());
			                    splitData.put("slotStatus", split.getSlotStatus());
			                    return splitData;
			                });
			            })
			            .collect(Collectors.toList());

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
			    List<User> doctors = userRepository.findByUserTypeAndUserIsActiveTrue("DOCTOR");
			    List<Map<String, Object>> doctorList = new ArrayList<>();

			    for (User user : doctors) {
			        Map<String, Object> doctorMap = new HashMap<>();

			        // Compose name
			        String name = (user.getFirstName() != null ? user.getFirstName() : "") +
			                      " " +
			                      (user.getLastName() != null ? user.getLastName() : "");
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

			        // Add data to map
			        doctorMap.put("userId", user.getUserId());
			        doctorMap.put("userName", name.trim());
			        doctorMap.put("specialties", specialtyNames);

			        doctorList.add(doctorMap);
			    }

			    return ResponseEntity.ok(doctorList);
			}


			}


	

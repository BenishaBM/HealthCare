package com.annular.healthCare.service.serviceImpl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

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
import com.annular.healthCare.model.DoctorSlotDate;
import com.annular.healthCare.model.DoctorSlotSpiltTime;
import com.annular.healthCare.model.DoctorSlotTime;
import com.annular.healthCare.model.MedicalTestConfig;
import com.annular.healthCare.model.MedicalTestDaySlot;
import com.annular.healthCare.model.MedicalTestSlot;
import com.annular.healthCare.model.MedicalTestSlotDate;
import com.annular.healthCare.model.MedicalTestSlotSpiltTime;
import com.annular.healthCare.model.MedicalTestSlotTime;
import com.annular.healthCare.model.MedicalTestSlotTimeOveride;
import com.annular.healthCare.repository.MedicalTestConfigRepository;
import com.annular.healthCare.repository.MedicalTestDaySlotRepository;
import com.annular.healthCare.repository.MedicalTestSlotDateRepository;
import com.annular.healthCare.repository.MedicalTestSlotRepository;
import com.annular.healthCare.repository.MedicalTestSlotSpiltTimeRepository;
import com.annular.healthCare.repository.MedicalTestSlotTimeOverideRepository;
import com.annular.healthCare.repository.MedicalTestSlotTimeRepository;
import com.annular.healthCare.service.MedicalTestConfigService;
import com.annular.healthCare.webModel.DaySlotWebModel;
import com.annular.healthCare.webModel.MedicalTestConfigWebModel;
import com.annular.healthCare.webModel.MedicalTestDaySlotWebModel;
import com.annular.healthCare.webModel.MedicalTestDto;
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
	MedicalTestDaySlotRepository medicalTestDaySlotRepository;
	
	@Autowired
	MedicalTestSlotTimeRepository medicalTestSlotTimeRepository;
	
	@Autowired
	MedicalTestSlotDateRepository medicalTestSlotDateRepository;
	
	@Autowired
	MedicalTestSlotSpiltTimeRepository medicalTestSlotSpiltTimeRepository;
	
	@Autowired
	MedicalTestSlotTimeOverideRepository medicalTestSlotTimeOverideRepository;

	@Override
	public ResponseEntity<?> saveMedicalTestName(MedicalTestConfigWebModel request) {

		    // Check if department exists, create if it doesn't
		    Department department = departmentRepository.findByName(request.getDepartment())
		            .orElse(null);
		    
		    // If department doesn't exist, create a new one
		    if (department == null) {
		        department = Department.builder()
		                .name(request.getDepartment())
		                .createdBy(request.getCreatedBy())
		                .hospitalId(request.getHospitalId())
		                .updatedBy(request.getUpdatedBy())
		                .isActive(true)
		                .build();
		        
		        department = departmentRepository.save(department);
		    }
		    
		    List<String> successfulTests = new ArrayList<>();
		    List<String> failedTests = new ArrayList<>();
		    
		    // Process each test name in the array
		    for (String testName : request.getMedicalTestNames()) {
		        try {
		            // Use a simpler query to check for existing tests
		            boolean exists = medicalTestConfigRepository.existsByHospitalIdAndMedicalTestNameAndDepartmentId(
		                request.getHospitalId(), 
		                testName, 
		                department.getId()
		            );
		            
		            if (exists) {
		                failedTests.add(testName);
		                continue;
		            }
		            
		            // Create and save the new test
		            MedicalTestConfig medicalTestConfig = MedicalTestConfig.builder()
		                    .department(department)
		                    .medicalTestName(testName)
		                    .mrp(request.getMrp())
		                    .gst(request.getGst())
		                    .createdBy(request.getCreatedBy())
		                    .updatedBy(request.getUpdatedBy())
		                    .isActive(true)
		                    .hospitalId(request.getHospitalId())
		                    .build();
		                
		            medicalTestConfigRepository.save(medicalTestConfig);
		            successfulTests.add(testName);
		        } catch (Exception e) {
		            failedTests.add(testName);
		        }
		    }
		    
		    // Prepare response message
		    String message;
		    if (failedTests.isEmpty()) {
		        message = "All medical tests saved successfully.";
		    } else if (successfulTests.isEmpty()) {
		        message = "Failed to save any medical tests. Tests that already exist or encountered errors: " 
		                + String.join(", ", failedTests);
		    } else {
		        message = "Partially successful. Saved tests: " + String.join(", ", successfulTests) 
		                + ". Failed tests: " + String.join(", ", failedTests);
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
	    SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm a");
	    
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
	@Transactional(rollbackFor = Exception.class)
	public ResponseEntity<?> saveMedicalTestOvverride(MedicalTestConfigWebModel medicalTestConfigWebModel) {
	    try {
	        // Step 1: Validate input
	        if (medicalTestConfigWebModel == null ||
	            medicalTestConfigWebModel.getMedicalTestSlotTimeId() == null ||
	            medicalTestConfigWebModel.getOverrideDate() == null ||
	            StringUtils.isEmpty(medicalTestConfigWebModel.getNewSlotTime())) {

	            return ResponseEntity.badRequest().body(new Response(0, "error", "Missing required fields"));
	        }
	       
	     

	        // Step 2: Normalize overrideDate to remove time portion
	        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	        // Step 2.1: Check the type of overrideDate and handle accordingly
	        Date overrideDate = null;

	        Object overrideDateObj = medicalTestConfigWebModel.getOverrideDate();

	        if (overrideDateObj == null) {
	            return ResponseEntity.badRequest().body(new Response(0, "error", "Override date is null"));
	        }

	        if (overrideDateObj instanceof String) {
	            // If it's a String, try to parse it to a Date
	            String dateString = (String) overrideDateObj;
	            try {
	                overrideDate = sdf.parse(dateString);
	            } catch (ParseException e) {
	                return ResponseEntity.badRequest().body(new Response(0, "error", "Invalid date string format"));
	            }
	        } else if (overrideDateObj instanceof Date) {
	            // If it's already a Date, use it directly
	            overrideDate = (Date) overrideDateObj;
	        } else {
	            // If the type is neither String nor Date, return an error
	            return ResponseEntity.badRequest().body(new Response(0, "error", "Invalid override date format"));
	        }

	        // If overrideDate is still null after parsing, return an error response
	        if (overrideDate == null) {
	            return ResponseEntity.badRequest().body(new Response(0, "error", "Invalid override date format."));
	        }

	        // Now format the overrideDate to String (without time)
	        String overrideDateOnly = sdf.format(overrideDate);

	        // Step 3: Query using normalized date and slotTimeId
	        Optional<MedicalTestSlotDate> medicalTestSlotDateOpt = medicalTestSlotDateRepository
	                .findByMedicalTestSlotTimeIdAndIsActive(
	                        medicalTestConfigWebModel.getMedicalTestSlotTimeId(),
	                        true
	                );

	        if (!medicalTestSlotDateOpt.isPresent()) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                    .body(new Response(0, "error", "No active MedicalTestSlotDate found for the given date and slot time."));
	        }

	        MedicalTestSlotDate medicalTestSlotDate = medicalTestSlotDateOpt.get();
	        Optional<MedicalTestSlotTime> date = medicalTestSlotTimeRepository.findById(medicalTestSlotDate.getMedicalTestSlotTimeId());
	        MedicalTestSlotTime db = date.get();
	        
	        // Step 6: Save override entry
	        MedicalTestSlotTimeOveride override = MedicalTestSlotTimeOveride.builder()
	                .originalSlot(db) // Assuming this is available in the model
	                .overrideDate(overrideDateOnly)  // Ensure this is passed as String
	                .newSlotTime(medicalTestConfigWebModel.getNewSlotTime())
	                .reason(medicalTestConfigWebModel.getReason())
	                .createdBy(medicalTestConfigWebModel.getUpdatedBy())
	                .isActive(true)
	                .build();
	        medicalTestSlotTimeOverideRepository.save(override);

	        // Step 7: Deactivate existing split times
	        List<MedicalTestSlotSpiltTime> existingSplitTimes = medicalTestSlotSpiltTimeRepository
	                .findByMedicalTestSlotDate_MedicalTestSlotDateIdAndIsActive(
	                        medicalTestSlotDate.getMedicalTestSlotDateId(), true);

	        for (MedicalTestSlotSpiltTime split : existingSplitTimes) {
	            split.setIsActive(false);
	            medicalTestSlotSpiltTimeRepository.save(split);
	        }

	        // Step 8: Recreate split times using new duration
	        int updatedSplitTimes = 0;
	        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);

	        for (MedicalTestSlotSpiltTime split : existingSplitTimes) {
	            LocalTime slotStart = LocalTime.parse(split.getSlotStartTime(), formatter);
	            LocalTime slotEnd = LocalTime.parse(split.getSlotEndTime(), formatter);

	            LocalTime current = slotStart;
	            LocalTime next = slotEnd;

	            // Calculate next time for the split
	            while (current.isBefore(next)) {
	                LocalTime nextSlot = current.plusMinutes(extractDurationInMinutes(medicalTestConfigWebModel.getNewSlotTime()));

	                MedicalTestSlotSpiltTime newSplit = MedicalTestSlotSpiltTime.builder()
	                        .slotStartTime(current.format(formatter))
	                        .slotEndTime(nextSlot.format(formatter))
	                        .ovverridenStatus("OVERRIDDEN")
	                        .medicalTestSlotDate(medicalTestSlotDate)
	                        .isActive(true)
	                        .createdBy(medicalTestConfigWebModel.getUpdatedBy())
	                        .createdOn(new Date())
	                        .build();

	                medicalTestSlotSpiltTimeRepository.save(newSplit);
	                updatedSplitTimes++;

	                current = nextSlot;
	            }
	        }

	        // Step 9: Return success
	        return ResponseEntity.ok(new Response(1, "success",
	                "Override saved successfully. Updated " + updatedSplitTimes + " split times."));

	    } catch (Exception e) {
	        e.printStackTrace();
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


	}

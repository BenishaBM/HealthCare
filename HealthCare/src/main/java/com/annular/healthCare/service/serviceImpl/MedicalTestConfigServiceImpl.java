package com.annular.healthCare.service.serviceImpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.annular.healthCare.Response;
import com.annular.healthCare.model.Department;
import com.annular.healthCare.model.MedicalTestConfig;
import com.annular.healthCare.repository.MedicalTestConfigRepository;
import com.annular.healthCare.service.MedicalTestConfigService;
import com.annular.healthCare.webModel.MedicalTestConfigWebModel;
import com.annular.healthCare.webModel.MedicalTestDto;

@Service
public class MedicalTestConfigServiceImpl implements MedicalTestConfigService{
	
	@Autowired
	MedicalTestConfigRepository medicalTestConfigRepository;
	
	@Autowired
	DepartmentRepository departmentRepository;

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



}

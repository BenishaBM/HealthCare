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
import com.annular.healthCare.model.MedicalTestConfig;
import com.annular.healthCare.repository.MedicalTestConfigRepository;
import com.annular.healthCare.service.MedicalTestConfigService;
import com.annular.healthCare.webModel.MedicalTestConfigWebModel;

@Service
public class MedicalTestConfigServiceImpl implements MedicalTestConfigService{
	
	@Autowired
	MedicalTestConfigRepository medicalTestConfigRepository;

	@Override
	public ResponseEntity<?> saveMedicalTestName(MedicalTestConfigWebModel medicalTestConfigWebModel) {
	    // Check if the record already exists
	    boolean exists = medicalTestConfigRepository.existsByHospitalIdAndMedicalTestNameAndDepartment(
	            medicalTestConfigWebModel.getHospitalId(),
	            medicalTestConfigWebModel.getMedicalTestName(),
	            medicalTestConfigWebModel.getDepartment()
	    );

	    if (exists) {
	        return ResponseEntity
	                .badRequest()
	                .body(new Response(0, "fail", "Medical test with same name, department, and hospital already exists."));
	    }


	    // Convert WebModel to Entity
	    MedicalTestConfig medicalTestConfig = MedicalTestConfig.builder()
	            .department(medicalTestConfigWebModel.getDepartment())
	            .medicalTestName(medicalTestConfigWebModel.getMedicalTestName())
	            .mrp(medicalTestConfigWebModel.getMrp())
	            .gst(medicalTestConfigWebModel.getGst())
	            .createdBy(medicalTestConfigWebModel.getCreatedBy())
	            .updatedBy(medicalTestConfigWebModel.getUpdatedBy())
	            .isActive(true)
	            .hospitalId(medicalTestConfigWebModel.getHospitalId())
	            .build();

	    medicalTestConfigRepository.save(medicalTestConfig);

	    return ResponseEntity.ok(new Response(1,"success","Medical test saved successfully."));
	}

	@Override
	public ResponseEntity<?> getAllMedicalTestNameByHospitalId(Integer hospitalId) {
	    try {
	        List<MedicalTestConfig> testConfigs = medicalTestConfigRepository.findByHospitalIdAndIsActiveTrue(hospitalId);

	        if (testConfigs.isEmpty()) {
	            return ResponseEntity.ok(new Response(0, "fail", "No medical tests found for the given hospital ID."));
	        }

	        List<Map<String, Object>> responseList = new ArrayList<>();

	        for (MedicalTestConfig config : testConfigs) {
	            Map<String, Object> map = new HashMap<>();
	            map.put("id", config.getId());
	            map.put("medicalTestName", config.getMedicalTestName());
	            map.put("department", config.getDepartment());
	            map.put("mrp", config.getMrp());
	            map.put("gst", config.getGst());

	            responseList.add(map);
	        }

	        return ResponseEntity.ok(new Response(1, "success", responseList));

	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(0, "error", "Something went wrong: " + e.getMessage()));
	    }
	}

	@Override
	public ResponseEntity<?> getMedicalTestNameById(Integer id) {
	    try {
	        Optional<MedicalTestConfig> optionalConfig = medicalTestConfigRepository.findById(id);

	        if (!optionalConfig.isPresent()) {
	        	return ResponseEntity.ok(new Response(0, "fail", "Medical test not found with ID: " + id));
	        }

	        MedicalTestConfig config = optionalConfig.get();

	        Map<String, Object> responseMap = new HashMap<>();
	        responseMap.put("id", config.getId());
	        responseMap.put("medicalTestName", config.getMedicalTestName());
	        responseMap.put("department", config.getDepartment());
	        responseMap.put("mrp", config.getMrp());
	        responseMap.put("gst", config.getGst());
	        responseMap.put("hospitalId", config.getHospitalId());

	        return ResponseEntity.ok(new Response(1, "success", responseMap));

	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(0, "error", "Something went wrong: " + e.getMessage()));
	    }
	}

	@Override
	public ResponseEntity<?> updateMedicalTestName(MedicalTestConfigWebModel webModel) {
	    try {
	        Optional<MedicalTestConfig> optionalConfig = medicalTestConfigRepository.findById(webModel.getId());

	        if (!optionalConfig.isPresent()) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                    .body(new Response(0, "fail", "Medical test not found with ID: " + webModel.getId()));
	        }

	        MedicalTestConfig config = optionalConfig.get();

	        // Update fields
	        config.setMedicalTestName(webModel.getMedicalTestName());
	        config.setDepartment(webModel.getDepartment());
	        config.setMrp(webModel.getMrp());
	        config.setGst(webModel.getGst());
	        config.setUpdatedBy(webModel.getUpdatedBy());
	        config.setUpdatedOn(new Date());
	        config.setHospitalId(webModel.getHospitalId());
	        config.setIsActive(true);

	        // Save the updated record
	        MedicalTestConfig updatedConfig = medicalTestConfigRepository.save(config);

	        return ResponseEntity.ok(new Response(1, "success", "updateSuccessfuly medicalTest"));

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



}

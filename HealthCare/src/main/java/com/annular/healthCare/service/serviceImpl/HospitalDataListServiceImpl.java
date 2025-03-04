package com.annular.healthCare.service.serviceImpl;

import java.io.IOException;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.annular.healthCare.Util.Base64FileUpload;
import com.annular.healthCare.Util.HealthCareConstant;
import com.annular.healthCare.model.HospitalDataList;
import com.annular.healthCare.model.MediaFile;
import com.annular.healthCare.repository.HospitalDataListRepository;
import com.annular.healthCare.repository.MediaFileRepository;
import com.annular.healthCare.service.HospitalDataListService;
import com.annular.healthCare.webModel.FileInputWebModel;
import com.annular.healthCare.webModel.HospitalDataListWebModel;
import com.annular.healthCare.Response;


@Service
public class HospitalDataListServiceImpl implements HospitalDataListService {
	
	public static final Logger logger = LoggerFactory.getLogger(HospitalDataListServiceImpl.class);
	
	@Autowired
	HospitalDataListRepository userRepository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	MediaFileRepository mediaFileRepository;
	
	@Value("${annular.app.imageLocation}")
	private String imageLocation;
	
	@Override
	public ResponseEntity<?> register(HospitalDataListWebModel userWebModel) {
	    HashMap<String, Object> response = new HashMap<>();
	    try {
	        logger.info("Register method started");

	        // Check if user already exists
	        Optional<HospitalDataList> existingUser = userRepository.findByEmailId(userWebModel.getEmailId(), userWebModel.getUserType());
	        if (existingUser.isPresent()) {
	            response.put("message", "User with this email already exists");
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	        }

	        // Create new hospital data entity
	        HospitalDataList newUser = HospitalDataList.builder()
	                .emailId(userWebModel.getEmailId())
	                .password(passwordEncoder.encode(userWebModel.getPassword())) // Encrypt password
	                .userType(userWebModel.getUserType())
	                .hospitalId(userWebModel.getHospitalId())
	                .phoneNumber(userWebModel.getPhoneNumber())
	                .userIsActive(true) // Default to active
	                .currentAddress(userWebModel.getCurrentAddress())
	                .empId(userWebModel.getEmpId())
	                .gender(userWebModel.getGender())
	                .createdBy(userWebModel.getCreatedBy())
	                .userName(userWebModel.getUserName())
	                .build();

	        // Save the hospital data list (which is the correct entity)
	        HospitalDataList savedUser = userRepository.save(newUser);

	        // Handle media file deletion if it exists
	        Optional<MediaFile> mediaFileOptional = mediaFileRepository.findByFileId(userWebModel.getFileId());
	        if (mediaFileOptional.isPresent()) {
	            MediaFile mediaFile = mediaFileOptional.get();
	            // Delete the old file from the server
	            Base64FileUpload.deleteFile(imageLocation + "/ProfilePic", mediaFile.getFileName());
	            // Delete the media record from the database
	            mediaFileRepository.deleteById(mediaFile.getFileId());
	        }

	        // Handle file uploads
	        if (userWebModel.getFilesInputWebModel() != null) {
	            handleFileUploads(savedUser, userWebModel.getFilesInputWebModel());
	        }

	        // Return successful response with user details
	        response.put("message", "User registered successfully");
	        response.put("userId", savedUser.getHospitalDataId());
	        return ResponseEntity.ok(response);

	    } catch (Exception e) {
	        logger.error("Error registering user: " + e.getMessage(), e);
	        response.put("message", "Registration failed");
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	    }
	}

	// Helper method to handle file uploads
	public void handleFileUploads(HospitalDataList user, List<FileInputWebModel> filesInputWebModel) throws IOException {
	    if (filesInputWebModel == null || filesInputWebModel.isEmpty()) {
	        return; // No files to upload
	    }

	    List<MediaFile> filesList = new ArrayList<>();
	    for (FileInputWebModel fileInput : filesInputWebModel) {
	        if (fileInput.getFileData() != null) {
	            MediaFile mediaFile = new MediaFile(); // Changed to MediaFile
	            String fileName = UUID.randomUUID().toString();

	            mediaFile.setFileName(fileName);
	            mediaFile.setFileOriginalName(fileInput.getFileName());
	            mediaFile.setFileSize(fileInput.getFileSize());
	            mediaFile.setFileType(fileInput.getFileType());
	            mediaFile.setFileDomainId(HealthCareConstant.ProfilePhoto); // Using ProfilePhoto as constant
	            mediaFile.setFileDomainReferenceId(user.getHospitalDataId()); // Set the correct reference ID
	            mediaFile.setFileIsActive(true);
	            mediaFile.setFileCreatedBy(user.getCreatedBy());

	            // Save media file to the database
	            mediaFile = mediaFileRepository.save(mediaFile);
	            filesList.add(mediaFile);

	            // Save the file to the file system
	            Base64FileUpload.saveFile(imageLocation + "/CustomerDocument", fileInput.getFileData(), fileName);
	        }
	    }
	}

	@Override
	public ResponseEntity<?> getHospitalDataByUserTypeAndHospitalId(String userType, Integer hospitalId) {
	    HashMap<String, Object> response = new HashMap<>();
	    try {
	        logger.info("Fetching hospital data for userType: " + userType + " and hospitalId: " + hospitalId);

	        // Query the repository for the matching data
	        Optional<HospitalDataList> hospitalDataOptional = userRepository.findByUserTypeAndHospitalId(userType, hospitalId);

	        // Check if data exists
	        if (!hospitalDataOptional.isPresent()) {
	           // response.put("message", "No hospital data found for the given userType and hospitalId");
	            //return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	            // You can pass an empty collection or an empty map for the 'data'
	            return ResponseEntity.ok(new Response(1, "No hospital data found for the given userType and hospitalId", new ArrayList<>()));
	        }

	        // Extract the hospital data
	        HospitalDataList hospitalData = hospitalDataOptional.get();

	        // Create a HashMap to store selected fields
	        HashMap<String, Object> data = new HashMap<>();
	        data.put("hospitalDataId", hospitalData.getHospitalDataId());
	        data.put("hospitalId", hospitalData.getHospitalId());
	        data.put("userName", hospitalData.getUserName());
	        data.put("emailId", hospitalData.getEmailId());
	        data.put("userType", hospitalData.getUserType());
	        data.put("phoneNumber", hospitalData.getPhoneNumber());
	        data.put("currentAddress", hospitalData.getCurrentAddress());
	        data.put("empId", hospitalData.getEmpId());
	        data.put("gender", hospitalData.getGender());
	        data.put("userIsActive", hospitalData.getUserIsActive());

	        // Add the data to the response map
	        response.put("data", data);
	        
	        // Return successful response with proper response object (assuming you have a Response class)
        return ResponseEntity.ok(new Response(1, "Success", response));

	    } catch (Exception e) {
	        logger.error("Error retrieving hospital data: " + e.getMessage(), e);
	        response.put("message", "Error retrieving data");
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	    }
	}


}

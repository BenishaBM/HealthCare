package com.annular.healthCare.service.serviceImpl;

import java.util.HashMap;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.annular.healthCare.model.HospitalDataList;
import com.annular.healthCare.model.MediaFile;
import com.annular.healthCare.repository.HospitalDataListRepository;
import com.annular.healthCare.repository.MediaFileRepository;
import com.annular.healthCare.service.HospitalDataListService;
import com.annular.healthCare.webModel.HospitalDataListWebModel;


@Service
public class HospitalDataListServiceImpl implements HospitalDataListService {
	
	public static final Logger logger = LoggerFactory.getLogger(HospitalDataListServiceImpl.class);
	
	@Autowired
	HospitalDataListRepository userRepository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	MediaFileRepository mediaFileRepository;
	

	@Override
	public ResponseEntity<?> register(HospitalDataListWebModel userWebModel) {
		return null;
//	    HashMap<String, Object> response = new HashMap<>();
//	    try {
//	        logger.info("Register method started");
//
//	        // Check if user already exists
//	        Optional<HospitalDataList> existingUser = userRepository.findByEmailId(userWebModel.getEmailId(), userWebModel.getUserType());
//	        if (existingUser.isPresent()) {
//	            response.put("message", "User with this email already exists");
//	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//	        }
//
//	        // Create new hospital data entity
//	        HospitalDataList newUser = HospitalDataList.builder()
//	                .emailId(userWebModel.getEmailId())
//	                .password(passwordEncoder.encode(userWebModel.getPassword())) // Encrypt password
//	                .userType(userWebModel.getUserType())
//	                .hospitalId(userWebModel.getHospitalId())
//	                .phoneNumber(userWebModel.getPhoneNumber())
//	                .userIsActive(true) // Default to active
//	                .currentAddress(userWebModel.getCurrentAddress())
//	                .empId(userWebModel.getEmpId())
//	                .gender(userWebModel.getGender())
//	                .createdBy(userWebModel.getCreatedBy())
//	                .userName(userWebModel.getUserName())
//	                .build();
//
//	        // Save the hospital data list (which is the correct entity)
//	        HospitalDataList savedUser = userRepository.save(newUser);
//
//	        // Handle media file deletion if it exists
//	        Optional<MediaFile> mediaFileOptional = mediaFileRepository.findByFileId(userWebModel.getFileId());
//	        if (mediaFileOptional.isPresent()) {
//	            MediaFile mediaFile = mediaFileOptional.get();
//	            Base64FileUpload.deleteFile(imageLocation + "/CustomerDocument", mediaFile.getFileName());
//	            mediaFilesRepository.deleteById(mediaFile.getFileId());
//	        }
//
//	        // Handle file uploads
//	        if (userWebModel.getFilesInputWebModel() != null) {
//	            handleFileUploads(savedUser, userWebModel.getFilesInputWebModel());
//	        }
//
//	        response.put("message", "User registered successfully");
//	        response.put("userId", savedUser.getHospitalDataId());
//	        return ResponseEntity.ok(response);
//
//	    } catch (Exception e) {
//	        logger.error("Error registering user: " + e.getMessage(), e);
//	        response.put("message", "Registration failed");
//	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//	    }
//	}
//
//	// Helper method to handle file uploads
//	public void handleFileUploads(HospitalDataList user, List<FileInputWebModel> filesInputWebModel) throws IOException {
//	    if (filesInputWebModel == null || filesInputWebModel.isEmpty()) {
//	        return; // No files to upload
//	    }
//
//	    List<MediaFiles> filesList = new ArrayList<>();
//	    for (FileInputWebModel fileInput : filesInputWebModel) {
//	        if (fileInput.getFileData() != null) {
//	            MediaFiles mediaFiles = new MediaFiles();
//	            String fileName = UUID.randomUUID().toString();
//
//	            mediaFiles.setFileName(fileName);
//	            mediaFiles.setFileOriginalName(fileInput.getFileName());
//	            mediaFiles.setFileSize(fileInput.getFileSize());
//	            mediaFiles.setFileType(fileInput.getFileType());
//	            mediaFiles.setFileDomainId(HotelConstant.CUSTOMERDOCUMENT);
//	            mediaFiles.setFileDomainReferenceId(user.getHospitalDataId()); // Set the correct reference ID
//	            mediaFiles.setFileIsActive(true);
//	            mediaFiles.setFileCreatedBy(user.getCreatedBy());
//
//	            // Save media file to the database
//	            mediaFiles = mediaFilesRepository.save(mediaFiles);
//	            filesList.add(mediaFiles);
//
//	            // Save file to the file system
//	            Base64FileUpload.saveFile(imageLocation + "/CustomerDocument", fileInput.getFileData(), fileName);
//	        }
//	    }
	}


}

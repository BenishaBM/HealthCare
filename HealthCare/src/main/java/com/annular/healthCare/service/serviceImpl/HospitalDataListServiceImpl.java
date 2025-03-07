package com.annular.healthCare.service.serviceImpl;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Date;
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
import com.annular.healthCare.model.DoctorSpecialty;
import com.annular.healthCare.model.HospitalAdmin;
import com.annular.healthCare.model.HospitalDataList;
import com.annular.healthCare.model.MediaFile;
import com.annular.healthCare.model.User;
import com.annular.healthCare.repository.DoctorRoleRepository;
import com.annular.healthCare.repository.DoctorSpecialityRepository;
import com.annular.healthCare.repository.HospitalAdminRepository;
import com.annular.healthCare.repository.HospitalDataListRepository;
import com.annular.healthCare.repository.MediaFileRepository;
import com.annular.healthCare.repository.UserRepository;
import com.annular.healthCare.service.HospitalDataListService;
import com.annular.healthCare.webModel.FileInputWebModel;
import com.annular.healthCare.webModel.HospitalAdminWebModel;
import com.annular.healthCare.model.DoctorRole;
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
	DoctorRoleRepository doctorRoleRepository;
	
	@Autowired
	HospitalDataListRepository hospitalDataListRepository;

	@Autowired
	DoctorSpecialityRepository doctorSpecialityRepository;

	@Autowired
	UserRepository usersRepository;

	@Autowired
	MediaFileRepository mediaFileRepository;
	
	@Autowired
	HospitalAdminRepository hospitalAdminRepository;
	

	@Value("${annular.app.imageLocation}")
	private String imageLocation;

	@Override
	public ResponseEntity<?> register(HospitalDataListWebModel userWebModel) {
	    HashMap<String, Object> response = new HashMap<>();
	    try {
	        logger.info("Register method started");

	        // Check if the hospital already exists based on the hospital name
	        Optional<HospitalDataList> existingHospital = userRepository.findByHospitalName(userWebModel.getHospitalName());
	        if (existingHospital.isPresent()) {
	            response.put("message", "Hospital with this name already exists");
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	        }

	        // Create a new hospital data entity (hospital)
	        HospitalDataList newHospitalData = HospitalDataList.builder()
	                .emailId(userWebModel.getEmailId())
	                .phoneNumber(userWebModel.getPhoneNumber())
	                .userIsActive(true) // Default to active
	                .currentAddress(userWebModel.getCurrentAddress())
	                .createdBy(userWebModel.getCreatedBy())
	                .hospitalName(userWebModel.getHospitalName()) // Set the hospital name here
	                .build();

	        // Save the hospital data list (hospital)
	        HospitalDataList savedHospitalData = userRepository.save(newHospitalData);

	        // Handle file uploads (e.g., hospital logo)
	        if (userWebModel.getFilesInputWebModel() != null) {
	            handleFileUploads(savedHospitalData, userWebModel.getFilesInputWebModel());
	        }
	     // Register multiple admins if provided
	        if (userWebModel.getAdmins() != null && !userWebModel.getAdmins().isEmpty()) {
	            for (HospitalAdminWebModel adminWebModel : userWebModel.getAdmins()) {
	                // Create a new admin entity for each admin
	                HospitalAdmin newAdmin = HospitalAdmin.builder()
	                        .userIsActive(true) // Default to active
	                        .adminUserId(userWebModel.getUserId()) // The user registering the hospital
	                        .createdBy(userWebModel.getCreatedBy())
	                        .hospitalDataList(savedHospitalData) // Associate the admin with the newly created hospital
	                        .build();

	                // Save the admin in the database
	                hospitalAdminRepository.save(newAdmin);
	            }
	        }

	        // Prepare the response with successful registration details
//	        response.put("message", "Hospital registered successfully with admins");
//	        response.put("hospitalId", savedHospitalData.getHospitalDataId());
	       // return ResponseEntity.ok(response);
	        return ResponseEntity.ok(new Response(1, "success", "Hospital registered successfully with admins"));


	    } catch (Exception e) {
	        logger.error("Error registering hospital: " + e.getMessage(), e);
	        response.put("message", "Registration failed");
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	    }
	}

	// Helper method to handle file uploads (hospital logo)
	public void handleFileUploads(HospitalDataList hospitalData, List<FileInputWebModel> filesInputWebModel)
	        throws IOException {
	    if (filesInputWebModel == null || filesInputWebModel.isEmpty()) {
	        return; // No files to upload
	    }

	    List<MediaFile> filesList = new ArrayList<>();
	    for (FileInputWebModel fileInput : filesInputWebModel) {
	        if (fileInput.getFileData() != null) {
	            // Create a new MediaFile instance for each file
	            MediaFile mediaFile = new MediaFile();
	            String fileName = UUID.randomUUID().toString(); // Generate a unique file name for each file

	            // Set properties of the media file
	            mediaFile.setFileName(fileName);
	            mediaFile.setFileOriginalName(fileInput.getFileName());
	            mediaFile.setFileSize(fileInput.getFileSize());
	            mediaFile.setFileType(fileInput.getFileType());
	            mediaFile.setFileDomainId(HealthCareConstant.hospitalLogo); // This constant can be changed to represent logo files
	            mediaFile.setFileDomainReferenceId(hospitalData.getHospitalDataId()); // Set the hospital ID reference
	            mediaFile.setFileIsActive(true);
	            mediaFile.setFileCreatedBy(hospitalData.getCreatedBy());

	            // Save media file to the database
	            mediaFile = mediaFileRepository.save(mediaFile);
	            filesList.add(mediaFile);

	            // Save the file to the file system
	            Base64FileUpload.saveFile(imageLocation + "/hospitalLogo", fileInput.getFileData(), fileName);
	        }
	    }
	}


	@Override
	public ResponseEntity<?> getHospitalDataByUserTypeAndHospitalId(String userType, Integer hospitalId) {
		HashMap<String, Object> response = new HashMap<>();
		try {
			logger.info("Fetching hospital data for userType: " + userType + " and hospitalId: " + hospitalId);

//	        // Query the repository for the matching data
//	        List<HospitalDataList> hospitalDataList = userRepository.findByUserTypeAndHospitalId(userType, hospitalId);

//	        // Check if data exists
//	        if (hospitalDataList.isEmpty()) {
//	            return ResponseEntity.ok(new Response(1, "No hospital data found for the given userType and hospitalId", new ArrayList<>()));
//	        }

			// Extract the hospital data
			List<HashMap<String, Object>> dataList = new ArrayList<>();
//	        for (HospitalDataList hospitalData : hospitalDataList) {
//	            HashMap<String, Object> data = new HashMap<>();
//	            data.put("hospitalDataId", hospitalData.getHospitalDataId());
//	            data.put("hospitalId", hospitalData.getHospitalId());
//	            data.put("userName", hospitalData.getUserName());
//	            data.put("emailId", hospitalData.getEmailId());
//	            data.put("userType", hospitalData.getUserType());
//	            data.put("phoneNumber", hospitalData.getPhoneNumber());
//	            data.put("currentAddress", hospitalData.getCurrentAddress());
//	            data.put("empId", hospitalData.getEmpId());
//	            data.put("gender", hospitalData.getGender());
//	            data.put("userIsActive", hospitalData.getUserIsActive());
//
//	            // Add the data to the list
//	            dataList.add(data);
//	        }
//
//	        // Add the data to the response map
//	        response.put("data", dataList);

			return ResponseEntity.ok(new Response(1, "Success", response));

		} catch (Exception e) {
			logger.error("Error retrieving hospital data: " + e.getMessage(), e);
			response.put("message", "Error retrieving data");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@Override
	public ResponseEntity<?> getHospitalDataByUserId(Integer hospitalDataId) {
		HashMap<String, Object> response = new HashMap<>();
		try {
			logger.info("Fetching hospital data for hospitalId: " + hospitalDataId);

			// Attempt to retrieve the hospital data by hospitalId
			Optional<HospitalDataList> hospitalDataOptional = userRepository.findByHospitalDataId(hospitalDataId);

			// Check if the hospital data is present
			if (hospitalDataOptional.isPresent()) {
				HospitalDataList hospitalData = hospitalDataOptional.get();
				// Create a HashMap for userDetails and map individual fields
				HashMap<String, Object> userDetails = new HashMap<>();
				userDetails.put("hospitalDataId", hospitalData.getHospitalDataId());
	            userDetails.put("emailId", hospitalData.getEmailId());
	            userDetails.put("phoneNumber", hospitalData.getPhoneNumber());
                userDetails.put("userIsActive", hospitalData.getUserIsActive());
                userDetails.put("currentAddress", hospitalData.getCurrentAddress());
	            userDetails.put("createdBy", hospitalData.getCreatedBy());
	            userDetails.put("userCreatedOn", hospitalData.getUserCreatedOn());
	            userDetails.put("userUpdatedBy", hospitalData.getUserUpdatedBy());
	            userDetails.put("userUpdatedOn", hospitalData.getUserUpdatedOn());

				// Retrieve media files associated with the hospital data (Profile Photo)
				List<MediaFile> files = mediaFileRepository.findByFileDomainIdAndFileDomainReferenceId(
						HealthCareConstant.hospitalLogo, hospitalData.getHospitalDataId());

				// Prepare the list of FileInputWebModel from retrieved media files
				ArrayList<FileInputWebModel> filesInputWebModel = new ArrayList<>();

				for (MediaFile mediaFile : files) {
					FileInputWebModel filesInput = new FileInputWebModel();
					filesInput.setFileName(mediaFile.getFileOriginalName());
					filesInput.setFileId(mediaFile.getFileId());
					filesInput.setFileSize(mediaFile.getFileSize());
					filesInput.setFileType(mediaFile.getFileType());

					String fileData = Base64FileUpload.encodeToBase64String(imageLocation + "/hospitalLogo",
							mediaFile.getFileName());
					filesInput.setFileData(fileData);

					filesInputWebModel.add(filesInput);
				}

				// Prepare the response map with hospital data, media files, and specialties
				HashMap<String, Object> responseMap = new HashMap<>();
				responseMap.put("userDetails", userDetails);
				responseMap.put("mediaFiles", filesInputWebModel);

				// Return successful response with hospital data and associated media files
				return ResponseEntity.ok(new Response(1, "Hospital data retrieved successfully", responseMap));
			} else {
				// Return a not found response if the hospital data is not found
				return ResponseEntity.badRequest().body(new Response(0, "Fail", "Hospital data not found"));
			}

		} catch (Exception e) {
			logger.error("Error retrieving hospital data: " + e.getMessage(), e);
			response.put("message", "Error retrieving data");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@Override
	public ResponseEntity<?> updateHospitalDataByUserId(HospitalDataListWebModel userWebModel) {
		HashMap<String, Object> response = new HashMap<>();
		try {
			logger.info("Updating hospital data for hospitalDataId: " + userWebModel.getHospitalDataId());

			// Step 1: Retrieve the existing hospital data by hospitalDataId
			Optional<HospitalDataList> hospitalDataOptional = userRepository.findById(userWebModel.getHospitalDataId());

			// Check if the hospital data exists
			if (!hospitalDataOptional.isPresent()) {
				response.put("message", "Hospital data not found");
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new Response(0, "Fail", "Hospital data not found"));
			}

			// Step 2: Get the existing hospital data entity
			HospitalDataList existingHospitalData = hospitalDataOptional.get();

			// Step 3: Update only the fields that are provided (non-null values)
//	        if (userWebModel.getUserName() != null) {
//	            existingHospitalData.setUserName(userWebModel.getUserName());
//	        }
			if (userWebModel.getEmailId() != null) {
				existingHospitalData.setEmailId(userWebModel.getEmailId());
			}
			if (userWebModel.getPhoneNumber() != null) {
				existingHospitalData.setPhoneNumber(userWebModel.getPhoneNumber());
			}
			if (userWebModel.getUserIsActive() != null) {
				existingHospitalData.setUserIsActive(userWebModel.getUserIsActive());
			}
			if (userWebModel.getCurrentAddress() != null) {
				existingHospitalData.setCurrentAddress(userWebModel.getCurrentAddress());
			}

			// Assuming the updater's ID is passed in 'createdBy' (could be renamed to
			// 'updatedBy')
			if (userWebModel.getCreatedBy() != null) {
				existingHospitalData.setUserUpdatedBy(userWebModel.getCreatedBy()); // Assuming 'createdBy' is the user
																					// who is updating
			}

			// Update the 'userUpdatedOn' field to current time
			existingHospitalData.setUserUpdatedOn(new Date());

			// Step 4: Save the updated hospital data entity back to the database
			HospitalDataList updatedHospitalData = userRepository.save(existingHospitalData);

			// Step 5: Handle media file deletion if it exists
			if (userWebModel.getFileId() != null) {
				Optional<MediaFile> mediaFileOptional = mediaFileRepository.findByFileId(userWebModel.getFileId());
				if (mediaFileOptional.isPresent()) {
					MediaFile mediaFile = mediaFileOptional.get();
					// Delete the old file from the server
					Base64FileUpload.deleteFile(imageLocation + "/ProfilePic", mediaFile.getFileName());
					// Delete the media record from the database
					mediaFileRepository.deleteById(mediaFile.getFileId());
				}
			}

			// Step 6: Handle file uploads (if any)
			if (userWebModel.getFilesInputWebModel() != null && !userWebModel.getFilesInputWebModel().isEmpty()) {
				// Handle file uploads here
				handleFileUploads(updatedHospitalData, userWebModel.getFilesInputWebModel());
			}

			// Step 7: Prepare and return the success response
			response.put("message", "Hospital data updated successfully");
			response.put("data", updatedHospitalData);

			return ResponseEntity.ok(new Response(1, "Hospital data updated successfully", response));

		} catch (Exception e) {
			logger.error("Error updating hospital data: " + e.getMessage(), e);
			response.put("message", "Error updating hospital data");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new Response(0, "Fail", "Error updating hospital data"));
		}
	}

	@Override
	public ResponseEntity<?> deleteHospitalDataByUserId(Integer hospitalDataId) {
		HashMap<String, Object> response = new HashMap<>();
		try {
			logger.info("Soft deleting hospital data for hospitalDataId: " + hospitalDataId);

			// Step 1: Retrieve the existing hospital data by hospitalDataId
			Optional<HospitalDataList> hospitalDataOptional = userRepository.findById(hospitalDataId);

			// Step 2: Check if hospital data exists
			if (!hospitalDataOptional.isPresent()) {
				response.put("message", "Hospital data not found");
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
			}

			// Step 3: Get the existing hospital data entity
			HospitalDataList existingHospitalData = hospitalDataOptional.get();

			// Step 4: Set the userIsActive flag to false for soft delete
			existingHospitalData.setUserIsActive(false); // Soft delete by marking user as inactive
			existingHospitalData.setUserUpdatedBy(existingHospitalData.getCreatedBy()); // Set the updated by user
			existingHospitalData.setUserUpdatedOn(new Date()); // Set the updated time to current time

			// Step 5: Save the updated hospital data entity back to the database
			HospitalDataList updatedHospitalData = userRepository.save(existingHospitalData);

			// Step 6: Handle associated media files
			List<MediaFile> mediaFiles = mediaFileRepository
					.findByFileDomainReferenceId(existingHospitalData.getHospitalDataId());

			if (!mediaFiles.isEmpty()) {
				for (MediaFile mediaFile : mediaFiles) {
					// Delete the file from the server
					String filePath = imageLocation + "/ProfilePic/" + mediaFile.getFileName();
					Base64FileUpload.deleteFile(filePath, mediaFile.getFileName());

					// Delete the media file record from the database
					mediaFileRepository.deleteById(mediaFile.getFileId());
				}
			}

			// Step 7: Prepare the success response
			response.put("message", "Hospital data and associated media files soft deleted successfully");
			response.put("data", updatedHospitalData);

			return ResponseEntity.ok(
					new Response(1, "Success", "Hospital data and associated media files soft deleted successfully"));

		} catch (Exception e) {
			logger.error("Error soft deleting hospital data and media files: " + e.getMessage(), e);
			response.put("message", "Error soft deleting hospital data and media files");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@Override
	public ResponseEntity<?> getByHopitalName() {
		HashMap<String, Object> response = new HashMap<>();
		try {
			// Fetch users with userType = "HOSPITAL"
			List<HospitalDataList> users = hospitalDataListRepository.findByData();

			// Check if no users were found
			if (users.isEmpty()) {
				response.put("message", "No hospitals found.");
				return ResponseEntity.ok(new Response(1, "No hospitals found.", new ArrayList<>())); // Return empty
																										// list on
																										// success
			}

			// Create a list to store the hospital ID and name
			List<HashMap<String, Object>> hospitalDataList = new ArrayList<>();

			// Extract userId (ID) and hospitalName from each user and add to the list
			for (HospitalDataList user : users) {
				HashMap<String, Object> hospitalData = new HashMap<>();
				hospitalData.put("id", user.getHospitalDataId());
				hospitalData.put("emailId", user.getEmailId());
				hospitalData.put("phoneNumber", user.getPhoneNumber());
				hospitalData.put("currentAddress", user.getCurrentAddress());
				hospitalData.put("isActive", user.getUserIsActive());
				hospitalData.put("hospitalName", user.getHospitalName());
				// hospitalData.put("hospitalName", user.getHospitalName());
				hospitalDataList.add(hospitalData);
			}

			// Return the response
			return ResponseEntity.ok(new Response(1, "Hospitals retrieved successfully.", hospitalDataList));

		} catch (Exception e) {
			logger.error("Error fetching hospitals: " + e.getMessage(), e);
			response.put("message", "Error retrieving hospitals.");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@Override
	public ResponseEntity<?> getByDoctorSpeciallity() {
		HashMap<String, Object> response = new HashMap<>();
		try {
			// Fetch all doctor specialties
			List<DoctorSpecialty> specialties = doctorSpecialityRepository.findAll();

			// Check if no specialties were found
			if (specialties.isEmpty()) {
				response.put("message", "No specialties found.");
				return ResponseEntity.ok(new Response(1, "No specialties found.", new ArrayList<>())); // Return empty
																										// list on
																										// success
			}

			// Create a list to store the specialty ID and name
			List<HashMap<String, Object>> specialtyDataList = new ArrayList<>();

			// Extract specialtyId and specialtyName from each DoctorSpecialty and add to
			// the list
			for (DoctorSpecialty specialty : specialties) {
				HashMap<String, Object> specialtyData = new HashMap<>();
				specialtyData.put("id", specialty.getDoctorSpecialtiesId());
				specialtyData.put("specialtyName", specialty.getSpecialtyName());
				specialtyDataList.add(specialtyData);
			}

			// Return the response
			return ResponseEntity.ok(new Response(1, "Specialties retrieved successfully.", specialtyDataList));

		} catch (Exception e) {
			logger.error("Error fetching specialties: " + e.getMessage(), e);
			response.put("message", "Error retrieving specialties.");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

}

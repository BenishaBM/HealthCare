package com.annular.healthCare.service.serviceImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.annular.healthCare.Response;
import com.annular.healthCare.Util.Base64FileUpload;
import com.annular.healthCare.Util.HealthCareConstant;
import com.annular.healthCare.model.HospitalAdmin;
import com.annular.healthCare.model.HospitalDataList;
import com.annular.healthCare.model.MediaFile;
import com.annular.healthCare.model.RefreshToken;
import com.annular.healthCare.model.User;
import com.annular.healthCare.repository.HospitalAdminRepository;
import com.annular.healthCare.repository.MediaFileRepository;
import com.annular.healthCare.repository.RefreshTokenRepository;
import com.annular.healthCare.repository.UserRepository;
import com.annular.healthCare.service.AuthService;
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
	RefreshTokenRepository refreshTokenRepository;
	
	@Autowired
	MediaFileRepository mediaFileRepository;
	
	@Autowired
	HospitalAdminRepository hospitalAdminRepository;
	
	@Value("${annular.app.imageLocation}")
	private String imageLocation;

	@Override
	public ResponseEntity<?> register(UserWebModel userWebModel) {
		HashMap<String, Object> response = new HashMap<>();
		try {
			logger.info("Register method start");

			// Check if a user with the same emailId, userType, and hospitalId already
			// exists
			Optional<User> existingUser = userRepository.findByEmailIdAndUserTypeAndHospitalId(
					userWebModel.getEmailId(), userWebModel.getUserType(), userWebModel.getHospitalId());

			if (existingUser.isPresent()) {
				response.put("message", "User with this email, user type, and hospital ID already exists");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
			}

			// Create new user entity
			User newUser = User.builder().emailId(userWebModel.getEmailId()).firstName(userWebModel.getFirstName())
					.lastName(userWebModel.getLastName()).password(passwordEncoder.encode(userWebModel.getPassword())) // Encrypt
																														// password
					.userType(userWebModel.getUserType()).phoneNumber(userWebModel.getPhoneNumber()).userIsActive(true) // Default
																														// active
					.currentAddress(userWebModel.getCurrentAddress()).empId(userWebModel.getEmpId())
					.gender(userWebModel.getGender()).createdBy(userWebModel.getCreatedBy())
					.userName(userWebModel.getFirstName() + " " + userWebModel.getLastName()) // Concatenate first name
																								// and last name
					.hospitalId(userWebModel.getHospitalId()) // Assuming `hospitalId` exists in the User entity
					.build();

			// Save user
			User savedUser = userRepository.save(newUser);
	        // Handle file uploads (e.g., hospital logo)
	        if (userWebModel.getFilesInputWebModel() != null) {
	            handleFileUploads(newUser, userWebModel.getFilesInputWebModel());
	        }

			return ResponseEntity.ok(new Response(1, "success", "User registered successfully"));

		} catch (Exception e) {
			logger.error("Error registering user: " + e.getMessage(), e);
			response.put("message", "Registration failed");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}
	
	// Helper method to handle file uploads (hospital logo)
		public void handleFileUploads(User hospitalData, List<FileInputWebModel> filesInputWebModel)
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
					mediaFile.setFileDomainId(HealthCareConstant.ProfilePhoto); // This constant can be changed to represent
																				// logo files
					mediaFile.setFileDomainReferenceId(hospitalData.getUserId()); // Set the hospital ID reference
					mediaFile.setFileIsActive(true);
					mediaFile.setFileCreatedBy(hospitalData.getCreatedBy());

					// Save media file to the database
					mediaFile = mediaFileRepository.save(mediaFile);
					filesList.add(mediaFile);

					// Save the file to the file system
					Base64FileUpload.saveFile(imageLocation + "/profilePhoto", fileInput.getFileData(), fileName);
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
	public ResponseEntity<Response> getUserDetailsByUserType(String userType) {
		logger.info("Fetching user details for userType: {}", userType);

		List<User> usersList = userRepository.findByUserType(userType);

		if (usersList.isEmpty()) {
			logger.warn("No users found for userType: {}", userType);
			return ResponseEntity.ok(new Response(0, "No user found for given userType", new ArrayList<>()));
		}

		List<Map<String, Object>> usersResponseList = usersList.stream().map(user -> {
			Map<String, Object> userMap = new HashMap<>();
			userMap.put("userId", user.getUserId());
			userMap.put("userName", user.getUserName());
			userMap.put("emailId", user.getEmailId());
			userMap.put("phoneNumber", user.getPhoneNumber());
			userMap.put("address", user.getCurrentAddress());
			userMap.put("userType", user.getUserType());
			userMap.put("userIsActive", user.getUserIsActive());
			userMap.put("empId", user.getEmpId());
			userMap.put("gender", user.getGender());

			return userMap;
		}).collect(Collectors.toList());

		HashMap<String, Object> responseMap = new HashMap<>();
		responseMap.put("users", usersResponseList);

		logger.info("Users retrieved successfully for userType: {}", userType);
		return ResponseEntity.ok(new Response(1, "Users retrieved successfully", responseMap));
	}
	@Override
	public ResponseEntity<?> getDropDownByUserTypeByHospitalId() {
	    try {
	        // Fetch users with userType = "ADMIN" and hospitalId is null
	        List<User> admins = userRepository.findByUserTypeAndHospitalIdIsNull("ADMIN");

	        // Extract only userId and userName
	        List<Map<String, Object>> adminList = admins.stream()
	            .map(admin -> {
	                Map<String, Object> userMap = new HashMap<>();
	                userMap.put("userId", admin.getUserId());
	                userMap.put("userName", admin.getUserName());
	                return userMap;
	            })
	            .collect(Collectors.toList());

	        // Prepare response using HashMap
	        Map<String, Object> response = new HashMap<>();
	        //response.put("admins", adminList.isEmpty() ? new ArrayList<>() : adminList); // Ensure empty array instead of null

	        return ResponseEntity.ok(new Response (1,"success",adminList.isEmpty() ? new ArrayList<>() : adminList));
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
	        // Log the incoming update request
	        logger.info("Updating user details for userId: " + userWebModel.getUserId());

	        // Step 1: Retrieve the existing user by userId
	        Optional<User> userOptional = userRepository.findById(userWebModel.getUserId());

	        // Step 2: Check if the user exists
	        if (!userOptional.isPresent()) {
	            response.put("message", "User not found");
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                    .body(new Response(0, "Fail", "User not found"));
	        }

	        // Step 3: Get the existing user entity
	        User existingUser = userOptional.get();

	        // Step 4: Update only the fields that are provided (non-null values)
	        if (userWebModel.getEmailId() != null) {
	            existingUser.setEmailId(userWebModel.getEmailId());
	        }
	        if (userWebModel.getFirstName() != null) {
	            existingUser.setFirstName(userWebModel.getFirstName());
	        }
	        
	        // Set the password only if it's provided and non-empty
	        if (userWebModel.getPassword() != null && !userWebModel.getPassword().isEmpty()) {
	            existingUser.setPassword(passwordEncoder.encode(userWebModel.getPassword()));
	        }
	        
	        if (userWebModel.getLastName() != null) {
	            existingUser.setLastName(userWebModel.getLastName());
	        }
	        if (userWebModel.getPhoneNumber() != null) {
	            existingUser.setPhoneNumber(userWebModel.getPhoneNumber());
	        }
	        if (userWebModel.getCurrentAddress() != null) {
	            existingUser.setCurrentAddress(userWebModel.getCurrentAddress());
	        }
	        if (userWebModel.getUserUpdatedBy() != null) {
	            existingUser.setUserUpdatedBy(userWebModel.getCreatedBy()); // Assuming 'createdBy' in the web model is the user making the update
	        }

	        // Update the 'userUpdatedOn' field to current time
	        existingUser.setUserUpdatedOn(new Date());
	        
	        // Merge firstName and lastName into userName
	        String fullName = (existingUser.getFirstName() != null ? existingUser.getFirstName() : "") + 
	                          " " + 
	                          (existingUser.getLastName() != null ? existingUser.getLastName() : "");
	        existingUser.setUserName(fullName.trim()); // Trim to remove extra spaces

	        // Step 5: Save the updated user entity back to the database
	        User updatedUser = userRepository.save(existingUser);

	        // Step 6: Prepare and return the success response
	        response.put("message", "User details updated successfully");
	        response.put("data", updatedUser);

	        return ResponseEntity.ok(new Response(1, "success", "User details updated successfully"));

	    } catch (Exception e) {
	        logger.error("Error updating user details: " + e.getMessage(), e);
	        response.put("message", "Error updating user details");
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
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                    .body(new Response(0, "Fail", "User not found"));
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
	            admin.setUserIsActive(false);  // Set admin as inactive
	            admin.setUserUpdatedOn(new Date());  // Update the timestamp for admin
	            hospitalAdminRepository.save(admin);  // Save the updated admin record
	        }

	        // Step 7: Return success response
	        response.put("message", "User and associated HospitalAdmin soft deleted successfully");
	        response.put("data", updatedUser);

	        return ResponseEntity.ok(new Response(1, "success", "deleted successfully"));

	    } catch (Exception e) {
	        logger.error("Error soft deleting user and HospitalAdmin details: " + e.getMessage(), e);
	        response.put("message", "Error soft deleting user and HospitalAdmin details");
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(0, "Fail", "Error soft deleting user and HospitalAdmin details"));
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
	        hospitalAdmin.setUserIsActive(false);  // Mark the admin as inactive
	        hospitalAdmin.setUserUpdatedOn(new Date());  // Update the timestamp of when it was deactivated

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


}

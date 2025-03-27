package com.annular.healthCare.service.serviceImpl;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.annular.healthCare.Response;
import com.annular.healthCare.Util.Base64FileUpload;
import com.annular.healthCare.Util.HealthCareConstant;
import com.annular.healthCare.model.DoctorDaySlot;
import com.annular.healthCare.model.DoctorLeaveList;
import com.annular.healthCare.model.DoctorRole;
import com.annular.healthCare.model.DoctorSlot;
import com.annular.healthCare.model.DoctorSlotTime;
import com.annular.healthCare.model.DoctorSpecialty;
import com.annular.healthCare.model.HospitalAdmin;
import com.annular.healthCare.model.HospitalDataList;
import com.annular.healthCare.model.MediaFile;
import com.annular.healthCare.model.MediaFileCategory;
import com.annular.healthCare.model.RefreshToken;
import com.annular.healthCare.model.User;
import com.annular.healthCare.repository.DoctorDaySlotRepository;
import com.annular.healthCare.repository.DoctorLeaveListRepository;
import com.annular.healthCare.repository.DoctorRoleRepository;
import com.annular.healthCare.repository.DoctorSlotRepository;
import com.annular.healthCare.repository.DoctorSlotTimeRepository;
import com.annular.healthCare.repository.DoctorSpecialityRepository;
import com.annular.healthCare.repository.HospitalAdminRepository;
import com.annular.healthCare.repository.HospitalDataListRepository;
import com.annular.healthCare.repository.MediaFileRepository;
import com.annular.healthCare.repository.RefreshTokenRepository;
import com.annular.healthCare.repository.UserRepository;
import com.annular.healthCare.service.AuthService;
import com.annular.healthCare.webModel.DoctorDaySlotWebModel;
import com.annular.healthCare.webModel.DoctorLeaveListWebModel;
import com.annular.healthCare.webModel.DoctorSlotTimeWebModel;
import com.annular.healthCare.webModel.FileInputWebModel;
import com.annular.healthCare.webModel.HospitalDataListWebModel;
import com.annular.healthCare.webModel.SplitSlotDurationWebModel;
import com.annular.healthCare.webModel.UserWebModel;
import com.fasterxml.jackson.databind.ObjectMapper;

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
	DoctorSlotTimeRepository doctorSlotTimeRepository;
	
	@Autowired
	HospitalDataListRepository hospitalDataListRepository;
	
	@Autowired
	DoctorRoleRepository doctorRoleRepository;
	
	@Autowired
	DoctorSlotRepository doctorSlotRepository;
	
	@Autowired
	DoctorDaySlotRepository doctorDaySlotRepository;
	
	@Autowired
	HospitalAdminRepository hospitalAdminRepository;
	
	@Autowired
	DoctorLeaveListRepository doctorLeaveListRepository;
	
	@Autowired
	DoctorSpecialityRepository doctorSpecialtyRepository;
	

	
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
				    .yearOfExperiences(userWebModel.getYearOfExperiences())																							// password
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
	        

	        if (userWebModel.getRoleIds() != null && !userWebModel.getRoleIds().isEmpty()) {
	            for (Integer roleId : userWebModel.getRoleIds()) {
	                DoctorRole doctorRole = DoctorRole.builder()
	                    .user(savedUser)
	                    .roleId(roleId)
	                    .createdBy(savedUser.getCreatedBy())
	                    .userIsActive(true)
	                    .build();
	                doctorRoleRepository.save(doctorRole);
	            }
	        }
	     // If user is a doctor, create default slot structure
	        if (userWebModel.getUserType().equalsIgnoreCase("DOCTOR")) {
	            DoctorSlot doctorSlot = DoctorSlot.builder()
	                    .user(savedUser)
	                    .createdBy(savedUser.getCreatedBy())
	                    .isActive(true)
	                    .build();
	            doctorSlot = doctorSlotRepository.save(doctorSlot); // Save doctor slot

	            // Loop through the provided days and create day slots
	            if (userWebModel.getDoctorDaySlots() != null) {
	                for (DoctorDaySlotWebModel daySlotModel : userWebModel.getDoctorDaySlots()) {
	                    DoctorDaySlot doctorDaySlot = DoctorDaySlot.builder()
	                            .doctorSlot(doctorSlot)
	                            .day(daySlotModel.getDay())
	                            .startSlotDate(daySlotModel.getStartSlotDate())
	                            .endSlotDate(daySlotModel.getEndSlotDate())
	                            .createdBy(savedUser.getCreatedBy())
	                            .isActive(true)
	                            .build();
	                    doctorDaySlot = doctorDaySlotRepository.save(doctorDaySlot); // Save day slot

	                    // Create time slots for each day slot
	                    if (daySlotModel.getDoctorSlotTimes() != null) {
	                        for (DoctorSlotTimeWebModel slotTimeModel : daySlotModel.getDoctorSlotTimes()) {
	                            DoctorSlotTime doctorSlotTime = DoctorSlotTime.builder()
	                                    .doctorDaySlot(doctorDaySlot)
	                                    .slotStartTime(slotTimeModel.getSlotStartTime())
	                                    .slotEndTime(slotTimeModel.getSlotEndTime())
	                                    .slotTime(slotTimeModel.getSlotTime())
	                                    .createdBy(savedUser.getCreatedBy())
	                                    .isActive(true)
	                                    .build();
	                            doctorSlotTimeRepository.save(doctorSlotTime);
	                        }
	                    }
	                }
	            }
	            // Save doctor leaves if provided
	            if (userWebModel.getDoctorLeaveList() != null) {
	                for (DoctorLeaveListWebModel leaveModel : userWebModel.getDoctorLeaveList()) {
	                    DoctorLeaveList doctorLeave = DoctorLeaveList.builder()
	                            .user(savedUser)
	                            .doctorLeaveDate(leaveModel.getDoctorLeaveDate())
	                            .createdBy(savedUser.getCreatedBy())
	                            .userIsActive(true)
	                            .build();
	                    doctorLeaveListRepository.save(doctorLeave);
	                }
	            }
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
					User hospitalUser = userRepository.findById(hospitalData.getUserId())
						    .orElseThrow(() -> new RuntimeException("User not found"));
					mediaFile.setUser(hospitalUser);

					mediaFile.setFileOriginalName(fileInput.getFileName());
					mediaFile.setFileSize(fileInput.getFileSize());
					mediaFile.setFileType(fileInput.getFileType());
					mediaFile.setCategory(MediaFileCategory.patientDocument); // Define a suitable enum value
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
	        logger.info("Updating user details for userId: " + userWebModel.getUserId());

	        // Step 1: Retrieve the existing user
	        Optional<User> userOptional = userRepository.findById(userWebModel.getUserId());
	        if (!userOptional.isPresent()) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                    .body(new Response(0, "Fail", "User not found"));
	        }

	        User existingUser = userOptional.get();

	        // Step 2: Update user details (only non-null values)
	        if (userWebModel.getEmailId() != null) existingUser.setEmailId(userWebModel.getEmailId());
	        if (userWebModel.getFirstName() != null) existingUser.setFirstName(userWebModel.getFirstName());
	        if (userWebModel.getPassword() != null && !userWebModel.getPassword().isEmpty()) {
	            existingUser.setPassword(passwordEncoder.encode(userWebModel.getPassword()));
	        }
	        if (userWebModel.getLastName() != null) existingUser.setLastName(userWebModel.getLastName());
	        if (userWebModel.getPhoneNumber() != null) existingUser.setPhoneNumber(userWebModel.getPhoneNumber());
	        if (userWebModel.getCurrentAddress() != null) existingUser.setCurrentAddress(userWebModel.getCurrentAddress());
	        if (userWebModel.getUserUpdatedBy() != null) existingUser.setUserUpdatedBy(userWebModel.getUserUpdatedBy());
	        if (userWebModel.getYearOfExperiences() != null) existingUser.setYearOfExperiences(userWebModel.getYearOfExperiences());

	        // Update timestamp
	        existingUser.setUserUpdatedOn(new Date());

	        // Merge firstName + lastName → userName
	        String fullName = (existingUser.getFirstName() != null ? existingUser.getFirstName() : "") + 
	                          " " + 
	                          (existingUser.getLastName() != null ? existingUser.getLastName() : "");
	        existingUser.setUserName(fullName.trim());

	        
	        // Step 3: Save updated user
	        User updatedUser = userRepository.save(existingUser);

	        // Step 4: Always delete old media files (if any)
	        List<MediaFile> oldMediaFiles = mediaFileRepository.findByUserId(HealthCareConstant.ProfilePhoto, updatedUser.getUserId());
	        if (!oldMediaFiles.isEmpty()) {
	            for (MediaFile oldMediaFile : oldMediaFiles) {
	                Base64FileUpload.deleteFile(imageLocation + "/profilePhoto", oldMediaFile.getFileName());
	                mediaFileRepository.deleteById(oldMediaFile.getFileId());
	            }
	        }

	        // Step 5: Upload new media file (if provided)
	        if (userWebModel.getFilesInputWebModel() != null && !userWebModel.getFilesInputWebModel().isEmpty()) {
	            handleFileUploads(updatedUser, userWebModel.getFilesInputWebModel());
	        }

	        // Step 6: Update User Roles (if provided)
	        if (userWebModel.getRoleIds() != null && !userWebModel.getRoleIds().isEmpty()) {
	            // Remove existing roles
	            doctorRoleRepository.deactivateUser(updatedUser.getUserId());

	            // Assign new roles
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
            // Save doctor leaves if provided
            if (userWebModel.getDoctorLeaveList() != null) {
                for (DoctorLeaveListWebModel leaveModel : userWebModel.getDoctorLeaveList()) {
                    DoctorLeaveList doctorLeave = DoctorLeaveList.builder()
                            .user(updatedUser)
                            .doctorLeaveDate(leaveModel.getDoctorLeaveDate())
                            .createdBy(updatedUser.getCreatedBy())
                            .userIsActive(true)
                            .build();
                    doctorLeaveListRepository.save(doctorLeave);
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

	        // Step 7: Soft delete associated Doctor entities (if any)
	        List<DoctorRole> doctors = doctorRoleRepository.findByDoctorUserId(userWebModel.getUserId());
	        for (DoctorRole doctor : doctors) {
	            doctor.setUserIsActive(false);  // Set doctor as inactive
	            doctor.setUserUpdatedOn(new Date());  // Update the timestamp for doctor
	            doctorRoleRepository.save(doctor);  // Save the updated doctor record
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
	        data.put("phoneNumber", user.getPhoneNumber());
	        data.put("gender", user.getGender());
	        data.put("dob", user.getDob());
	        data.put("yearOfExperience", user.getYearOfExperiences());
	        Integer hospitalId = user.getHospitalId();
	        data.put("hospitalId", hospitalId);

	        if (hospitalId != null) {
	            try {
	                Optional<HospitalDataList> hospitalData = hospitalDataListRepository.findByHospitalId(hospitalId);
	                data.put("hospitalName", hospitalData.isPresent() ? hospitalData.get().getHospitalName() : "N/A");
	            } catch (Exception e) {
	                logger.error("Error retrieving hospital name for hospitalId {}: {}", hospitalId, e.getMessage());
	                data.put("hospitalName", "Error retrieving");
	            }
	        } else {
	            data.put("hospitalName", "N/A");
	        }

	        data.put("userIsActive", user.getUserIsActive());

	        // Fetch role details if user is a doctor
	        List<Map<String, Object>> roleDetails = new ArrayList<>();
	        if (user.getDoctorRoles() != null) {
	            for (DoctorRole doctorRole : user.getDoctorRoles()) {
	                if (doctorRole.getUserIsActive()) {
	                    Map<String, Object> roleMap = new HashMap<>();
	                    roleMap.put("roleId", doctorRole.getRoleId());

	                    try {
	                        String specialtyName = doctorSpecialtyRepository.findSpecialtyNameByRoleId(doctorRole.getRoleId());
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

	        // Fetch media files for profile photos
	        List<MediaFile> files = mediaFileRepository.findByFileDomainIdAndFileDomainReferenceId(
	                HealthCareConstant.ProfilePhoto, user.getUserId());
	        if (files == null) {
	            files = new ArrayList<>();
	        }

	        List<FileInputWebModel> filesInputWebModel = new ArrayList<>();
	        for (MediaFile mediaFile : files) {
	            FileInputWebModel filesInput = new FileInputWebModel();
	            filesInput.setFileName(mediaFile.getFileOriginalName());
	            filesInput.setFileId(mediaFile.getFileId());
	            filesInput.setFileSize(mediaFile.getFileSize());
	            filesInput.setFileType(mediaFile.getFileType());

	            try {
	                String fileData = Base64FileUpload.encodeToBase64String(imageLocation + "/ProfilePhoto",
	                        mediaFile.getFileName());
	                filesInput.setFileData(fileData);
	            } catch (Exception e) {
	                logger.error("Error encoding file {}: {}", mediaFile.getFileName(), e.getMessage());
	                filesInput.setFileData("Error encoding file");
	            }

	            filesInputWebModel.add(filesInput);
	        }

	        data.put("profilePhotos", filesInputWebModel);

	        // Fetch slot details if the user is a doctor
	        if ("DOCTOR".equalsIgnoreCase(user.getUserType())) {
	            List<Map<String, Object>> doctorSlotList = new ArrayList<>();

	            List<DoctorSlot> doctorSlots = doctorSlotRepository.findByUser(user);
	            for (DoctorSlot doctorSlot : doctorSlots) {
	                Map<String, Object> slotData = new HashMap<>();
	                slotData.put("slotId", doctorSlot.getDoctorSlotId());
	                slotData.put("isActive", doctorSlot.getIsActive());

	                // Fetch day slots
	                List<Map<String, Object>> daySlotList = new ArrayList<>();
	                List<DoctorDaySlot> doctorDaySlots = doctorDaySlotRepository.findByDoctorSlot(doctorSlot);
	                for (DoctorDaySlot daySlot : doctorDaySlots) {
	                    Map<String, Object> daySlotData = new HashMap<>();
	                    daySlotData.put("daySlotId", daySlot.getDoctorDaySlotId());
	                    daySlotData.put("day", daySlot.getDay());
	                    daySlotData.put("startSlotDate", daySlot.getStartSlotDate());
	                    daySlotData.put("endSlotDate", daySlot.getEndSlotDate());
	                    daySlotData.put("isActive", daySlot.getIsActive());

	                    // Fetch time slots
	                    List<Map<String, Object>> timeSlotList = new ArrayList<>();
	                    List<DoctorSlotTime> doctorSlotTimes = doctorSlotTimeRepository.findByDoctorDaySlot(daySlot);
	                    for (DoctorSlotTime slotTime : doctorSlotTimes) {
	                        Map<String, Object> timeSlotData = new HashMap<>();
	                        timeSlotData.put("timeSlotId", slotTime.getDoctorSlotTimeId());
	                        timeSlotData.put("slotStartTime", slotTime.getSlotStartTime());
	                        timeSlotData.put("slotEndTime", slotTime.getSlotEndTime());
	                        timeSlotData.put("slotTime", slotTime.getSlotTime());
	                        timeSlotData.put("isActive", slotTime.getIsActive());

	                        // Split slots
	                        List<Map<String, String>> splitSlots = generateSplitSlots(
	                                slotTime.getSlotStartTime(),
	                                slotTime.getSlotEndTime(),
	                                slotTime.getSlotTime()
	                        );

	                        timeSlotData.put("splitSlotDuration", splitSlots);
	                        timeSlotList.add(timeSlotData);
	                    }
	                    daySlotData.put("slotTimes", timeSlotList);
	                    daySlotList.add(daySlotData);
	                }
	                slotData.put("daySlots", daySlotList);
	                doctorSlotList.add(slotData);
	            }
	            data.put("doctorSlots", doctorSlotList);
	                   // Fetch doctor leave details
            List<Map<String, Object>> doctorLeaveList = new ArrayList<>();
            List<DoctorLeaveList> doctorLeaves = doctorLeaveListRepository.findByUser(user);

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

	// Helper method to generate split slots
	private List<Map<String, String>> generateSplitSlots(String startTime, String endTime, String slotDuration) {
	    List<Map<String, String>> splitSlots = new ArrayList<>();
	    try {
	        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
	        Date start = sdf.parse(startTime);
	        Date end = sdf.parse(endTime);
	        int duration = Integer.parseInt(slotDuration.replaceAll("\\D+", "")); // Extracts numeric value from "30 minutes"

	        Calendar cal = Calendar.getInstance();
	        cal.setTime(start);

	        while (cal.getTime().before(end)) {
	            Date splitStart = cal.getTime();
	            cal.add(Calendar.MINUTE, duration);
	            Date splitEnd = cal.getTime();

	            if (splitEnd.after(end)) break;

	            Map<String, String> splitSlot = new HashMap<>();
	            splitSlot.put("startTime", sdf.format(splitStart));
	            splitSlot.put("endTime", sdf.format(splitEnd));
	            splitSlots.add(splitSlot);
	        }
	    } catch (Exception e) {
	        logger.error("Error parsing slot time: {}", e.getMessage());
	    }
	    return splitSlots;
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
	        return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                .body(new Response(0, "Fail", "Doctor role not found"));
	    } catch (Exception e) {
	        logger.error("Error disabling doctor role: {}", e.getMessage(), e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(0, "Fail", "Error disabling doctor role"));
	    }
	}

	@Override
	public ResponseEntity<?> getDoctorSlotById(Integer userId, LocalDate requestDate) {
	    try {
	        // Validate input parameters
	        if (userId == null || requestDate == null) {
	            return ResponseEntity.badRequest()
	                    .body(Collections.singletonMap("message", "Invalid user ID or request date"));
	        }

	        // Find user with optional check
	        Optional<User> userData = userRepository.findById(userId);
	        if (userData.isEmpty()) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                    .body(Collections.singletonMap("message", "User not found"));
	        }

	        User user = userData.get();
	        Map<String, Object> response = new HashMap<>();
	        response.put("userId", user.getUserId());
	        response.put("userName", user.getUserName());

	        // If user is not a doctor, return basic user info
	        if (!"DOCTOR".equalsIgnoreCase(user.getUserType())) {
	            return ResponseEntity.ok(response);
	        }

	        // Fetch and filter doctor slots
	        List<Map<String, Object>> doctorSlotList = doctorDaySlotRepository.findByDoctorSlot_User(user)
	                .stream()
	                .filter(slot -> isValidSlot(slot, requestDate))
	                .map(this::mapDoctorSlot)
	                .filter(slot -> !((List<?>) slot.get("daySlots")).isEmpty())
	                .collect(Collectors.toList());

	        response.put("doctorSlots", doctorSlotList);
	        return ResponseEntity.ok(response);
	    } catch (Exception e) {
	        logger.error("Exception while retrieving doctor slots for user {}: ", userId, e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(Collections.singletonMap("message", "Error retrieving doctor slots"));
	    }
	}

	private boolean isValidSlot(DoctorDaySlot doctorSlot, LocalDate requestDate) {
	    if (doctorSlot == null || requestDate == null) {
	        return false;
	    }

	    LocalDate startDate = convertToLocalDate(doctorSlot.getStartSlotDate());
	    LocalDate endDate = convertToLocalDate(doctorSlot.getEndSlotDate());

	    return doctorSlot.getIsActive() != null 
	           && doctorSlot.getIsActive() 
	           && !requestDate.isBefore(startDate) 
	           && !requestDate.isAfter(endDate);
	}

	private Map<String, Object> mapDoctorSlot(DoctorDaySlot daySlot) {
	    List<Map<String, Object>> daySlotList = doctorDaySlotRepository.findByDoctorSlot(daySlot.getDoctorSlot())
	            .stream()
	            .filter(this::isValidDaySlot)
	            .map(this::mapDoctorDaySlot)
	            .collect(Collectors.toList());

	    Map<String, Object> doctorSlotData = new HashMap<>();
	    doctorSlotData.put("doctorSlotId", daySlot.getDoctorSlot().getDoctorSlotId());
	    doctorSlotData.put("daySlots", daySlotList);
	    return doctorSlotData;
	}

	private boolean isValidDaySlot(DoctorDaySlot daySlot) {
	    if (daySlot == null) {
	        return false;
	    }

	    LocalDate startDate = convertToLocalDate(daySlot.getStartSlotDate());
	    LocalDate endDate = convertToLocalDate(daySlot.getEndSlotDate());
	    LocalDate now = LocalDate.now();

	    return daySlot.getIsActive() != null 
	           && daySlot.getIsActive() 
	           && !now.isBefore(startDate) 
	           && !now.isAfter(endDate);
	}

	private Map<String, Object> mapDoctorDaySlot(DoctorDaySlot daySlot) {
	    Map<String, Object> daySlotData = new HashMap<>();
	    daySlotData.put("daySlotId", daySlot.getDoctorDaySlotId());
	    daySlotData.put("day", daySlot.getDay());
	    daySlotData.put("startSlotDate", daySlot.getStartSlotDate());
	    daySlotData.put("endSlotDate", daySlot.getEndSlotDate());
	    daySlotData.put("isActive", daySlot.getIsActive());

	    List<Map<String, Object>> timeSlotList = doctorSlotTimeRepository.findByDoctorDaySlot(daySlot)
	            .stream()
	            .filter(slotTime -> slotTime.getIsActive() != null && slotTime.getIsActive())
	            .map(this::mapDoctorSlotTime)
	            .collect(Collectors.toList());

	    daySlotData.put("slotTimes", timeSlotList);
	    return daySlotData;
	}

	private Map<String, Object> mapDoctorSlotTime(DoctorSlotTime slotTime) {
	    Map<String, Object> timeSlotData = new HashMap<>();
	    timeSlotData.put("timeSlotId", slotTime.getDoctorSlotTimeId());
	    timeSlotData.put("slotStartTime", slotTime.getSlotStartTime());
	    timeSlotData.put("slotEndTime", slotTime.getSlotEndTime());
	    timeSlotData.put("slotTime", slotTime.getSlotTime());
	    timeSlotData.put("isActive", slotTime.getIsActive());
	    timeSlotData.put("splitSlotDuration", generateSplitSlots(
	            slotTime.getSlotStartTime(),
	            slotTime.getSlotEndTime(),
	            slotTime.getSlotTime()));
	    return timeSlotData;
	}

	// Utility method to convert Date to LocalDate
	private LocalDate convertToLocalDate(Date date) {
	    return date != null 
	           ? date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() 
	           : null;
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

	        // Fetch the DoctorSlot manually
	        DoctorSlot doctorSlot = doctorSlotRepository.findById(userWebModel.getDoctorSlotId())
	                .orElseThrow(() -> new RuntimeException("DoctorSlot not found with ID: " + userWebModel.getDoctorSlotId()));

	        // Loop through provided day slots and save them
	        if (userWebModel.getDoctorDaySlots() != null) {
	            for (DoctorDaySlotWebModel daySlotModel : userWebModel.getDoctorDaySlots()) {
	                try {
	                    DoctorDaySlot doctorDaySlot = DoctorDaySlot.builder()
	                            .doctorSlot(doctorSlot) // Set doctorSlot object
	                            .day(daySlotModel.getDay())
	                            .startSlotDate(daySlotModel.getStartSlotDate())
	                            .endSlotDate(daySlotModel.getEndSlotDate())
	                            .createdBy(userWebModel.getCreatedBy())
	                            .isActive(true)
	                            .build();
	                    
	                    doctorDaySlot = doctorDaySlotRepository.save(doctorDaySlot);

	                    // Create time slots for each day slot
	                    if (daySlotModel.getDoctorSlotTimes() != null) {
	                        for (DoctorSlotTimeWebModel slotTimeModel : daySlotModel.getDoctorSlotTimes()) {
	                            try {
	                                DoctorSlotTime doctorSlotTime = DoctorSlotTime.builder()
	                                        .doctorDaySlot(doctorDaySlot) // Use saved day slot ID
	                                        .slotStartTime(slotTimeModel.getSlotStartTime())
	                                        .slotEndTime(slotTimeModel.getSlotEndTime())
	                                        .slotTime(slotTimeModel.getSlotTime())
	                                        .createdBy(userWebModel.getCreatedBy())
	                                        .isActive(true)
	                                        .build();
	                                doctorSlotTimeRepository.save(doctorSlotTime);
	                            } catch (Exception e) {
	                                e.printStackTrace();
	                                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                                        .body(new Response(0, "Error", "Failed to save time slot: " + e.getMessage()));
	                            }
	                        }
	                    }
	                } catch (Exception e) {
	                    e.printStackTrace();
	                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                            .body(new Response(0, "Error", "Failed to save day slot: " + e.getMessage()));
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

}

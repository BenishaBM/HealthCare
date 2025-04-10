package com.annular.healthCare.service.serviceImpl;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.annular.healthCare.Util.Base64FileUpload;
import com.annular.healthCare.Util.HealthCareConstant;
import com.annular.healthCare.model.DoctorSpecialty;
import com.annular.healthCare.model.HospitalAdmin;
import com.annular.healthCare.model.HospitalDataList;
import com.annular.healthCare.model.HospitalSpeciality;
import com.annular.healthCare.model.MediaFile;
import com.annular.healthCare.model.PatientAppointmentTable;
import com.annular.healthCare.model.PatientDetails;
import com.annular.healthCare.model.User;
import com.annular.healthCare.repository.DoctorRoleRepository;
import com.annular.healthCare.repository.DoctorSlotDateRepository;
import com.annular.healthCare.repository.DoctorSlotSpiltTimeRepository;
import com.annular.healthCare.repository.DoctorSlotTimeOverrideRepository;
import com.annular.healthCare.repository.DoctorSlotTimeRepository;
import com.annular.healthCare.repository.DoctorSpecialityRepository;
import com.annular.healthCare.repository.HospitalAdminRepository;
import com.annular.healthCare.repository.HospitalDataListRepository;
import com.annular.healthCare.repository.HospitalSpecialityRepository;
import com.annular.healthCare.repository.MediaFileRepository;
import com.annular.healthCare.repository.PatientAppoitmentTablerepository;
import com.annular.healthCare.repository.PatientDetailsRepository;
import com.annular.healthCare.repository.UserRepository;
import com.annular.healthCare.service.HospitalDataListService;
import com.annular.healthCare.webModel.DoctorSlotTimeOverrideWebModel;
import com.annular.healthCare.webModel.FileInputWebModel;
import com.annular.healthCare.webModel.HospitalAdminWebModel;
import com.annular.healthCare.model.DoctorRole;
import com.annular.healthCare.model.DoctorSlotDate;
import com.annular.healthCare.model.DoctorSlotSpiltTime;
import com.annular.healthCare.model.DoctorSlotTime;
import com.annular.healthCare.model.DoctorSlotTimeOverride;
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
	
	@Autowired
	DoctorSlotTimeRepository doctorSlotTimeRepository;
	
	@Autowired
	DoctorSlotTimeOverrideRepository doctorSlotTimeOverrideRepository;
	
	@Autowired
	PatientAppoitmentTablerepository patientAppoinmentRepository;
	
	@Autowired
	PatientDetailsRepository patientDetailsRepository;
	
	@Autowired
	HospitalSpecialityRepository hospitalSpecialityRepository;
	
	@Autowired
	DoctorSlotSpiltTimeRepository doctorSlotSplitTimeRepository;
	
	@Autowired
	DoctorSlotDateRepository doctorSlotDateRepository;

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
	        
	        // ✅ Save specialities into hospital_speciality table
	        if (userWebModel.getSpecialityIds() != null && !userWebModel.getSpecialityIds().isEmpty()) {
	            List<HospitalSpeciality> specialities = userWebModel.getSpecialityIds().stream()
	                .map(specId -> HospitalSpeciality.builder()
	                    .specialityId(specId)
	                    .hospitalDataList(savedHospitalData)
	                    .build())
	                .collect(Collectors.toList());

	            // Optional: set them into the hospital entity if you use bi-directional mapping
	            savedHospitalData.setSpecialities(specialities);

	            // Save all specialities
	            hospitalSpecialityRepository.saveAll(specialities);
	        }

	        // Handle file uploads (e.g., hospital logo)
	        if (userWebModel.getFilesInputWebModel() != null) {
	            handleFileUploads(savedHospitalData, userWebModel.getFilesInputWebModel());
	        }

	        // Register multiple admins if provided and update their hospitalId in the user table
	        if (userWebModel.getAdmins() != null && !userWebModel.getAdmins().isEmpty()) {
	            for (HospitalAdminWebModel adminWebModel : userWebModel.getAdmins()) {
	                // Create a new admin entity for each admin
	                HospitalAdmin newAdmin = HospitalAdmin.builder()
	                        .userIsActive(true) // Default to active
	                        .adminUserId(adminWebModel.getUserAdminId()) // Use userAdminId
	                        .createdBy(userWebModel.getCreatedBy())
	                        .hospitalDataList(savedHospitalData) // Associate the admin with the hospital
	                        .build();

	                // Save the admin in the database
	                hospitalAdminRepository.save(newAdmin);

	                // Update the admin's hospitalId in the User table
	                Optional<User> adminUser = usersRepository.findById(adminWebModel.getUserAdminId());
	                if (adminUser.isPresent()) {
	                    User user = adminUser.get();
	                    user.setHospitalId(savedHospitalData.getHospitalDataId());
	                    usersRepository.save(user);
	                } else {
	                    logger.warn("Admin user not found: " + adminWebModel.getUserAdminId());
	                }
	            }
	        }



	        // Prepare the response with successful registration details
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
				mediaFile.setFileDomainId(HealthCareConstant.hospitalLogo); // This constant can be changed to represent
																			// logo files
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

	        // Query the repository for the matching data
	        List<User> hospitalDataList = usersRepository.findByUserTypeAndHospitalIds(userType, hospitalId);

	        // Check if data exists
	        if (hospitalDataList.isEmpty()) {
	            return ResponseEntity.ok(new Response(1, "No hospital data found for the given userType and hospitalId", new ArrayList<>()));
	        }

	        // Extract the hospital data
	        List<HashMap<String, Object>> dataList = new ArrayList<>();
	        for (User hospitalData : hospitalDataList) {
	            HashMap<String, Object> data = new HashMap<>();
	            data.put("hospitalDataId", hospitalData.getUserId());
	            data.put("hospitalId", hospitalData.getHospitalId());
	            data.put("userName", hospitalData.getUserName());
	            data.put("firstName", hospitalData.getFirstName()); // Corrected the field
	            data.put("lastName", hospitalData.getLastName()); // Added missing lastName
	            data.put("emailId", hospitalData.getEmailId());
	            data.put("userType", hospitalData.getUserType());
	            data.put("userId", hospitalData.getUserId());
	            data.put("phoneNumber", hospitalData.getPhoneNumber());
	            data.put("currentAddress", hospitalData.getCurrentAddress());
	            data.put("empId", hospitalData.getEmpId());
	            data.put("yearOfExperience", hospitalData.getYearOfExperiences());
	            data.put("gender", hospitalData.getGender());
	            data.put("userIsActive", hospitalData.getUserIsActive());

	            // Filter only active doctor roles
	            List<Map<String, Object>> roleDetails = new ArrayList<>();
	            if (hospitalData.getDoctorRoles() != null) { // Corrected reference
	                for (DoctorRole doctorRole : hospitalData.getDoctorRoles()) {
	                    if (doctorRole.getUserIsActive()) { // Check if the role is active
	                        Map<String, Object> roleMap = new HashMap<>();
	                        roleMap.put("roleId", doctorRole.getRoleId());
	                        roleMap.put("doctorRoleId", doctorRole.getDoctorRoleId());

	                        try {
	                            String specialtyName = doctorSpecialityRepository.findSpecialtyNameByRoleId(doctorRole.getRoleId());
	                            roleMap.put("specialtyName", specialtyName != null ? specialtyName : "N/A");
	                        } catch (Exception e) {
	                            logger.error("Error fetching specialty name for roleId {}: {}", doctorRole.getRoleId(), e.getMessage());
	                            roleMap.put("specialtyName", "Error retrieving");
	                        }

	                        roleDetails.add(roleMap);
	                    }
	                }
	            }
	            data.put("doctorRoles", roleDetails); // Added doctorRoles to response
	            dataList.add(data);
	        }

	        return ResponseEntity.ok(new Response(1, "Success", dataList));

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

				// Step 4: Retrieve all HospitalAdmin details using hospitalDataId
				List<HospitalAdmin> hospitalAdminList = hospitalAdminRepository.findByAdminUserIds(hospitalDataId);

				// Create a HashMap for HospitalAdmin details
				HashMap<String, Object> adminDetails = new HashMap<>();
				if (hospitalAdminList != null && !hospitalAdminList.isEmpty()) {
					// Prepare a list to store each admin's data
					ArrayList<HashMap<String, Object>> allAdminDetails = new ArrayList<>();

					// Iterate through all HospitalAdmin records
					for (HospitalAdmin hospitalAdmin : hospitalAdminList) {
						HashMap<String, Object> adminData = new HashMap<>();
						adminData.put("adminId", hospitalAdmin.getAdminId());
						adminData.put("adminUserId", hospitalAdmin.getAdminUserId());
						adminData.put("userIsActive", hospitalAdmin.getUserIsActive());

						// Retrieve User data (firstName, lastName) from User table using adminUserId
						Optional<User> userOptional = usersRepository.findByUserId(hospitalAdmin.getAdminUserId());
						if (userOptional.isPresent()) {
							User user = userOptional.get();
						    String fullName = (user.getFirstName() != null ? user.getFirstName() : "") + 
				                      " " + 
				                      (user.getLastName() != null ? user.getLastName() : "");
							adminData.put("firstName", user.getFirstName());
							adminData.put("lastName", user.getLastName());
							 adminData.put("userName", fullName.trim()); // Trim to remove extra spaces
						} else {
							adminData.put("message", "No user found for this adminUserId.");
						}

						adminData.put("createdBy", hospitalAdmin.getCreatedBy());
						adminData.put("userCreatedOn", hospitalAdmin.getUserCreatedOn());
						adminData.put("userUpdatedBy", hospitalAdmin.getUserUpdatedBy());
						adminData.put("userUpdatedOn", hospitalAdmin.getUserUpdatedOn());

						// Add each admin's data to the list
						allAdminDetails.add(adminData);
					}

					// Add the list of all admins to the response
					adminDetails.put("hospitalAdmins", allAdminDetails);
				} else {
					// If no admins found, return a message
					adminDetails.put("message", "No admins found for this hospital.");
				}

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
				responseMap.put("hospitalAdmins", adminDetails);

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
			if (userWebModel.getEmailId() != null) {
				existingHospitalData.setEmailId(userWebModel.getEmailId());
			}
			if(userWebModel.getHospitalName() != null)
			{
				existingHospitalData.setHospitalName(userWebModel.getHospitalName());
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
																					// updating
			}

			// Update the 'userUpdatedOn' field to current time
			existingHospitalData.setUserUpdatedOn(new Date());

			// Step 4: Save the updated hospital data entity back to the database
			HospitalDataList updatedHospitalData = userRepository.save(existingHospitalData);

			// Step 5: Register multiple admins if provided
			if (userWebModel.getAdmins() != null && !userWebModel.getAdmins().isEmpty()) {
				for (HospitalAdminWebModel adminWebModel : userWebModel.getAdmins()) {
					// Create a new admin entity for each admin
					HospitalAdmin newAdmin = HospitalAdmin.builder().userIsActive(true) // Default to active
							.adminUserId(userWebModel.getUserId()) // The user registering the hospital
							.createdBy(userWebModel.getCreatedBy()).hospitalDataList(updatedHospitalData) // Associate
																											// the admin
																											// with the
																											// updated
																											// hospital
																											// data
							.build();

					// Save the admin in the database
					hospitalAdminRepository.save(newAdmin);
				}
			}

			// Step 6: Handle media file deletion if it exists (before updating with a new
			// file)
	        // Step 4: Always delete old media files (if any)
	        List<MediaFile> oldMediaFiles = mediaFileRepository.findByUserId(HealthCareConstant.hospitalLogo, updatedHospitalData.getHospitalDataId());
	        if (!oldMediaFiles.isEmpty()) {
	            for (MediaFile oldMediaFile : oldMediaFiles) {
	                Base64FileUpload.deleteFile(imageLocation + "/hospitalLogo", oldMediaFile.getFileName());
	                mediaFileRepository.deleteById(oldMediaFile.getFileId());
	            }
	        }


			// Step 7: Handle file uploads (if any)
			if (userWebModel.getFilesInputWebModel() != null && !userWebModel.getFilesInputWebModel().isEmpty()) {
				// Handle the upload of the new image(s)
				handleFileUploads(updatedHospitalData, userWebModel.getFilesInputWebModel());
			}

			// Step 8: Prepare and return the success response
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
					String filePath = imageLocation + "/hospitalLogo" + mediaFile.getFileName();
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
	            return ResponseEntity.ok(new Response(1, "No hospitals found.", new ArrayList<>()));
	        }

	        // Create a list to store hospital details
	        List<HashMap<String, Object>> hospitalDataList = new ArrayList<>();

	        // Extract hospital details and associated admins
	        for (HospitalDataList user : users) {
	            HashMap<String, Object> hospitalData = new HashMap<>();
	            hospitalData.put("id", user.getHospitalDataId());
	            hospitalData.put("emailId", user.getEmailId());
	            hospitalData.put("phoneNumber", user.getPhoneNumber());
	            hospitalData.put("currentAddress", user.getCurrentAddress());
	            hospitalData.put("isActive", user.getUserIsActive());
	            hospitalData.put("hospitalName", user.getHospitalName());

	            // Retrieve all HospitalAdmin details using hospitalDataId
	            List<HospitalAdmin> hospitalAdminList = hospitalAdminRepository.findByAdminUserIds(user.getHospitalDataId());

	            // Prepare a list to store each admin's data
	            List<HashMap<String, Object>> allAdminDetails = new ArrayList<>();

	            if (hospitalAdminList != null && !hospitalAdminList.isEmpty()) {
	                for (HospitalAdmin hospitalAdmin : hospitalAdminList) {
	                    HashMap<String, Object> adminData = new HashMap<>();
	                    adminData.put("adminId", hospitalAdmin.getAdminId());
	                    adminData.put("adminUserId", hospitalAdmin.getAdminUserId());
	                    adminData.put("userIsActive", hospitalAdmin.getUserIsActive());

	                    // Retrieve User data (firstName, lastName) from User table using adminUserId
	                    Optional<User> userOptional = usersRepository.findByUserId(hospitalAdmin.getAdminUserId());
	                    if (userOptional.isPresent()) {
	                        User adminUser = userOptional.get();
	                        String fullName = (adminUser.getFirstName() != null ? adminUser.getFirstName() : "") + " " +
	                                          (adminUser.getLastName() != null ? adminUser.getLastName() : "");
	                        adminData.put("firstName", adminUser.getFirstName());
	                        adminData.put("lastName", adminUser.getLastName());
	                        adminData.put("userName", fullName.trim()); // Trim to remove extra spaces
	                    } else {
	                        adminData.put("message", "No user found for this adminUserId.");
	                    }

	                    allAdminDetails.add(adminData);
	                }
	            }

	            // Add the list of admin details to the hospital data
	            hospitalData.put("hospitalAdmins", allAdminDetails);

	            // Add hospital data to the final list
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
//	@Transactional
//	public ResponseEntity<?> saveDoctorSlotTimeOverride(DoctorSlotTimeOverrideWebModel webModel) {
//	    try {
//	        if (webModel == null || webModel.getDoctorSlotTimeId() == null ||
//	                webModel.getOverrideDate() == null || StringUtils.isEmpty(webModel.getNewSlotTime())) {
//	            return ResponseEntity.badRequest().body(new Response(0, "error", "Missing required fields"));
//	        }
//
//	        Optional<DoctorSlotTime> doctorSlotTimeOpt = doctorSlotTimeRepository.findById(webModel.getDoctorSlotTimeId());
//	        if (!doctorSlotTimeOpt.isPresent()) {
//	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//	                    .body(new Response(0, "error", "DoctorSlotTime not found with ID: " + webModel.getDoctorSlotTimeId()));
//	        }
//
//	        DoctorSlotTime slot = doctorSlotTimeOpt.get();
//
//	        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);
//	        LocalTime originalStart = LocalTime.parse(slot.getSlotStartTime(), formatter);
//
//	        int durationMinutes = extractDurationInMinutes(webModel.getNewSlotTime());
//	        if (durationMinutes <= 0) {
//	            return ResponseEntity.badRequest().body(new Response(0, "error", "Invalid new slot time format."));
//	        }
//
//	        List<DoctorSlotTimeOverride> existingOverrides = doctorSlotTimeOverrideRepository
//	                .findByOriginalSlotDoctorSlotTimeIdAndOverrideDate(webModel.getDoctorSlotTimeId(), webModel.getOverrideDate());
//
//	        if (!existingOverrides.isEmpty()) {
//	            for (DoctorSlotTimeOverride overrideItem : existingOverrides) {
//	                overrideItem.setIsActive(false);
//	            }
//	            doctorSlotTimeOverrideRepository.saveAll(existingOverrides);
//	        }
//
//
//	        // ✅ Step 2: Save new override entry with isActive = true
//	        DoctorSlotTimeOverride override = DoctorSlotTimeOverride.builder()
//	                .originalSlot(slot)
//	                .overrideDate(webModel.getOverrideDate())
//	                .newSlotTime(webModel.getNewSlotTime()) // saves "30 mins"
//	                .reason(webModel.getReason())
//	                .isActive(true) // ✅ mark new one as active
//	                .build();
//
//	        doctorSlotTimeOverrideRepository.save(override);
//
//	        // Update appointments
//	        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//	        String dateString = sdf.format(webModel.getOverrideDate());
//
//	        List<PatientAppointmentTable> appointments = patientAppoinmentRepository
//	                .findByAppointmentDateAndDoctorSlotTimeId(dateString, slot.getDoctorSlotTimeId());
//
//	        for (PatientAppointmentTable appointment : appointments) {
//	            LocalTime apptStart = LocalTime.parse(appointment.getSlotStartTime(), formatter);
//	            LocalTime apptEnd = LocalTime.parse(appointment.getSlotEndTime(), formatter);
//
//	            appointment.setSlotStartTime(apptStart.plusMinutes(durationMinutes).format(formatter));
//	            appointment.setSlotEndTime(apptEnd.plusMinutes(durationMinutes).format(formatter));
//	            System.out.println("Updating appointment ID: " + appointment.getAppointmentId());
//	        }
//
//	        patientAppoinmentRepository.saveAll(appointments);
//
//	        return ResponseEntity.ok(new Response(1, "success",
//	                "Override saved and " + appointments.size() + " appointments updated."));
//
//	    } catch (DateTimeParseException e) {
//	        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//	                .body(new Response(0, "error", "Invalid time format: " + e.getMessage()));
//	    } catch (Exception e) {
//	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//	                .body(new Response(0, "error", "Error while saving override: " + e.getMessage()));
//	    }
//	}
//
//	// Extracts minutes from "60 mins" or "30 mins"
//	private int extractDurationInMinutes(String newSlotTime) {
//	    try {
//	        return Integer.parseInt(newSlotTime.trim().split(" ")[0]);
//	    } catch (Exception e) {
//	        return 0;
//	    }
//	}
	//test
	@Transactional
	public ResponseEntity<?> saveDoctorSlotTimeOverride(DoctorSlotTimeOverrideWebModel webModel) {
	    try {
	        if (webModel == null || webModel.getDoctorSlotTimeId() == null ||
	                webModel.getOverrideDate() == null || StringUtils.isEmpty(webModel.getNewSlotTime())) {
	            return ResponseEntity.badRequest().body(new Response(0, "error", "Missing required fields"));
	        }

	        Optional<DoctorSlotTime> doctorSlotTimeOpt = doctorSlotTimeRepository.findById(webModel.getDoctorSlotTimeId());
	        if (!doctorSlotTimeOpt.isPresent()) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                    .body(new Response(0, "error", "DoctorSlotTime not found with ID: " + webModel.getDoctorSlotTimeId()));
	        }

	        DoctorSlotTime slot = doctorSlotTimeOpt.get();
	        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);
	        LocalTime originalStart = LocalTime.parse(slot.getSlotStartTime(), formatter);
	        LocalTime originalEnd = LocalTime.parse(slot.getSlotEndTime(), formatter);

	        int durationMinutes = extractDurationInMinutes(webModel.getNewSlotTime());
	        if (durationMinutes <= 0) {
	            return ResponseEntity.badRequest().body(new Response(0, "error", "Invalid new slot time format."));
	        }

	        // Save override entry
	        DoctorSlotTimeOverride override = DoctorSlotTimeOverride.builder()
	                .originalSlot(slot)
	                .overrideDate(webModel.getOverrideDate())
	                .newSlotTime(webModel.getNewSlotTime())
	                .reason(webModel.getReason())
	                .isActive(true)
	                .build();
	        doctorSlotTimeOverrideRepository.save(override);

	        // Find DoctorSlotDate
	        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	        String dateString = sdf.format(webModel.getOverrideDate());
	        Optional<DoctorSlotDate> doctorSlotDateOpt = doctorSlotDateRepository
	                .findByDateAndDoctorSlotTimeIdAndIsActive(dateString, slot.getDoctorSlotTimeId(), true);

	        if (doctorSlotDateOpt.isPresent()) {
	            Integer doctorSlotDateId = doctorSlotDateOpt.get().getDoctorSlotDateId();
	            List<DoctorSlotSpiltTime> existingSplitTimes = doctorSlotSplitTimeRepository
	                    .findByDoctorSlotDateIdAndIsActive(doctorSlotDateId, true);

	            // Get current time and override date
	            LocalDate today = LocalDate.now();
	            LocalTime now = LocalTime.now();
	            LocalDate overrideLocalDate = webModel.getOverrideDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

	            // Update only future split times
	            int overriddenSlots = 0;
	            for (DoctorSlotSpiltTime splitTime : existingSplitTimes) {
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
	                    doctorSlotSplitTimeRepository.save(splitTime);

	                    // Calculate new start/end with override offset
	                    LocalTime newStart = slotStart.plusMinutes(durationMinutes);
	                    LocalTime newEnd = LocalTime.parse(splitTime.getSlotEndTime(), formatter).plusMinutes(durationMinutes);

	                    DoctorSlotSpiltTime newSplit = DoctorSlotSpiltTime.builder()
	                            .slotStartTime(newStart.format(formatter))
	                            .slotEndTime(newEnd.format(formatter))
	                            .slotStatus("OVERRIDDEN")
	                            .doctorSlotDateId(doctorSlotDateId)
	                            .isActive(true)
	                            .createdBy(webModel.getUpdatedBy())
	                            .createdOn(new Date())
	                            .build();

	                    doctorSlotSplitTimeRepository.save(newSplit);
	                    overriddenSlots++;
	                }
	            }
	        }

	        // Update appointments
	        List<PatientAppointmentTable> appointments = patientAppoinmentRepository
	                .findByAppointmentDateAndDoctorSlotTimeId(dateString, slot.getDoctorSlotTimeId());

	        LocalDate today = LocalDate.now();
	        LocalTime now = LocalTime.now();
	        LocalDate overrideDate = webModel.getOverrideDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

	        int updatedAppointments = 0;

	        for (PatientAppointmentTable appointment : appointments) {
	            LocalTime apptStart = LocalTime.parse(appointment.getSlotStartTime(), formatter);
	            LocalTime apptEnd = LocalTime.parse(appointment.getSlotEndTime(), formatter);

	            boolean shouldUpdate = true;

	            if (overrideDate.isEqual(today)) {
	                if (!apptStart.isAfter(now)) {
	                    shouldUpdate = false;
	                }
	            }

	            if (shouldUpdate) {
	                appointment.setSlotStartTime(apptStart.plusMinutes(durationMinutes).format(formatter));
	                appointment.setSlotEndTime(apptEnd.plusMinutes(durationMinutes).format(formatter));
	                updatedAppointments++;
	            }
	        }

	        patientAppoinmentRepository.saveAll(appointments);

	        return ResponseEntity.ok(new Response(1, "success",
	                "Override saved. Updated " + updatedAppointments + " appointments and " +
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

	
}

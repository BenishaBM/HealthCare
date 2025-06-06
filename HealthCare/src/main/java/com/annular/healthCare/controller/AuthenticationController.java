package com.annular.healthCare.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.annular.healthCare.service.AuthService;
import com.annular.healthCare.webModel.FileInputWebModel;
import com.annular.healthCare.webModel.HospitalDataListWebModel;
import com.annular.healthCare.webModel.PatientDetailsWebModel;
import com.annular.healthCare.webModel.UserWebModel;
import com.annular.healthCare.Response;
import com.annular.healthCare.UserStatusConfig;
import com.annular.healthCare.Util.Base64FileUpload;
import com.annular.healthCare.Util.HealthCareConstant;
import com.annular.healthCare.model.HospitalDataList;
import com.annular.healthCare.model.MediaFile;
import com.annular.healthCare.model.PatientDetails;
import com.annular.healthCare.model.RefreshToken;
import com.annular.healthCare.model.User;
import com.annular.healthCare.repository.HospitalDataListRepository;
import com.annular.healthCare.repository.MediaFileRepository;
import com.annular.healthCare.repository.PatientDetailsRepository;
import com.annular.healthCare.repository.PatientMappedHospitalIdRepository;
import com.annular.healthCare.repository.RefreshTokenRepository;
import com.annular.healthCare.repository.UserRepository;
import com.annular.healthCare.security.Jwt.JwtResponse;
import com.annular.healthCare.security.Jwt.JwtUtils;
import com.annular.healthCare.security.UserDetailsImpl;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

	public static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

	@Autowired
	AuthService authService;

	@Autowired
	JwtUtils jwtUtils;

	@Autowired
	UserStatusConfig loginConstants;

	@Autowired
	RefreshTokenRepository refreshTokenRepository;
	
	@Autowired
	PatientDetailsRepository patientDetailsRepository;

	@Autowired
	AuthenticationManager authenticationManager;
	
	@Autowired
	HospitalDataListRepository hospitalDataListRepository;

	@Autowired
	UserRepository userRepository;
	
	@Value("${annular.app.imageLocation}")
	private String imageLocation;
	
	@Autowired
	MediaFileRepository mediaFileRepository;
	
	@Autowired
	PatientMappedHospitalIdRepository patientMappedHospitalIdRepository;

	@PostMapping("register")
	public ResponseEntity<?> userRegister(@RequestBody UserWebModel userWebModel) {
		try {
			logger.info("User register controller start");
			return authService.register(userWebModel);
		} catch (Exception e) {
			logger.error("userRegister Method Exception {}" + e);
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new Response(-1, "Fail", e.getMessage()));
		}

	}

	@PostMapping("login")
	public ResponseEntity<?> login(@RequestBody UserWebModel userWebModel) {
	    try {
	        Optional<User> checkUser = userRepository.findByEmailIds(userWebModel.getEmailId());

	        if (checkUser.isPresent()) {
	            User user = checkUser.get();

	            // Authenticate user with email and password
	            Authentication authentication = authenticationManager.authenticate(
	                    new UsernamePasswordAuthenticationToken(userWebModel.getEmailId(), userWebModel.getPassword()));

	            SecurityContextHolder.getContext().setAuthentication(authentication);

	            // Generate refresh token
	            RefreshToken refreshToken = authService.createRefreshToken(user);

	            // Retrieve hospital name and logo
	            String hospitalName = "";
	            String hospitalCode = "";
	            ArrayList<FileInputWebModel> filesInputWebModel = new ArrayList<>();

	            if (user.getHospitalId() != null) {
	                Optional<HospitalDataList> hospitalDataOpt = hospitalDataListRepository.findById(user.getHospitalId());
	                if (hospitalDataOpt.isPresent()) {
	                    HospitalDataList hospitalData = hospitalDataOpt.get();
	                    hospitalName = hospitalData.getHospitalName();
	                    hospitalCode = hospitalData.getHospitalCode();

	                    // Retrieve hospital logo
	                    List<MediaFile> files = mediaFileRepository.findByFileDomainIdAndFileDomainReferenceId(
	                            HealthCareConstant.hospitalLogo, hospitalData.getHospitalDataId());

	                    for (MediaFile mediaFile : files) {
	                        FileInputWebModel filesInput = new FileInputWebModel();
	                        filesInput.setFileName(mediaFile.getFileOriginalName());
	                        filesInput.setFileId(mediaFile.getFileId());
	                        filesInput.setFileSize(mediaFile.getFileSize());
	                        filesInput.setFileType(mediaFile.getFileType());

	                        String fileData = Base64FileUpload.encodeToBase64String(
	                                imageLocation + "/hospitalLogo", mediaFile.getFileName());
	                        filesInput.setFileData(fileData);

	                        filesInputWebModel.add(filesInput);
	                    }
	                }
	            }

	            // Generate JWT token
	            String jwt = jwtUtils.generateJwtToken(authentication);
	            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

	            logger.info("Login successful for user: {}", user.getEmailId());

	            // Return response with JWT, refresh token, and logo
	            return ResponseEntity.ok(new JwtResponse(
	                    jwt,
	                    userDetails.getId(),
	                    1,
	                    refreshToken.getToken(),
	                    userDetails.getUserType(),
	                    userDetails.getUserEmailId(),
	                    user.getHospitalId(),
	                    hospitalName,
	                    filesInputWebModel,
	                    hospitalCode
	                    
	            ));
	        } else {
	            return ResponseEntity.badRequest().body(new Response(-1, "Fail", "Invalid email or password"));
	        }
	    } catch (BadCredentialsException e) {
	        logger.error("Login failed: Invalid credentials");
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                .body(new Response(-1, "Fail", "Invalid email or password"));
	    } catch (Exception e) {
	        logger.error("Error at login() -> {}", e.getMessage(), e);
	        return ResponseEntity.internalServerError()
	                .body(new Response(-1, "Fail", "An error occurred during login"));
	    }
	}

	@PostMapping("refreshToken")
	public ResponseEntity<?> refreshToken(@RequestBody UserWebModel userWebModel) {
		Optional<RefreshToken> data = refreshTokenRepository.findByToken(userWebModel.getToken());
		if (data.isPresent()) {
			Response token = authService.verifyExpiration(data.get());
			Optional<User> userData = userRepository.findById(data.get().getUserId());
			String jwt = jwtUtils.generateJwtTokenForRefreshToken(userData.get());
			RefreshToken refreshToken = data.get();
			   // Retrieve hospital name

			refreshToken.setExpiryToken(LocalTime.now().plusMinutes(17));
			refreshTokenRepository.save(refreshToken);
			return ResponseEntity.ok(new JwtResponse(jwt, userData.get().getUserId(),

					1, token.getData().toString(), userData.get().getUserType(), userData.get().getEmailId(), userData.get().getHospitalId(),"",null,""));
		}
		return ResponseEntity.badRequest().body(new Response(-1, "Fail", "Refresh Token Failed"));
	}

	@GetMapping("getUserDetailsByUserType")
	public ResponseEntity<?> getUserDetailsByUserType(@RequestParam("userType") String userType,@RequestParam("pageNo")Integer pageNo,@RequestParam("pageSize")Integer pageSize) {
		try {
			logger.info("getUserDetailsByUserType request for userType: {}", userType);
			return authService.getUserDetailsByUserType(userType,pageNo,pageSize);
		} catch (Exception e) {
			logger.error("getUserDetailsByUserType Method Exception: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new Response(-1, "Fail", e.getMessage()));
		}
	}
	
	@GetMapping("getDropDownByUserTypeByHospitalId")
	public ResponseEntity<?> getDropDownByUserTypeByHospitalId() {
		try {
			logger.info("getDropDownByUserTypeByHospitalId request for userType: {}");
			return authService.getDropDownByUserTypeByHospitalId();
		} catch (Exception e) {
			logger.error("getDropDownByUserTypeByHospitalId Method Exception: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new Response(-1, "Fail", e.getMessage()));
		}
}
	
	@PostMapping("updateUserDetailsByUserId")
	public ResponseEntity<?> updateUserDetailsByUserId(@RequestBody HospitalDataListWebModel userWebModel) {
	    try {
	        // Call the service to perform the update
	        return authService.updateUserDetailsByUserId(userWebModel);
	    } catch (Exception e) {
	        // Handle errors and return a meaningful response
	        logger.error("updateUserDetailsByUserId Method Exception: {}", e.getMessage(), e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(-1, "Fail", e.getMessage()));
	    }
	}
	@PostMapping("deleteUserDetailsByUserId")
	public ResponseEntity<?> deleteUserDetailsByUserId(@RequestBody HospitalDataListWebModel userWebModel) {
	    try {
	        // Call the service to perform the update
	        return authService.deleteUserDetailsByUserId(userWebModel);
	    } catch (Exception e) {
	        // Handle errors and return a meaningful response
	        logger.error("deleteUserDetailsByUserId Method Exception: {}", e.getMessage(), e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(-1, "Fail", e.getMessage()));
	    }
	}
	
	@DeleteMapping("deletehospitalAdminByUserId")
	public ResponseEntity<?> deletehospitalAdminByUserId(@RequestParam("id") Integer id) {
	    try {
	        // Call the service to perform the update
	        return authService.deletehospitalAdminByUserId(id);
	    } catch (Exception e) {
	        // Handle errors and return a meaningful response
	        logger.error("deletehospitalAdminByUserId Method Exception: {}", e.getMessage(), e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(-1, "Fail", e.getMessage()));
	    }
	}
	
	@GetMapping("getUserDetailsByUserId")
	public ResponseEntity<?> getUserDetailsByUserId(@RequestParam("userId") Integer userId) {
	    try {
	        // Call the service to perform the update
	        return authService.getUserDetailsByUserId(userId);
	    } catch (Exception e) {
	        // Handle errors and return a meaningful response
	        logger.error("getUserDetailsByUserId Method Exception: {}", e.getMessage(), e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(-1, "Fail", e.getMessage()));
	    }
	}
	@DeleteMapping("deleteDoctorRoleById")
	public ResponseEntity<?> deleteDoctorRoleById(@RequestParam("doctorRoleId")Integer doctorRoleId) {
	    try {
	        // Call the service to perform the update
	        return authService.deleteDoctorRoleById(doctorRoleId);
	    } catch (Exception e) {
	        // Handle errors and return a meaningful response
	        logger.error("deleteDoctorRoleById Method Exception: {}", e.getMessage(), e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(-1, "Fail", e.getMessage()));
	    }
	}
	
	@GetMapping("getDoctorSlotById")
	public ResponseEntity<?> getDoctorSlotById(@RequestParam("userId") Integer userId, 
	                                           @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
	    try {
	        return authService.getDoctorSlotById(userId, date);
	    } catch (Exception e) {
	        logger.error("getDoctorSlotById Method Exception: {}", e.getMessage(), e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(-1, "Fail", e.getMessage()));
	    }
	}

	@DeleteMapping("deleteDoctorLeaveByLeaveId")
	public ResponseEntity<?> deleteDoctorLeaveByLeaveId(@RequestParam("doctorLeaveListId") Integer doctorLeaveListId) {
	    try {
	        // Call the service to perform the update
	        return authService.deleteDoctorLeaveByLeaveId(doctorLeaveListId);
	    } catch (Exception e) {
	        // Handle errors and return a meaningful response
	        logger.error("deleteDoctorLeaveByLeaveId Method Exception: {}", e.getMessage(), e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(-1, "Fail", e.getMessage()));
	    }
	}
	
	@PostMapping("addTimeSlotByDoctor")
	public ResponseEntity<?> addTimeSlotByDoctor(@RequestBody HospitalDataListWebModel userWebModel) {
	    try {
	        // Call the service to perform the update
	        return authService.addTimeSlotByDoctor(userWebModel);
	    } catch (Exception e) {
	        // Handle errors and return a meaningful response
	        logger.error("addTimeSlotByDoctor Method Exception: {}", e.getMessage(), e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(-1, "Fail", e.getMessage()));
	    }
	}
	
	@DeleteMapping("deleteTimeSlotById")
	public ResponseEntity<?> deleteTimeSlotById(@RequestParam("doctorDaySlotId") Integer doctorDaySlotId) {
	    try {
	        // Call the service to perform the update
	        return authService.deleteTimeSlotById(doctorDaySlotId);
	    } catch (Exception e) {
	        // Handle errors and return a meaningful response
	        logger.error("doctorDaySlotId Method Exception: {}", e.getMessage(), e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(-1, "Fail", e.getMessage()));
	    }
	}
	
	@DeleteMapping("doctorSlotById")
	public ResponseEntity<?> doctorSlotById(@RequestParam("doctorSlotId") Integer doctorSlotId) {
	    try {
	        // Call the service to perform the update
	        return authService.doctorSlotById(doctorSlotId);
	    } catch (Exception e) {
	        // Handle errors and return a meaningful response
	        logger.error("doctorSlotById Method Exception: {}", e.getMessage(), e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(-1, "Fail", e.getMessage()));
	    }
	}
	

	
	@GetMapping("verifyMobileNumber")
	public ResponseEntity<?> verifyMobileNumber(@RequestParam("mobileNumber") String mobileNumber,@RequestParam("hospitalId")Integer hospitalId) {
	    try {
	        // Call the service to perform the update
	        return authService.verifyMobileNumber(mobileNumber,hospitalId);
	    } catch (Exception e) {
	        // Handle errors and return a meaningful response
	        logger.error("verifyMobileNumber Method Exception: {}", e.getMessage(), e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(-1, "Fail", e.getMessage()));
	    }
	}
	@GetMapping("verifyMobileNumberWithoutHospitalId")
	public ResponseEntity<?> verifyMobileNumberWithoutHospitalId(@RequestParam("mobileNumber") String mobileNumber) {
	    try {
	        // Call the service to perform the update
	        return authService.verifyMobileNumberWithoutHospitalId(mobileNumber);
	    } catch (Exception e) {
	        // Handle errors and return a meaningful response
	        logger.error("verifyMobileNumberWithoutHospitalId Method Exception: {}", e.getMessage(), e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(-1, "Fail", e.getMessage()));
	    }
	}
	@PostMapping("adminPatientLogin")
	public ResponseEntity<?> adminPatientLogin(@RequestBody PatientDetailsWebModel userWebModel) {
	    try {
	        Optional<PatientDetails> checkUser = patientDetailsRepository.findByMobileNumber(userWebModel.getMobileNumber());
	        if (checkUser.isPresent()) {
	            PatientDetails user = checkUser.get();
	            logger.info("Attempting login for mobileNumber: " + user.getMobileNumber() + ", entered OTP: " + userWebModel.getOtp());
	            
	            // Check if OTP matches directly instead of using authentication manager
	            if (!userWebModel.getOtp().equals(user.getOtp())) {
	                logger.error("Login failed: Invalid OTP");
	                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                        .body(new Response(-1, "Fail", "Invalid OTP"));
	            }
	            
	            // Create authentication token manually since we've already verified the OTP
	            UserDetailsImpl userDetails = UserDetailsImpl.build(user);
	            Authentication authentication = new UsernamePasswordAuthenticationToken(
	                userDetails, null, userDetails.getAuthorities());
	                
	            SecurityContextHolder.getContext().setAuthentication(authentication);
	            logger.info("Authentication successful for: " + user.getMobileNumber());
	            
	            // Generate refresh token
	            //RefreshToken refreshToken = authService.createRefreshToken(user);
	            
	            // Retrieve hospital name
	            String hospitalName = "";
	           
	            // Generate JWT token
	            String jwt = jwtUtils.generateJwtToken(authentication);
	            
	            return ResponseEntity.ok(new JwtResponse(
	                jwt, 
	                user.getPatientDetailsId(), 
	                1, "",
	              //  refreshToken.getToken(), 
	                "PATIENT", 
	                user.getEmailId(), 
	                1,
	                hospitalName, null,""
	            ));
	        } else {
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                    .body(new Response(-1, "Fail", "Invalid mobile number or OTP"));
	        }
	    } catch (Exception e) {
	        logger.error("Error at adminPatientLogin() -> {}", e.getMessage(), e);
	        return ResponseEntity.internalServerError()
	                .body(new Response(-1, "Fail", "An error occurred during login"));
	    }
	}

	@GetMapping("checkExistingUserOrNewUserByPatentientId")
	public ResponseEntity<?> checkExistingUserOrNewUserByPatentientId(@RequestParam("patientId") Integer patientId,@RequestParam("hospitalId") Integer hospitalId) {
	    try {
	        // Call the service to perform the update
	        return authService.checkExistingUserOrNewUserByPatentientId(patientId,hospitalId);
	    } catch (Exception e) {
	        // Handle errors and return a meaningful response
	        logger.error("checkExistingUserOrNewUserByPatentientId Method Exception: {}", e.getMessage(), e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(-1, "Fail", e.getMessage()));
	    }
	}
	
	@PostMapping("savePatientIdAndHospitalIdByExistingUser")
	public ResponseEntity<?> savePatientIdAndHospitalIdByExistingUser(@RequestBody UserWebModel userWebModel) {
	    try {
	        // Call the service to perform the update
	        return authService.savePatientIdAndHospitalIdByExistingUser(userWebModel);
	    } catch (Exception e) {
	        // Handle errors and return a meaningful response
	        logger.error("savePatientIdAndHospitalIdByExistingUser Method Exception: {}", e.getMessage(), e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(-1, "Fail", e.getMessage()));
	    }
	}

	@PostMapping("saveHospitalLink")
	public ResponseEntity<?> saveHospitalLink(@RequestBody HospitalDataListWebModel userWebModel){
	    try {
	        // Call the service to perform the update
	        return authService.saveHospitalLink(userWebModel);
	    } catch (Exception e) {
	        // Handle errors and return a meaningful response
	        logger.error("saveHospitalLink Method Exception: {}", e.getMessage(), e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(-1, "Fail", e.getMessage()));
	    }
	}
}
package com.annular.healthCare.controller;

import java.time.LocalTime;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.annular.healthCare.webModel.HospitalDataListWebModel;
import com.annular.healthCare.webModel.UserWebModel;
import com.annular.healthCare.Response;
import com.annular.healthCare.UserStatusConfig;
import com.annular.healthCare.model.HospitalDataList;
import com.annular.healthCare.model.RefreshToken;
import com.annular.healthCare.model.User;
import com.annular.healthCare.repository.HospitalDataListRepository;
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
	AuthenticationManager authenticationManager;
	
	@Autowired
	HospitalDataListRepository hospitalDataListRepository;

	@Autowired
	UserRepository userRepository;

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
				
				   // Retrieve hospital name
	            String hospitalName = "";
	            if (user.getHospitalId() != null) {
	                Optional<HospitalDataList> hospitalData = hospitalDataListRepository.findById(user.getHospitalId());
	                hospitalName = hospitalData.map(HospitalDataList::getHospitalName).orElse("");
	            }


				// Generate JWT token
				String jwt = jwtUtils.generateJwtToken(authentication);
				UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

				logger.info("Login successful for user: {}", user.getEmailId());

				// Return response with JWT and refresh token
				return ResponseEntity.ok(new JwtResponse(jwt, userDetails.getId(), 1, // Assuming this is a status or
																						// role value
						refreshToken.getToken(), userDetails.getUserType(), userDetails.getUserEmailId(), user.getHospitalId(),hospitalName));
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

					1, token.getData().toString(), userData.get().getUserType(), userData.get().getEmailId(), userData.get().getHospitalId(),""));
		}
		return ResponseEntity.badRequest().body(new Response(-1, "Fail", "Refresh Token Failed"));
	}

	@GetMapping("getUserDetailsByUserType")
	public ResponseEntity<?> getUserDetailsByUserType(@RequestParam("userType") String userType) {
		try {
			logger.info("getUserDetailsByUserType request for userType: {}", userType);
			return authService.getUserDetailsByUserType(userType);
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
	public ResponseEntity<?> getDoctorSlotById(@RequestParam("userId")Integer userId) {
	    try {
	        // Call the service to perform the update
	        return authService.getDoctorSlotById(userId);
	    } catch (Exception e) {
	        // Handle errors and return a meaningful response
	        logger.error("getDoctorSlotById Method Exception: {}", e.getMessage(), e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(-1, "Fail", e.getMessage()));
	    }
	}
	
	

}

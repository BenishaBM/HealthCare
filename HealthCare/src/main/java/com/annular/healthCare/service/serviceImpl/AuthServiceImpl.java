package com.annular.healthCare.service.serviceImpl;

import java.util.ArrayList;
import java.util.Collections;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.annular.healthCare.Response;
import com.annular.healthCare.model.RefreshToken;
import com.annular.healthCare.model.User;
import com.annular.healthCare.repository.RefreshTokenRepository;
import com.annular.healthCare.repository.UserRepository;
import com.annular.healthCare.service.AuthService;
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

			return ResponseEntity.ok(new Response(1, "success", "User registered successfully"));

		} catch (Exception e) {
			logger.error("Error registering user: " + e.getMessage(), e);
			response.put("message", "Registration failed");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
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
}

package com.annular.healthCare.service.serviceImpl;

import java.util.HashMap;

import java.util.Optional;
import java.util.UUID;

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

	        // Check if user already exists
	        Optional<User> existingUser = userRepository.findByEmailId(userWebModel.getEmailId());
	        if (existingUser.isPresent()) {
	            response.put("message", "User with this email already exists");
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	        }

	        // Create new user entity
	        User newUser = User.builder()
	                .emailId(userWebModel.getEmailId())
	                .password(passwordEncoder.encode(userWebModel.getPassword())) // Encrypt password
	                .userType(userWebModel.getUserType())
	                .phoneNumber(userWebModel.getPhoneNumber())
	                .userIsActive(true) // Default active
	                .currentAddress(userWebModel.getCurrentAddress())
	                .empId(userWebModel.getEmpId())
	                .gender(userWebModel.getGender())
	                .createdBy(userWebModel.getCreatedBy())
	                .userName(userWebModel.getUserName())
	                .build();

	        // Save user
	        User savedUser = userRepository.save(newUser);

	        response.put("message", "User registered successfully");
	        response.put("userId", savedUser.getUserId());
	        return ResponseEntity.ok(response);

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
			Optional<User> checkUser = userRepository.findByEmailId(user.getEmailId());

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

}

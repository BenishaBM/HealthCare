package com.annular.healthCare.service.serviceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.NonUniqueResultException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.annular.healthCare.UserStatusConfig;
import com.annular.healthCare.model.PatientDetails;
import com.annular.healthCare.model.User;
import com.annular.healthCare.repository.PatientDetailsRepository;
import com.annular.healthCare.repository.UserRepository;
import com.annular.healthCare.security.UserDetailsImpl;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    public static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);
    private static final String CARET = "^";
    private static final String ESCAPED_CARET = "\\^";
    
    @Autowired
    UserRepository userRepo;
    
    @Autowired
    PatientDetailsRepository patientDetailsRepository;
    
    @Autowired
    UserStatusConfig loginConstants;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("I am from loadUserByUsername() !!! ");
        logger.info("Username: {}, UserType from LoginConstants: {}", username, loginConstants.getUserType());
        
        String email;
        String userType = null; // Initialize as null instead of uninitialized
        
        // Check if username contains the caret separator
        if (username.contains(CARET)) {
            String[] parts = username.split(ESCAPED_CARET);
            email = parts[0];
            if (parts.length > 1) {
                userType = parts[1];
            }
            logger.info("Split username - Email: {}, UserType: {}", email, userType);
        } else {
            email = username;
            logger.info("No caret found - Email: {}, UserType: {}", email, userType);
        }
        
        // If userType is PATIENT, try to load a patient
        if ("PATIENT".equals(userType)) {
            logger.info("Attempting to load a patient with email: {}", email);
            Optional<PatientDetails> optionalPatient = patientDetailsRepository.findByEmailIdIgnoreCase(email);
            
            if (optionalPatient.isPresent()) {
                PatientDetails patient = optionalPatient.get();
                logger.info("Patient found: ID={}, Email={}", patient.getPatientDetailsId(), patient.getEmailId());
                return buildPatientUserDetails(patient);
            } else {
                // If not found by email, try with mobile number
                logger.info("Attempting to load a patient with mobile number: {}", email);
                Optional<PatientDetails> optionalPatientByMobile = patientDetailsRepository.findByMobileNumber(email);
                
                if (optionalPatientByMobile.isPresent()) {
                    PatientDetails patient = optionalPatientByMobile.get();
                    logger.info("Patient found by mobile: ID={}, Mobile={}", 
                               patient.getPatientDetailsId(), patient.getMobileNumber());
                    return buildPatientUserDetails(patient);
                } else {
                    logger.error("Patient not found with email or mobile: {}", email);
                    throw new UsernameNotFoundException("Patient not found with identifier: " + email);
                }
            }
        } else {
            // Default behavior for regular users
            logger.info("Attempting to load a regular user with email: {}, userType: {}", email, userType);
            
            // Handle the case where userType might be null
            if (userType == null) {
                logger.warn("UserType is null - this might cause NonUniqueResultException if multiple users exist with same email");
                
                // Option 1: Try to get all users and handle multiple results
                try {
                    List<User> users = userRepo.findAllByEmailIdIgnoreCaseAndUserIsActive(email, true);
                    
                    if (users.isEmpty()) {
                        logger.error("No users found with email: {}", email);
                        throw new UsernameNotFoundException("User not found with email: " + email);
                    } else if (users.size() == 1) {
                        User user = users.get(0);
                        logger.info("Single user found - ID: {}, Email: {}, UserType: {}", 
                                   user.getUserId(), user.getEmailId(), user.getUserType());
                        return UserDetailsImpl.build(user);
                    } else {
                        // Multiple users found
                        logger.error("Multiple users found with email: {}. Count: {}", email, users.size());
                        for (User user : users) {
                            logger.info("Available user - ID: {}, UserType: {}", user.getUserId(), user.getUserType());
                        }
                        throw new UsernameNotFoundException(
                            "Multiple users found with email: " + email + 
                            ". Please specify userType in format: email^userType"
                        );
                    }
                } catch (Exception e) {
                    if (e instanceof UsernameNotFoundException) {
                        throw e;
                    }
                    logger.error("Error finding users by email only: {}", e.getMessage());
                    
                    // Fallback: try the original method (this might throw NonUniqueResultException)
                    try {
                        Optional<User> optionalUser = userRepo.findByEmailIdss(email, userType);
                        if (optionalUser.isPresent()) {
                            User user = optionalUser.get();
                            logger.info("User from DB --> {} -- {} -- {}", user.getUserId(), user.getEmailId(), user.getUserType());
                            return UserDetailsImpl.build(user);
                        } else {
                            logger.error("User not found with email: {}", email);
                            throw new UsernameNotFoundException("User not found with email: " + email);
                        }
                    } catch (NonUniqueResultException nure) {
                        logger.error("Multiple users found with email: {} - {}", email, nure.getMessage());
                        throw new UsernameNotFoundException(
                            "Multiple users found with email: " + email + 
                            ". Please specify userType in format: email^userType"
                        );
                    }
                }
            } else {
                // UserType is provided - use the original method
                Optional<User> optionalUser = userRepo.findByEmailIdss(email, userType);
                
                if (optionalUser.isPresent()) {
                    User user = optionalUser.get();
                    logger.info("User from DB --> {} -- {} -- {}", user.getUserId(), user.getEmailId(), user.getUserType());
                    return UserDetailsImpl.build(user);
                } else {
                    logger.error("User not found with email: {} and userType: {}", email, userType);
                    throw new UsernameNotFoundException("User not found with email: " + email + " and userType: " + userType);
                }
            }
        }
    }
    private UserDetails buildPatientUserDetails(PatientDetails patient) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_PATIENT"));
        
        // Convert OTP to String if needed
        String otpAsString = patient.getOtp() != null ? patient.getOtp().toString() : "";
        
        return new UserDetailsImpl(
            patient.getPatientDetailsId(), 
            patient.getMobileNumber(),   // Using mobile number as username
            patient.getEmailId(),        // Email
            "PATIENT",                   // User type
            otpAsString,                 // Password (OTP)
            authorities                  // Authorities
        );
    }
}
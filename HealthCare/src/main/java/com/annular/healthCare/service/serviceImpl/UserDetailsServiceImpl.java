package com.annular.healthCare.service.serviceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        String userType = null;
        
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
        }
        
        // If userType is PATIENT, try to load a patient
        if ("PATIENT".equals(userType)) {
            logger.info("Attempting to load a patient with email: {}", email);
            Optional<PatientDetails> optionalPatient = patientDetailsRepository.findByEmailId(email);
            
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
            logger.info("Attempting to load a regular user with email: {}", email);
            Optional<User> optionalUser = userRepo.findByEmailIds(email);
            
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                logger.info("User from DB --> {} -- {} -- {}", user.getUserId(), user.getEmailId(), user.getUserType());
                return UserDetailsImpl.build(user);
            } else {
                logger.error("User not found with email: {}", email);
                throw new UsernameNotFoundException("User not found with email: " + email);
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
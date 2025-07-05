package com.annular.healthCare.service.serviceImpl;

import java.util.ArrayList;
import java.util.List;

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
        if (username == null) {
            logger.error("Username received is null in loadUserByUsername()");
            throw new UsernameNotFoundException("Username cannot be null");
        }

        String emailOrMobile;
        String userType = null;

        if (username.contains("^")) {
            String[] parts = username.split("\\^");
            emailOrMobile = parts[0];
            if (parts.length > 1) {
                userType = parts[1];
            }
        } else {
            emailOrMobile = username;
        }

        if ("PATIENT".equals(userType)) {
            return patientDetailsRepository.findByEmailIdIgnoreCase(emailOrMobile)
                .map(this::buildPatientUserDetails)
                .or(() -> patientDetailsRepository.findByMobileNumber(emailOrMobile)
                    .map(this::buildPatientUserDetails))
                .orElseThrow(() -> new UsernameNotFoundException("Patient not found with identifier: " + emailOrMobile));
        } else {
            return userRepo.findByEmailIds(emailOrMobile)
                .map(UserDetailsImpl::build)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + emailOrMobile));
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
            authorities                // Authorities
        );
    }



}
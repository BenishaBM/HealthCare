package com.annular.healthCare.security;

import java.util.ArrayList;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import com.annular.healthCare.model.PatientDetails;
import com.annular.healthCare.repository.PatientDetailsRepository;
import com.annular.healthCare.service.serviceImpl.AwsS3ServiceImpl;

@Component
public class OtpAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private PatientDetailsRepository patientDetailsRepository;
    
    private static final Logger logger = LoggerFactory.getLogger(OtpAuthenticationProvider.class);

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String mobileNumber = authentication.getName();
        String otpEntered = authentication.getCredentials().toString();

        logger.info("Authenticating user: " + mobileNumber + ", OTP entered: " + otpEntered);

        Optional<PatientDetails> userOpt = patientDetailsRepository.findByMobileNumber(mobileNumber);

        if (userOpt.isPresent()) {
            PatientDetails user = userOpt.get();
            Integer storedOtp = user.getOtp();

            logger.info("Stored OTP in DB: " + storedOtp);

            if (!otpEntered.equals(storedOtp)) {
                logger.error("Invalid OTP! Entered: " + otpEntered + ", Expected: " + storedOtp);
                throw new BadCredentialsException("Invalid OTP");
            }

            logger.info("OTP Verified Successfully!");
            return new UsernamePasswordAuthenticationToken(mobileNumber, otpEntered, new ArrayList<>());
        } else {
            logger.error("User not found: " + mobileNumber);
            throw new BadCredentialsException("User not found");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}



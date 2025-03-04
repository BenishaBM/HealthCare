package com.annular.healthCare.service.serviceImpl;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.annular.healthCare.UserStatusConfig;
import com.annular.healthCare.model.User;
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
    UserStatusConfig loginConstants;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        logger.info("I am from loadUserByUsername() !!! ");
        logger.info("Email :- {}, UserType from LoginConstants :- {}", email, loginConstants.getUserType());
        email = email.contains(CARET) ? email.split(ESCAPED_CARET)[0] : email;
        Optional<User> optionalUser = userRepo.findByEmailIds(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            logger.info("User from DB --> {} -- {} -- {}", user.getUserId(), user.getEmailId(), user.getUserType());
            return UserDetailsImpl.build(user);
        } else {
            throw new UsernameNotFoundException("User Not Found with email: " + email);
        }
    }
}

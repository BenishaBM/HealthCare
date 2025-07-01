package com.annular.healthCare.security.Jwt;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.annular.healthCare.service.serviceImpl.UserDetailsServiceImpl;

public class AuthTokenFilter extends OncePerRequestFilter {

	@Autowired
	private JwtUtils jwtUtils;

	@Autowired
	private UserDetailsServiceImpl userDetailsService;

	private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		try {
		    String jwt = parseJwt(request);
		    logger.info("JWT from request :- {}", jwt);

		    if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
		        logger.info("JWT available...");

		        String userEmailId = jwtUtils.getDataFromJwtToken(jwt, "userEmailId");
		        String userType = jwtUtils.getDataFromJwtToken(jwt, "userType");

		        // ✅ If userEmailId is null, fallback to subject (mobile number)
		        if (userEmailId == null || userEmailId.trim().isEmpty()) {
		            userEmailId = jwtUtils.getUserNameFromJwtToken(jwt); // subject = mobile number
		            logger.info("Fallback to mobile number from subject: {}", userEmailId);
		        }

		        // ✅ Make sure userType is also not null
		        if (userType == null || userType.trim().isEmpty()) {
		            logger.warn("userType is missing in JWT. Cannot authenticate.");
		            throw new RuntimeException("Missing userType in JWT");
		        }

		        String userNameWithUserType = userEmailId + "^" + userType;
		        logger.info("Username with UserType: {}", userNameWithUserType);

		        UserDetails userDetails = userDetailsService.loadUserByUsername(userNameWithUserType);

		        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
		                userDetails, null, userDetails.getAuthorities());
		        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

		        SecurityContextHolder.getContext().setAuthentication(authentication);
		    } else {
		        logger.info("JWT not available...");
		    }
		} catch (Exception e) {
		    logger.error("Cannot set user authentication...", e);
		}

		filterChain.doFilter(request, response);
	}

	private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7, headerAuth.length());
        }
        return null;
    }
}
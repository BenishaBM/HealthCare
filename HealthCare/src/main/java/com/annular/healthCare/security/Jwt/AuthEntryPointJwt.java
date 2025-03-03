package com.annular.healthCare.security.Jwt;


import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;


@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(AuthEntryPointJwt.class);

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        logger.error("Unauthorized error...", authException);
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//		response.getOutputStream().println("{ \"message\": \"" + authException.getMessage() + "\" \n \"status\": \"" + -1 + "\"}");
//		response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error: Unauthorized");
        //response.getWriter().write(new JSONObject().put("message", authException.getMessage()).put("status", -1).toString());

        // Create a JSON response with `data`, `message`, and `status`
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("data", authException.getMessage()); // Setting `data` as null
        jsonResponse.put("message", "Fail"); // Use the actual authentication error message
        jsonResponse.put("status", -1); // Status code for unauthorized access

        // Write the JSON response to the output
        response.getWriter().write(jsonResponse.toString());
    }
}
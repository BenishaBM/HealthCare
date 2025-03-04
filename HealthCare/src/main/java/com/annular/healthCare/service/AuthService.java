package com.annular.healthCare.service;

import org.springframework.http.ResponseEntity;

import com.annular.healthCare.Response;
import com.annular.healthCare.model.RefreshToken;
import com.annular.healthCare.model.User;
import com.annular.healthCare.webModel.UserWebModel;

public interface AuthService {

	ResponseEntity<?> register(UserWebModel userWebModel);

	RefreshToken createRefreshToken(User user);

	Response verifyExpiration(RefreshToken refreshToken);

	ResponseEntity<?> getUserDetailsByUserType(String userType);

}

package com.annular.healthCare.service;

import java.time.LocalDate;

import org.springframework.http.ResponseEntity;

import com.annular.healthCare.Response;
import com.annular.healthCare.model.RefreshToken;
import com.annular.healthCare.model.User;
import com.annular.healthCare.webModel.HospitalDataListWebModel;
import com.annular.healthCare.webModel.UserWebModel;

public interface AuthService {

	ResponseEntity<?> register(UserWebModel userWebModel);

	RefreshToken createRefreshToken(User user);

	Response verifyExpiration(RefreshToken refreshToken);

	ResponseEntity<?> getUserDetailsByUserType(String userType);

	ResponseEntity<?> getDropDownByUserTypeByHospitalId();

	ResponseEntity<?> updateUserDetailsByUserId(HospitalDataListWebModel userWebModel);

	ResponseEntity<?> deleteUserDetailsByUserId(HospitalDataListWebModel userWebModel);

	ResponseEntity<?> deletehospitalAdminByUserId(Integer id);

	ResponseEntity<?> getUserDetailsByUserId(Integer userId);

	ResponseEntity<?> deleteDoctorRoleById(Integer doctorRoleId);

	ResponseEntity<?> getDoctorSlotById(Integer userId, LocalDate date);

	ResponseEntity<?> deleteDoctorLeaveByLeaveId(Integer doctorLeaveListId);

	ResponseEntity<?> addTimeSlotByDoctor(HospitalDataListWebModel userWebModel);

	ResponseEntity<?> deleteTimeSlotById(Integer doctorDaySlotId);

	ResponseEntity<?> doctorSlotById(Integer doctorDaySlotId);

	ResponseEntity<?> verifyMobileNumber(String mobileNumber, Integer hospitalId);

	ResponseEntity<?> checkExistingUserOrNewUserByPatentientId(Integer patientId, Integer hospitalId);

	ResponseEntity<?> savePatientIdAndHospitalIdByExistingUser(UserWebModel userWebModel);

	ResponseEntity<?> verifyMobileNumberWithoutHospitalId(String mobileNumber);

	
}

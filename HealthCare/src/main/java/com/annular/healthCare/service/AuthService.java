package com.annular.healthCare.service;

import java.time.LocalDate;

import org.springframework.http.ResponseEntity;

import com.annular.healthCare.Response;
import com.annular.healthCare.model.PatientDetails;
import com.annular.healthCare.model.RefreshToken;
import com.annular.healthCare.model.User;
import com.annular.healthCare.webModel.HospitalDataListWebModel;
import com.annular.healthCare.webModel.UserWebModel;

public interface AuthService {

	ResponseEntity<?> register(UserWebModel userWebModel);

	RefreshToken createRefreshToken(User user);

	Response verifyExpiration(RefreshToken refreshToken);

	ResponseEntity<?> getUserDetailsByUserType(String userType, Integer pageNo, Integer pageSize);

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

	ResponseEntity<?> saveHospitalLink(HospitalDataListWebModel userWebModel);

	ResponseEntity<?> bookingDemoRegister(UserWebModel userWebModel);

	ResponseEntity<?> getAllbookingDemo(Integer page, Integer size);

	ResponseEntity<?> getAllHospitalListCount(String startDate, String endDate);

	

	ResponseEntity<?> getAllPatientListCount(String startDate, String endDate, Integer hospitalId);

	ResponseEntity<?> getAllEmployeeListCountByHospitalId(String startDate, String endDate, Integer hospitalId, String userType);

	ResponseEntity<?> getAllPatientListCountByHospitalId(String startDate, String endDate, Integer hospitalId);

	
	ResponseEntity<?> getAllAppointmentListByOnlineAndOfflineByHospitalId(String startDate, String endDate,
			Integer hospitalId);

	ResponseEntity<?> getAllEmployeeListCount(String startDate, String endDate, String userType, Integer hospitalId);

	ResponseEntity<?> emailNotificationSendToForgotPassword(HospitalDataListWebModel userWebModel);

	ResponseEntity<?> forgotPassword(HospitalDataListWebModel userWebModel);

	ResponseEntity<?> getAllAppointmentListByOnlineAndOffline(String startDate, String endDate, Integer hospitalId);

	ResponseEntity<?> saveContactUs(HospitalDataListWebModel userWebModel);

	ResponseEntity<?> getAllContactUs(Integer page, Integer size);

	ResponseEntity<?> getAllContactUsById(Integer id);

	ResponseEntity<?> getAllBookingDemoById(Integer id);

	RefreshToken createRefreshTokens(PatientDetails user);


	
}

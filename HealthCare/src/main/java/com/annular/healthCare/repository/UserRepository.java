package com.annular.healthCare.repository;

import java.util.Date;
import java.util.List;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.annular.healthCare.model.HospitalDataList;
import com.annular.healthCare.model.PatientDetails;
import com.annular.healthCare.model.User;


@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

	@Query("SELECT u FROM User u WHERE u.emailId = :email AND u.userType = :userType AND u.userIsActive = true")
	Optional<User> findByEmailId(@Param("email") String email, @Param("userType") String userType);

	@Query("SELECT u FROM User u WHERE u.emailId = :emailId AND u.userIsActive = true")
	Optional<User> findByEmailIds(String emailId);

	@Query("SELECT u FROM User u WHERE u.userType = :userType AND u.userIsActive = true")
	List<User> findByUserType(String userType);

	// Custom query to check for existing user
	@Query("SELECT u FROM User u WHERE u.emailId = :emailId AND u.userType = :userType "
			+ "AND (:hospitalId IS NULL OR u.hospitalId = :hospitalId) AND u.userIsActive = true")
	Optional<User> findByEmailIdAndUserTypeAndHospitalId(String emailId, String userType, Integer hospitalId);

	@Query("SELECT u FROM User u WHERE u.userType = :admin AND u.userIsActive = true AND u.hospitalId IS NULL")
	List<User> findByUserTypeAndHospitalIdIsNull(String admin);

	@Query("SELECT u FROM User u WHERE u.userId = :adminUserId ")
	Optional<User> findByUserId(Integer adminUserId);
//
//	@Query("SELECT u FROM User u WHERE u.userId = :userId")
//	Optional<User> findByIds(Integer userId);
	
	@Query("SELECT u FROM User u WHERE u.userIsActive = true AND u.hospitalId = :hospitalId AND u.userType = :userType")
    List<User> findByUserTypeAndHospitalIds(String userType, Integer hospitalId);

	@Query("SELECT u FROM User u WHERE u.userIsActive = true AND u.hospitalId = :hospitalId AND u.userType = :string")
	List<User> findByHospitalIdAndUserType(Integer hospitalId, String string);

	@Query("SELECT u FROM User u WHERE u.emailId = :emailId")
	User findByEmailId(@Param("emailId") String emailId);

	@Query("SELECT u FROM User u WHERE LOWER(u.userType) = LOWER(:userType) AND u.userIsActive = true")
	List<User> findByUserTypeIgnoreCaseAndUserIsActiveTrue(@Param("userType") String userType);


	List<User> findByHospitalIdAndUserTypeIn(Integer hospitalDataId, List<String> userTypes);

	@Query("SELECT u FROM User u WHERE u.userType = :userType AND u.userIsActive = true")
	Page<User> findByUserType(String userType, PageRequest pageRequest);

	@Query("SELECT u FROM User u WHERE u.emailId = :emailId AND u.userIsActive = true AND u.userType = :userType")
	Optional<User> findByEmailIdss(String emailId, String userType);

	List<User> findByEmailIdAndUserTypeAndUserIsActiveTrue(String email, String userType);

	List<User> findByEmailIdAndUserIsActiveTrue(String email);

	Optional<User> findByEmailIdIgnoreCase(String email);

	List<User> findAllByEmailIdIgnoreCaseAndUserIsActive(String email, boolean b);

	@Query("SELECT u FROM User u WHERE u.emailId = :emailId AND u.userIsActive = true")
	Optional<User> findByEmailIdss(String emailId);



	@Query("SELECT COUNT(u) FROM User u WHERE u.userType = :userType AND u.userIsActive = true AND u.userCreatedOn BETWEEN :start AND :end")
	Integer countActiveByUserTypeAndDateRange(String userType,Date start,Date end);

	@Query("SELECT COUNT(u) FROM User u WHERE u.userType = :userType AND u.userIsActive = true")
	Integer countByUserType(@Param("userType") String userType);

	@Query("SELECT COUNT(u) FROM User u WHERE u.userType = :userType AND u.hospitalId = :hospitalId")
	Integer countByUserTypeAndHospitalId(String userType,
	                                     Integer hospitalId);

	@Query("SELECT COUNT(u) FROM User u WHERE u.userType = :userType AND u.hospitalId = :hospitalId AND u.userIsActive = true AND u.userCreatedOn BETWEEN :start AND :end")
	Integer countActiveUsersByHospitalIdAndDateRange(String userType,
	                                                 Integer hospitalId,
	                                                 Date start,
	                                                 Date end);
	@Query("SELECT u FROM User u WHERE u.emailId = emailId AND u.userIsActive = true")
	Optional<User> findByEmailIdsss(String email);

	   @Query("SELECT DISTINCT u.userType FROM User u")
	    List<String> findAllDistinctUserTypes();

	List<User> findByUserTypeIgnoreCase(String string);

	@Query("SELECT u FROM User u WHERE u.userType = :userType AND u.hospitalId = :hospitalId")
	List<User> findByUserTypeAndHospitalId(String userType,Integer hospitalId);

	@Query("SELECT DISTINCT u.userType FROM User u WHERE u.hospitalId = :hospitalId")
	List<String> findAllDistinctUserTypesByHospital(Integer hospitalId);

	@Query("SELECT u FROM PatientDetails u WHERE u.mobileNumber = :mobileNumber AND u.userIsActive = true")
	Optional<PatientDetails> findByMobileNumber(String mobileNumber);




	
	



}

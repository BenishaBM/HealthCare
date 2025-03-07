package com.annular.healthCare.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.annular.healthCare.model.HospitalDataList;
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

}

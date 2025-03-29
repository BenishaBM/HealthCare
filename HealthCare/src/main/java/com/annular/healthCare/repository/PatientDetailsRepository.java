package com.annular.healthCare.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.annular.healthCare.model.PatientDetails;

@Repository
public interface PatientDetailsRepository extends JpaRepository<PatientDetails, Integer> {

	@Query("SELECT p FROM PatientDetails p WHERE p.hospitalId = :hospitalId")
	List<PatientDetails> findByHospitalId(Integer hospitalId);

	@Query("SELECT p FROM PatientDetails p WHERE p.mobileNumber = :mobileNumber AND p.userIsActive = true")
	Optional<PatientDetails> findByMobileNumberAndHospitalId(@Param("mobileNumber") String mobileNumber);

	Optional<PatientDetails> findByMobileNumber(String mobileNumber);

	@Query("SELECT p FROM PatientDetails p WHERE p.emailId = :emailId AND p.userIsActive = true")
	Optional<PatientDetails> findByEmailId(String email);
	
	@Query("SELECT p FROM PatientDetails p WHERE p.mobileNumber = :mobileNumber AND p.userIsActive = true AND p.hospitalId = :hospitalId")
	Optional<PatientDetails> findByMobileNumberAndHospitalIds(@Param("mobileNumber") String mobileNumber, @Param("hospitalId") Integer hospitalId);



}

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

	@Query("SELECT p FROM PatientDetails p WHERE p.mobileNumber = :mobileNumber AND p.hospitalId = :hospitalId")
	Optional<PatientDetails> findByMobileNumberAndHospitalId(@Param("mobileNumber") String mobileNumber, @Param("hospitalId") Integer hospitalId);


}

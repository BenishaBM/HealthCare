package com.annular.healthCare.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.annular.healthCare.model.PatientDetails;
import com.annular.healthCare.model.PatientMappedHospitalId;

@Repository
public interface PatientMappedHospitalIdRepository extends JpaRepository<PatientMappedHospitalId,Integer>{

	List<PatientMappedHospitalId> findByHospitalId(Integer hospitalId);

	Optional<PatientMappedHospitalId> findByPatientId(Integer patientDetailsID);

	Optional<PatientMappedHospitalId> findByPatientIdAndHospitalId(Integer patientDetailsId, Integer hospitalId);

	@Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM PatientMappedHospitalId p " +
		       "WHERE p.patientId = :patientId AND p.hospitalId = :hospitalId AND p.userIsActive = true")
		boolean existsByPatientIdAndHospitalId(@Param("patientId") Integer patientId, @Param("hospitalId") Integer hospitalId);

	@Query("SELECT p FROM PatientMappedHospitalId p WHERE p.patientId = :patientId AND p.hospitalId = :hospitalId")
	Optional<PatientMappedHospitalId> findMappedData(@Param("patientId") Integer patientId, @Param("hospitalId") Integer hospitalId);

	@Query("SELECT COUNT(p) FROM PatientMappedHospitalId p WHERE p.hospitalId = :hospitalId")
	Integer countTotalPatientsByHospitalId(Integer hospitalId);

	@Query("SELECT COUNT(p) FROM PatientMappedHospitalId p WHERE p.hospitalId = :hospitalId AND p.userIsActive = true AND p.userCreatedOn BETWEEN :start AND :end")
	Integer countActivePatientsByHospitalIdAndDateRange(Integer hospitalId,
	                                                    Date start,
	                                                    Date end);


	@Query("SELECT pd FROM PatientDetails pd WHERE pd.patientDetailsId IN (SELECT pmh.patientId FROM PatientMappedHospitalId pmh WHERE pmh.hospitalId = :hospitalId)")
	List<PatientDetails> findAllPatientsByHospitalId(Integer hospitalId);






}

package com.annular.healthCare.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.annular.healthCare.model.PatientSubChildDetails;

@Repository
public interface PatientSubChildDetailsRepository extends JpaRepository<PatientSubChildDetails, Integer> {

	Optional<PatientSubChildDetails> findByPatientDetailsIdAndPatientName(Integer patientDetailsId, String patientName);

	List<PatientSubChildDetails> findByPatientDetailsId(Integer patientDetailsID);

	@Query("SELECT p.patientSubChildDetailsId, p.patientName FROM PatientSubChildDetails p WHERE p.patientDetailsId = :patientDetailsId AND p.relationshipType = :relationshipType")
	List<Object[]> findIdAndNameByPatientDetailsIdAndRelationshipType(
			@Param("patientDetailsId") Integer patientDetailsId, @Param("relationshipType") String relationshipType);

	@Query("SELECT COUNT(p) FROM PatientSubChildDetails p")
	Integer countAllSubRelations();



	@Query("SELECT COUNT(p) FROM PatientSubChildDetails p WHERE p.createdBy IN (" +
		       "SELECT pm.patientId FROM PatientMappedHospitalId pm WHERE pm.hospitalId = :hospitalId)")
		Integer countTotalSubPatientsByHospitalId(Integer hospitalId);


	@Query("SELECT COUNT(p) FROM PatientSubChildDetails p WHERE p.userIsActive = true AND p.userCreatedOn BETWEEN :start AND :end " +
		       "AND p.createdBy IN (" +
		       "SELECT pm.patientId FROM PatientMappedHospitalId pm WHERE pm.hospitalId = :hospitalId)")
		Integer countActiveSubPatientsByHospitalIdAndDateRange(Integer hospitalId,
		                                                       Date start,
		                                                      Date end);

}

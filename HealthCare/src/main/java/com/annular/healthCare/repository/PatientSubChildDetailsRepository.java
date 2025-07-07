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



	 // OPTION 2A: Use JPQL to avoid table name issues
    @Query("SELECT COUNT(p) FROM PatientSubChildDetails p " +
           "WHERE p.patientDetailsId IN (" +
           "   SELECT pm.patientId FROM PatientMappedHospitalId pm WHERE pm.hospitalId = :hospitalId" +
           ")")
    Integer countTotalSubPatientsByHospitalId(@Param("hospitalId") Integer hospitalId);
    
    // FIXED: Added missing @Param annotation
    @Query("SELECT COUNT(p) FROM PatientSubChildDetails p " + 
           "WHERE p.userIsActive = true " +
           "AND p.userCreatedOn BETWEEN :start AND :end " + 
           "AND p.patientDetailsId IN (" +
           "   SELECT pm.patientId FROM PatientMappedHospitalId pm WHERE pm.hospitalId = :hospitalId" + 
           ")")
    Integer countActiveSubPatientsByHospitalIdAndDateRange(
            @Param("hospitalId") Integer hospitalId, 
            @Param("start") Date start, 
            @Param("end") Date end);
    
    // FIXED: Use correct table name in native query
    @Query(value = "SELECT sc.* " + 
           "FROM patient_sub_child_details sc " +
           "JOIN patient_details pd ON sc.patientdetailsid = pd.patientdetailsid " +
           "JOIN patient_mapped_hospital_id pmh ON pd.patientdetailsid = pmh.patientid " +
           "WHERE pmh.hospitalid = :hospitalId", nativeQuery = true)
    List<PatientSubChildDetails> findAllSubPatientsByHospitalId(@Param("hospitalId") Integer hospitalId);

//    @Query("SELECT COUNT(p) FROM PatientSubChildDetails p WHERE p.hospitalId = :hospitalId")
//    Integer countSubRelationsByHospitalId(@Param("hospitalId") Integer hospitalId);

   
    @Query("SELECT COUNT(p) " +
    	       "FROM PatientSubChildDetails p " +
    	       "JOIN PatientDetails d ON p.patientDetailsId = d.patientDetailsId " +
    	       "JOIN PatientMappedHospitalId m ON d.patientDetailsId = m.patientId " +
    	       "WHERE m.hospitalId = :hospitalId " +
    	       "AND p.userCreatedOn BETWEEN :startDate AND :endDate")
    	Integer countSubRelationsByHospitalIdAndDateRange(@Param("hospitalId") Integer hospitalId,
    	                                                  @Param("startDate") Date startDate,
    	                                                  @Param("endDate") Date endDate);




    @Query("SELECT COUNT(p) FROM PatientSubChildDetails p WHERE p.userCreatedOn BETWEEN :startDate AND :endDate")
    Integer countSubRelationsByDateRange(@Param("startDate") Date startDate,
                                         @Param("endDate") Date endDate);

    
    





}


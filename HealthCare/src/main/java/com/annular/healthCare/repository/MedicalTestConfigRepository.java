package com.annular.healthCare.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.annular.healthCare.model.MedicalTestConfig;

@Repository
public interface MedicalTestConfigRepository extends JpaRepository<MedicalTestConfig, Integer> {

	@Query("SELECT CASE WHEN COUNT(mtc) > 0 THEN true ELSE false END FROM MedicalTestConfig mtc WHERE mtc.hospitalId = :hospitalId AND mtc.medicalTestName = :medicalTestName AND mtc.department = :department AND mtc.isActive = true")
	boolean existsByMedicalTest(@Param("hospitalId") Integer hospitalId,
	                            @Param("medicalTestName") String medicalTestName,
	                            @Param("department") String department);

	boolean existsByHospitalIdAndMedicalTestNameAndDepartment(Integer hospitalId, String medicalTestName,
			String department);

	List<MedicalTestConfig> findByHospitalIdAndIsActiveTrue(Integer hospitalId);

}

package com.annular.healthCare.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.annular.healthCare.model.Department;
import com.annular.healthCare.model.MedicalTestConfig;

@Repository
public interface MedicalTestConfigRepository extends JpaRepository<MedicalTestConfig, Integer> {


	List<MedicalTestConfig> findByHospitalIdAndIsActiveTrue(Integer hospitalId);

	boolean existsByHospitalIdAndMedicalTestNameAndDepartment(
		    Integer hospitalId,
		    String medicalTestName,
		    Department department
		);



		// Fixed method option 2 - using department ID instead:
		@Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM MedicalTestConfig m " +
		       "WHERE m.hospitalId = :hospitalId AND m.medicalTestName = :medicalTestName " +
		       "AND m.department.id = :departmentId")
		boolean existsByHospitalIdAndMedicalTestNameAndDepartmentId(
		    @Param("hospitalId") Integer hospitalId,
		    @Param("medicalTestName") String medicalTestName,
		    @Param("departmentId") Integer departmentId
		);

		List<MedicalTestConfig> findByHospitalIdAndMedicalTestName(Integer hospitalId, String testName);

		boolean existsByHospitalIdAndMedicalTestNameAndDepartmentIdAndIdNot(Integer hospitalId, String testName,
				Integer id, Integer medicalId);

		List<MedicalTestConfig> findByDepartmentId(Integer id);

}

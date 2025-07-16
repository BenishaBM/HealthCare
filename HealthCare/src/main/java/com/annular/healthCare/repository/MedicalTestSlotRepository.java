package com.annular.healthCare.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.annular.healthCare.model.Department;
import com.annular.healthCare.model.MedicalTestSlot;

@Repository
public interface MedicalTestSlotRepository extends JpaRepository<MedicalTestSlot, Integer>{

	List<MedicalTestSlot> findByDepartmentIdAndIsActiveTrue(Integer departmentId);

	List<MedicalTestSlot> findByDepartmentAndIsActiveTrue(Department department);

}

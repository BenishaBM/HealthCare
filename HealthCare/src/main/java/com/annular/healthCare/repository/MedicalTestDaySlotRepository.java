package com.annular.healthCare.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.annular.healthCare.model.Department;
import com.annular.healthCare.model.MedicalTestDaySlot;
import com.annular.healthCare.model.MedicalTestSlot;

@Repository
public interface MedicalTestDaySlotRepository extends JpaRepository<MedicalTestDaySlot, Integer> {

	List<MedicalTestDaySlot> findByMedicalTestSlotMedicalTestSlotIdAndIsActiveTrue(Integer medicalTestSlotId);

	

    List<MedicalTestDaySlot> findByMedicalTestSlot_Department(Department department);
    
    List<MedicalTestDaySlot> findByMedicalTestSlot(MedicalTestSlot medicalTestSlot);

}

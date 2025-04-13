package com.annular.healthCare.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.annular.healthCare.model.MedicalTestSlotTime;

@Repository
public interface MedicalTestSlotTimeRepository extends JpaRepository<MedicalTestSlotTime,Integer>{

	List<MedicalTestSlotTime> findByMedicalTestDaySlotMedicalTestDaySlotIdAndIsActiveTrue(Integer medicalTestDaySlotId);

}

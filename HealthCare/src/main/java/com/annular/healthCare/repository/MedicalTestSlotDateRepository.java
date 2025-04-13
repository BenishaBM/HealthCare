package com.annular.healthCare.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.annular.healthCare.model.MedicalTestSlotDate;

@Repository
public interface MedicalTestSlotDateRepository extends JpaRepository<MedicalTestSlotDate,Integer> {

	List<MedicalTestSlotDate> findByMedicalTestSlotIdAndMedicalTestDaySlotIdAndMedicalTestSlotTimeId(
			Integer medicalTestSlotId, Integer medicalTestDaySlotId, Integer medicalTestSlotTimeId);

}

package com.annular.healthCare.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.annular.healthCare.model.MedicalTestSlotSpiltTime;

@Repository
public interface MedicalTestSlotSpiltTimeRepository extends JpaRepository<MedicalTestSlotSpiltTime,Integer>{

	//List<MedicalTestSlotSpiltTime> findByMedicalTestSlotDateId(Integer medicalTestSlotDateId);
	
	List<MedicalTestSlotSpiltTime> findByMedicalTestSlotDate_MedicalTestSlotDateId(Integer medicalTestSlotDateId);


	List<MedicalTestSlotSpiltTime> findByMedicalTestSlotDate_MedicalTestSlotDateIdAndIsActive(Integer medicalTestSlotDateId, boolean isActive);


	@Query("SELECT m FROM MedicalTestSlotSpiltTime m WHERE m.medicalTestSlotDate.medicalTestSlotDateId = :slotDateId AND m.isActive = true")
    List<MedicalTestSlotSpiltTime> findActiveBySlotDateId(@Param("slotDateId") Integer slotDateId);
}

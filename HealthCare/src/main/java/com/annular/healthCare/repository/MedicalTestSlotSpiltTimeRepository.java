package com.annular.healthCare.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.annular.healthCare.model.MedicalTestSlotSpiltTime;

@Repository
public interface MedicalTestSlotSpiltTimeRepository extends JpaRepository<MedicalTestSlotSpiltTime,Integer>{

	//List<MedicalTestSlotSpiltTime> findByMedicalTestSlotDateId(Integer medicalTestSlotDateId);
	
	List<MedicalTestSlotSpiltTime> findByMedicalTestSlotDate_MedicalTestSlotDateId(Integer medicalTestSlotDateId);


}

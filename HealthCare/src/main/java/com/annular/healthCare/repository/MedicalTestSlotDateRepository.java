package com.annular.healthCare.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.annular.healthCare.model.MedicalTestSlotDate;

@Repository
public interface MedicalTestSlotDateRepository extends JpaRepository<MedicalTestSlotDate,Integer> {

	List<MedicalTestSlotDate> findByMedicalTestSlotIdAndMedicalTestDaySlotIdAndMedicalTestSlotTimeId(
			Integer medicalTestSlotId, Integer medicalTestDaySlotId, Integer medicalTestSlotTimeId);


	   @Query("SELECT m FROM MedicalTestSlotDate m WHERE m.medicalTestSlotDateId = :medicalTestSlotTimeId AND m.isActive = :isActive")
	    Optional<MedicalTestSlotDate> findByMedicalTestSlotTimeIdAndIsActive(
	            @Param("medicalTestSlotTimeId") Integer medicalTestSlotTimeId,
	            @Param("isActive") boolean isActive);


	Optional<MedicalTestSlotDate> findByDateAndMedicalTestSlotTimeIdAndIsActive(String dateString,
			Integer medicalTestSlotTimeId, boolean b);


	List<MedicalTestSlotDate> findByMedicalTestSlotTimeIdAndMedicalTestDaySlotIdAndMedicalTestSlotIdAndDateAndIsActive(
			Integer medicalTestSlotTimeId, Integer medicalTestDaySlotId, Integer medicalTestSlotId, String string,
			boolean b);
	
    List<MedicalTestSlotDate> findByMedicalTestSlotTimeIdAndMedicalTestDaySlotIdAndMedicalTestSlotIdAndDateAndIsActive(
            Integer medicalTestSlotTimeId, 
            Integer medicalTestDaySlotId,
            Integer medicalTestSlotId,
            String date,
            Boolean isActive
        );
}

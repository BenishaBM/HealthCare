package com.annular.healthCare.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.annular.healthCare.model.MedicalTestSlotDate;
import com.annular.healthCare.model.MedicalTestSlotSpiltTime;

@Repository
public interface MedicalTestSlotSpiltTimeRepository extends JpaRepository<MedicalTestSlotSpiltTime, Integer> {

	// List<MedicalTestSlotSpiltTime> findByMedicalTestSlotDateId(Integer
	// medicalTestSlotDateId);

	List<MedicalTestSlotSpiltTime> findByMedicalTestSlotDate_MedicalTestSlotDateId(Integer medicalTestSlotDateId);

	// List<MedicalTestSlotSpiltTime>
	// findByMedicalTestSlotDate_MedicalTestSlotDateIdAndIsActive(Integer
	// medicalTestSlotDateId, boolean isActive);

	@Query("SELECT m FROM MedicalTestSlotSpiltTime m WHERE m.medicalTestSlotDate.medicalTestSlotDateId = :slotDateId AND m.isActive = true")
	List<MedicalTestSlotSpiltTime> findActiveBySlotDateId(@Param("slotDateId") Integer slotDateId);

	List<MedicalTestSlotSpiltTime> findByMedicalTestSlotDate_MedicalTestSlotDateIdAndIsActive(Integer id,
			boolean isActive);

	boolean existsBySlotStartTimeAndSlotEndTimeAndMedicalTestSlotDate_MedicalTestSlotDateId(String start, String end,
			Integer id);

	// List<MedicalTestSlotSpiltTime> findByMedicalTestSlotDateIdAndIsActive(Integer
	// medicalTestSlotDateId, boolean b);

	// Updated to use proper relationship
	List<MedicalTestSlotSpiltTime> findByMedicalTestSlotDate(MedicalTestSlotDate medicalTestSlotDate);

//    // Keep this method as a fallback if you've used it elsewhere
//    List<MedicalTestSlotSpiltTime> findByMedicalTestSlotDateIdAndIsActive(
//        Integer medicalTestSlotDateId, 
//        Boolean isActive
//    );

	List<MedicalTestSlotSpiltTime> findByMedicalTestSlotDate_MedicalTestSlotDateIdAndIsActive(
			Integer medicalTestSlotDateId, Boolean isActive);

	List<MedicalTestSlotSpiltTime> findByMedicalTestSlotDate_MedicalTestSlotDateIdAndIsActiveTrue(
			Integer medicalTestSlotDateId);

	List<MedicalTestSlotSpiltTime> findByMedicalTestSlotDateAndIsActiveTrue(MedicalTestSlotDate savedSlotDate);

	@Query("SELECT COUNT(m) > 0 FROM MedicalTestSlotSpiltTime m " +
		       "JOIN MedicalTestSlotDate d ON m.medicalTestSlotDate.medicalTestSlotDateId = d.medicalTestSlotDateId " +
		       "JOIN MedicalTestSlot s ON d.medicalTestSlotId = s.medicalTestSlotId " +
		       "WHERE d.date = :date " +
		       "AND m.slotStartTime = :startTime " +
		       "AND m.slotEndTime = :endTime " +
		       "AND m.isActive = true " +
		       "AND s.department.id = :departmentId")
		boolean existsOverlappingSlotForDepartment(Integer departmentId,
		                                           String date,
		                                            String startTime,
		                                          String endTime);



}

package com.annular.healthCare.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.annular.healthCare.model.DoctorSlotSpiltTime;
import com.annular.healthCare.model.User;

@Repository
public interface DoctorSlotSpiltTimeRepository extends JpaRepository<DoctorSlotSpiltTime,Integer>{

	@Query("SELECT d FROM DoctorSlotSpiltTime d WHERE d.doctorSlotDateId = :doctorSlotDateId AND d.isActive = true")
	List<DoctorSlotSpiltTime> findByDoctorSlotDateId(Integer doctorSlotDateId);

	List<DoctorSlotSpiltTime> findByDoctorSlotDateIdAndIsActive(Integer doctorSlotDateId, boolean b);

	boolean existsBySlotStartTimeAndSlotEndTimeAndDoctorSlotDateId(String format, String format2,
			Integer doctorSlotDateId);

//	@Query("SELECT s FROM DoctorSlotSplitTime s WHERE s.doctorSlotDateId = :id AND s.isActive = true")
//	List<DoctorSlotSpiltTime> findByDoctorSlotDateIdAndIsActive(@Param("id") Integer doctorSlotDateId);


}

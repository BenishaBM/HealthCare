package com.annular.healthCare.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.annular.healthCare.model.DoctorSlotDate;

@Repository
public interface DoctorSlotDateRepository extends JpaRepository<DoctorSlotDate, Integer> {

	List<DoctorSlotDate> findByDoctorSlotIdAndDoctorDaySlotIdAndDoctorSlotTimeId(Integer doctorSlotId,
			Integer doctorDaySlotId, Integer doctorSlotTimeId);

	List<DoctorSlotDate> findByDoctorSlotTimeIdAndDoctorDaySlotIdAndDoctorSlotIdAndDateAndIsActive(
			Integer doctorSlotTimeId, Integer doctorDaySlotId, Integer doctorSlotId, String string, boolean b);

	Optional<DoctorSlotDate> findByDateAndDoctorSlotTimeIdAndIsActive(String dateString, Integer doctorSlotTimeId,
			boolean b);


}

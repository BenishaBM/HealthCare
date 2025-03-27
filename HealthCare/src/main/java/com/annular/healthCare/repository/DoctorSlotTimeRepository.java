package com.annular.healthCare.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.annular.healthCare.model.DoctorDaySlot;
import com.annular.healthCare.model.DoctorSlotTime;

@Repository
public interface DoctorSlotTimeRepository extends JpaRepository<DoctorSlotTime, Integer> {

	List<DoctorSlotTime> findByDoctorDaySlot(DoctorDaySlot daySlot);

	void deleteByDoctorDaySlot(DoctorDaySlot doctorDaySlot);

	void deleteByDoctorDaySlot(Integer doctorSlot);

}

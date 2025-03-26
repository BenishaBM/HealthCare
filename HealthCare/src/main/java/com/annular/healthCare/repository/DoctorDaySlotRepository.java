package com.annular.healthCare.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.annular.healthCare.model.DoctorDaySlot;
import com.annular.healthCare.model.DoctorSlot;
import com.annular.healthCare.model.User;

@Repository
public interface DoctorDaySlotRepository extends JpaRepository<DoctorDaySlot, Integer>{

	List<DoctorDaySlot> findByDoctorSlot(DoctorSlot doctorSlot);

	 List<DoctorDaySlot> findByDoctorSlot_User(User user);

}

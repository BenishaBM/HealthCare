package com.annular.healthCare.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.annular.healthCare.model.DoctorSlot;
import com.annular.healthCare.model.User;

@Repository
public interface DoctorSlotRepository extends JpaRepository<DoctorSlot, Integer> {

	List<DoctorSlot> findByUser(User user);

}

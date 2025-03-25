package com.annular.healthCare.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.annular.healthCare.model.DoctorDaySlot;

@Repository
public interface DoctorDaySlotRepository extends JpaRepository<DoctorDaySlot, Integer>{

}

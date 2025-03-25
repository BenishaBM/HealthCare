package com.annular.healthCare.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.annular.healthCare.model.DoctorSlot;

@Repository
public interface DoctorSlotRepository extends JpaRepository<DoctorSlot, Integer> {

}

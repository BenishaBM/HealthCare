package com.annular.healthCare.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.annular.healthCare.model.DoctorSlotTime;

@Repository
public interface DoctorSlotTimeRepository extends JpaRepository<DoctorSlotTime, Integer> {

}

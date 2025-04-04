package com.annular.healthCare.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.annular.healthCare.model.AppointmentMedicalTest;

@Repository
public interface AppointmentMedicalTestRepository extends JpaRepository<AppointmentMedicalTest, Integer>{

}

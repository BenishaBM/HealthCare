package com.annular.healthCare.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.annular.healthCare.model.DoctorSpecialty;

@Repository
public interface DoctorSpecialityRepository  extends JpaRepository<DoctorSpecialty, Integer>{

}

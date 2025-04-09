package com.annular.healthCare.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.annular.healthCare.model.HospitalSpeciality;

@Repository
public interface HospitalSpecialityRepository extends JpaRepository<HospitalSpeciality,Integer> {

}

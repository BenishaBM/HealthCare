package com.annular.healthCare.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.annular.healthCare.model.MedicalTestSlotTimeOveride;

@Repository
public interface MedicalTestSlotTimeOverideRepository extends JpaRepository<MedicalTestSlotTimeOveride,Integer>{

}

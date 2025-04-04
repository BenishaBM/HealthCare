package com.annular.healthCare.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.annular.healthCare.model.MedicalTest;

@Repository
public interface MedicalTestRepository extends JpaRepository<MedicalTest,Integer>{

	List<MedicalTest> findByHospitalIdAndIsActiveTrue(Integer hospitalId);

}

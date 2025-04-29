package com.annular.healthCare.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.annular.healthCare.model.Medicines;

@Repository
public interface MedicinesRepository extends JpaRepository<Medicines,Integer>{

	@Query("SELECT m FROM Medicines m WHERE m.hospitalId = hospitalId AND m.isActive = true")
	List<Medicines> findByHospitalIdAndIsActiveTrue(Integer hospitalId);

}

package com.annular.healthCare.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.annular.healthCare.model.HospitalList;

@Repository
public interface HospitalRepository extends JpaRepository<HospitalList, Integer> {

}

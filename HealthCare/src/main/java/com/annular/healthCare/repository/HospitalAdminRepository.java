package com.annular.healthCare.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.annular.healthCare.model.HospitalAdmin;

@Repository
public interface HospitalAdminRepository extends JpaRepository<HospitalAdmin, Integer>{

}

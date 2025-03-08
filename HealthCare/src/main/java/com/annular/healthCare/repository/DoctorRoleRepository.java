package com.annular.healthCare.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.annular.healthCare.model.DoctorRole;
import com.annular.healthCare.model.HospitalDataList;

@Repository
public interface DoctorRoleRepository extends JpaRepository<DoctorRole, Integer> {

    @Query("SELECT dr FROM DoctorRole dr WHERE dr.user.userId = :userId")
	List<DoctorRole> findByDoctorUserId(Integer userId);

	
}

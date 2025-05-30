package com.annular.healthCare.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.annular.healthCare.model.DoctorSpecialty;

@Repository
public interface DoctorSpecialityRepository  extends JpaRepository<DoctorSpecialty, Integer>{

	  @Query("SELECT ds.specialtyName FROM DoctorSpecialty ds WHERE ds.doctorSpecialtiesId = :roleId")
	  String findSpecialtyNameByRoleId(@Param("roleId") Integer roleId);
	  
	  @Query("SELECT ds FROM DoctorSpecialty ds JOIN DoctorRole dr ON ds.doctorSpecialtiesId = dr.roleId WHERE dr.user.userId = :userId AND ds.userIsActive = true")
	  List<DoctorSpecialty> findSpecialtiesByUserId(@Param("userId") Integer userId);

		List<DoctorSpecialty> findBySpecialtyNameIgnoreCase(String speciality);

		



}

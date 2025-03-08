package com.annular.healthCare.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.annular.healthCare.model.DoctorSpecialty;

@Repository
public interface DoctorSpecialityRepository  extends JpaRepository<DoctorSpecialty, Integer>{

	  @Query("SELECT ds.specialtyName FROM DoctorSpecialty ds WHERE ds.doctorSpecialtiesId = :roleId")
	  String findSpecialtyNameByRoleId(@Param("roleId") Integer roleId);

}

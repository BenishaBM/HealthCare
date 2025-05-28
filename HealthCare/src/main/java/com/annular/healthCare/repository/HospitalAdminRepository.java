package com.annular.healthCare.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.annular.healthCare.model.HospitalAdmin;
import com.annular.healthCare.model.HospitalDataList;

@Repository
public interface HospitalAdminRepository extends JpaRepository<HospitalAdmin, Integer> {

	@Query("SELECT ha FROM HospitalAdmin ha WHERE ha.adminUserId = :userId")
	List<HospitalAdmin> findByAdminUserId(Integer userId);

	@Query("SELECT ha FROM HospitalAdmin ha WHERE ha.hospitalDataList.hospitalDataId = :hospitalDataId AND ha.userIsActive = true")
	List<HospitalAdmin> findByAdminUserIds(Integer hospitalDataId);

	List<HospitalAdmin> findByHospitalDataList(HospitalDataList existingHospitalData);

	@Query("SELECT h FROM HospitalAdmin h WHERE h.hospitalDataList.hospitalDataId = :hospitalDataId AND h.userIsActive = true")
	List<HospitalAdmin> findActiveAdminsByHospitalDataId(@Param("hospitalDataId") Integer hospitalDataId);

	//Optional<HospitalAdmin> findByAdminUserIds(Integer hospitalDataId);
	
}

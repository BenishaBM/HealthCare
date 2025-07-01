package com.annular.healthCare.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.annular.healthCare.model.HospitalDataList;
import com.annular.healthCare.model.User;

@Repository
public interface HospitalDataListRepository extends JpaRepository<HospitalDataList, Integer> {

	Optional<HospitalDataList> findByHospitalDataId(Integer hospitalDataId);

	@Query("SELECT u FROM HospitalDataList u WHERE u.hospitalName = :hospitalName AND u.userIsActive = true")
	Optional<HospitalDataList> findByHospitalName(String hospitalName);

	@Query("SELECT u FROM HospitalDataList u WHERE u.userIsActive = true")
	List<HospitalDataList> findByData();

	@Query("SELECT u FROM HospitalDataList u WHERE u.hospitalDataId = :hospitalId")
	Optional<HospitalDataList> findByHospitalId(Integer hospitalId);

	// Modified repository method to handle potential database-specific issues
	@Query(value = "SELECT hospital_code FROM hospital_data_list WHERE hospital_code LIKE 'HC%' ORDER BY LENGTH(hospital_code), hospital_code DESC LIMIT 1", nativeQuery = true)
	String findLastHospitalCode();

	List<HospitalDataList> findByCurrentAddressContainingIgnoreCaseAndUserIsActiveTrue(String location);

	@Query("SELECT u FROM HospitalDataList u WHERE u.emailId = :emailId AND u.userIsActive = true")
	Optional<HospitalDataList> findByEmailId(String emailId);

	@Query("SELECT COUNT(h) FROM HospitalDataList h WHERE h.userIsActive = true")
	Integer countAllHospitals();



	@Query("SELECT COUNT(h) FROM HospitalDataList h WHERE h.userCreatedOn BETWEEN :start AND :end AND h.userIsActive = :active")
	Integer countByCreatedOnBetweenAndIsActive(Date start,Date end, Boolean active);




}

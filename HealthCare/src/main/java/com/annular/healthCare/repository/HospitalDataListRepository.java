package com.annular.healthCare.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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




}

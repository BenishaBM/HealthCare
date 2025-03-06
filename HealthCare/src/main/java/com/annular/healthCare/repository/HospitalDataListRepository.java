package com.annular.healthCare.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.annular.healthCare.model.HospitalDataList;

@Repository
public interface HospitalDataListRepository extends JpaRepository<HospitalDataList, Integer> {

	Optional<HospitalDataList> findByHospitalDataId(Integer hospitalDataId);

	@Query("SELECT u FROM HospitalDataList u WHERE u.hospitalName = :hospitalName AND u.userIsActive = true")
	Optional<HospitalDataList> findByHospitalName(String hospitalName);

}

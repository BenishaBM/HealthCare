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

	@Query("SELECT u FROM HospitalDataList u WHERE u.emailId = :emailId AND u.userType = :userType AND u.userIsActive = true")
	Optional<HospitalDataList> findByEmailId(String emailId, String userType);

	@Query("SELECT u FROM HospitalDataList u WHERE u.userType = :userType AND u.hospitalId = :hospitalId AND u.userIsActive = true")
	Optional<HospitalDataList> findByUserTypeAndHospitalId(String userType, Integer hospitalId);

	Optional<HospitalDataList> findByHospitalDataId(Integer hospitalDataId);


}

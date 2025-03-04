package com.annular.healthCare.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.annular.healthCare.model.HospitalDataList;

@Repository
public interface HospitalDataListRepository extends JpaRepository<HospitalDataList, Integer> {

	@Query("SELECT u FROM User u WHERE u.emailId = :emailId AND u.userType = :userType AND u.userIsActive = true")
	Optional<HospitalDataList> findByEmailId(String emailId, String userType);

}

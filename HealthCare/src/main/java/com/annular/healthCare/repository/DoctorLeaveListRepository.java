package com.annular.healthCare.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.annular.healthCare.model.DoctorLeaveList;
import com.annular.healthCare.model.User;

@Repository
public interface DoctorLeaveListRepository extends JpaRepository<DoctorLeaveList, Integer>{

	@Query("SELECT dl FROM DoctorLeaveList dl WHERE dl.user = :user AND dl.userIsActive = true")
	List<DoctorLeaveList> findByUser(User user);

}

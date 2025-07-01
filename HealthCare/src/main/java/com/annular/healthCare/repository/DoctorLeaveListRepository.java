package com.annular.healthCare.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.annular.healthCare.model.DoctorLeaveList;
import com.annular.healthCare.model.User;

@Repository
public interface DoctorLeaveListRepository extends JpaRepository<DoctorLeaveList, Integer>{

	@Query("SELECT dl FROM DoctorLeaveList dl WHERE dl.user = :user AND dl.userIsActive = true")
	List<DoctorLeaveList> findByUser(User user);
	
	// Check if a leave exists for a doctor on a specific date
    boolean existsByUserAndDoctorLeaveDateAndUserIsActive(User user, Date doctorLeaveDate, Boolean userIsActive);
    
    // Alternative method if you need more precise date comparison
    @Query("SELECT COUNT(d) > 0 FROM DoctorLeaveList d WHERE d.user = :user AND " +
           "FUNCTION('DATE', d.doctorLeaveDate) = FUNCTION('DATE', :leaveDate) AND d.userIsActive = true")
    boolean isDoctorOnLeave(@Param("user") User user, @Param("leaveDate") Date leaveDate);

    @Query("SELECT COUNT(d) > 0 FROM DoctorLeaveList d WHERE d.user.userId = :userId AND d.doctorLeaveDate = :leaveDate AND d.userIsActive = true")
	boolean existsByUserUserIdAndDoctorLeaveDate(Integer userId, Date leaveDate);

    @Query("SELECT d FROM DoctorLeaveList d WHERE d.user.userId = :userId AND d.doctorLeaveDate BETWEEN :start AND :end")
    List<DoctorLeaveList> findByUserUserIdAndDoctorLeaveDateBetween(Integer userId,
                                                        Date start,
                                                       Date end);

	

}

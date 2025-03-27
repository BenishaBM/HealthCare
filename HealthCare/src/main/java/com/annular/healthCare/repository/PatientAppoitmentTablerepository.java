package com.annular.healthCare.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.annular.healthCare.model.PatientAppointmentTable;

@Repository
public interface PatientAppoitmentTablerepository extends JpaRepository<PatientAppointmentTable,Integer> {

	@Query("SELECT COUNT(p) > 0 FROM PatientAppointmentTable p " +
		       "WHERE p.doctorSlotId = :doctorSlotId " +
		       "AND p.daySlotId = :daySlotId " +
		       "AND p.isActive = true " +
		       "AND ((p.slotStartTime < :newSlotEndTime AND p.slotEndTime > :newSlotStartTime))")
		boolean isSlotBooked(@Param("doctorSlotId") Integer doctorSlotId, 
		                     @Param("daySlotId") Integer daySlotId, 
		                     @Param("newSlotStartTime") String newSlotStartTime,
		                     @Param("newSlotEndTime") String newSlotEndTime);

	List<PatientAppointmentTable> findByPatient_UserId(Integer patientDetailsID);




}

package com.annular.healthCare.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.annular.healthCare.model.PatientAppointmentTable;

@Repository
public interface PatientAppoitmentTablerepository extends JpaRepository<PatientAppointmentTable,Integer> {

	@Query("SELECT COUNT(p) > 0 FROM PatientAppointmentTable p WHERE p.doctorSlotId = :doctorSlotId AND p.daySlotId = :daySlotId AND p.timeSlotId = :timeSlotId AND p.isActive = true")
	boolean isSlotBooked(@Param("doctorSlotId") Integer doctorSlotId, @Param("daySlotId") Integer daySlotId, @Param("timeSlotId") Integer timeSlotId);


}

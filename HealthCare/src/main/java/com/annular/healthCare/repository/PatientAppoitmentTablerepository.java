package com.annular.healthCare.repository;

import java.util.Date;
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

	List<PatientAppointmentTable> findByPatient_PatientDetailsId(Integer patientDetailsID);

	@Query("SELECT p FROM PatientAppointmentTable p WHERE p.patient.patientDetailsId = :patientDetailsId")
	List<PatientAppointmentTable> findByPatientDetailsId(@Param("patientDetailsId") Integer patientDetailsId);

	 @Query("SELECT a FROM PatientAppointmentTable a WHERE a.patient.patientDetailsId = :patientId AND a.isActive = true")
	    List<PatientAppointmentTable> findByPatientId(@Param("patientId") Integer patientId);



	 @Query("SELECT p FROM PatientAppointmentTable p WHERE " +
		       "(:doctorId IS NULL OR p.doctor.userId = :doctorId) AND " +
		       "(:appointmentDate IS NULL OR FUNCTION('DATE', p.appointmentDate) = FUNCTION('DATE', :appointmentDate)) AND " +
		       "(:appointmentType IS NULL OR p.appointmentType = :appointmentType) AND " +
		       "p.appointmentStatus = 'SCHEDULED'")
	List<PatientAppointmentTable> findAppointmentsByFilter(String appointmentDate, String appointmentType,
			Integer doctorId);

	 @Query("SELECT COUNT(p) FROM PatientAppointmentTable p WHERE p.appointmentDate = :appointmentDate AND p.doctor.userId = :doctorId AND p.appointmentType = :appointmentType")
	 int countByAppointmentDateAndDoctorIdAndAppointmentType(String appointmentDate, Integer doctorId, String appointmentType);

	    
	 @Query("SELECT COUNT(a) > 0 " +
		       "FROM PatientAppointmentTable a " +
		       "WHERE a.timeSlotId = :timeSlotId " +
		       "AND a.appointmentDate = :appointmentDate " +
		       "AND ((a.slotStartTime < :slotEndTime AND a.slotEndTime > :slotStartTime))")
		boolean isSlotBookeds(
		    @Param("timeSlotId") Integer timeSlotId,
		    @Param("appointmentDate") String appointmentDate,
		    @Param("slotStartTime") String slotStartTime,
		    @Param("slotEndTime") String slotEndTime
		);

	List<PatientAppointmentTable> findByAppointmentDate(String currentDate);

	List<PatientAppointmentTable> findByPatient_PatientDetailsIdAndAppointmentDate(Integer patientId, String appointmentDate);


//	@Query("SELECT p FROM PatientAppointmentTable p WHERE p.appointmentDate = :appointmentDate AND p.timeSlotId = :slotTimeId")
//	List<PatientAppointmentTable> findByAppointmentDateAndDoctorSlotTimeId(
//	    @Param("appointmentDate") Date appointmentDate,
//	    @Param("slotTimeId") Integer slotTimeId);

	@Query("SELECT p FROM PatientAppointmentTable p WHERE p.appointmentDate = :appointmentDate AND p.timeSlotId = :slotTimeId")
	List<PatientAppointmentTable> findByAppointmentDateAndDoctorSlotTimeId(String appointmentDate, Integer slotTimeId);

	@Query("SELECT DISTINCT a FROM PatientAppointmentTable a LEFT JOIN FETCH a.appointmentMedicines m WHERE a.patient.patientDetailsId = :patientId AND a.appointmentDate = :appointmentDate")
	List<PatientAppointmentTable> findAppointmentsWithMedicines(@Param("patientId") Integer patientId, @Param("appointmentDate") String appointmentDate);









}

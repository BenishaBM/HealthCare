package com.annular.healthCare.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.annular.healthCare.model.Department;
import com.annular.healthCare.model.PatientAppointmentTable;

@Repository
public interface PatientAppoitmentTablerepository extends JpaRepository<PatientAppointmentTable, Integer> {

//	@Query("SELECT COUNT(p) > 0 FROM PatientAppointmentTable p " +
//		       "WHERE p.doctorSlotId = :doctorSlotId " +
//		       "AND p.daySlotId = :daySlotId " +
//		       "AND p.isActive = true " +
//		       "AND ((p.slotStartTime < :newSlotEndTime AND p.slotEndTime > :newSlotStartTime))")
//		boolean isSlotBooked(@Param("doctorSlotId") Integer doctorSlotId, 
//		                     @Param("daySlotId") Integer daySlotId, 
//		                     @Param("newSlotStartTime") String newSlotStartTime,
//		                     @Param("newSlotEndTime") String newSlotEndTime);
//	@Query("SELECT COUNT(p) > 0 FROM PatientAppointmentTable p " + "WHERE p.doctorSlotId = :doctorSlotId "
//			+ "AND p.daySlotId = :daySlotId " + "AND p.isActive = true "
//			+ "AND (p.appointmentStatus IS NULL OR p.appointmentStatus != 'CANCELLED') " + // 🔥 Filter out cancelled
//																							// appointments
//			"AND (p.slotStartTime < :newSlotEndTime AND p.slotEndTime > :newSlotStartTime)")
//	boolean isSlotBooked(@Param("doctorSlotId") Integer doctorSlotId, @Param("daySlotId") Integer daySlotId,
//			@Param("newSlotStartTime") String newSlotStartTime, @Param("newSlotEndTime") String newSlotEndTime);
	
	@Query("SELECT COUNT(p) > 0 FROM PatientAppointmentTable p " +
		       "WHERE p.doctorSlotId = :doctorSlotId " +
		       "AND p.daySlotId = :daySlotId " +
		       "AND p.doctorSlotSpiltTimeId = :doctorSlotSpiltTimeId " + // ✅ New condition
		       "AND p.isActive = true " +
		       "AND (p.appointmentStatus IS NULL OR p.appointmentStatus != 'CANCELLED') " +
		       "AND (p.slotStartTime < :newSlotEndTime AND p.slotEndTime > :newSlotStartTime)")
		boolean isSlotBooked(@Param("doctorSlotId") Integer doctorSlotId,
		                     @Param("daySlotId") Integer daySlotId,
		                     @Param("newSlotEndTime") String newSlotEndTime,
		                     @Param("newSlotStartTime") String newSlotStartTime,
		                     @Param("doctorSlotSpiltTimeId") Integer doctorSlotSpiltTimeId);


	List<PatientAppointmentTable> findByPatient_PatientDetailsId(Integer patientDetailsID);

	@Query("SELECT p FROM PatientAppointmentTable p WHERE p.patient.patientDetailsId = :patientDetailsId")
	List<PatientAppointmentTable> findByPatientDetailsId(@Param("patientDetailsId") Integer patientDetailsId);

	@Query("SELECT a FROM PatientAppointmentTable a WHERE a.patient.patientDetailsId = :patientId AND a.isActive = true")
	List<PatientAppointmentTable> findByPatientId(@Param("patientId") Integer patientId);

	@Query("SELECT p FROM PatientAppointmentTable p WHERE " + "(:doctorId IS NULL OR p.doctor.userId = :doctorId) AND "
			+ "(:appointmentDate IS NULL OR FUNCTION('DATE', p.appointmentDate) = FUNCTION('DATE', :appointmentDate)) AND "
			+ "(:appointmentType IS NULL OR p.appointmentType = :appointmentType) AND "
			+ "p.appointmentStatus IN ('SCHEDULED', 'RESCHEDULED')")
	List<PatientAppointmentTable> findAppointmentsByFilter(String appointmentDate, String appointmentType,
			Integer doctorId);

	@Query("SELECT COUNT(p) FROM PatientAppointmentTable p WHERE p.appointmentDate = :appointmentDate AND p.doctor.userId = :doctorId AND p.appointmentType = :appointmentType")
	int countByAppointmentDateAndDoctorIdAndAppointmentType(String appointmentDate, Integer doctorId,
			String appointmentType);

	@Query("SELECT COUNT(a) > 0 " + "FROM PatientAppointmentTable a " + "WHERE a.timeSlotId = :timeSlotId "
			+ "AND a.appointmentDate = :appointmentDate "
			+ "AND ((a.slotStartTime < :slotEndTime AND a.slotEndTime > :slotStartTime))")
	boolean isSlotBookeds(@Param("timeSlotId") Integer timeSlotId, @Param("appointmentDate") String appointmentDate,
			@Param("slotStartTime") String slotStartTime, @Param("slotEndTime") String slotEndTime);

	@Query("SELECT a FROM PatientAppointmentTable a LEFT JOIN FETCH a.appointmentMedicalTests WHERE a.appointmentDate = :currentDate")
	List<PatientAppointmentTable> findByAppointmentDate(@Param("currentDate") String currentDate);

	List<PatientAppointmentTable> findByPatient_PatientDetailsIdAndAppointmentDate(Integer patientId,
			String appointmentDate);

//	@Query("SELECT p FROM PatientAppointmentTable p WHERE p.appointmentDate = :appointmentDate AND p.timeSlotId = :slotTimeId")
//	List<PatientAppointmentTable> findByAppointmentDateAndDoctorSlotTimeId(
//	    @Param("appointmentDate") Date appointmentDate,
//	    @Param("slotTimeId") Integer slotTimeId);

	@Query("SELECT p FROM PatientAppointmentTable p WHERE p.appointmentDate = :appointmentDate AND p.timeSlotId = :slotTimeId")
	List<PatientAppointmentTable> findByAppointmentDateAndDoctorSlotTimeId(String appointmentDate, Integer slotTimeId);

	//@Query("SELECT DISTINCT a FROM PatientAppointmentTable a LEFT JOIN FETCH a.appointmentMedicines m WHERE a.patient.patientDetailsId = :patientId AND a.appointmentDate = :appointmentDate")
	@Query("SELECT DISTINCT a FROM PatientAppointmentTable a " +
		       "LEFT JOIN FETCH a.appointmentMedicines m " +
		       "WHERE a.patient.patientDetailsId = :patientId " +
		       "AND a.appointmentDate = :appointmentDate " +
		       "AND a.appointmentId = :appontmentId")
	List<PatientAppointmentTable> findAppointmentsWithMedicines(@Param("patientId") Integer patientId,
			@Param("appointmentDate") String appointmentDate,Integer appontmentId);

	@Query("SELECT p FROM PatientAppointmentTable p WHERE " + "(:doctorId IS NULL OR p.doctor.userId = :doctorId) AND "
			+ "(:appointmentDate IS NULL OR FUNCTION('DATE', p.appointmentDate) = FUNCTION('DATE', :appointmentDate))")
	List<PatientAppointmentTable> findAppointments(String appointmentDate, Integer doctorId);

	@Query("SELECT a FROM PatientAppointmentTable a WHERE a.slotStartTime = :targetTime")
	List<PatientAppointmentTable> findAppointmentsByTimeSlot(String targetTime);

	@Query("SELECT a FROM PatientAppointmentTable a WHERE a.appointmentDate = :appointmentDate AND a.slotStartTime = :startTime AND a.appointmentStatus = 'SCHEDULED'")
	List<PatientAppointmentTable> findUpcomingAppointments(@Param("appointmentDate") String appointmentDate,
			@Param("startTime") String startTime);

	List<PatientAppointmentTable> findByFollowUpDate(String todayDate);

	@Query("SELECT p FROM PatientAppointmentTable p " + "WHERE p.doctor.hospitalId = :hospitalId "
			+ "AND p.appointmentDate = :appointmentDate " + "ORDER BY p.appointmentId ASC")
	List<PatientAppointmentTable> findByHospitalAndAppointmentDateOrderByIdAsc(Integer hospital,
			String appointmentDate);

	@Query("SELECT p FROM PatientAppointmentTable p  WHERE p.doctor.hospitalId = :hospitalId ")
	List<PatientAppointmentTable> findAppointmentsByDoctorHospitalId(@Param("hospitalId") Integer hospitalId);

	List<PatientAppointmentTable> findByPatient_PatientDetailsIdAndAppointmentDateAndAppointmentId(Integer patientId,
			String appointmentDate, Integer appointmentId);

	@Query(value = "SELECT appointment_type, COUNT(*) as count " + "FROM patient_appointment "
			+ "WHERE DATE(created_on) BETWEEN :start AND :end " + "AND is_active = true "
			+ "GROUP BY appointment_type", nativeQuery = true)
	List<Object[]> getAppointmentCountsByType(@Param("start") Date start, @Param("end") Date end);

	@Query(value = "SELECT appointment_status, COUNT(*) " + "FROM patient_appointment "
			+ "WHERE DATE(created_on) BETWEEN :start AND :end " + "AND is_active = true "
			+ "GROUP BY appointment_status", nativeQuery = true)
	List<Object[]> getAppointmentCountsByStatus(@Param("start") Date start, @Param("end") Date end);

	@Query(value = "SELECT pharmacy_status, COUNT(*) " + "FROM patient_appointment "
			+ "WHERE DATE(created_on) BETWEEN :start AND :end " + "AND is_active = true "
			+ "GROUP BY pharmacy_status", nativeQuery = true)
	List<Object[]> getAppointmentCountsByPharmacyStatus(@Param("start") Date start, @Param("end") Date end);

	@Query("SELECT p.appointmentType, COUNT(p) FROM PatientAppointmentTable p "
			+ "WHERE p.doctor.hospitalId = :hospitalId AND p.createdOn BETWEEN :startDate AND :endDate "
			+ "GROUP BY p.appointmentType")
	List<Object[]> getAppointmentCountsByType(Date startDate, Date endDate, Integer hospitalId);

	@Query("SELECT p.appointmentStatus, COUNT(p) FROM PatientAppointmentTable p "
			+ "WHERE p.doctor.hospitalId = :hospitalId AND p.createdOn BETWEEN :startDate AND :endDate "
			+ "GROUP BY p.appointmentStatus")
	List<Object[]> getAppointmentCountsByStatus(Date startDate, Date endDate, Integer hospitalId);

	@Query("SELECT p.pharmacyStatus, COUNT(p) FROM PatientAppointmentTable p "
			+ "WHERE p.doctor.hospitalId = :hospitalId AND p.createdOn BETWEEN :startDate AND :endDate "
			+ "GROUP BY p.pharmacyStatus")
	List<Object[]> getAppointmentCountsByPharmacyStatus(Date startDate, Date endDate, Integer hospitalId);

	

	
}

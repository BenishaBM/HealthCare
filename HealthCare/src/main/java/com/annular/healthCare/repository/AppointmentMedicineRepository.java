package com.annular.healthCare.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.annular.healthCare.model.AppointmentMedicine;
import com.annular.healthCare.model.PatientAppointmentTable;

@Repository
public interface AppointmentMedicineRepository extends JpaRepository<AppointmentMedicine, Integer> {

	List<AppointmentMedicine> findByAppointmentAppointmentIdAndMedicineId(Integer appointmentId,
			Integer medicineId);

	boolean existsByAppointment(PatientAppointmentTable app);

}

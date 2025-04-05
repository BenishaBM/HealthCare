package com.annular.healthCare.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.annular.healthCare.model.AppointmentMedicine;

@Repository
public interface AppointmentMedicineRepository extends JpaRepository<AppointmentMedicine, Integer> {

	Optional<AppointmentMedicine> findByAppointmentAppointmentIdAndMedicineId(Integer appointmentId,
			Integer medicineId);

}

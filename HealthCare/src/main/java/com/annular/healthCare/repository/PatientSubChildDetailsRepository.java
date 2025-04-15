package com.annular.healthCare.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.annular.healthCare.model.PatientSubChildDetails;

@Repository
public interface PatientSubChildDetailsRepository extends JpaRepository<PatientSubChildDetails,Integer> {

	Optional<PatientSubChildDetails> findByPatientDetailsIdAndPatientName(Integer patientDetailsId, String patientName);

}

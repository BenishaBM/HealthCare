package com.annular.healthCare.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.annular.healthCare.model.PatientMappedHospitalId;

@Repository
public interface PatientMappedHospitalIdRepository extends JpaRepository<PatientMappedHospitalId,Integer>{

	List<PatientMappedHospitalId> findByHospitalId(Integer hospitalId);

	Optional<PatientMappedHospitalId> findByPatientId(Integer patientDetailsID);

	Optional<PatientMappedHospitalId> findByPatientIdAndHospitalId(Integer patientDetailsId, Integer hospitalId);

}

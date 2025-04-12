package com.annular.healthCare.service;

import org.springframework.http.ResponseEntity;

import com.annular.healthCare.webModel.MedicalTestConfigWebModel;

public interface MedicalTestConfigService {

	ResponseEntity<?> saveMedicalTestName(MedicalTestConfigWebModel medicalTestConfigWebModel);

	ResponseEntity<?> getAllMedicalTestNameByHospitalId(Integer hospitalId);

	ResponseEntity<?> getMedicalTestNameById(Integer id);

	ResponseEntity<?> updateMedicalTestName(MedicalTestConfigWebModel medicalTestConfigWebModel);

	ResponseEntity<?> deleteMedicalTestNameById(Integer id);

	ResponseEntity<?> deleteDepartmentById(Integer id);

}

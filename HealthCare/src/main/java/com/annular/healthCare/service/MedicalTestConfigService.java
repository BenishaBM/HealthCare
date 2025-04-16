package com.annular.healthCare.service;

import java.time.LocalDate;

import org.springframework.http.ResponseEntity;

import com.annular.healthCare.webModel.MedicalTestConfigWebModel;

public interface MedicalTestConfigService {

	ResponseEntity<?> saveMedicalTestName(MedicalTestConfigWebModel medicalTestConfigWebModel);

	ResponseEntity<?> getAllMedicalTestNameByHospitalId(Integer hospitalId);

	ResponseEntity<?> getMedicalTestNameById(Integer id);

	ResponseEntity<?> updateMedicalTestName(MedicalTestConfigWebModel medicalTestConfigWebModel);

	ResponseEntity<?> deleteMedicalTestNameById(Integer id);

	ResponseEntity<?> deleteDepartmentById(Integer id);

	ResponseEntity<?> saveMedicalTestSlotByDepartmentId(MedicalTestConfigWebModel medicalTestConfigWebModel);

	ResponseEntity<?> getMedicalTestSlotByDepartmentId(Integer id);

	ResponseEntity<?> saveMedicalTestOvverride(MedicalTestConfigWebModel medicalTestConfigWebModel);

	ResponseEntity<?> addTimeSlotByMedicalTest(MedicalTestConfigWebModel medicalTestConfigWebModel);

	ResponseEntity<?> deleteSlotByMedicalTestById(Integer id);

	ResponseEntity<?> getMedicalTestSlotById(Integer id, LocalDate date);

	ResponseEntity<?> saveDepartment(MedicalTestConfigWebModel medicalTestConfigWebModel);

	ResponseEntity<?> getAllDepartmentByhospitalId(Integer hospitalId);

}

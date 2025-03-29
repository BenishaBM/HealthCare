package com.annular.healthCare.service;

import java.util.Date;

import org.springframework.http.ResponseEntity;

public interface DoctorAppoitmentService {

	ResponseEntity<?> getAllPatientAppointmentByFilter(String appointmentDate, String appointmentType, Integer doctorId);

}

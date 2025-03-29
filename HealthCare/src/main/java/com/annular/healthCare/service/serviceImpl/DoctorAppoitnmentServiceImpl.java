package com.annular.healthCare.service.serviceImpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.annular.healthCare.Response;
import com.annular.healthCare.model.PatientAppointmentTable;
import com.annular.healthCare.model.PatientDetails;
import com.annular.healthCare.repository.PatientAppoitmentTablerepository;
import com.annular.healthCare.repository.PatientDetailsRepository;
import com.annular.healthCare.service.DoctorAppoitmentService;

@Service
public class DoctorAppoitnmentServiceImpl implements DoctorAppoitmentService{
	
	@Autowired
	PatientAppoitmentTablerepository patientAppointmentRepository;
	
	@Autowired
	PatientDetailsRepository patientDetailsRepository;

	 @Override
	    public ResponseEntity<?> getAllPatientAppointmentByFilter(String appointmentDate, String appointmentType, Integer doctorId) {
	        try {
	            List<PatientAppointmentTable> appointments = patientAppointmentRepository.findAppointmentsByFilter(appointmentDate, appointmentType, doctorId);
	            
	            if (appointments.isEmpty()) {
	            	return ResponseEntity.ok(new Response(0, "No Appointments Found", null));
	            }

	            List<Map<String, Object>> appointmentDetails = new ArrayList<>();

	            for (PatientAppointmentTable appointment : appointments) {
	                Map<String, Object> details = new HashMap<>();
	            // Appointment details
	                // Appointment details
	                details.put("appointmentId", appointment.getAppointmentId());
	                details.put("appointmentDate", appointment.getAppointmentDate());
	                details.put("appointmentType", appointment.getAppointmentType());
	                details.put("appointmentStatus", appointment.getAppointmentStatus());
	                details.put("slotStartTime", appointment.getSlotStartTime());
	                details.put("slotEndTime", appointment.getSlotEndTime());
	                details.put("slotTime", appointment.getSlotTime());
	                details.put("isActive", appointment.getIsActive());
	                details.put("createdBy", appointment.getCreatedBy());
	                details.put("createdOn", appointment.getCreatedOn());
	                details.put("updatedBy", appointment.getUpdatedBy());
	                details.put("updatedOn", appointment.getUpdatedOn());
	                details.put("patientNotes", appointment.getPatientNotes());
	                details.put("doctorSlotId", appointment.getDoctorSlotId());
	                details.put("daySlotId", appointment.getDaySlotId());
	                details.put("timeSlotId", appointment.getTimeSlotId());
	                details.put("age", appointment.getAge());
	                details.put("dob", appointment.getDateOfBirth());
	                details.put("relationshipType", appointment.getRelationShipType());
	                details.put("patientName", appointment.getPatientName());

	            // Doctor details
	            details.put("doctorId", appointment.getDoctor().getUserId());
	            details.put("doctorName", appointment.getDoctor().getUserName());

	            // Fetching Patient Details
	            Optional<PatientDetails> patientDetailsOpt = patientDetailsRepository.findById(appointment.getPatient().getUserId());
	            if (patientDetailsOpt.isPresent()) {
	                PatientDetails patient = patientDetailsOpt.get();
	                details.put("patientId", patient.getPatientDetailsId());
	                details.put("patientName", patient.getPatientName());
	                details.put("patientAge", patient.getAge());
	                details.put("patientDOB", patient.getDob());
	                details.put("gender", patient.getGender());
	                details.put("bloodGroup", patient.getBloodGroup());
	                details.put("mobileNumber", patient.getMobileNumber());
	                details.put("emailId", patient.getEmailId());
	                details.put("address", patient.getAddress());
	                details.put("emergencyContact", patient.getEmergencyContact());
	                details.put("hospitalId", patient.getHospitalId());
	                details.put("purposeOfVisit", patient.getPurposeOfVisit());
	                details.put("policyNumber", patient.getPolicyNumber());
	                details.put("disability", patient.getDisability());
	                details.put("previousMedicalHistory", patient.getPreviousMedicalHistory());
	            }
	                appointmentDetails.add(details);
	            }

	            return ResponseEntity.ok(new Response(1, "Appointments Retrieved", appointmentDetails));

	        } catch (Exception e) {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                    .body(new Response(-1, "Failed to retrieve appointments", e.getMessage()));
	        }
	    }

}

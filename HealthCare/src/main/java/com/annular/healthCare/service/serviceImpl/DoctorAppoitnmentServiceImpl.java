package com.annular.healthCare.service.serviceImpl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Column;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.annular.healthCare.Response;
import com.annular.healthCare.Util.Base64FileUpload;
import com.annular.healthCare.Util.HealthCareConstant;
import com.annular.healthCare.model.AppointmentMedicalTest;
import com.annular.healthCare.model.AppointmentMedicine;
import com.annular.healthCare.model.DoctorRole;
import com.annular.healthCare.model.DoctorSlotSpiltTime;
import com.annular.healthCare.model.DoctorSpecialty;
import com.annular.healthCare.model.HospitalDataList;
import com.annular.healthCare.model.MediaFile;
import com.annular.healthCare.model.MediaFileCategory;
import com.annular.healthCare.model.MedicalTest;
import com.annular.healthCare.model.MedicalTestConfig;
import com.annular.healthCare.model.MedicalTestSlotSpiltTime;
import com.annular.healthCare.model.Medicines;
import com.annular.healthCare.model.PatientAppointmentTable;
import com.annular.healthCare.model.PatientDetails;
import com.annular.healthCare.model.PatientMappedHospitalId;
import com.annular.healthCare.model.User;
import com.annular.healthCare.repository.AppointmentMedicalTestRepository;
import com.annular.healthCare.repository.AppointmentMedicineRepository;
import com.annular.healthCare.repository.DoctorSlotSpiltTimeRepository;
import com.annular.healthCare.repository.DoctorSpecialityRepository;
import com.annular.healthCare.repository.HospitalDataListRepository;
import com.annular.healthCare.repository.MediaFileRepository;
import com.annular.healthCare.repository.MedicalTestConfigRepository;
import com.annular.healthCare.repository.MedicalTestRepository;
import com.annular.healthCare.repository.MedicalTestSlotSpiltTimeRepository;
import com.annular.healthCare.repository.MedicinesRepository;
import com.annular.healthCare.repository.PatientAppoitmentTablerepository;
import com.annular.healthCare.repository.PatientDetailsRepository;
import com.annular.healthCare.repository.PatientMappedHospitalIdRepository;
import com.annular.healthCare.repository.UserRepository;
import com.annular.healthCare.service.DoctorAppoitmentService;
import com.annular.healthCare.service.SmsService;
import com.annular.healthCare.webModel.AppointmentMedicalTestWebModel;
import com.annular.healthCare.webModel.FileInputWebModel;
import com.annular.healthCare.webModel.HospitalDataListWebModel;
import com.annular.healthCare.webModel.MedicineScheduleWebModel;
import com.annular.healthCare.webModel.PatientAppointmentWebModel;

@Service
public class DoctorAppoitnmentServiceImpl implements DoctorAppoitmentService{
	
	@Autowired
	PatientAppoitmentTablerepository patientAppointmentRepository;
	
	@Autowired
	PatientDetailsRepository patientDetailsRepository;
	
	@Autowired
	DoctorSlotSpiltTimeRepository doctorSlotSpiltTimeRepository;
	
	@Autowired
	MedicinesRepository medicineRepository;
	
	@Autowired
	MedicalTestRepository medicalTestRepository;
	
	@Autowired
	HospitalDataListRepository hospitalDataListRepository;
	
	@Autowired
	DoctorSpecialityRepository doctorSpecialtyRepository;
	
	@Autowired
	MedicalTestConfigRepository medicalTestConfigRepository;
	
	@Autowired
	MedicalTestSlotSpiltTimeRepository medicalTestSlotSpiltTimeRepository;
	
	@Autowired
	private AppointmentMedicineRepository appointmentMedicineRepository;
	
	@Autowired
	PatientDetailsRepository patientDetailRepository;
	
	@Autowired
	PatientMappedHospitalIdRepository patientMappedHospitalIdRepository;

	@Autowired
	private AppointmentMedicalTestRepository appointmentMedicalTestRepository;

	@Autowired
	MediaFileRepository mediaFilesRepository;
	
	@Autowired
	private SmsService smsService;
	
	@Autowired
	MediaFileRepository mediaFileRepository;
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	DoctorSlotSpiltTimeRepository doctorSlotSplitTimeRepository;
	
	@Value("${annular.app.imageLocation}")
	private String imageLocation;

	 @Override
	    public ResponseEntity<?> getAllPatientAppointmentByFilter(String appointmentDate, String appointmentType, Integer doctorId) {
	        try {
	           // List<PatientAppointmentTable> appointments = patientAppointmentRepository.findAppointmentsByFilter(appointmentDate, appointmentType, doctorId);
	        	List<PatientAppointmentTable> appointments = patientAppointmentRepository
	        	        .findAppointmentsByFilter(appointmentDate, appointmentType, doctorId)
	        	        .stream()
	        	        .sorted(Comparator.comparing(PatientAppointmentTable::getCreatedOn).reversed())
	        	        .collect(Collectors.toList()); 
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
	                details.put("token", appointment.getToken());

	            // Doctor details
	            details.put("doctorId", appointment.getDoctor().getUserId());
	            details.put("doctorName", appointment.getDoctor().getUserName());

	            // Fetching Patient Details
	            Optional<PatientDetails> patientDetailsOpt = patientDetailsRepository.findById(appointment.getPatient().getPatientDetailsId());
	            if (patientDetailsOpt.isPresent()) {
	                PatientDetails patient = patientDetailsOpt.get();
	                details.put("patientId", patient.getPatientDetailsId());
	             //   details.put("patientName", patient.getPatientName());
	                details.put("patientAge", patient.getAge());
	                details.put("patientDOB", patient.getDob());
	                details.put("gender", patient.getGender());
	                details.put("bloodGroup", patient.getBloodGroup());
	                details.put("mobileNumber", patient.getMobileNumber());
	                details.put("emailId", patient.getEmailId());
	                details.put("address", patient.getAddress());
	                details.put("emergencyContact", patient.getEmergencyContact());
	               
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
	 @Override
	 @Transactional
	 public ResponseEntity<?> saveDoctorAppoitment(HospitalDataListWebModel userWebModel) {
	     try {
	         if (userWebModel == null || userWebModel.getAppointmentId() == null) {
	             return ResponseEntity.badRequest().body(new Response(0, "error", "Appointment ID is required."));
	         }

	         Optional<PatientAppointmentTable> optionalAppointment = patientAppointmentRepository.findById(userWebModel.getAppointmentId());
	         if (optionalAppointment.isEmpty()) {
	             return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response(0, "error", "Appointment not found."));
	         }

	         PatientAppointmentTable appointment = optionalAppointment.get();

	         // Update appointment details
	         appointment.setAppointmentStatus("COMPLETED");
	         appointment.setDoctorFees(userWebModel.getDoctorFees());
	         appointment.setDoctorPrescription(userWebModel.getDoctorPrescription());
	         appointment.setFollowUpDate(userWebModel.getFollowUpDate());
	         appointment.setPharmacyStatus("PENDING");

	         Integer userId = userWebModel.getUserId(); // Who is saving this

	         float totalMedicalTestAmount = 0f;

	         // Save Medicines
	         if (userWebModel.getSchedules() != null) {
	             for (MedicineScheduleWebModel medSchedule : userWebModel.getSchedules()) {
	            	 Optional<Medicines> optionalMedicine = medicineRepository.findById(medSchedule.getMedicineId());
	            	 if (optionalMedicine.isPresent()) {
	            	     Medicines medicine = optionalMedicine.get();
	            	     int days = medSchedule.getDays();
	            	     Date currentDate = new Date();

	            	     // Calculate end date
	            	     Calendar cal = Calendar.getInstance();
	            	     cal.setTime(currentDate);
	            	     cal.add(Calendar.DATE, days);
	            	     Date prescribedEndDate = cal.getTime();

	            	     // Check expiry
	            	     Date expireDate = medicine.getExpireDate();
	            	     if (expireDate != null && prescribedEndDate.after(expireDate)) {
	            	         String formattedExpireDate = new SimpleDateFormat("yyyy-MM-dd").format(expireDate);
	            	         String errorMessage = String.format(
	            	             "The medicine '%s' will expire on %s. You cannot prescribe it for %d days.",
	            	             medicine.getName(), formattedExpireDate, days
	            	         );
	            	         return ResponseEntity.badRequest().body(new Response(0, "error", errorMessage));
	            	     }

	            	     // Save appointment medicine
	            	     AppointmentMedicine am = AppointmentMedicine.builder()
	            	         .appointment(appointment)
	            	         .medicine(medicine)
	            	         .isActive(true)
	            	         .createdBy(userId)
	            	         .updatedBy(userId)
	            	         .patientStatus(false)
	            	         .morningBF(medSchedule.getMorningBF())
	            	         .morningAF(medSchedule.getMorningAF())
	            	         .afternoonBF(medSchedule.getAfternoonBF())
	            	         .afternoonAF(medSchedule.getAfternoonAF())
	            	         .totalTabletCount(medSchedule.getTotalTabletCount())
	            	         .nightBF(medSchedule.getNightBF())
	            	         .nightAF(medSchedule.getNightAF())
	            	         .every6Hours(medSchedule.getEvery6Hours())
	            	         .every8Hours(medSchedule.getEvery8Hours())
	            	         .days(days)
	            	         .bottleCount(medSchedule.getBottleCount())
	            	         .ml(medSchedule.getMl())
	            	         .customizeDays(days)
	            	         .build();

	            	     appointmentMedicineRepository.save(am);
	            	 }


	               //  medicineRepository.findById(medSchedule.getMedicineId()).ifPresent(medicine -> {
//	                     AppointmentMedicine am = AppointmentMedicine.builder()
//	                         .appointment(appointment)
//	                         .medicine(medicine)
//	                         .isActive(true)
//	                         .createdBy(userId)
//	                         .updatedBy(userId)
//	                         .patientStatus(false)
//	                         .morningBF(medSchedule.getMorningBF())
//	                         .morningAF(medSchedule.getMorningAF())
//	                         .afternoonBF(medSchedule.getAfternoonBF())
//	                         .afternoonAF(medSchedule.getAfternoonAF())
//	                         .totalTabletCount(medSchedule.getTotalTabletCount())
//	                         .nightBF(medSchedule.getNightBF())
//	                         .nightAF(medSchedule.getNightAF())
//	                         .every6Hours(medSchedule.getEvery6Hours())
//	                         .every8Hours(medSchedule.getEvery8Hours())
//	                         .days(medSchedule.getDays())
//	                         .customizeDays(medSchedule.getDays())
//	                         .build();
//
//	                     appointmentMedicineRepository.save(am);
//	                 });
	             }
	         }

	         // Save Medical Tests and update slot statuses
	         if (userWebModel.getMedicalTests() != null) {
	             for (AppointmentMedicalTestWebModel testModel : userWebModel.getMedicalTests()) {
	                 Optional<MedicalTestConfig> testOpt = medicalTestConfigRepository.findById(testModel.getMedicalTestId());

	                 if (testOpt.isPresent()) {
	                     MedicalTestConfig testConfig = testOpt.get();

	                     AppointmentMedicalTest amt = AppointmentMedicalTest.builder()
	                         .appointment(appointment)
	                         .medicalTest(testConfig)
	                         .patientStatus(false)
	                         .isActive(true)
	                         .createdBy(userId)
	                         .updatedBy(userId)
	                         .medicalTestSlotSpiltTimeId(testModel.getMedicalTestSlotSpiltTimeId())
	                         .build();

	                     // Save test to get ID
	                     AppointmentMedicalTest savedAmt = appointmentMedicalTestRepository.save(amt);

	                     // Generate and update barcode
	                     String barcode = "MT" + savedAmt.getId() + String.format("%08d", new Random().nextInt(100_000_000));
	                     savedAmt.setMedicalTestBarCode(barcode);
	                     appointmentMedicalTestRepository.save(savedAmt);

	                     // Update Slot Status
	                     medicalTestSlotSpiltTimeRepository.findById(testModel.getMedicalTestSlotSpiltTimeId()).ifPresent(slotTime -> {
	                         slotTime.setSlotStatus("BOOKED");
	                         slotTime.setUpdatedBy(userId);
	                         slotTime.setUpdatedOn(new Date());
	                         medicalTestSlotSpiltTimeRepository.save(slotTime);
	                     });

	                     // Add MRP to total
	                     totalMedicalTestAmount += testConfig.getMrp();
	                 }
	             }
	         }

	         appointment.setTotalMedicalTestAmount(totalMedicalTestAmount);
	         patientAppointmentRepository.save(appointment);


	         Integer hospital = appointment.getDoctor().getHospitalId();
	        String appointmentDate = appointment.getAppointmentDate();

	         List<PatientAppointmentTable> sameDayAppointments =
	                 patientAppointmentRepository.findByHospitalAndAppointmentDateOrderByIdAsc(hospital, appointmentDate);

	         int minuteCounter = 5;

	         for (PatientAppointmentTable apt : sameDayAppointments) {
	             if ("PAID".equalsIgnoreCase(apt.getMedicineStatus())) {
	                 minuteCounter = 5;
	             } else if (apt.getMedicineStatus() == null) {
	                 apt.setReadyMinutesStatus(minuteCounter + " minutes");
	                 patientAppointmentRepository.save(apt);
	                 minuteCounter += 5;
	             }
	         }

	         // Send SMS to Patient
	         Optional<PatientDetails> patientOpt = patientDetailRepository.findById(appointment.getPatient().getPatientDetailsId());
	         if (patientOpt.isPresent()) {
	             PatientDetails patient = patientOpt.get();
	             String phoneNumber = patient.getMobileNumber();
	             String patientName = patient.getPatientName() != null ? patient.getPatientName() : "Patient";

	             try {


	                 String pharmacyMsg = String.format("Hope you feel better now. Your pharmacy order will be ready in next %s.", appointment.getReadyMinutesStatus());
	                 smsService.sendSms(phoneNumber, pharmacyMsg);
	             } catch (Exception smsEx) {
	                 // Log SMS error
	            	 return ResponseEntity.ok(new Response(1, "success", "Appointment updated successfully."));
	             }
	         }

	        

	         return ResponseEntity.ok(new Response(1, "success", "Appointment updated successfully."));
	     } catch (Exception e) {
	         e.printStackTrace();
//	         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//	             .body(new Response(0, "error", "An error occurred while updating the appointment."));
	         return ResponseEntity.ok(new Response(1, "success", "Appointment updated successfully."));
	     }
	 }


	 @Override
	 public ResponseEntity<?> getAllMedicineDetailsByHospitalId(Integer hospitalId) {
	     try {
	         List<Medicines> medicineList = medicineRepository.findByHospitalIdAndIsActiveTrue(hospitalId);

	         if (medicineList.isEmpty()) {
	             return ResponseEntity.ok(new Response(0, "success", "No medicines found for the given hospital ID."));
	         }

	         List<Map<String, Object>> result = medicineList.stream().map(med -> {
	             Map<String, Object> map = new HashMap<>();
	             map.put("id", med.getId());
	             map.put("name", med.getName());
	             map.put("medicineType", med.getMedicineType());
	             map.put("expireDate", med.getExpireDate());
	             return map;
	         }).collect(Collectors.toList());

	         return ResponseEntity.ok(new Response(0, "success", result));

	     } catch (Exception e) {
	         e.printStackTrace(); // Optional: use proper logging
	         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	             .body("An error occurred while fetching medicine details.");
	     }
	 }
	 @Override
	 public ResponseEntity<?> getAllMedicalTestByHospitalId(Integer hospitalId) {
	     try {
	         List<MedicalTest> medicalTests = medicalTestRepository.findByHospitalIdAndIsActiveTrue(hospitalId);

	         if (medicalTests.isEmpty()) {
	             return ResponseEntity.ok(new Response(0, "success", "No medical tests found for the given hospital ID."));
	         }

	         List<Map<String, Object>> result = medicalTests.stream().map(test -> {
	             Map<String, Object> map = new HashMap<>();
	             map.put("id", test.getId());
	             map.put("testName", test.getTestName());
	             return map;
	         }).collect(Collectors.toList());

	         return ResponseEntity.ok(new Response(0, "success", result));

	     } catch (Exception e) {
	         e.printStackTrace(); // Log properly in production
	         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	             .body("An error occurred while fetching medical tests.");
	     }
	 }
	 @Override
	 public ResponseEntity<?> getAllPatientPharamcyByHospitalIdAndDate(Integer hospitalId, String currentDate) {
	     try {
	         // Fetch all appointments for the given date
	         List<PatientAppointmentTable> appointments = patientAppointmentRepository.findByAppointmentDate(currentDate);

	         // Fetch all patient-hospital mappings for the given hospitalId
	         List<PatientMappedHospitalId> mappings = patientMappedHospitalIdRepository.findByHospitalId(hospitalId);

	         // Extract patient IDs that belong to the hospital
	         List<Integer> patientIds = mappings.stream()
	             .map(PatientMappedHospitalId::getPatientId)
	             .collect(Collectors.toList());

	         // Filter appointments and allow duplicates
	         List<Map<String, Object>> filteredData = appointments.stream()
	             .filter(app -> app.getPatient() != null
	                         && patientIds.contains(app.getPatient().getPatientDetailsId())
	                         && appointmentMedicineRepository.existsByAppointment(app))
	             .map(app -> {
	                 PatientDetails patient = app.getPatient();
	                 Map<String, Object> map = new HashMap<>();
	                 map.put("patientDetailsId", patient.getPatientDetailsId());
	                 map.put("patientName", patient.getPatientName());
	                 map.put("appointmentId", app.getAppointmentId());
	                 map.put("dob", patient.getDob());
	                 map.put("gender", patient.getGender());
	                 map.put("bloodGroup", patient.getBloodGroup());
	                 map.put("mobileNumber", patient.getMobileNumber());
	                 map.put("emailId", patient.getEmailId());
	                 map.put("pharmacyStatus", app.getPharmacyStatus());
	                 map.put("address", patient.getAddress());
	                 return map;
	             })
	             .collect(Collectors.toList()); // No .distinct()

	         return ResponseEntity.ok(new Response(1, "success", filteredData));

	     } catch (Exception e) {
	         e.printStackTrace();
	         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	             .body(new Response(0, "error", "An error occurred while fetching patient details."));
	     }
	 }


//	 @Override
//	 public ResponseEntity<?> getAllPatientPharamcyByHospitalIdAndDate(Integer hospitalId, String currentDate) {
//	     try {
//	         List<PatientAppointmentTable> appointments = patientAppointmentRepository.findByAppointmentDate(currentDate);
//
//	         List<PatientMappedHospitalId> mappings = patientMappedHospitalIdRepository.findByHospitalId(hospitalId);
//	         List<Map<String, Object>> filteredData = appointments.stream()
//	             .filter(app -> app.getPatient() != null
//	                         && app.getPatient()..equals(hospitalId)
//	                         && appointmentMedicineRepository.existsByAppointment(app)) // Check if medicine exists
//	             .map(app -> {
//	                 PatientDetails patient = app.getPatient();
//	                 Map<String, Object> map = new HashMap<>();
//	                 map.put("patientDetailsId", patient.getPatientDetailsId());
//	                 map.put("patientName", patient.getPatientName());
//	                 map.put("dob", patient.getDob());
//	                 map.put("gender", patient.getGender());
//	                 map.put("bloodGroup", patient.getBloodGroup());
//	                 map.put("mobileNumber", patient.getMobileNumber());
//	                 map.put("emailId", patient.getEmailId());
//	                 map.put("address", patient.getAddress());
//	                 return map;
//	             })
//	             .distinct()
//	             .collect(Collectors.toList());
//
//	         return ResponseEntity.ok(new Response(1, "success", filteredData));
//
//	     } catch (Exception e) {
//	         e.printStackTrace();
//	         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//	             .body(new Response(0, "error", "An error occurred while fetching patient details."));
//	     }
//	 }

	 @Override
	 public ResponseEntity<?> getAllPatientPharamcyBypatientIdAndDate(Integer patientId, String appointmentDate,Integer appontmentId) {
	     try {
	         // Use JOIN FETCH version to fetch medicines
	         List<PatientAppointmentTable> appointments =
	                 patientAppointmentRepository.findAppointmentsWithMedicines(patientId, appointmentDate, appontmentId);

	         Set<String> uniqueKeys = new HashSet<>();

	         List<Map<String, Object>> filteredData = appointments.stream()
	                 .filter(appointment -> {
	                     String key = appointment.getPatient().getPatientDetailsId() + "_" + appointment.getAppointmentDate();
	                     return uniqueKeys.add(key);
	                 })
	                 .map(appointment -> {
	                     Map<String, Object> map = new HashMap<>();

	                     map.put("doctorSlotId", appointment.getDoctorSlotId());
	                     map.put("daySlotId", appointment.getDaySlotId());
	                     map.put("timeSlotId", appointment.getTimeSlotId());
	                     map.put("appointmentDate", appointment.getAppointmentDate());
	                     map.put("slotStartTime", appointment.getSlotStartTime());
	                     map.put("slotEndTime", appointment.getSlotEndTime());
	                     map.put("slotTime", appointment.getSlotTime());
	                     map.put("isActive", appointment.getIsActive());
	                     map.put("createdBy", appointment.getCreatedBy());
	                     map.put("createdOn", appointment.getCreatedOn());
	                     map.put("updatedBy", appointment.getUpdatedBy());
	                     map.put("updatedOn", appointment.getUpdatedOn());
	                     map.put("appointmentStatus", appointment.getAppointmentStatus());
	                     map.put("patientNotes", appointment.getPatientNotes());
	                     map.put("doctorSlotStartTime", appointment.getDoctorSlotStartTime());
	                     map.put("doctorSlotEndTime", appointment.getDoctorSlotEndTime());
	                     map.put("appointmentType", appointment.getAppointmentType());
	                     map.put("age", appointment.getAge());
	                     map.put("dateOfBirth", appointment.getDateOfBirth());
	                     map.put("patientName", appointment.getPatientName());
	                     map.put("relationshipType", appointment.getRelationShipType());
	                     map.put("token", appointment.getToken());
	                     map.put("pharmacyStatus", appointment.getPharmacyStatus());
	                     map.put("labStatus", appointment.getLabStatus());
	                     map.put("appointmentId", appointment.getAppointmentId());
	                     map.put("totalMedicineAmount", appointment.getTotalMedicineAmount());
	                     map.put("totalMedicalTestAmount", appointment.getTotalMedicalTestAmount());
	                     

	                     // PatientDetails
	                     PatientDetails patient = appointment.getPatient();
	                     if (patient != null) {
	                         map.put("patientDetailsId", patient.getPatientDetailsId());
	                         map.put("patientName", patient.getPatientName());
	                         map.put("dob", patient.getDob());
	                         map.put("gender", patient.getGender());
	                         map.put("bloodGroup", patient.getBloodGroup());
	                         map.put("mobileNumber", patient.getMobileNumber());
	                         map.put("emailId", patient.getEmailId());
	                         map.put("address", patient.getAddress());
	                     }

	                     // AppointmentMedicines
	                     List<AppointmentMedicine> medicines = appointment.getAppointmentMedicines();
	                     List<Map<String, Object>> medicineList = new ArrayList<>();
	                     if (medicines != null && !medicines.isEmpty()) {
	                         for (AppointmentMedicine med : medicines) {
	                             Map<String, Object> medMap = new HashMap<>();
	                             medMap.put("appointmentMedicineId", med.getId());
	                             medMap.put("isActive", med.getIsActive());
	                             medMap.put("createdBy", med.getCreatedBy());
	                             medMap.put("createdOn", med.getCreatedOn());
	                             medMap.put("updatedBy", med.getUpdatedBy());
	                             medMap.put("updatedOn", med.getUpdatedOn());
	                             medMap.put("medicineStatus", med.getPatientStatus());
                          
	                             medMap.put("morningBF", med.getMorningBF());
	                             medMap.put("morningAF", med.getMorningAF());
	                             medMap.put("afternoonBF", med.getAfternoonBF());
	                             medMap.put("afternoonAF", med.getAfternoonAF());
	                             medMap.put("nightBF", med.getNightBF());
	                             medMap.put("nightAF", med.getNightAF());
	                             medMap.put("every6Hours", med.getEvery6Hours());
	                             medMap.put("amount", med.getAmount());
	                             medMap.put("prescribedTotalAmount", med.getPrescribedTotalAmount());
	                             medMap.put("every8Hours", med.getEvery8Hours());
	                             medMap.put("days", med.getDays());
	                             medMap.put("patientMedicineDays", med.getPatientMedicineDays());
	                             medMap.put("customizeDays", med.getCustomizeDays());
	                             medMap.put("totalTabletCount", med.getTotalTabletCount());
	                             medMap.put("ml", med.getMl());
	                             medMap.put("bottleCount", med.getBottleCount());
	                             medMap.put("dispensedTabletCount", med.getDispensedTabletCount());

	                             Medicines medicine = med.getMedicine();
	                             if (medicine != null) {
	                                 medMap.put("medicineId", medicine.getId());
	                                 medMap.put("name", medicine.getName());
	                                 medMap.put("price", medicine.getPrice());
	                                 medMap.put("manufacturerName", medicine.getManufacturerName());
	                                 medMap.put("type", medicine.getType());
	                                 medMap.put("packSizeLabel", medicine.getPackSizeLabel());
	                                 medMap.put("shortComposition1", medicine.getShortComposition1());
	                                 medMap.put("shortComposition2", medicine.getShortComposition2());
	                                 medMap.put("expireDate", medicine.getExpireDate());
	                             }

	                             medicineList.add(medMap);
	                         }
	                     }

	                     map.put("appointmentMedicines", medicineList);

	                     return map;
	                 })
	                 .collect(Collectors.toList());

	         return ResponseEntity.ok(new Response(1, "success", filteredData));

	     } catch (Exception e) {
	         e.printStackTrace();
	         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                 .body(new Response(0, "error", "An error occurred while fetching pharmacy appointments."));
	     }
	 }

	 @Override
	 public ResponseEntity<?> saveMedicineDetailByPharamacy(HospitalDataListWebModel userWebModel) {
	     try {
	         Integer appointmentId = userWebModel.getAppointmentId();

	         // Get Appointment
	         Optional<PatientAppointmentTable> appointmentOpt = patientAppointmentRepository.findById(appointmentId);
	         if (!appointmentOpt.isPresent()) {
	             return ResponseEntity.badRequest().body("Invalid appointment ID");
	         }

	         PatientAppointmentTable appointment = appointmentOpt.get();

	         List<HospitalDataListWebModel.MedicineDetail> medicineDetails = userWebModel.getMedicineDetails();
	         if (medicineDetails == null || medicineDetails.isEmpty()) {
	             return ResponseEntity.badRequest().body("No medicine details provided.");
	         }

	         float totalAmount = 0; // initialize total amount

	         for (HospitalDataListWebModel.MedicineDetail detail : medicineDetails) {
	             Integer medicineId = detail.getMedicineId();

	             List<AppointmentMedicine> existingMedicines =
	                 appointmentMedicineRepository.findByAppointmentAppointmentIdAndMedicineId(appointmentId, medicineId);

	             for (AppointmentMedicine existing : existingMedicines) {
	                 System.out.println("detail.getPatientStatus(): " + detail.getPatientStatus());
	                 existing.setPatientStatus(detail.getPatientStatus());
	                 existing.setCustomizeDays(detail.getPatientMedicineDays());
	                 existing.setUpdatedBy(appointment.getCreatedBy()); // or session user
                     existing.setPrescribedTotalAmount(detail.getPrescribedTotalAmount());
                     existing.setDispensedTabletCount(detail.getDispensedTabletCount());
	                 // Use wrapper Float to safely handle null
	                 Float amount = detail.getAmount();
	                 existing.setAmount(amount != null ? amount : 0f); // default to 0f if null
	                 totalAmount += (amount != null) ? amount : 0;

	                 existing.setUpdatedOn(new Date());
	                 appointmentMedicineRepository.save(existing);
	             }
	         }

	         // Set the calculated total medicine amount
	         appointment.setTotalMedicineAmount(totalAmount);

	         // Update pharmacy status to COMPLETED
	         appointment.setPharmacyStatus("INPROGRESS");

	         patientAppointmentRepository.save(appointment);

	         return ResponseEntity.ok(new Response(1, "success", "Medicines updated successfully"));
	     } catch (Exception e) {
	         e.printStackTrace();
	         return ResponseEntity.internalServerError().body(new Response(0, "error", "An error occurred: " + e.getMessage()));
	     }
	 }
	 
	
	 @Override
	 public ResponseEntity<?> getAllPatientMedicalTestByHospitalIdAndDate(Integer hospitalId, String currentDate) {
	     try {
	         // Step 1: Get appointments by date
	         List<PatientAppointmentTable> appointments = patientAppointmentRepository.findByAppointmentDate(currentDate);

	         // Filter appointments where:
	         // - Patient is not null
	         // - Medicines exist for the appointment
	         List<Map<String, Object>> filteredData = appointments.stream()
	             .filter(app -> app.getPatient() != null
	                         && appointmentMedicalTestRepository.existsByAppointment(app))
	             .sorted(Comparator.comparing(PatientAppointmentTable::getCreatedOn).reversed()) // Sort by createdOn descending
	             .map(app -> {
	                 PatientDetails patient = app.getPatient();
	                 Map<String, Object> map = new HashMap<>();
	                 map.put("patientDetailsId", patient.getPatientDetailsId());
	                 map.put("patientName", patient.getPatientName());
	                 map.put("dob", patient.getDob());
	                 map.put("gender", patient.getGender());
	                 map.put("bloodGroup", patient.getBloodGroup());
	                 map.put("mobileNumber", patient.getMobileNumber());
	                 map.put("emailId", patient.getEmailId());
	                 map.put("labStaus",app.getLabStatus());
	                 map.put("address", patient.getAddress());
	                 map.put("appointmentId", app.getAppointmentId());
	                 return map;
	             })
	             .distinct() // Optional: removes duplicate patient maps
	             .collect(Collectors.toList());

	         return ResponseEntity.ok(new Response(1, "success", filteredData));

	     } catch (Exception e) {
	         e.printStackTrace();
	         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	             .body(new Response(0, "error", "An error occurred while fetching patient details."));
	     }
	 }


	 @Override
	 public ResponseEntity<?> getAllPatientMedicalTestBypatientIdAndDate(Integer patientId, String appointmentDate,Integer appointmentId) {
	     try {
	         // Fetch appointments from the repository
	         List<PatientAppointmentTable> appointments =
	                 patientAppointmentRepository.findByPatient_PatientDetailsIdAndAppointmentDateAndAppointmentId(patientId, appointmentDate,appointmentId);

	         // Check if appointments exist
	         if (appointments == null || appointments.isEmpty()) {
	             return ResponseEntity.ok(Collections.emptyList());
	         }

	         // Transform to a list of maps for response
	         List<Map<String, Object>> response = appointments.stream()
	                 .map(appointment -> {
	                     Map<String, Object> map = new HashMap<>();

	                     // Appointment-related fields
	                     map.put("doctorSlotId", appointment.getDoctorSlotId());
	                     map.put("daySlotId", appointment.getDaySlotId());
	                     map.put("timeSlotId", appointment.getTimeSlotId());
	                     map.put("appointmentDate", appointment.getAppointmentDate());
	                     map.put("slotStartTime", appointment.getSlotStartTime());
	                     map.put("slotEndTime", appointment.getSlotEndTime());
	                     map.put("slotTime", appointment.getSlotTime());
	                     map.put("isActive", appointment.getIsActive());
	                     map.put("createdBy", appointment.getCreatedBy());
	                     map.put("createdOn", appointment.getCreatedOn());
	                     map.put("updatedBy", appointment.getUpdatedBy());
	                     map.put("updatedOn", appointment.getUpdatedOn());
	                     map.put("appointmentStatus", appointment.getAppointmentStatus());
	                     map.put("patientNotes", appointment.getPatientNotes());
	                     map.put("doctorSlotStartTime", appointment.getDoctorSlotStartTime());
	                     map.put("doctorSlotEndTime", appointment.getDoctorSlotEndTime());
	                     map.put("appointmentType", appointment.getAppointmentType());
	                     map.put("age", appointment.getAge());
	                     map.put("dateOfBirth", appointment.getDateOfBirth());
	                     map.put("patientName", appointment.getPatientName());
	                     map.put("relationshipType", appointment.getRelationShipType());
	                     map.put("token", appointment.getToken());
	                     map.put("pharmacyStatus", appointment.getPharmacyStatus());
	                     map.put("labStatus", appointment.getLabStatus());
	                     map.put("appointmentId", appointment.getAppointmentId());
	                     map.put("totalMedicalTestAmount", appointment.getTotalMedicalTestAmount());
	                     map.put("totalMedicineTestAmount", appointment.getTotalMedicineAmount());

	                     // Patient details
	                     PatientDetails patient = appointment.getPatient();
	                     if (patient != null) {
	                         map.put("patientDetailsId", patient.getPatientDetailsId());
	                         map.put("patientName", patient.getPatientName());
	                         map.put("dob", patient.getDob());
	                         map.put("gender", patient.getGender());
	                         map.put("bloodGroup", patient.getBloodGroup());
	                         map.put("mobileNumber", patient.getMobileNumber());
	                         map.put("emailId", patient.getEmailId());
	                         map.put("address", patient.getAddress());
	                     }

	                     // Appointment Medical Tests
	                     List<AppointmentMedicalTest> tests = appointment.getAppointmentMedicalTests();
	                     List<Map<String, Object>> testList = new ArrayList<>();

	                     if (tests != null && !tests.isEmpty()) {
	                         for (AppointmentMedicalTest test : tests) {
	                             Map<String, Object> testMap = new HashMap<>();
	                             testMap.put("appointmentMedicalTestId", test.getId());
	                             testMap.put("medicalTes", test.getMedicalTestBarCode());
	                             testMap.put("isActive", test.getIsActive());
	                             testMap.put("createdBy", test.getCreatedBy());
	                             testMap.put("createdOn", test.getCreatedOn());
	                             testMap.put("updatedBy", test.getUpdatedBy());
	                             testMap.put("updatedOn", test.getUpdatedOn());
	                             testMap.put("testStatus", test.getPatientStatus());
	                             boolean resultStatus = getResultStatusForAppointment(test.getId());
	                             testMap.put("resultStatus", resultStatus);

	                             // Medical Test Details
	                             Optional<MedicalTestConfig> medicalTestOpt = medicalTestConfigRepository.findById(test.getMedicalTest().getId());
	                             if (medicalTestOpt.isPresent()) {
	                                 MedicalTestConfig medicalTest = medicalTestOpt.get();
	                                 testMap.put("medicalTestId", medicalTest.getId());
	                                 testMap.put("testName", medicalTest.getMedicalTestName());
	                                 testMap.put("mrp", medicalTest.getMrp());
	                                 testMap.put("gst", medicalTest.getGst());
	                                 testMap.put("isActive", medicalTest.getIsActive());
	                             }

	                             // Slot Time Details
	                             Optional<MedicalTestSlotSpiltTime> slotTimeOpt = 
	                                     medicalTestSlotSpiltTimeRepository.findById(test.getMedicalTestSlotSpiltTimeId());

	                             if (slotTimeOpt.isPresent()) {
	                                 MedicalTestSlotSpiltTime slotTime = slotTimeOpt.get();
	                                 Map<String, Object> slotMap = new HashMap<>();
	                                 slotMap.put("medicalTestSlotSpiltTimeId", slotTime.getMedicalTestSlotSpiltTimeId());
	                                 slotMap.put("slotStartTime", slotTime.getSlotStartTime());
	                                 slotMap.put("slotEndTime", slotTime.getSlotEndTime());
	                                 slotMap.put("slotStatus", slotTime.getSlotStatus());
	                                 slotMap.put("createdBy", slotTime.getCreatedBy());
	                                 slotMap.put("createdOn", slotTime.getCreatedOn());
	                                 slotMap.put("updatedBy", slotTime.getUpdatedBy());
	                                 slotMap.put("updatedOn", slotTime.getUpdatedOn());
	                                 slotMap.put("isActive", slotTime.getIsActive());
	                                 slotMap.put("overriddenStatus", slotTime.getOvverridenStatus());

	                                 if (slotTime.getMedicalTestSlotDate() != null) {
	                                     slotMap.put("medicalTestSlotDateId", slotTime.getMedicalTestSlotDate().getMedicalTestSlotDateId());
	                                 }

	                                 testMap.put("medicalTestSlotSpiltTime", slotMap);
	                             }

	                             testList.add(testMap);
	                         }
	                     }

	                     map.put("appointmentMedicalTest", testList);
	                     return map;
	                 })
	                 .collect(Collectors.toList());

	         return ResponseEntity.ok(new Response(1, "success", response));

	     } catch (Exception e) {
	         e.printStackTrace();
	         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                 .body(new Response(0, "error", "An error occurred while fetching medical test appointments."));
	     }
	 }

	 private boolean getResultStatusForAppointment(Integer appointmentId) {
		    // Fetch only active media files related to the appointment with category 'resultDocument'
		    List<MediaFile> resultFiles = mediaFilesRepository
		        .findByFileDomainReferenceIdAndCategoryAndFileIsActiveTrue(appointmentId, MediaFileCategory.resutDocument);

		    // If there are result files, return true (1), otherwise return false (0)
		    return !resultFiles.isEmpty();  // If result files exist, status is true
		}


	 @Override
	 public ResponseEntity<?> deleteParticularSpiltSlot(Integer id) {
	     try {
	         Optional<DoctorSlotSpiltTime> optionalSlot = doctorSlotSpiltTimeRepository.findById(id);

	         if (!optionalSlot.isPresent()) {
	             return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                     .body(new Response(0, "Fail", "Slot not found with ID: " + id));
	         }

	         DoctorSlotSpiltTime slot = optionalSlot.get();
	         slot.setIsActive(false);  // Soft delete
	         slot.setUpdatedOn(new Date()); // Optional: update timestamp
	         doctorSlotSpiltTimeRepository.save(slot);

	         return ResponseEntity.ok(new Response(1, "Success", "Slot marked as inactive successfully"));
	     } catch (Exception e) {
	         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                 .body(new Response(0, "Error", "An error occurred while deleting the slot"));
	     }
	 }
	@Override
	public ResponseEntity<?> getAllPatientAppointment(String appointmentDate, Integer doctorId) {
		   try {
	            List<PatientAppointmentTable> appointments = patientAppointmentRepository.findAppointments(appointmentDate, doctorId);
	            
	            if (appointments.isEmpty()) {
	            	return ResponseEntity.ok(new Response(0, "No Appointments Found", null));
	            
	            }
	            // Sort appointments by createdOn descending
	            appointments.sort(Comparator.comparing(PatientAppointmentTable::getCreatedOn).reversed());
	            
	            List<Map<String, Object>> appointmentDetails = new ArrayList<>();

	            for (PatientAppointmentTable appointment : appointments) {
	                Map<String, Object> details = new HashMap<>();
	            // Appointment details
	                // Appointment details
	                details.put("appointmentId", appointment.getAppointmentId());
	                details.put("dischargeSummary", appointment.getDischargeSummary());
	                details.put("dischargeSummaryStatus", appointment.getDischargeSummaryStatus());
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
	                details.put("doctorprescription", appointment.getDoctorPrescription());
	                details.put("age", appointment.getAge());
	                details.put("dob", appointment.getDateOfBirth());
	                details.put("relationshipType", appointment.getRelationShipType());
	                details.put("patientName", appointment.getPatientName());
	                details.put("token", appointment.getToken());

	            // Doctor details
	            details.put("doctorId", appointment.getDoctor().getUserId());
	            details.put("doctorName", appointment.getDoctor().getUserName());

	            // Fetching Patient Details
	            Optional<PatientDetails> patientDetailsOpt = patientDetailsRepository.findById(appointment.getPatient().getPatientDetailsId());
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
	@Override
	public ResponseEntity<?> getAllPatientAppointmentDetails(Integer patientId, String appointmentDate,Integer appointmentId) {
	    try {
	        // Fetch all appointments with medicines and tests eagerly loaded
	        List<PatientAppointmentTable> appointments =
	                patientAppointmentRepository.findByPatient_PatientDetailsIdAndAppointmentDateAndAppointmentId(patientId, appointmentDate,appointmentId);

	        Set<String> uniqueKeys = new HashSet<>();

	        List<Map<String, Object>> filteredData = appointments.stream()
	            .filter(appointment -> uniqueKeys.add(appointment.getPatient().getPatientDetailsId() + "_" + appointment.getAppointmentDate()))
	            .map(appointment -> {
	                Map<String, Object> map = new HashMap<>();

	                // Appointment details
	                map.put("appointmentId", appointment.getAppointmentId());
	                map.put("dischargeSummary", appointment.getDischargeSummary());
	                map.put("dischargeStatus", appointment.getDischargeSummaryStatus());
	                map.put("doctorSlotSpiltTimeId",appointment.getDoctorSlotSpiltTimeId());
	                map.put("appointmentDate", appointment.getAppointmentDate());
	                map.put("doctorSlotId", appointment.getDoctorSlotId());
	                map.put("hospitalId", appointment.getDoctor().getHospitalId());
	                map.put("doctorName", appointment.getDoctor().getUserName());
	                
	                Integer hospitalId = appointment.getDoctor().getHospitalId();
	                String hospitalName = null;

	                if (hospitalId != null) {
	                    Optional<HospitalDataList> hospitalOptional = hospitalDataListRepository.findById(hospitalId);
	                    if (hospitalOptional.isPresent()) {
	                        hospitalName = hospitalOptional.get().getHospitalName();
	                    }
	                }

//	                // Add to map
//	                map.put("hospitalId", hospitalId);
	                map.put("hospitalName", hospitalName);
	                
	                map.put("daySlotId", appointment.getDaySlotId());
	                map.put("timeSlotId", appointment.getTimeSlotId());
	                map.put("slotStartTime", appointment.getSlotStartTime());
	                map.put("slotEndTime", appointment.getSlotEndTime());
	                map.put("slotTime", appointment.getSlotTime());
	                map.put("appointmentStatus", appointment.getAppointmentStatus());
	                map.put("patientNotes", appointment.getPatientNotes());
	                map.put("doctorSlotStartTime", appointment.getDoctorSlotStartTime());
	                map.put("doctorSlotEndTime", appointment.getDoctorSlotEndTime());
	                map.put("appointmentType", appointment.getAppointmentType());
	                map.put("age", appointment.getAge());
	                map.put("dateOfBirth", appointment.getDateOfBirth());
	                map.put("patientName", appointment.getPatientName());
	                map.put("relationshipType", appointment.getRelationShipType());
	                map.put("token", appointment.getToken());
	                map.put("pharmacyStatus", appointment.getPharmacyStatus());
	                map.put("labStatus", appointment.getLabStatus());
	                map.put("totalMedicineAmount", appointment.getTotalMedicineAmount());
	                map.put("totalmedicaltestamount", appointment.getTotalMedicalTestAmount());
	                map.put("doctorfees",appointment.getDoctorFees());
	                map.put("doctorfeesStatus", appointment.getDoctorFeesStatus());
	                map.put("doctorprescription", appointment.getDoctorPrescription());
	                User user = appointment.getDoctor();

	                // Doctor Roles
	                List<Map<String, Object>> roleDetails = new ArrayList<>();
	                if (user.getDoctorRoles() != null) {
	                    for (DoctorRole doctorRole : user.getDoctorRoles()) {
	                        if (Boolean.TRUE.equals(doctorRole.getUserIsActive())) {
	                            Map<String, Object> roleMap = new HashMap<>();
	                            roleMap.put("roleId", doctorRole.getRoleId());
	                            roleMap.put("isActive", doctorRole.getUserIsActive());
	                            try {
	                                String specialtyName = doctorSpecialtyRepository
	                                        .findSpecialtyNameByRoleId(doctorRole.getRoleId());
	                                roleMap.put("specialtyName", specialtyName != null ? specialtyName : "N/A");
	                            } catch (Exception e) {
	                                //logger.error("Error fetching specialty name for roleId {}: {}", doctorRole.getRoleId(), e.getMessage());
	                                roleMap.put("specialtyName", "Error retrieving");
	                            }
	                            roleDetails.add(roleMap);
	                        }
	                    }
	                }
	                map.put("roles", roleDetails);

	                // Audit fields
	                map.put("isActive", appointment.getIsActive());
	                map.put("createdBy", appointment.getCreatedBy());
	                map.put("createdOn", appointment.getCreatedOn());
	                map.put("updatedBy", appointment.getUpdatedBy());
	                map.put("updatedOn", appointment.getUpdatedOn());

	                // Patient details
	                PatientDetails patient = appointment.getPatient();
	                if (patient != null) {
	                    map.put("patientDetailsId", patient.getPatientDetailsId());
	                    map.put("patientName", patient.getPatientName());
	                    map.put("dob", patient.getDob());
	                    map.put("gender", patient.getGender());
	                    map.put("bloodGroup", patient.getBloodGroup());
	                    map.put("mobileNumber", patient.getMobileNumber());
	                    map.put("emailId", patient.getEmailId());
	                    map.put("address", patient.getAddress());
	                }

	                // Appointment Medicines
	                List<Map<String, Object>> medicineList = new ArrayList<>();
	                if (appointment.getAppointmentMedicines() != null) {
	                    for (AppointmentMedicine med : appointment.getAppointmentMedicines()) {
	                        Map<String, Object> medMap = new HashMap<>();
	                        medMap.put("appointmentMedicineId", med.getId());
	                        medMap.put("medicineStatus", med.getPatientStatus());
	                        medMap.put("days", med.getDays());
	                        medMap.put("patientMedicineDays", med.getPatientMedicineDays());
	                        medMap.put("morningBF", med.getMorningBF());
	                        medMap.put("morningAF", med.getMorningAF());
	                        medMap.put("afternoonBF", med.getAfternoonBF());
	                        medMap.put("afternoonAF", med.getAfternoonAF());
	                        medMap.put("nightBF", med.getNightBF());
	                        medMap.put("nightAF", med.getNightAF());
	                        medMap.put("every6Hours", med.getEvery6Hours());
	                        medMap.put("every8Hours", med.getEvery8Hours());
                            medMap.put("amount", med.getAmount());
                            medMap.put("prescribedAmount", med.getPrescribedTotalAmount());
                            medMap.put("totalTabletCount", med.getTotalTabletCount());
	                        // Audit fields
	                        medMap.put("isActive", med.getIsActive());
	                        medMap.put("createdBy", med.getCreatedBy());
	                        medMap.put("createdOn", med.getCreatedOn());
	                        medMap.put("updatedBy", med.getUpdatedBy());
	                        medMap.put("updatedOn", med.getUpdatedOn());
	                        medMap.put("customizeDays", med.getCustomizeDays());
	                        medMap.put("ml", med.getMl());
	                        medMap.put("bottleCount", med.getBottleCount());

	                        // Medicine details
	                        Medicines medicine = med.getMedicine();
	                        if (medicine != null) {
	                            medMap.put("medicineId", medicine.getId());
	                            medMap.put("name", medicine.getName());
	                            medMap.put("price", medicine.getPrice());
	                            medMap.put("manufacturerName", medicine.getManufacturerName());
	                            medMap.put("type", medicine.getType());
	                            medMap.put("packSizeLabel", medicine.getPackSizeLabel());
	                            medMap.put("shortComposition1", medicine.getShortComposition1());
	                            medMap.put("shortComposition2", medicine.getShortComposition2());
	                            medMap.put("expireDate", medicine.getExpireDate());
	                            medMap.put("medicineType", medicine.getMedicineType());	                        }

	                        medicineList.add(medMap);
	                    }
	                }
	                map.put("appointmentMedicines", medicineList);

	                // Appointment Tests
	                List<Map<String, Object>> testList = new ArrayList<>();
	                if (appointment.getAppointmentMedicalTests() != null) {
	                    for (AppointmentMedicalTest test : appointment.getAppointmentMedicalTests()) {
	                        Map<String, Object> testMap = new HashMap<>();
	                        testMap.put("appointmentMedicalTestId", test.getId());
	                        testMap.put("medicalTes", test.getMedicalTestBarCode());
	                        testMap.put("testStatus", test.getPatientStatus());
	                        testMap.put("isActive", test.getIsActive());
	                        testMap.put("createdBy", test.getCreatedBy());
	                        testMap.put("createdOn", test.getCreatedOn());
	                        testMap.put("updatedBy", test.getUpdatedBy());
	                        testMap.put("updatedOn", test.getUpdatedOn());

	                        // Medical Test details
	                        Optional<MedicalTestConfig> testConfig = medicalTestConfigRepository.findById(test.getMedicalTest().getId());
	                        testConfig.ifPresent(config -> {
	                            testMap.put("medicalTestId", config.getId());
	                            testMap.put("testName", config.getMedicalTestName());
	                            testMap.put("mrp", config.getMrp());
	                            testMap.put("gst", config.getGst());
	                            testMap.put("isActive", config.getIsActive());
	                        });

	                        // Medical Test Slot
	                        medicalTestSlotSpiltTimeRepository.findById(test.getMedicalTestSlotSpiltTimeId()).ifPresent(slot -> {
	                            testMap.put("medicalTestSlotSpiltTimeId", slot.getMedicalTestSlotSpiltTimeId());
	                            testMap.put("slotStartTime", slot.getSlotStartTime());
	                            testMap.put("slotEndTime", slot.getSlotEndTime());
	                            testMap.put("slotStatus", slot.getSlotStatus());
	                            testMap.put("createdBy", slot.getCreatedBy());
	                            testMap.put("createdOn", slot.getCreatedOn());
	                            testMap.put("updatedBy", slot.getUpdatedBy());
	                            testMap.put("updatedOn", slot.getUpdatedOn());
	                            testMap.put("isActive", slot.getIsActive());
	                        });

	                        testList.add(testMap);
	                    }
	                }
	                map.put("appointmentMedicalTests", testList);

	                return map;
	            })
	            .collect(Collectors.toList());

	        return ResponseEntity.ok(new Response(1, "success", filteredData));
	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(0, "error", "An error occurred while fetching appointment details."));
	    }
	}
	@Override
	public ResponseEntity<?> saveDoctorFees(HospitalDataListWebModel userWebModel) {
	    try {
	        Integer appointmentId = userWebModel.getAppointmentId();
	        Integer doctorFees = userWebModel.getDoctorFees();

	        // Fetch the appointment by ID
	        Optional<PatientAppointmentTable> optionalAppointment = patientAppointmentRepository.findById(appointmentId);

	        if (optionalAppointment.isPresent()) {
	            PatientAppointmentTable appointment = optionalAppointment.get();

	            // Update fees and status
	            appointment.setDoctorFees(doctorFees);

	            appointment.setUpdatedOn(new Date());

	            // Save back to DB
	            patientAppointmentRepository.save(appointment);

	            return ResponseEntity.ok(new Response(1,"Doctor fees and status updated successfully.",appointment.getDoctorFees()));
	        } else {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Appointment not found.");
	        }
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body("Error while saving doctor fees: " + e.getMessage());
	    }
	}
	@Override
	public ResponseEntity<?> updateFeesStatus(HospitalDataListWebModel userWebModel) {
	    try {
	        Integer appointmentId = userWebModel.getAppointmentId();

	        Optional<PatientAppointmentTable> optionalAppointment = patientAppointmentRepository.findById(appointmentId);

	        if (optionalAppointment.isPresent()) {
	            PatientAppointmentTable appointment = optionalAppointment.get();

	            // Update doctor fees status
	            if (userWebModel.getDoctorFeesStatus() != null) {
	                appointment.setDoctorFeesStatus(userWebModel.getDoctorFeesStatus());
	            }

	            // Logic for pharmacyStatus based on dispensedTabletCount vs totalTabletCount
	            if (userWebModel.getMedicineStatus() != null) {
	                List<AppointmentMedicine> medicineList = appointmentMedicineRepository.findByAppointmentIds(appointmentId);
	               
	                boolean allPaid = true;

	                for (AppointmentMedicine med : medicineList) {
	                    Integer dispensed = med.getDispensedTabletCount() != null ? med.getDispensedTabletCount() : 0;
	                    Integer total = med.getTotalTabletCount() != null ? med.getTotalTabletCount() : 0;

	                    if (!dispensed.equals(total)) {
	                        allPaid = false;
	                        break;
	                    }
	                }

	                if (!medicineList.isEmpty()) {
	                    if (allPaid) {
	                        appointment.setPharmacyStatus("Paid");
	                    } else {
	                        appointment.setPharmacyStatus("PartiallyPaid");
	                    }
	                }
	            }

	            // Lab status
	            if (userWebModel.getMedicalTestStatus() != null) {
	                appointment.setLabStatus(userWebModel.getMedicalTestStatus());
	            }

	            // Transaction IDs
	            if (userWebModel.getTransactionMedicalTestId() != null) {
	                appointment.setTransactionMedicalTestId(userWebModel.getTransactionMedicalTestId());
	            }
	            if (userWebModel.getTransactionMedicineId() != null) {
	                appointment.setTransactionMedicineId(userWebModel.getTransactionMedicineId());
	            }
	            if (userWebModel.getTransactionDoctorFeesId() != null) {
	                appointment.setTransactionDoctorFeesId(userWebModel.getTransactionDoctorFeesId());
	            }

	            // Timestamp
	            appointment.setUpdatedOn(new Date());
	            patientAppointmentRepository.save(appointment);

	            return ResponseEntity.ok(new Response(1, "Appointment status updated successfully.", appointment.getDoctorFees()));
	        } else {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Appointment not found.");
	        }

	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body("Error while updating status: " + e.getMessage());
	    }
	}


	@Override
	public ResponseEntity<?> getAllDoctorFilterByLocation(String location) {
	    try {
	        // Step 1: Get hospitals in the specified location
	        List<HospitalDataList> hospitals = hospitalDataListRepository
	                .findByCurrentAddressContainingIgnoreCaseAndUserIsActiveTrue(location);

	        if (hospitals.isEmpty()) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                    .body(new Response(0, "Failure", "No hospitals found in the given location"));
	        }

	        // Step 2: Get all active doctors
	        List<User> allDoctors = userRepository.findByUserTypeIgnoreCaseAndUserIsActiveTrue("DOCTOR");

	        // Step 3: Filter doctors based on hospital location
	        List<Map<String, Object>> doctorList = new ArrayList<>();

	        for (User doctor : allDoctors) {
	            Integer hospitalId = doctor.getHospitalId();
	            if (hospitalId == null) continue;

	            Optional<HospitalDataList> hospitalOpt = hospitals.stream()
	                    .filter(h -> h.getHospitalDataId().equals(hospitalId))
	                    .findFirst();

	            if (hospitalOpt.isEmpty()) continue;

	            HospitalDataList hospital = hospitalOpt.get();

	            // Compose full name
	            String fullName = (doctor.getFirstName() != null ? doctor.getFirstName() : "") +
	                              " " +
	                              (doctor.getLastName() != null ? doctor.getLastName() : "");
	            if (fullName.trim().isEmpty()) {
	                fullName = doctor.getUserName();
	            }

	            // Get specialties
	            List<DoctorSpecialty> specialties = doctorSpecialtyRepository.findSpecialtiesByUserId(doctor.getUserId());
	            List<String> specialtyNames = specialties.stream()
	                    .map(DoctorSpecialty::getSpecialtyName)
	                    .filter(Objects::nonNull)
	                    .collect(Collectors.toList());


	            List<MediaFile> files = mediaFileRepository.findByFileDomainIdAndFileDomainReferenceId(
	                    HealthCareConstant.ProfilePhoto, doctor.getUserId());

	            List<FileInputWebModel> profilePhotos = new ArrayList<>();
	            for (MediaFile mediaFile : files) {
	                FileInputWebModel input = new FileInputWebModel();
	                input.setFileName(mediaFile.getFileOriginalName());
	                input.setFileId(mediaFile.getFileId());
	                input.setFileSize(mediaFile.getFileSize());
	                input.setFileType(mediaFile.getFileType());

	                String fileData = Base64FileUpload.encodeToBase64String(imageLocation + "/profilePhoto",
	                        mediaFile.getFileName());
	                input.setFileData(fileData);

	                profilePhotos.add(input);
	            }

	            // Prepare final doctor map
	            Map<String, Object> doctorMap = new HashMap<>();
	            doctorMap.put("userId", doctor.getUserId());
	            doctorMap.put("userName", fullName.trim());
	            doctorMap.put("hospitalId", hospital.getHospitalDataId());
	            doctorMap.put("hospitalName", hospital.getHospitalName());
	            doctorMap.put("specialties", specialtyNames);
	            doctorMap.put("profilePhotos", profilePhotos); //  Added profile photos

	            doctorList.add(doctorMap);
	        }

	        if (doctorList.isEmpty()) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                    .body(new Response(0, "Failure", "No doctors found in the given location"));
	        }

	        return ResponseEntity.ok(new Response(1, "Success", doctorList));

	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(0, "Failure", "An error occurred while fetching doctor data"));
	    }
	}
	@Override
	public ResponseEntity<?> cancelAppointmentOnlineAndOffline(HospitalDataListWebModel userWebModel) {
	    try {
	        Optional<PatientAppointmentTable> optionalAppointment = patientAppointmentRepository.findById(userWebModel.getId());
	        Optional<DoctorSlotSpiltTime> optionalSlot = doctorSlotSpiltTimeRepository.findById(userWebModel.getDoctorSlotSpiltTimeId());

	        if (!optionalAppointment.isPresent()) {
	            return ResponseEntity.badRequest().body(new Response(-1, "Fail", "Appointment not found"));
	        }

	        if (!optionalSlot.isPresent()) {
	            return ResponseEntity.badRequest().body(new Response(-1, "Fail", "Slot not found"));
	        }

	        // Cancel the appointment
	        PatientAppointmentTable appointment = optionalAppointment.get();
	        appointment.setAppointmentStatus("CANCELLED");
	        appointment.setIsActive(true);
	        appointment.setUpdatedOn(new Date());
	        appointment.setUpdatedBy(userWebModel.getUserUpdatedBy());

	        patientAppointmentRepository.save(appointment);

	        // Update slot status to "Available"
	        DoctorSlotSpiltTime slot = optionalSlot.get();
	        slot.setSlotStatus("Available");
	        slot.setUpdatedBy(userWebModel.getUserUpdatedBy());
	        slot.setUpdatedOn(new Date());

	        doctorSlotSpiltTimeRepository.save(slot);

	        return ResponseEntity.ok(new Response(1, "Success", "Appointment cancelled and slot marked as Available"));
	    } catch (Exception e) {
	        return ResponseEntity.internalServerError().body(new Response(-1, "Fail", "Error occurred while cancelling appointment"));
	    }
	}
	@Override
	public ResponseEntity<?> rescheduleAppointmentOnlineAndOffline(HospitalDataListWebModel userWebModel) {
	    try {
	        Optional<PatientAppointmentTable> optionalAppointment = patientAppointmentRepository.findById(userWebModel.getId());

	        if (!optionalAppointment.isPresent()) {
	            return ResponseEntity.badRequest().body(new Response(-1, "Fail", "Appointment not found"));
	        }

	        // Get the existing appointment
	        PatientAppointmentTable appointment = optionalAppointment.get();

	        // Step 1: Free the old slot
	        Integer oldSlotId = appointment.getDoctorSlotSpiltTimeId();
	        if (oldSlotId != null) {
	            Optional<DoctorSlotSpiltTime> oldSlotOpt = doctorSlotSplitTimeRepository.findById(oldSlotId);
	            if (oldSlotOpt.isPresent()) {
	                DoctorSlotSpiltTime oldSlot = oldSlotOpt.get();
	                oldSlot.setSlotStatus("Available");
	                oldSlot.setUpdatedBy(userWebModel.getUserUpdatedBy());
	                oldSlot.setUpdatedOn(new Date());
	                doctorSlotSplitTimeRepository.save(oldSlot);
	            }
	        }

	        // Step 2: Update appointment with new details
	        appointment.setAppointmentStatus("RESCHEDULED");
	        Optional<User> db  = userRepository.findById(userWebModel.getUserId());
	        appointment.setDoctor(db.get());
	        appointment.setDoctorSlotId(userWebModel.getDoctorSlotId());
	        appointment.setDaySlotId(userWebModel.getDaySlotId());
	        appointment.setDoctorSlotSpiltTimeId(userWebModel.getDoctorSlotSpiltTimeId());
	        appointment.setAppointmentDate(userWebModel.getAppointmentDate());
	        appointment.setSlotStartTime(userWebModel.getSlotStartTime());
	        appointment.setSlotEndTime(userWebModel.getSlotEndTime());
	        appointment.setUpdatedOn(new Date());
	        appointment.setUpdatedBy(userWebModel.getUserUpdatedBy());
	        appointment.setIsActive(true);

	        patientAppointmentRepository.save(appointment);

	        // Step 3: Mark new slot as booked
	        Optional<DoctorSlotSpiltTime> newSlotOpt = doctorSlotSplitTimeRepository.findById(userWebModel.getDoctorSlotSpiltTimeId());
	        if (newSlotOpt.isPresent()) {
	            DoctorSlotSpiltTime newSlot = newSlotOpt.get();
	            newSlot.setSlotStatus("Booked");
	            newSlot.setUpdatedBy(userWebModel.getUserUpdatedBy());
	            newSlot.setUpdatedOn(new Date());
	            doctorSlotSplitTimeRepository.save(newSlot);
	        }

	        return ResponseEntity.ok(new Response(1, "Success", "Appointment rescheduled successfully"));

	    } catch (Exception e) {
	        return ResponseEntity.internalServerError().body(new Response(-1, "Fail", "Error occurred while rescheduling appointment"));
	    }
	}
	
	
	@Override
	public ResponseEntity<?> addDischargeSummaryByAppointmentId(PatientAppointmentWebModel userWebModel) {
	    try {
	        if (userWebModel.getAppointmentId() == null) {
	            return ResponseEntity.badRequest().body(new Response(0, "Fail", "Appointment ID is required"));
	        }

	        Optional<PatientAppointmentTable> optionalAppointment = patientAppointmentRepository.findById(userWebModel.getAppointmentId());

	        if (!optionalAppointment.isPresent()) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response(0, "Fail", "Appointment not found"));
	        }

	        PatientAppointmentTable appointment = optionalAppointment.get();

	        // Update discharge summary and status
	        appointment.setDischargeSummary(userWebModel.getDischargeSummary());
	        appointment.setDischargeSummaryStatus(true);
	        appointment.setUpdatedOn(new Date());
	        appointment.setUpdatedBy(userWebModel.getUpdatedBy()); // Assuming this is passed

	        patientAppointmentRepository.save(appointment);

	        return ResponseEntity.ok(new Response(1, "Success", "Discharge summary updated successfully"));
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(0, "Error", e.getMessage()));
	    }
	}






}

package com.annular.healthCare.webModel;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PatientDetailsWebModel {

	private Integer patientDetailsId;
	private String patientName;
	private String dob;
	private String gender;
	private String bloodGroup;
	private String mobileNumber;
	private String emailId;
	private String address;
	private Integer hospitalId;// map the hospital
	private String purposeOfVisit;
	private Integer doctorId;
	private Boolean userIsActive;
	private String currentAddress;
	private Integer createdBy;
	private Date userCreatedOn;
	private Integer userUpdatedBy;
	private Integer doctorSlotDateId;
	private Date userUpdatedOn;
	private String emergencyContact;
    private String dateOfBirth;
	private String previousMedicalHistory;
	private String insuranceDetails;
	private String insurerName;
	private String insuranceProvider;
	private String policyNumber;
	private Boolean disability;
	private String relationshipType;
	private String age;
	private List<MultipartFile> files;
	private List<FileOutputWebModel> filess;

	// Appointment-related Fields
	private Integer doctorSlotId;
	private Integer daySlotId;
	private Integer timeSlotId;
	private String appointmentDate;
	private Integer doctorFees;

	private String slotStartTime;
	private String slotEndTime;
	private String slotTime;
	private String patientNotes;
	private Integer otp;
	private String appointmentType;
    private Integer doctorSlotSpiltTimeId;
   private List<PatientAppointmentWebModel> appointments;
   private Integer PatientSubChildDetailsId;
   private List<PatientSubChildDetailsWebModel> subChildDetails;




}

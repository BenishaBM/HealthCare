package com.annular.healthCare.webModel;

import java.util.Date;

import javax.persistence.Column;

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
public class PatientAppointmentWebModel {
	
	 private Integer appointmentId;
	    private Integer doctorId;
	    private Integer doctorSlotId;
	    private Integer daySlotId;
	    private Integer timeSlotId;
	    private String appointmentDate;
	    private String slotStartTime;
	    private String slotEndTime;
	    private String slotTime;
	    private Boolean isActive;
	    private Integer createdBy;
	    private Date createdOn;
	    private Integer updatedBy;
	    private Date updatedOn;
	    private String appointmentStatus;
	    private String patientNotes;
	    private String age;
	    private String dob;
	    private String patientName;
	    private String relationShipType;
	    private String doctorPrescription;
	    private String medicineData;
	    private String token;

}

package com.annular.healthCare.webModel;

import java.util.Date;

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
	private Date dob;
	private String gender;
	private String bloodGroup;
	private String mobileNumber;
	private String emailId;
	private String address;
	private Integer hospitalId;//map the hospital
	private String purposeOfVisit;
	private Integer doctorId;
	private Boolean userIsActive;
	private String currentAddress;
	private Integer createdBy;
	private Date userCreatedOn;
	private Integer userUpdatedBy;
	private Date userUpdatedOn;
	private String emergencyContact;

}

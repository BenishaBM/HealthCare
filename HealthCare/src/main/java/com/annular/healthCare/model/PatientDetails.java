package com.annular.healthCare.model;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "patient_details")
@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PatientDetails {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "patientDetailsId")
	private Integer patientDetailsId;
	
	@Column(name = "patientName")
	private String patientName;
	
	@Column(name = "dob")
	private Date dob;
	
	@Column(name = "gender")
	private String gender;
	
	@Column(name = "bloodGroup")
	private String bloodGroup;

	@Column(name = "mobileNumber")
	private String mobileNumber;
	
	@Column(name = "emailId")
	private String emailId;
	
	@Column(name = "address")
	private String address;
	
	@Column(name = "emergencyContact")
	private String emergencyContact;
	
	@Column(name = "hospitalId")
	private Integer hospitalId;//map the hospital
	
	@Column(name = "purposeOfVisit")
	private String purposeOfVisit;
	
	@Column(name = "doctorId")
	private Integer doctorId;
	
	
	@Column(name = "userIsActive")
	private Boolean userIsActive;

	@Column(name = "current_address")
	private String currentAddress;

	@Column(name = "created_by")
	private Integer createdBy;

	@CreationTimestamp
	@Column(name = "user_created_on")
	private Date userCreatedOn;

	@Column(name = "user_updated_by")
	private Integer userUpdatedBy;

	@Column(name = "user_updated_on")
	@CreationTimestamp
	private Date userUpdatedOn;

}

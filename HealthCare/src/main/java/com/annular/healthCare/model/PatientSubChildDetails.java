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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "patientSubChildDetails")
@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PatientSubChildDetails {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "patientSubChildDetailsId")
	private Integer patientSubChildDetailsId;
	
	@Column(name = "patientName")
	private String patientName;
	
	@Column(name = "dob")
	private String dob;

	
	@Column(name = "gender")
	private String gender;
	
	@Column(name = "bloodGroup")
	private String bloodGroup;
	
	@Column(name = "address")
	private String address;
	
	@Column(name = "emergencyContact")
	private String emergencyContact;
	
	
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
	
	@Column(name = "previousMedicalHistory")
	private String previousMedicalHistory;
	
	@Column(name = "insuranceDetails")
	private String insuranceDetails;

	@Column(name = "insurerName")
	private String insurerName;
	
	@Column(name = "insuranceprovider")
	private String insuranceProvider;
	
	@Column(name = "policyNumber")
	private String policyNumber;
	
	@Column(name = "disability")
	private Boolean disability;
	
	@Column(name = "age")
	private String age;

	@Column(name = "patientDetailsId")
	private Integer patientDetailsId;
	
	@Column(name = "relationshipType")
	private String relationshipType;



}

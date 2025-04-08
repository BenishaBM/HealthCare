package com.annular.healthCare.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "patient_mapped_hospital_id")
@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PatientMappedHospitalId {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "patientMappedHospitalId")
	private Integer patientMappedHospitalId;
	
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
	
	@Column(name = "userIsActive")
	private Boolean userIsActive;
	
	@Column(name = "patientId")
	private Integer patientId;
	
	@Column(name = "hospitalId")
	private Integer hospitalId;
	
	@Column(name = "medicalHistoryStatus")
	private Boolean medicalHistoryStatus;
	
	@Column(name = "personalDataStatus")
	private Boolean personalDataStatus;
	

}

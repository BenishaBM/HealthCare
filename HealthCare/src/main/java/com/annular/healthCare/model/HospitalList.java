package com.annular.healthCare.model;

import java.util.Date;

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
@Table(name = "hospitalList")
@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class HospitalList {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "hospital_id")
	private Integer hospitalId;

	@Column(name = "emailId")
	private String emailId;

	@JsonIgnore
	@Column(name = "password")
	private String password;

	@Column(name = "phone_number")
	private String phoneNumber;

	@Column(name = "hospitalIsActive")
	private Boolean hospitalIsActive;

	@Column(name = "current_address")
	private String currentAddress;

	@Column(name = "created_by")
	private Integer createdBy;

	@CreationTimestamp
	@Column(name = "hospital_created_on")
	private Date hospitalCreatedOn;

	@Column(name = "hospital_updated_by")
	private Integer hospitalUpdatedBy;

	@Column(name = "hospital_updated_on")
	@CreationTimestamp
	private Date hospitalUpdatedOn;

	@Column(name = "hospitalName")
	private String hospitalName;

	@Column(name = "userName")
	private String userName;

}

package com.annular.healthCare.model;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
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
@Table(name = "hospitalDataList")
@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class HospitalDataList {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "hospitalDataId")
	private Integer hospitalDataId;

	@Column(name = "emailId")
	private String emailId;

	@Column(name = "phone_number")
	private String phoneNumber;

	@Column(name = "userIsActive")
	private Boolean userIsActive;

	@Column(name = "current_address")
	private String currentAddress;

	@Column(name = "created_by")
	private Integer createdBy;
	
	@Column(name = "addressLine1")
	private String addressLine1;
	
	@Column(name = "addressLine2")
	private String addressLine2;
	
	@Column(name = "countryCode")
	private String countryCode;

	@CreationTimestamp
	@Column(name = "user_created_on")
	private Date userCreatedOn;

	@Column(name = "user_updated_by")
	private Integer userUpdatedBy;

	@Column(name = "user_updated_on")
	@CreationTimestamp
	private Date userUpdatedOn;
	
	@Column(name = "hospitalName")
	private String hospitalName;
	
	@Column(name = "hospitalCode")
	private String hospitalCode;
	
	@Column(name = "hospitalLink")
	private String hospitalLink;
	
	@Column(name = "linkStaus")
	private Boolean linkstatus;
	
	// One hospital can have multiple admins
	@OneToMany(mappedBy = "hospitalDataList", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<HospitalAdmin> admins;

//	@OneToMany(mappedBy = "hospitalDataList", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//	private List<DoctorRole> doctorRoles;
	
	@OneToMany(mappedBy = "hospitalDataList", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<HospitalSpeciality> specialities;


}

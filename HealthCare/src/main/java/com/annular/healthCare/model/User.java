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
@Table(name = "users")
@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "userId")
	private Integer userId;

	@Column(name = "emailId")
	private String emailId;
	
	@Column(name = "firstName")
	private String firstName;
	
	@Column(name = "lastName")
	private String lastName;

	@JsonIgnore
	@Column(name = "password")
	private String password;

	@Column(name = "user_type")
	private String userType;

	@Column(name = "phone_number")
	private String phoneNumber;

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

	@Column(name = "userName")
	private String userName;

	@Column(name = "empId")
	private String empId;

	@Column(name = "gender")
	private String gender;
	
	@Column(name = "dob")
	private String dob;
	
	@Column(name = "yearOfExperience")
	private String yearOfExperiences;

	@Column(name = "hospitalId")
	private Integer hospitalId;//map the hospital in HospitalDataList
	
    // One user can have multiple doctor roles
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<DoctorRole> doctorRoles;
    
    @Column(name = "doctorFees")
    private Integer doctorFees;
    
	@Column(name = "supportStaffId")
	private Integer supportStaffId;
	
	@Column(name = "labMasterDataId")
	private Integer labMasterDataId;
}

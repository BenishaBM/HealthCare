package com.annular.healthCare.webModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
public class HospitalDataListWebModel {

	private Integer hospitalDataId;
	private Integer hospitalId;
	private String userName;
	private String emailId;
	private String password;
	private String phoneNumber;
	private Boolean userIsActive;
	private String currentAddress;
	private Integer createdBy;
	private Date userCreatedOn;
	private Integer userUpdatedBy;
	private Date userUpdatedOn;
	private String empId;
	private String gender;
	private String userType;
	private Integer fileId;
	private Date dateOfBirth;
	private List<Integer> roles; // Add this field to hold the roles (e.g., ["ROLE_DOCTOR", "ROLE_ADMIN"])
	private ArrayList<FileInputWebModel> filesInputWebModel;

}

package com.annular.healthCare.webModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.web.multipart.MultipartFile;

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
	private Integer userId;
	private String password;
	private String phoneNumber;
	private String firstName;
	private String lastName;
	private Boolean userIsActive;
	private String currentAddress;
	private Integer createdBy;
	private String yearOfExperiences;
	private Date userCreatedOn;
	private Integer userUpdatedBy;
	private Date userUpdatedOn;
	private String hospitalName;
	private List<Integer> roleIds; // List of role IDs (multiple roles can be assigned)
	private String empId;
	private String gender;
	private String userType;
	private Integer fileId;
	private List<HospitalAdminWebModel> admins; // List of admins if provided
	private Date dateOfBirth;
	private List<Integer> roles; // Add this field to hold the roles (e.g., ["ROLE_DOCTOR", "ROLE_ADMIN"])
	private ArrayList<FileInputWebModel> filesInputWebModel;
	 // For document uploads (Insurance card, Aadhaar, PAN etc.)
    private List<MultipartFile> files;

}

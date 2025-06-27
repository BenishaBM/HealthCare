package com.annular.healthCare.webModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
public class UserWebModel {

	private Integer userId;
	private String emailId;
	private String password;
	private String userType;
	private String phoneNumber;
	private Boolean userIsActive;
	private String currentAddress;
	private Integer createdBy;
	private Date userCreatedOn;
	private Integer userUpdatedBy;
	private Date userUpdatedOn;
	private String userName;
	private String empId;
	private String yearOfExperiences;
	private String countryCode;
	private String gender;
	private Integer hospitalId;
	private String firstName;
	private String lastName;
	private List<Integer> roleIds; // List of role IDs (multiple roles can be assigned)
	private String token;
	private String hospitalName;
	private ArrayList<FileInputWebModel> filesInputWebModel;
	private List<DoctorDaySlotWebModel> doctorDaySlots;
	private List<DoctorLeaveListWebModel> doctorLeaveList;
    private Integer patientId;
	private Boolean medicalHistoryStatus;
	private Boolean personalDataStatus;
	private Integer doctorfees;
	private Integer supportStaffId;
	private Integer labMasterDataId;
	private String dob;
	private Integer doctorSlotSpiltTimeId;
    private String name;
    private String country;
    private String businessName;
    private String businessNameType;
    private String remarks;
    private String websiteUrl;
    private String email;
    private String city;
    private String mobileNumber;

	

	

}

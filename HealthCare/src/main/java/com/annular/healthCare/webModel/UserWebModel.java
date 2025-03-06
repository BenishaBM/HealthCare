package com.annular.healthCare.webModel;

import java.util.ArrayList;
import java.util.Date;

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
	private String gender;
	private Integer hospitalId;
	private String firstName;
	private String lastName;
	private String token;
	private String hospitalName;
	private ArrayList<FileInputWebModel> filesInputWebModel;
	

}

package com.annular.healthCare.webModel;

import java.util.Date;

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
public class HospitalWebModel {

	private Integer hospitalId;
	private String emailId;
	private String password;
	private String phoneNumber;
	private Boolean hospitalIsActive;
	private String currentAddress;
	private Integer createdBy;
	private Date hospitalCreatedOn;
	private Integer hospitalUpdatedBy;
	private Date hospitalUpdatedOn;
	private String hospitalName;
	private String userName;

}

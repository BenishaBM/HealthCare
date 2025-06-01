package com.annular.healthCare.webModel;

import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PatientSubChildDetailsWebModel {
	
    private Integer patientSubChildDetailsId;
    private String patientName;
    private String dob;
    private String gender;
    private String bloodGroup;
    private String address;
    private String emergencyContact;
    private String purposeOfVisit;
    private Integer doctorId;
    private Boolean userIsActive;
    private String currentAddress;
    private Integer createdBy;
    private Date userCreatedOn;
    private Integer userUpdatedBy;
    private Date userUpdatedOn;
    private String previousMedicalHistory;
    private String insuranceDetails;
    private String insurerName;
    private String insuranceProvider;
    private String policyNumber;
    private Boolean disability;
    private String age;
    private Integer patientDetailsId;
    private String relationshipType;
    private List<FileInputWebModel> mediaFiless;


}

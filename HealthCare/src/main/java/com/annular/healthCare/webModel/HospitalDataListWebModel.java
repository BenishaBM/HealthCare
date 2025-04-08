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
    private List<DoctorLeaveListWebModel> doctorLeaveList;
	private List<DoctorDaySlotWebModel> doctorDaySlots;
	private Integer doctorSlotId;
    private String doctorPrescription;
    private String medicineData;
    private String token;
    private Integer appointmentId;
    private List<Integer> medicineIds;
    private List<Integer> medicalTestIds;
    private List<MedicineDetail> medicineDetails;
    private List<MedicineDetail> medicines;
    private Integer morningBF;
    private Integer morningAF;
    private Integer afternoonBF;
    private Integer afternoonAF;
    private Integer nightBF;
    private Integer nightAF;
    private Boolean every6Hours;
    private Boolean every8Hours;
    private Integer days;
    // Medicine Schedule
    private List<MedicineScheduleWebModel> schedules;
    
    @Getter
    @Setter
    public static class MedicineDetail {
        private Integer medicineId;
        private Boolean patientStatus;
    }




}

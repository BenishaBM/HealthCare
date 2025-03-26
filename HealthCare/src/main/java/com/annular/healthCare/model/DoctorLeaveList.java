package com.annular.healthCare.model;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Entity
@Table(name = "doctor_leave_list")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DoctorLeaveList {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "doctorLeaveListId")
	private Integer doctorLeaveListId;
	
	@Column(name = "doctorLeaveDate")
	private Date doctorLeaveDate;
	
	@Column(name = "userId")
	private Integer userId;
	
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
	
	@Column(name = "userIsActive")
	private Boolean userIsActive;

}

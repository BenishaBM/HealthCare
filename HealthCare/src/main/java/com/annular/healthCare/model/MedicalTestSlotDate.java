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
import lombok.ToString;

@Entity
@Table(name = "medicalTestSlotDate")
@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class MedicalTestSlotDate {
	
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "medicalTestSlotDateId")
	private Integer medicalTestSlotDateId;
	
	@Column(name = "medicalTestSlotId")
	private Integer medicalTestSlotId;
	
	@Column(name = "medicalTestDaySlotId")
	private Integer medicalTestDaySlotId;
	
	@Column(name = "medicalTestSlotTimeId")
	private Integer medicalTestSlotTimeId;
	
	@Column(name = "created_by")
	private Integer createdBy;

	@CreationTimestamp
	@Column(name = "created_on")
	private Date createdOn;

	@Column(name = "updated_by")
	private Integer updatedBy;

	@Column(name = "updated_on")
	@CreationTimestamp
	private Date updatedOn;
	
	@Column(name = "isActive")
	private Boolean isActive;
	
	@Column(name = "date")
	private String date;

}

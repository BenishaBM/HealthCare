package com.annular.healthCare.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "medicalTestSlotSpiltTime")
@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class MedicalTestSlotSpiltTime {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "medicalTestSlotSpiltTimeId")
	private Integer medicalTestSlotSpiltTimeId;
	
	@Column(name = "slotStartTime")
	private String slotStartTime;
	
	@Column(name = "slotEndTime")
	private String slotEndTime;
	
	@Column(name = "slotStatus")
	private String slotStatus;
	
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
	
	@Column(name = "overriddenStatus")
	private String ovverridenStatus;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "medicalTestSlotDateId", referencedColumnName = "medicalTestSlotDateId")
	private MedicalTestSlotDate medicalTestSlotDate;


}

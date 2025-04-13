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
@Table(name = "MedicalTestSlot")
@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class MedicalTestSlot {
	
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "medicalTestSlotId")
	private Integer medicalTestSlotId;
	
    // Correcting relationship: Many DoctorRole records can belong to one User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", referencedColumnName = "id", insertable = true, updatable = true)
    private Department department;
    
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

}

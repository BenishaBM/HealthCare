package com.annular.healthCare.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
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
@Table(name = "medicalTestSlotTimeOveride")
@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class MedicalTestSlotTimeOveride {
	
	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Integer overrideId;

	    @ManyToOne
	    @JoinColumn(name = "original_slot_id", nullable = false)
	    private MedicalTestSlotTime originalSlot;

	    @Column(name = "overrideDate")
	    private String overrideDate;

	    @Column(name = "newSlotTime")
	    private String newSlotTime;

	    @Column(name = "reason")
	    private String reason;

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

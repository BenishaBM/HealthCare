package com.annular.healthCare.model;

import java.util.Date;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import javax.persistence.Id;

@Entity
@Table(name = "doctorSlotTimeOverride")
@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class DoctorSlotTimeOverride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer overrideId;

    @ManyToOne
    @JoinColumn(name = "original_slot_id", nullable = false)
    private DoctorSlotTime originalSlot;

    @Column(name = "overrideDate")
    private Date overrideDate;

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

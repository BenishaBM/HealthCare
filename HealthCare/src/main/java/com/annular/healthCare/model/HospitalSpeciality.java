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
@Table(name = "hospital_speciality")  // Naming the table as `hospital_admin` for clarity
@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class HospitalSpeciality {
	
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "speciality_id", nullable = false)
    private Integer specialityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_data_id") // FK column to hospital table
    private HospitalDataList hospitalDataList;
    
    @CreationTimestamp
    @Column(name = "user_created_on")
    private Date userCreatedOn;  // Timestamp of when the admin was created

    @Column(name = "user_updated_by")
    private Integer userUpdatedBy;  // ID of the user who last updated this record

    @CreationTimestamp
    @Column(name = "user_updated_on")
    private Date userUpdatedOn;  // Timestamp of when the admin was last updated
    
    @Column(name = "userIsActive")
    private Boolean userIsActive;

}

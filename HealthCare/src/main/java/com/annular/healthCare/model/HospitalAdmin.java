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
@Table(name = "hospital_admin")  // Naming the table as `hospital_admin` for clarity
@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class HospitalAdmin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "adminId")  // Primary Key Column
    private Integer adminId;

    @Column(name = "userIsActive")
    private Boolean userIsActive;  // This indicates whether the admin is active or not

    @Column(name = "created_by")
    private Integer createdBy;  // User ID or admin ID who created the entry

    @Column(name = "adminUserId")  // Renamed to clarify it represents admin's user ID
    private Integer adminUserId;  // Reference to the user ID of the admin

    @CreationTimestamp
    @Column(name = "user_created_on")
    private Date userCreatedOn;  // Timestamp of when the admin was created

    @Column(name = "user_updated_by")
    private Integer userUpdatedBy;  // ID of the user who last updated this record

    @CreationTimestamp
    @Column(name = "user_updated_on")
    private Date userUpdatedOn;  // Timestamp of when the admin was last updated

    // Many admins belong to one hospital
    @ManyToOne(fetch = FetchType.LAZY)  // Lazy loading to prevent unnecessary loading
    @JoinColumn(name = "hospitalDataId")  // Foreign key to HospitalDataList table
    private HospitalDataList hospitalDataList;  // The hospital this admin belongs to

}

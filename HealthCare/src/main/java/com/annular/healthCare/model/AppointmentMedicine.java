package com.annular.healthCare.model;

import java.util.Date;

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


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "appointment_medicines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentMedicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", referencedColumnName = "appointment_id")
    private PatientAppointmentTable appointment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", referencedColumnName = "id") // Assuming Medicines has 'id'
    private Medicines medicine;

    @Column(name = "is_active")
    private Boolean isActive;
    
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
	
    @Column(name = "patientStatus")
    private Boolean patientStatus;
    
    // Morning
    @Column(name = "morningBF")
    private Integer morningBF;
    
    @Column(name = "morningAF")
    private Integer morningAF;

    // Afternoon
    @Column(name = "afternoonBF")
    private Integer afternoonBF;
    
    @Column(name = "afternoonAF")
    private Integer afternoonAF;

    // Night
    @Column(name = "nightBF")
    private Integer nightBF;
    
    @Column(name = "nightAF")
    private Integer nightAF;

    // Other frequency types
    @Column(name = "every6Hours")
    private Boolean every6Hours;
    
    @Column(name = "every8Hours")
    private Boolean every8Hours;

    @Column(name = "days")
    private Integer days; // number of days for the medication
    
    @Column(name = "patientMedicineDays")
    private Integer patientMedicineDays;
    
    @Column(name = "amount")
    private float amount;
    
    @Column(name = "customizeDays")
    private Integer customizeDays;
}

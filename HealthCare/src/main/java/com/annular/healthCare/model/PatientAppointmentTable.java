package com.annular.healthCare.model;

import java.util.Date;
import java.util.List;

import javax.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import lombok.*;

@Entity
@Table(name = "patient_appointment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class PatientAppointmentTable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "appointment_id")
    private Integer appointmentId;

    // Relationship with User (Doctor)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", referencedColumnName = "userId")
    private User doctor;

    // Relationship with User (Patient)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", referencedColumnName = "patientDetailsId")
    private PatientDetails patient;

    @Column(name = "doctor_slot_id")
    private Integer doctorSlotId;//foregignKey for DoctorSlot table

    @Column(name = "day_slot_id")
    private Integer daySlotId; //foreignKey for DoctorDaySlot

    @Column(name = "time_slot_id")
    private Integer timeSlotId; //foreignKey for DoctorSlotTime

    @Column(name = "appointment_date")
    private String appointmentDate;


    @Column(name = "slot_start_time")
    private String slotStartTime;

    @Column(name = "slot_end_time")
    private String slotEndTime;

    @Column(name = "slot_time")
    private String slotTime;

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
    private Date updatedOn;

    // Additional fields you might want to add
    @Column(name = "appointment_status")
    private String appointmentStatus; // e.g., "SCHEDULED", "COMPLETED", "CANCELLED"

    @Column(name = "patient_notes")
    private String patientNotes;
    
    @Column(name = "doctorSlotStartTime")
    private String doctorSlotStartTime;
    
    @Column(name = "doctorSlotEndTime")
    private String doctorSlotEndTime;
    
    @Column(name = "appointmentType")
    private String appointmentType;
    
    @Column(name = "age")
    private String age;
    
    @Column(name = "dateOfBirth")
    private String dateOfBirth;
    
    @Column(name = "name")
    private String patientName;
    
    @Column(name = "relationshipType")
    private String relationShipType;
        
    @Column(name = "token")
    private String token;
    
    @Column(name = "doctorPrescription")
    private String doctorPrescription;
   
    @Column(name = "pharmacyStatus")
    private String pharmacyStatus;
    
    @Column(name = "labStatus")
    private String labStatus;
    
    @OneToMany(mappedBy = "appointment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AppointmentMedicalTest> appointmentMedicalTests;
    
    @OneToMany(mappedBy = "appointment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AppointmentMedicine> appointmentMedicines;
    
    @Column(name = "doctorFees")
    private Integer doctorFees;
    
    @Column(name = "doctorsFeesStatus")
    private String doctorFeesStatus;
    
    @Column(name = "medicineStatus")
    private String medicineStatus;
    
    @Column(name = "medicalTestStatus")
    private String medicalTestStatus;

    @Column(name = "totalMedicineAmount")
    private float totalMedicineAmount;
    
    @Column(name = "totalMedicalTestAmount")
    private float totalMedicalTestAmount;
    
    @Column(name = "followUpDate")
    private String followUpDate;
    
}
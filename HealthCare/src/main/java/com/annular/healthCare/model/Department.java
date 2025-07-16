package com.annular.healthCare.model;

import javax.persistence.*;

import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;
import java.util.List;

import lombok.*;

@Entity
@Table(name = "departments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MedicalTestConfig> medicalTests;
    
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
	
	@Column(name = "hospitalId")
	private Integer hospitalId;
}

package com.annular.healthCare.webModel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.CreationTimestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class MedicalTestConfigWebModel {
	

	    private Integer id; 
	    private String department;
	    private String medicalTestName;
	    private float mrp;
	    private BigDecimal gst;
		private Integer createdBy;
		private Date createdOn;
		private Integer updatedBy;
		private Date updatedOn;
		private Boolean isActive;
		private Integer hospitalId;
		private List<String> medicalTestNames;
	    private List<Integer> medicalIds;
	    private List<MedicalTestDto> tests; // List of MedicalTestDto
	    private List<MedicalTestDaySlotWebModel> medicalTestDaySlots;
	    private Integer medicalTestSlotId;
	    private Integer departmentId;
	    private List<DaySlotWebModel> daySlots;
	    private String name;
	    private String ovverridenStatus;
	    private Date overrideDate;
	    private String newSlotTime;
	    private String reason;
	    private Integer medicalTestSlotTimeId;


}

package com.annular.healthCare.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bookingDemo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingDemo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

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

    // Newly added fields
    @Column(name = "name")
    private String name;

    @Column(name = "country")
    private String country;

    @Column(name = "business_name")
    private String businessName;

    @Column(name = "business_name_type")
    private String businessNameType;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "website_url")
    private String websiteUrl;

    @Column(name = "email")
    private String email;

    @Column(name = "city")
    private String city;

    @Column(name = "mobile_number")
    private String mobileNumber;

    @Column(name = "country_code")
    private String countryCode;
}

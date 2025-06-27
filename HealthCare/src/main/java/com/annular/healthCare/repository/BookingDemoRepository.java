package com.annular.healthCare.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.annular.healthCare.model.BookingDemo;

@Repository
public interface BookingDemoRepository extends JpaRepository<BookingDemo,Integer>{

	Page<BookingDemo> findByIsActiveTrue(Pageable pageable);

}

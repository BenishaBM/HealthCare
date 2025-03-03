package com.annular.healthCare.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.annular.healthCare.model.User;


@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

	@Query("SELECT u FROM User u WHERE u.emailId = :email")
	Optional<User> findByEmailId(String email);

	@Query("SELECT u FROM User u WHERE u.emailId = :emailId")
	Optional<User> findByEmailIds(String emailId);

}

package com.annular.healthCare.security;

import java.util.Collection;

import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.annular.healthCare.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class UserDetailsImpl implements UserDetails {
	private static final long serialVersionUID = 1L;

	private final Integer id;
	private final String userName;
	private final String userEmailId;
	private final String userType;

	@JsonIgnore
	private final String password;

	private final Collection<? extends GrantedAuthority> authorities;

	public UserDetailsImpl(Integer id, String userName, String userEmailId, String userType, String password,
			Collection<? extends GrantedAuthority> authorities) {
		this.id = id;
		this.userName = userName;
		this.userEmailId = userEmailId;
		this.userType = userType;
		this.password = password;
		this.authorities = authorities;
	}

	public static UserDetailsImpl build(User user) {
		return new UserDetailsImpl(user.getUserId(), user.getUserName(), // Ensure it correctly returns `userName`
				user.getEmailId(), user.getUserType(), user.getPassword(), Set.of() // Default empty authorities, modify
																					// as needed
		);
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	public Integer getId() {
		return id;
	}

	public String getUserEmailId() {
		return userEmailId;
	}

	public String getUserType() {
		return userType;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return userName;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public String toString() {
		return "UserDetailsImpl{" + "id=" + id + ", userName='" + userName + '\'' + ", userEmailId='" + userEmailId
				+ '\'' + ", userType='" + userType + '\'' + '}';
	}
}
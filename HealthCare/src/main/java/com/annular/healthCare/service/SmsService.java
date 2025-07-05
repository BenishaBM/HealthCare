package com.annular.healthCare.service;

public interface SmsService {
	
	void sendSms(String to, String message);

	public void sendEmail(String toEmail, String body);

}

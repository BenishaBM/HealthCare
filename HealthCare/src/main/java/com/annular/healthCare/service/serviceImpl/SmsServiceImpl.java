package com.annular.healthCare.service.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.annular.healthCare.Config.TwilioConfig;
import com.annular.healthCare.service.SmsService;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SmsServiceImpl implements SmsService {
	
	 @Autowired
	    private TwilioConfig twilioConfig;
	@Autowired
	private JavaMailSender mailSender;

	    public void sendSms(String toPhoneNumber, String messageBody) {
	        Message message = Message.creator(
	                new PhoneNumber(toPhoneNumber),
	                new PhoneNumber(twilioConfig.getFromPhoneNumber()),
	                messageBody
	        ).create();

	        System.out.println("SMS sent with SID: " + message.getSid());
	    }

	public void sendEmail(String toEmail, String body) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(toEmail);
		message.setSubject("Password Changed ✔");
		message.setText(body);
		mailSender.send(message);
	}

}

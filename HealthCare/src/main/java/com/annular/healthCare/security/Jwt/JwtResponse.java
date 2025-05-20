package com.annular.healthCare.security.Jwt;

import java.util.List;

import com.annular.healthCare.webModel.FileInputWebModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class JwtResponse {




	private String jwt;
    private Integer id;
    private Integer status;
    private String token;
    private String userType;
    private String emailId;
    private Integer hospitalId;
    private String hospitalName;
    private List<FileInputWebModel> hospitalLogo;
    private String hospitalCode;
   
}

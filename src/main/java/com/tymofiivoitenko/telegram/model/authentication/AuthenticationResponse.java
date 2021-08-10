package com.tymofiivoitenko.telegram.model.authentication;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class AuthenticationResponse {

    private String jwt;
}

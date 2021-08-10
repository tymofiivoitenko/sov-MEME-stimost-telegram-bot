package com.tymofiivoitenko.telegram.web;

import com.tymofiivoitenko.telegram.model.authentication.AuthenticationRequest;
import com.tymofiivoitenko.telegram.model.authentication.AuthenticationResponse;
import com.tymofiivoitenko.telegram.model.user.User;
import com.tymofiivoitenko.telegram.service.JWTService;
import com.tymofiivoitenko.telegram.service.MyUserDetailsService;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
public class HomeResource {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private MyUserDetailsService userDetailsService;

    @Autowired
    private JWTService jwtService;

    @GetMapping("/")
    public String home() {
        return "<h1>WELCOME ALL</h1>";
    }

    @GetMapping("/user")
    public String userHome() {
        return "<h1>WELCOME user</h1>";
    }

    @GetMapping("/admin")
    public String adminHome() {
        return "<h1>WELCOME admin</h1>";
    }

    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) throws Exception {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword())
            );
        } catch (BadCredentialsException ex) {
            throw new Exception("Incorrect username or password: " + ex);
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
        final String jwt = jwtService.generateToken(userDetails);

        return ResponseEntity.ok(new AuthenticationResponse(jwt));
    }
}
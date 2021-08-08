package com.tymofiivoitenko.telegram.service;

import com.tymofiivoitenko.telegram.model.user.User;
import com.tymofiivoitenko.telegram.model.user.userDetails.MyUserDetails;
import com.tymofiivoitenko.telegram.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByUserName(userName);
        user.orElseThrow(() -> new UsernameNotFoundException("Not found: " + userName));

        UserDetails userDetails = user.map(MyUserDetails::new).get();
        log.info("ud: " + userDetails);
        return userDetails;
    }
}

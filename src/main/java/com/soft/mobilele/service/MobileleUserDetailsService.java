package com.soft.mobilele.service;

import com.soft.mobilele.mapper.MapStructMapper;
import com.soft.mobilele.model.user.MobileleUserDetails;
import com.soft.mobilele.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

// NOTE: This is not annotated as @Service, because we will return it as a bean.
public class MobileleUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    private final MapStructMapper mapper;

    public MobileleUserDetailsService(UserRepository userRepository, MapStructMapper mapper) {

        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<MobileleUserDetails> mobileleUserDetails = userRepository.findByUsername(username)
                .map(u -> {
                    MobileleUserDetails userDetails = mapper.toUserDetails(u);

                    userDetails.setAuthorities(u.getUserRoles().stream().map(mapper::toGrantedAuthority)
                            .toList());


                    return userDetails;
                });

        return mobileleUserDetails
                .orElseThrow(() -> new UsernameNotFoundException("User with username " + username + " not found"));
    }

}

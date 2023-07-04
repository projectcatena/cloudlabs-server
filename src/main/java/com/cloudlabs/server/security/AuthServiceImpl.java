package com.cloudlabs.server.security;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.cloudlabs.server.WebSecurityConfig;
import com.cloudlabs.server.user.User;
import com.cloudlabs.server.user.UserDto;

@Service
public class AuthServiceImpl implements AuthService {

    private JwtHelper jwtHelper;

    public AuthServiceImpl(JwtHelper jwtHelper) {
        this.jwtHelper = jwtHelper;
    }

    public String createJwtToken(String email, User user, UserDetails userDetails) {
        Map<String, String> claims = new HashMap<>();
			claims.put("email", email);
			claims.put("sub", user.getName());
			
			String roles = userDetails.getAuthorities().stream()
			.map(GrantedAuthority::getAuthority)
			.collect(Collectors.joining(" "));
			claims.put(WebSecurityConfig.AUTHORITIES_CLAIM_NAME, roles);
			claims.put("userId", "" + user.getId());
			String jwt = jwtHelper.createJwtForClaims(email, claims);
			System.out.println(jwt);

            return jwt;
    }

    public UserDto createNewUser(String name, String email, String password) {
        UserDto userDto = new UserDto();
		userDto.setName(name);
		userDto.setEmail(email);
		userDto.setPassword(password);

        return userDto;
    }
}

package com.grs.api.config.security;

import com.grs.api.model.UserInformation;
import com.grs.core.dao.GrsRoleDAO;
import com.grs.core.dao.UserDAO;
import com.grs.core.domain.grs.GrsRole;
import com.grs.core.domain.projapoti.User;
import com.grs.utils.BanglaConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Acer on 9/27/2017.
 */
@Slf4j
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private UserDAO userDAO;
    @Autowired
    private HttpServletResponse httpServletResponse;
    @Autowired
    private OISFPasswordService passwordService;
    @Autowired
    private OISFUserDetailsServiceImpl oisfUserDetailsService;
    @Autowired
    private GrsRoleDAO grsRoleDAO;
    @Autowired
    private final UserDetailsServiceImpl userDetailsService;

    public CustomAuthenticationProvider(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String name = authentication.getName();
        String password = authentication.getCredentials().toString();

        // 1) Try GRS Complainant Login
        try {
            UserDetailsImpl userDetails1 = userDetailsService.loadUserByUsername(name);

            if (userDetails1 != null && userDetails1.getUserInformation() != null) {

                // IMPORTANT: password/PIN check must be here
                // Example only: use your actual password/PIN checking logic
                // if (!passwordService.checkPassword(password, userDetails1.getPassword())) {
                //     throw new BadCredentialsException("Invalid complainant credentials");
                // }

                System.out.println("GRS Complainant Login Success: " + userDetails1.getUserInformation());

                return new CustomAuthenticationToken(
                        name,
                        password,
                        userDetails1.getAuthorities(),
                        userDetails1.getUserInformation()
                );
            }

        } catch (UsernameNotFoundException ex) {
            System.out.println("GRS Complainant Login Failed. Trying OISF login...");
        }

        // 2) Try OISF User Login
        User user = this.userDAO.findByUsername(BanglaConverter.convertToEnglish(name));

        if (user == null) {
            System.out.println("OISF Login Failed: user not found");
            throw new BadCredentialsException("Invalid username or password");
        }

        System.out.println("OISF User Found: " + user.getUsername());

        // IMPORTANT: enable password check in production
        // if (!passwordService.checkPassword(password, user.getPassword())) {
        //     throw new BadCredentialsException("Invalid username or password");
        // }

        UserInformation userInformation = this.oisfUserDetailsService.getUserInfo(user);

        String roleName;
        if (userInformation.getGrsUserType() != null) {
            roleName = userInformation.getGrsUserType().name();
        } else {
            roleName = userInformation.getOisfUserType().name();
        }

        GrsRole grsRole = this.grsRoleDAO.findByRole(roleName);

        if (grsRole == null) {
            throw new BadCredentialsException("Role not found for user");
        }

        List<GrantedAuthorityImpl> grantedAuthorities = grsRole
                .getPermissions()
                .stream()
                .map(permission -> GrantedAuthorityImpl.builder()
                        .role(permission.getName())
                        .build())
                .collect(Collectors.toList());

        return new CustomAuthenticationToken(
                name,
                password,
                grantedAuthorities,
                userInformation
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}

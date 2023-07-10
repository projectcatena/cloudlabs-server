package com.cloudlabs.server.user;

import com.cloudlabs.server.role.Role;
import java.util.List;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

//@JsonInclude(Include.NON_DEFAULT)
public class UserDto {

    private Long id;
    @NotEmpty
    private String name;
    @NotEmpty(message = "Email should not be empty")
    @Email
    private String email;
    @NotEmpty(message = "Password should not be empty")
    private String password;

    private List<Role> roles;

    public UserDto() {
    }

    public UserDto(Long id, @NotEmpty String name,
            @NotEmpty(message = "Email should not be empty") @Email String email,
            @NotEmpty(message = "Password should not be empty") String password, List<Role> roles) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.roles = roles;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }
}

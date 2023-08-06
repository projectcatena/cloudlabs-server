package com.cloudlabs.server.user;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.cloudlabs.server.role.Role;
import com.cloudlabs.server.snapshot.SaveSnapshot;

@Entity
@Table(name = "user_table") // should not use User or user as reserved keywords
public class User implements UserDetails {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(nullable = false)
  private String fullname;

  @Column(nullable = false, unique = true)
  private String username;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String password;

  @ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
  @JoinTable(name = "users_roles", joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
  private Set<Role> roles;

  @OneToMany(mappedBy = "user")
  private Set<SaveSnapshot> snapshots;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return roles.stream()
        .map(
            role -> new SimpleGrantedAuthority("ROLE_" + role.getName().name()))
        .collect(Collectors.toList());
  }

  public User() {
  }

  public User(String fullname, String username, String email, String password) {
    this.fullname = fullname;
    this.username = username;
    this.email = email;
    this.password = password;
  }

  public User(String fullname, String username, String email, String password,
      Set<Role> roles) {
    this.fullname = fullname;
    this.username = username;
    this.email = email;
    this.password = password;
    this.roles = roles;
  }

  @Override
  public String getPassword() {
    return password;
  }

  // Retrieve by email for UserDetailsService
  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getFullname() {
    return fullname;
  }

  public void setFullname(String fullname) {
    this.fullname = fullname;
  }

  // To return actual username, as the previos @Override getUsername returns
  // email
  public String getUserName() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public Set<Role> getRoles() {
    return roles;
  }

  public void setRoles(Set<Role> roles) {
    this.roles = roles;
  }

  public Set<SaveSnapshot> getSnapshots() {
    return snapshots;
  }

  public void setSnapshots(Set<SaveSnapshot> snapshots) {
    this.snapshots= snapshots;
  }

}

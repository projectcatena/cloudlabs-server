package com.cloudlabs.server.security.resource;

/**
 * Login response object containing the JWT
 *
 * @author imesha
 */
public class LoginResultDTO {

	private String jwt;

	public LoginResultDTO() {
	}

	public LoginResultDTO(String jwt) {
		this.jwt = jwt;
	}

	public String getJwt() {
		return jwt;
	}

	public void setJwt(String jwt) {
		this.jwt = jwt;
	}
}

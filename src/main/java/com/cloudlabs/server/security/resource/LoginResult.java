package com.cloudlabs.server.security.resource;

/**
 * Login response object containing the JWT
 *
 * @author imesha
 */
public class LoginResult {

	private String jwt;

	public LoginResult() {
	}

	public LoginResult(String jwt) {
		this.jwt = jwt;
	}

	public String getJwt() {
		return jwt;
	}

	public void setJwt(String jwt) {
		this.jwt = jwt;
	}
}

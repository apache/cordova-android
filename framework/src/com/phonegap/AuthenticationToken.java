package com.phonegap;

/**
 * The Class AuthenticationToken defines the principal and credentials to be used for authenticating a web resource
 */
public class AuthenticationToken {
    private String principal;
    private String credentials;
    
    /**
     * Gets the principal.
     * 
     * @return the principal
     */
    public String getPrincipal() {
        return principal;
    }
    
    /**
     * Sets the principal.
     * 
     * @param principal
     *            the new principal
     */
    public void setPrincipal(String principal) {
        this.principal = principal;
    }
    
    /**
     * Gets the credentials.
     * 
     * @return the credentials
     */
    public String getCredentials() {
        return credentials;
    }
    
    /**
     * Sets the credentials.
     * 
     * @param credentials
     *            the new credentials
     */
    public void setCredentials(String credentials) {
        this.credentials = credentials;
    }
    
    
}

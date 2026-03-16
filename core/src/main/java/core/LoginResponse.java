package core;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginResponse {
    private final String status;
    private final String userID;
    private final String jwt;

    public LoginResponse(@JsonProperty("status") String status, @JsonProperty("userID") String userID, @JsonProperty("jwt") String jwt){
        this.status = status;
        this.userID = userID;
        this.jwt = jwt;
    }

    public String getStatus() { return status; }
    public String getUserID() { return userID; }
    public String getJwt() { return jwt; }
}

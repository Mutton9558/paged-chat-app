package core;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserConfig {

    @JsonProperty("userId")
    private final String userId;

    @JsonProperty("deviceId")
    private final String deviceId;

    @JsonProperty("jwtToken")
    private final String jwtToken;

    public UserConfig(){
        this.userId = null;
        this.deviceId = null;
        this.jwtToken = null;
    }

    public UserConfig(String userId, String deviceId, String jwtToken){
        this.userId = userId;
        this.deviceId = deviceId;
        this.jwtToken = jwtToken;
    }

    public String getUserId() { return userId; }
    public String getDeviceId() { return deviceId; }
    public String getToken() { return jwtToken; }
}

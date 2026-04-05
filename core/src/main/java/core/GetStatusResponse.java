package core;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GetStatusResponse {
    private final String status;

    public GetStatusResponse(@JsonProperty("status") String status){
        this.status = status;
    }

    public String getStatus() { return status; }
}

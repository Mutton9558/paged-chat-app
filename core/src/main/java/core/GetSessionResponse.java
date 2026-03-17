package core;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GetSessionResponse {
    private final String status;

    public GetSessionResponse(@JsonProperty("status") String status){
        this.status = status;
    }

    public String getStatus() { return status; }
}

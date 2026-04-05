package core;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RecipientKeyResponse {
    private final String recipientPublic;

    public RecipientKeyResponse(@JsonProperty("recipientPublic") String targetPublicKey){
        this.recipientPublic = targetPublicKey;
    }

    public String returnRecipientPublic(){ return this.recipientPublic; }
}

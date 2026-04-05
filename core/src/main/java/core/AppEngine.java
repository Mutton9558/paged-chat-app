package core;
import io.socket.client.Socket;
import java.net.URI;
import io.socket.client.IO;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.io.File;
import java.util.UUID;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

// Public and Private key generation
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.security.Signature;

import com.fasterxml.jackson.databind.ObjectMapper;

public class AppEngine {

    private volatile String userId;
    private volatile String deviceId;
    private volatile String jwt;
   
    private NetworkThread networkThread;
    private UpdaterThread updaterThread;
    private Socket clientSocket;
    private boolean activeSession;

    private final File configFile = new File("paged_config.json");
    private final ObjectMapper mapper = new ObjectMapper();

    public static KeyPair generateRSAKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        keyPairGen.initialize(2048, new SecureRandom());
        return keyPairGen.generateKeyPair();
    }

    public static PublicKey stringToPublicKey(String publicKeyStr) throws Exception {
        // 1. Decode the Base64 String back into raw bytes
        byte[] keyBytes = Base64.getDecoder().decode(publicKeyStr);

        // 2. Create a specification for an X.509 encoded key
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);

        // 3. Use KeyFactory to convert the spec into a real PublicKey object
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    private PublicKey publicKey;
    private PrivateKey privateKey;

    // public void getUserFromDB(String username){
    //     System.out.println("Attempting to retrieve user from database");
    //     // code
    // }

    // // will fill later once design is settled
    // public void registerUser(){}



    private void generateDeviceID(){
        this.deviceId = UUID.randomUUID().toString();
    }

    private void createConfig(){
        try{
            UserConfig newConfig = new UserConfig(this.userId, this.deviceId, this.jwt);
            mapper.writeValue(configFile, newConfig);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private void createSocket(){
        Map<String, String> authData = new HashMap<>();
        authData.put("userId", this.userId);
        authData.put("deviceId", this.deviceId);

        IO.Options options = IO.Options.builder()
        .setAuth(authData)
        .build();
        // socket to backend server
        this.clientSocket = IO.socket(URI.create("https://pagedbackend.firebase.whatever"), options);
    }

    // might change to char[] for password for internal protection
    public void loginUser(String username, String password){
        if(!this.activeSession){
            if(this.deviceId == null){
                generateDeviceID();
            }

            try{
                HttpClient client = HttpClient.newHttpClient();

                ObjectMapper objectMapper = new ObjectMapper();

                Map<String, String> loginData = Map.of(
                    "username", username,
                    "password", password,
                    "deviceID", this.deviceId
                );

                //Serialize to JSON string safely
                String json = objectMapper.writeValueAsString(loginData);
                // switch later
                HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:3000/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if(response.statusCode() == 200){
                    ObjectMapper mapper = new ObjectMapper();

                    LoginResponse serverRes = mapper.readValue(response.body(), LoginResponse.class);
                    this.userId = serverRes.getUserID();
                    this.jwt = serverRes.getJwt();
                }    
            } catch (IOException e){
                System.out.println(e);
            } catch (InterruptedException e){
                System.out.println(e);
            }
            createConfig();
            createSocket();
        }
    }

    private void registerUser(String username, String phoneNum, String email, String password){
        
    }

    private void send_message(String message, String targetRecipientID){
        PublicKey recipientPublic = null;
        try{
            // Fetch recipient's public key from server
            HttpClient client = HttpClient.newHttpClient();

            ObjectMapper objectMapper = new ObjectMapper();

            Map<String, String> recipientIDMap = Map.of(
                "userID", targetRecipientID
            );

            //Serialize to JSON string safely
            String json = objectMapper.writeValueAsString(recipientIDMap);
            HttpRequest request = HttpRequest.newBuilder()
            // temp URL
            .uri(URI.create("http://localhost:3000/get_target_key"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if(response.statusCode() == 200){
                ObjectMapper mapper = new ObjectMapper();

                RecipientKeyResponse serverRes = mapper.readValue(response.body(), RecipientKeyResponse.class);
                String recipientKeyString = serverRes.returnRecipientPublic();
                recipientPublic = stringToPublicKey(recipientKeyString);

            } 

            // generate aes key
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256);
            SecretKey aesKey = keyGen.generateKey();

            // cipher algorithm
            Cipher aesCipher = Cipher.getInstance("AES/GCM/NoPadding");
            byte[] iv = new byte[12];
            new SecureRandom().nextBytes(iv);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv);
            aesCipher.init(Cipher.ENCRYPT_MODE, aesKey, parameterSpec);
            byte[] encryptedMessageBytes = aesCipher.doFinal(message.getBytes());

            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            rsaCipher.init(Cipher.ENCRYPT_MODE, recipientPublic);
            byte[] encryptedAESKeyBytes = rsaCipher.doFinal(aesKey.getEncoded());

            Signature privateSignature = Signature.getInstance("SHA256withRSA");
            privateSignature.initSign(this.privateKey);

            privateSignature.update(encryptedAESKeyBytes);

            byte[] signatureBytes = privateSignature.sign();

            // Convert all the raw byte arrays to Base64 Strings
            String base64EncryptedMessage = Base64.getEncoder().encodeToString(encryptedMessageBytes);
            String base64EncryptedAESKey = Base64.getEncoder().encodeToString(encryptedAESKeyBytes);
            String base64IV = Base64.getEncoder().encodeToString(iv);
            String base64Signature = Base64.getEncoder().encodeToString(signatureBytes);

            // code to send to express
            HttpClient sender = HttpClient.newHttpClient();
            ObjectMapper encryptedMapping = new ObjectMapper();

            Map<String, String> encryptedItems = Map.of(
                "EncryptedMessage", base64EncryptedMessage,
                "EncryptedAESKey", base64EncryptedAESKey,
                "EncryptedIV", base64IV,
                "Signature", base64Signature,
                "SenderId", this.userId
            );

            String jsonReq = objectMapper.writeValueAsString(encryptedItems);
            HttpRequest req = HttpRequest.newBuilder()
            // temp URL
            .uri(URI.create("http://localhost:3000/send_message"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonReq))
            .build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

            if(res.statusCode() == 200){
                System.out.println("Message sent successfully!");
                // code to show ui of "sent"
            } else {
                System.out.println("Message failed to send.");
                // code to show ui of "fail"
            }
        } catch (Exception e){
            System.out.println(e);
        } 
    }
    
    private void loadConfig(){
        try{
            if(configFile.exists()){
                UserConfig config = mapper.readValue(configFile, UserConfig.class);
                this.userId = config.getUserId();
                this.deviceId = config.getDeviceId();
                this.jwt = config.getToken();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public AppEngine(){
        this.activeSession = false;
        this.userId = null;
        this.deviceId = null;
        this.jwt = null;

        try{
            KeyPair keyPair = generateRSAKeyPair();

            this.publicKey = keyPair.getPublic();
            this.privateKey = keyPair.getPrivate();
        } catch (NoSuchAlgorithmException e){
            System.out.println("Error");
        }
        
        loadConfig();
        if(this.jwt != null){
            try{
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:3000/get_session"))
                    .header("Authorization", "Bearer " + this.jwt)
                    .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if(response.statusCode() == 200){
                    System.out.println("Valid JWT");
                    this.activeSession = true;
                } else if (response.statusCode() == 401){
                    System.out.println("No JWT found");
                } else if (response.statusCode() == 403){
                    System.out.println("Invalid JWT");
                }
            } catch (IOException e){
                System.out.println(e);
            }
            catch (InterruptedException e){
                System.out.println(e);
            }   
        }
        
        if(this.activeSession){
            createSocket();
        }
    }

    public Boolean isActiveSession(){ return this.activeSession; }

    public void start() {
        // network = msg, updater = db+cache
        this.networkThread = new NetworkThread();
        this.updaterThread = new UpdaterThread();

        Thread network = new Thread(this.networkThread);
        Thread updater = new Thread(this.updaterThread);

        network.start();
        updater.start(); 
    }
}

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

    public void loginUser(String username, String password){
        if(!this.activeSession){
            if(this.deviceId == null){
                generateDeviceID();
            }

            try{
                HttpClient client = HttpClient.newHttpClient();

                String json = String.format("""
                        {
                            "username": "%s",
                            "password": %s,
                            "deviceID": %s
                        }
                """, username, password, this.deviceId);
                // switch later
                HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:3000/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                ObjectMapper mapper = new ObjectMapper();

                LoginResponse serverRes = mapper.readValue(response.body(), LoginResponse.class);
                this.userId = serverRes.getUserID();
                this.jwt = serverRes.getJwt();
            } catch (IOException e){
                System.out.println(e);
            } catch (InterruptedException e){
                System.out.println(e);
            }
            createConfig();
            createSocket();
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
        loadConfig();
        if(this.jwt != null){
            try{
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:3000/get_session"))
                    .header("Authorization", "Bearer " + this.jwt)
                    .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                ObjectMapper mapper = new ObjectMapper();
                GetSessionResponse serverRes = mapper.readValue(response.body(), GetSessionResponse.class);
                if(serverRes.getStatus() == "Success"){
                    this.activeSession = true;
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

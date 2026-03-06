package core;

public class AppEngine {

    private volatile String userId;

    public void setUserId(String targetId){
        this.userId = targetId;
    }

    public void getUserFromDB(String username){
        System.out.println("Attempting to retrieve user from database");
        // code
    }

    // will fill later once design is settled
    public void registerUser(){}

    public void loginUser(String username, String password){
        System.out.println("Attempting to log in user");
        // code
        getUserFromDB(username);
    }

    public void start() {
        // network = msg, updater = db+cache
        Thread network = new Thread(new WorkerThread());
        Thread updater = new Thread(new WorkerThread());

        network.start();
        updater.start();
    }
}

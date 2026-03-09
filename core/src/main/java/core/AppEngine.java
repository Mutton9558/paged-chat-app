package core;

public class AppEngine {

    private volatile String userId;
    private NetworkThread networkThread;
    private UpdaterThread updaterThread;

    public void setUserId(String targetId){
        this.userId = targetId;
    }

    // public void getUserFromDB(String username){
    //     System.out.println("Attempting to retrieve user from database");
    //     // code
    // }

    // // will fill later once design is settled
    // public void registerUser(){}

    // public void loginUser(String username, String password){
    //     System.out.println("Attempting to log in user");
    //     // code
    //     getUserFromDB(username);
    // }

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

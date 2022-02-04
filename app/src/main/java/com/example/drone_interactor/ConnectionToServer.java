package com.example.drone_interactor;

public class ConnectionToServer {
    private static ConnectionToServer instance;

    public String ip;
    public String port;

    public static ConnectionToServer getInstance() {
        if (instance == null) {
            instance = new ConnectionToServer();
        }
        return instance;
    }

    private ConnectionToServer() {  }

    public void setConnection(String ipAndPort) {
        String[] connectionArr = ipAndPort.split(":");
        this.ip = connectionArr[0];
        this.port = connectionArr[1];
    }

    public void sendMessage(DataPoint[] dataPoints) {

    }


}

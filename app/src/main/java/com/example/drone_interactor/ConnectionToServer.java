package com.example.drone_interactor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class ConnectionToServer {
    private static ConnectionToServer instance;

    public String ip;
    public String port;
    public String path = "/";

    private URL url;
    private HttpURLConnection connection;

    public static ConnectionToServer getInstance() {
        if (instance == null) {
            instance = new ConnectionToServer();
        }
        return instance;
    }

    private ConnectionToServer() {  }

    public void setConnectionString(String ipAndPort) {
        String[] connectionArr = ipAndPort.split(":");
        if (connectionArr.length >= 2) {
            this.ip = connectionArr[0];
            this.port = connectionArr[1];

            String[] portAndPath = connectionArr[1].split("/");
            if (portAndPath.length > 1) {
                this.port = portAndPath[0];
                this.path = portAndPath[1];
            }
        } else {
            this.ip = "localhost";
            this.port = "3000";
        }
    }

    private void setConnection() throws IOException {
        this.url = new URL(this.ip + ":" + this.port + "/" + this.path);
        if (this.url != null) {
            this.connection = (HttpURLConnection)url.openConnection();
            this.connection.setRequestMethod("POST");
            this.connection.setRequestProperty("Content-Type", "application/json; utf-8");
            this.connection.setRequestProperty("Accept", "application/json");
            this.connection.setDoOutput(true);
        } else {
            throw new IOException("Could not establish a connection to url");
        }
    }

    public int sendMessage(DataPoint[] dataPoints) throws IOException {
        this.setConnection();

        String arrayString = "[";
        for (int i = 0; i < dataPoints.length; i++) {
            if (i != 0) {
                arrayString += ", [";
            } else {
                arrayString += "[";
            }
            arrayString += dataPoints[i].getX() +
                ", " + dataPoints[i].getY() +
                ", " + dataPoints[i].getZ() + "]";
        }
        arrayString += "]";
        String jsonInputString = "{\"data\": " + arrayString + "}";

        // data sender
        OutputStream os = this.connection.getOutputStream();
        byte[] input = jsonInputString.getBytes("utf-8");
        os.write(input, 0, input.length);

        // data receiver
        BufferedReader br = new BufferedReader(
                new InputStreamReader(this.connection.getInputStream(), "utf-8"));
        StringBuilder response = new StringBuilder();
        String responseLine = null;
        while ((responseLine = br.readLine()) != null) {
            response.append(responseLine.trim());
        }
        // disconnects client
        this.connection.disconnect();
        this.connection = null;
        return dataPoints.length;
    }
}

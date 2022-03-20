package com.example.drone_interactor;

import com.android.volley.RequestQueue;
import com.google.gson.JsonArray;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * 
 * A connection singleton class which handles communication with 
 * an URL via HTTP request.
 * 
 * It is a singleton class since there should only be one instance of
 * the class handling the communication. 
 */
public class ConnectionToServer {
    private static ConnectionToServer instance;
    public String ip;
    public String port;
    public String path = "/";
    public String name;

    private URL url;
    private HttpURLConnection connection;

    private int numberOfPackages = 0;

    /**
     * 
     * Returns the instance of the class. Creates a new one
     * if no instance exists.
     * 
     * @return The instance of the ConnectionToServer class.
     */
    public static ConnectionToServer getInstance() {
        if (instance == null) {
            instance = new ConnectionToServer();
        }
        return instance;
    }
    
    /**
     * private constructor to prevent initialization.
     */
    private ConnectionToServer() {  }

    /**
     * 
     * Sets up the connection to our back-end, and
     * saves the session name to a variable. 
     * 
     * @param name The name of the session.
     */
    public void setConnectionString(String name) {
        this.name = name;
        this.ip = "http://130.240.202.87";
        this.port = "3000";
        this.path = "/";
    }

    /**
     * 
     * Sets parameters on the HTTP connection.
     * 
     * @throws IOException Throws I/O exception for faulty URL:s, among other things.
     */
    private void setConnection() throws IOException {
        this.url = new URL(this.ip + ":" + this.port + this.path);
        this.connection = (HttpURLConnection)url.openConnection();
        this.connection.setRequestMethod("POST");
        this.connection.setRequestProperty("Accept", "application/json");
        this.connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
        this.connection.setUseCaches(false);
        this.connection.setDoOutput(true);
    }

    /**
     * 
     * Transforms an array of data points to a JSON string,
     * and sends that string via the HTTP connection.
     * 
     * @param dataPoints An array of xyz coordinates.
     * @return Returns the total number of points sent during this session.
     * @throws IOException Throws I/O exception for faulty URL:s, among other things.
     * @throws JSONException Throws a JSON exception for faulty JSON formatting, among other things.
     */
    public int sendMessage(DataPoint[] dataPoints) throws IOException, JSONException {
        this.setConnection();

        String objectString = "";
        String arrayString = "[";

        // The following for loop adds the point data to the JSON string
        for (int i = 0; i < dataPoints.length; i++) {
            String stringOfPoints = "{\"x\":" + dataPoints[i].getX() +
                    ",\"y\":" + dataPoints[i].getY() +
                    ",\"z\":" + dataPoints[i].getZ() + "}";
            if (i == 0) {
                arrayString += stringOfPoints;
            } else {
                arrayString += ", " + stringOfPoints;
            }
        }
        arrayString += "]";
        objectString = "{\"data\": " + arrayString + ", \"name\": \""+ this.name + "\"}";

        // data sender
        byte[] input = objectString.getBytes(StandardCharsets.UTF_8);
        this.connection.setRequestProperty("Content-Length", Integer.toString( input.length ));
        this.connection.connect();

        try (DataOutputStream wr = new DataOutputStream(this.connection.getOutputStream())) {
            wr.write(input);
        }

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

        this.numberOfPackages += dataPoints.length;
        return this.numberOfPackages;
    }

    /**
     * Resets the variable keeping track of the total number of points
     * sent during a session.
     */
    public void reset() {
        this.numberOfPackages = 0;
    }

}

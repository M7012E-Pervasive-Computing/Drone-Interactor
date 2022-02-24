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

public class ConnectionToServer {
    private static ConnectionToServer instance;
    public String ip;
    public String port;
    public String path = "/";
    public String name;

    private URL url;
    private HttpURLConnection connection;

    private int numberOfPackages = 0;

    public static ConnectionToServer getInstance() {
        if (instance == null) {
            instance = new ConnectionToServer();
        }
        return instance;
    }

    private ConnectionToServer() {  }

    public void setConnectionString(String name) {
        this.name = name;
        this.ip = "http://130.240.202.87";
        this.port = "3000";
        this.path = "/";
//        String[] connectionArr = ipAndPort.split(":");
//        if (connectionArr.length >= 2) {
//            this.ip = connectionArr[0];
//            this.port = connectionArr[1];
//
//            String[] portAndPath = connectionArr[1].split("/");
//            if (portAndPath.length > 1) {
//                this.port = portAndPath[0];
//                this.path = portAndPath[1];
//            }
//        } else {
//            this.ip = "130.240.202.87";
//            this.port = "3000";
//            this.path = "/";
//            this.name = ipAndPort;
//        }
    }

//    private void test() {
//        RequestQueue queue = Volley.newRequestQueue(this);
//        String url ="https://www.google.com";
//
//// Request a string response from the provided URL.
//        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//                        // Display the first 500 characters of the response string.
//                        textView.setText("Response is: "+ response.substring(0,500));
//                    }
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                textView.setText("That didn't work!");
//            }
//        });

// // Add the request to the RequestQueue.
//        queue.add(stringRequest);
//    }

    private void setConnection() throws IOException {
        this.url = new URL(this.ip + ":" + this.port + this.path);
        this.connection = (HttpURLConnection)url.openConnection();
        this.connection.setRequestMethod("POST");
        this.connection.setRequestProperty("Accept", "application/json");
        this.connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
        this.connection.setUseCaches(false);
        this.connection.setDoOutput(true);
    }

    public int sendMessage(DataPoint[] dataPoints) throws IOException, JSONException {
        this.setConnection();

        String objectString = "";
        String arrayString = "[";

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

}

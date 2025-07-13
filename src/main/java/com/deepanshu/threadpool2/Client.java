package com.deepanshu.threadpool2;

import javax.crypto.spec.PSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.Buffer;
import java.sql.SQLOutput;
import java.time.Duration;
import java.time.Instant;


/**
 * Client class simulates a simple HTTP client that connects to a local server.
 * Each instance represents a single client sending a request to the server.
 *
 * Why Runnable?
 *   • Enables concurrent execution using threads.
 *   • Useful when simulating multiple clients in parallel.
 *
 * Why Instant + Duration?
 *   • Measures how long the request‑response cycle takes.
 *   • Helps us benchmark or stress test the server's response time.
 *
 * How Request is Formed:
 *   • Sends a basic HTTP GET request using raw sockets.
 *   • Terminates the request by sending a blank line.
 *   */

public class Client implements Runnable{

    private final int clientNumber;


    // Constructor assigns a unique ID to each client
    public Client(int clientNumber){
        this.clientNumber=clientNumber;
    }

    @Override
    public void run() {
        int port=8010;
        try{

            // Establish connection to the server
            Socket socket=new Socket("localhost",port);

            // Writer to send request to server
            PrintWriter toServer=new PrintWriter(socket.getOutputStream(),true);
            // Reader to receive response from server
            BufferedReader fromServer=new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Minimal valid HTTP GET request
            Instant start=Instant.now();
            toServer.println("GET/index HTTP/1.1");
            toServer.println("Host: localhost");
            toServer.println("Connection: close");
            toServer.println(); // End of headers

            String responseLine = fromServer.readLine();
            Instant end = Instant.now();


            if (responseLine != null) {
                System.out.println("Client " + clientNumber + " => " + responseLine +
                        " | Time: " + Duration.between(start, end).toMillis() + " ms");
            }

            // Clean up resources
            fromServer.close();
            toServer.close();
            socket.close();

        } catch (IOException e) {
            System.err.println("Client " + clientNumber + " failed: " + e.getMessage());
        }
    }


    // Launches multiple client threads to test concurrency
    public static void main(String[] args){
        int clientCount=50;
        for(int i=0; i<clientCount; i++){
            Thread clientThread=new Thread(new Client(i+1
            ));
            clientThread.start();
        }
    }
}

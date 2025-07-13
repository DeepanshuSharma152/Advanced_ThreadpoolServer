package com.deepanshu.threadpool2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    /**
     * A ThreadPool-based multi-client server.
     * This server listens on a specified port and handles each client request
     * using a fixed number of threads (thread pool) to optimize resource usage.
     */

        private final ExecutorService threadPool; //Thread pool to manage concurrent client handling

        /**
         * Constructor to initialize the server with a fixed-size thread pool.
         * @param poolSize Number of threads in the pool.
         */

        public Server(int poolSize){
            this.threadPool= Executors.newFixedThreadPool(poolSize);
        }

        public void handleClient(Socket clientSocket){
            Router router=new Router();
            router.handleClient(clientSocket);
        }


        /**
         * Main method to start the server, accept connections, and dispatch them to the thread pool.
         */

        public static void main(String[] args){
            int port=8010;  // Port on which server listens
            int poolSize=1000; // Size of the thread pool
            Server server=new Server(poolSize);
            try{
                ServerSocket serverSocket=new ServerSocket(port);
                serverSocket.setSoTimeout(70000);   // Optional timeout for accept() method
                System.out.println("Server is listening to port " + port);

                // Loop to accept and handle client connections
                while(true){
                    Socket clientSocket=serverSocket.accept();

                    // Submit the client-handling task to the thread pool
                    server.threadPool.execute(()-> server.handleClient(clientSocket));
                }
            } catch (IOException e) {
                e.printStackTrace(); // Handle server socket exceptions
            } finally {
                // Ensure thread pool is shut down when server is stopped
                server.threadPool.shutdown();
            }
        }

    }



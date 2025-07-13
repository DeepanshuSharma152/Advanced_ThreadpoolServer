package com.deepanshu.threadpool2;

import java.io.*;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Router handles incoming HTTP requests from the client socket.
 *
 * Responsibilities:
 *   • Parse HTTP GET and POST requests.
 *   • Route to appropriate static or dynamic content.
 *   • Manage sessions using cookies.
 *
 * Notes:
 *   • Only supports basic GET and POST.
 *   • Uses raw socket streams instead of a full HTTP framework.
 *   • Handles session via SessionManager and sends Set-Cookie header.
 */


public class Router {

    public void handleClient(Socket clientSocket) {

        // Send a response to the client
        try (BufferedReader fromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter toClient = new PrintWriter(clientSocket.getOutputStream(), true)) {

            //👌Read the first line of http request
            String requestLine = fromClient.readLine();
            System.out.println("Request Line: " + requestLine);

            if (requestLine == null || requestLine.isEmpty()) {
                toClient.println("HTTP/1.1 400 BAD REQUEST");
                toClient.println();
                return;
            }

            //😉Parse the request Line: METHOD PATH VERSION
            String[] parts = requestLine.split(" ");
            if (parts.length != 3) {
                toClient.println("HTTP/1.1 400 Bad Request");
                toClient.println();
                return;
            }

            String method = parts[0];
            String path = parts[1];
            String httpVersion = parts[2];

            System.out.println("Method: " + method + ", Path: " + path + ",Version " + httpVersion);
         //session and cookies
            String line;
            String cookieLine=null;

            while(!(line= fromClient.readLine()).isEmpty()){
                if(line.startsWith("Cookie:")){
                    cookieLine=line;
                }
            }

            String sessionId=null;
            if(cookieLine!=null && cookieLine.contains("sessionId=")){
                sessionId=cookieLine.split("sessionId=")[1].split(";")[0];
            }

     // =================== Handle GET Requests ===================
            if (method.equals("GET")) {
                if(path.equals("/") || path.equals("/index")){
                    // Serve index.html
                    try{
                        String html=Files.readString(Paths.get("public/index.html"),StandardCharsets.UTF_8);
                        toClient.println("HTTP/1.1 200 OK");
                        toClient.println("Content-Type: text/html");
                        toClient.println("Content-Length: " + html.length());
                        toClient.println();
                        toClient.println(html);

                    } catch(IOException ex){
                        toClient.println("HTTP/1.1 404 Not Found");
                        toClient.println();
                        toClient.println("Error loading index.html");
                    }
                }

                else if(path.equals("/register")) {
                    // Serve register.html
                    try {
                        System.out.println("Trying to load: public/form.html");
                        String html = Files.readString(Paths.get("public/register.html"), StandardCharsets.UTF_8);
                        toClient.println("HTTP/1.1 200 OK");
                        toClient.println("Content-Type: text/html");
                        toClient.println("Content-Length: " + html.getBytes(StandardCharsets.UTF_8).length
                        );
                        toClient.println();
                        toClient.println(html);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                } else if (path.equals("/login")) {
                    // Serve login.html
                    try {
                        String html = Files.readString(Paths.get("public/login.html"), StandardCharsets.UTF_8);
                        toClient.println("HTTP/1.1 200 OK");
                        toClient.println("Content-Type: text/html");
                        toClient.println("Content-Length: " + html.length());
                        toClient.println();
                        toClient.println(html);
                    }
                    catch (IOException ex) {
                        toClient.println("HTTP/1.1 404 Not Found");
                        toClient.println();
                        toClient.println("Login page not found");
                    }

                }  else if(path.equals("/logout")){
                // Clear session and remove cookie
                    SessionManager.destroySession(sessionId);
                    toClient.println("HTTP/1.1 200 OK");
                    toClient.println("Set-Cookie: sessionId=deleted; Max-Age=0");
                    toClient.println("Content-Type: text/plain");
                    toClient.println();
                    toClient.println("You’ve been logged out!");

                }

                else if (path.equals("/hello")) {
                    // Plaintext Hello message
                    toClient.println("HTTP/1.1 200 OK");
                    toClient.println("Content-Type: text/plain");
                    toClient.println();
                    toClient.println("Hello, Deepanshu Sharma ji !");

                } else if (path.equals("/time")) {
                    // Server time display
                    toClient.println("HTTP/1.1 200 OK");
                    toClient.println("Content-Type: text/plain");
                    toClient.println();
                    toClient.println("Server time is: " + java.time.LocalTime.now());

                } else if (path.equals("/dashboard")) {
                    // Protected page
                    String username = SessionManager.getUsername(sessionId);
                    if (username != null) {
                        // User is authenticated
                        try {
                            String html = Files.readString(Paths.get("public/dashboard.html"), StandardCharsets.UTF_8);
                            toClient.println("HTTP/1.1 200 OK");
                            toClient.println("Content-Type: text/html");
                            toClient.println("Content-Length: " + html.length());
                            toClient.println();
                            toClient.println(html);
                        } catch (IOException e) {
                            toClient.println("HTTP/1.1 500 Internal Server Error");
                            toClient.println("Content-Type: text/plain");
                            toClient.println();
                            toClient.println("Error loading dashboard.html");

                        }

                    } else {
                        // Unauthorized
                        toClient.println("HTTP/1.1 401 Unauthorized");
                        toClient.println("Content-Type: text/html");
                        toClient.println();
                        toClient.println("<h2>401 Unauthorized</h2>");
                        toClient.println("<p>You must log in first. <a href='/login'>Login</a></p>");
                    }
                    // Optional static file fallback
                    if(path.startsWith("/")) {
                        boolean served = StaticFileService.serveStaticFile(path, clientSocket.getOutputStream());
                        if (!served) {
                            toClient.println("HTTP/404 Not Found");
                            toClient.println();
                            toClient.println("File not found ");
                        }
                    }
                //In-case of user wants to Logout
                }  else if (path.equals("/logout")) {
                    cookieLine = null;
                    while (!(line = fromClient.readLine()).isEmpty()) {
                        if (line.startsWith("Cookie:")) {
                            cookieLine = line;
                        }
                    }

                     sessionId = null;
                    if (cookieLine != null && cookieLine.contains("sessionId=")) {
                        sessionId = cookieLine.split("sessionId=")[1].split(";")[0];
                    }

                    SessionManager.destroySession(sessionId);

                    toClient.println("HTTP/1.1 200 OK");
                    toClient.println("Set-Cookie: sessionId=deleted; Max-Age=0");
                    toClient.println("Content-Type: text/html");
                    toClient.println();
                    toClient.println("<h2>You've been logged out!</h2>");
                    toClient.println("<a href='/login'>Login Again</a>");
                }

                else {
                    // 404 fallback
                    toClient.println("HTTP/1.1 404 Not Found");
                    toClient.println("Content-Type: text/plain");
                    toClient.println();
                    toClient.println("404 Page not Found ");
                }

                // =================== Handle POST Requests ===================
            } else if(method.equals("POST") && path.equals("/register")){
                StringBuilder body=new StringBuilder();

                while(fromClient.ready()) {
                    body.append((char) fromClient.read());
                }

                Map<String,String> fromData=parseFormData(body.toString());
                String username = fromData.getOrDefault("username", "Unknown");
                String gmail = fromData.getOrDefault("gmail", "none");

                String password = fromData.getOrDefault("password", "none");
                String hashedpassword=Utility.hashPassword(password);
                JDBC.insertUser(username, gmail,  hashedpassword);


                // Respond to user for now (JDBC step will come next)
                toClient.println("HTTP/1.1 200 OK");
                toClient.println("Content-Type: text/html");
                toClient.println();
                toClient.println("<h2>Registration Successful </h2>");
                toClient.println("<p>Name: " + username + "</p>");


            } else if (method.equals("POST") && path.equals("/login")) {
                StringBuilder body = new StringBuilder();

                while (fromClient.ready()) {
                    body.append((char) fromClient.read());
                }

                Map<String,String> loginData=parseFormData(body.toString());
                String username = loginData.get("username");
                String password = loginData.get("password");
                String hashedPassword = Utility.hashPassword(password);



                if (JDBC.validateUser(username, hashedPassword)) {
                    sessionId=UUID.randomUUID().toString();
                    SessionManager.createSession(sessionId,username);

                    // Set cookie
                    toClient.println("HTTP/1.1 200 OK");
                    toClient.println("Set-Cookie: sessionId=" + sessionId + "; HttpOnly");  //cookie sent to client having session id
                    toClient.println("Content-Type: text/html");
                    toClient.println();
                    toClient.println("<h2>Login Successful!</h2>");
                    toClient.println("<p>Welcome, " + username + "</p>");

                } else {
                    toClient.println("HTTP/1.1 401 Unauthorized");
                    toClient.println("Content-Type: text/html");
                    toClient.println();
                    toClient.println("<h2>Incorrect username or password!</h2>");
                }
            }

            else {
                toClient.println("HTTP/1.1 405 Method Not Allowed");
                toClient.println();
            }

        } catch(IOException ex) {
            ex.printStackTrace(); // Handle I/O exceptions
        } finally {

            try{
                clientSocket.close();  //let server listen the response of client amd then close the socket
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        }

    // Parse URL-encoded form data (e.g., username=abc&password=xyz)
    private Map<String, String> parseFormData(String data) throws UnsupportedEncodingException {
        Map<String, String> formData = new HashMap<>();
        for (String pair : data.split("&")) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                formData.put(URLDecoder.decode(keyValue[0], "UTF-8"), URLDecoder.decode(keyValue[1], "UTF-8"));
            }
        }
        return formData;
    }
    }


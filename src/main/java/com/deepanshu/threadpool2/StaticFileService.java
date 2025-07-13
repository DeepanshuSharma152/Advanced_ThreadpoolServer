package com.deepanshu.threadpool2;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * StaticFileService serves static files (HTML, CSS, JS, images, etc.) from the "public" folder.
 *
 * Why a dedicated service?
 *   • Keeps static file handling modular and separate from routing logic.
 *   • Allows centralized control over file access rules (e.g., MIME types, security).
 *
 * Notes:
 *   • Files are served only if they exist and are not directories.
 *   • Uses Content-Type detection via Files.probeContentType().
 *   • Normalizes the file path to prevent directory traversal attacks.
 */

public class StaticFileService {
    /**
     * Attempts to serve a static file based on request path.
     *
     * @param path    The URI path from the client request (e.g., "/style.css")
     * @param output  OutputStream to write the response to
     * @return true if the file was found and served; false if not found
     * @throws IOException if reading or writing fails
     */
   public static boolean serveStaticFile(String path, OutputStream output) throws IOException{

       // Construct full path to file under "public" directory
       String FilePath="public" + path;
      Path fullPath= Paths.get(FilePath).normalize();

       // Only serve if file exists and is not a directory
      if(Files.exists(fullPath) && !Files.isDirectory(fullPath)){

          // Determine MIME type (e.g., text/html, image/png, etc.)
          String mimiType=Files.probeContentType(fullPath);
          byte[] fileData=Files.readAllBytes(fullPath);

          // Construct minimal HTTP 200 OK response header
          String responseHeader="HTTP/1.1 200 OK\r\n" +
                  "Content-Type: " + mimiType + "\r\n" +
                  "Content-Length: " + fileData.length + "\r\n" +
                  "\r\n";

          // Write header followed by file content
          output.write(responseHeader.getBytes());
          output.write(fileData);
          return true;
      } else{
          // File not found or is a directory
          return false;
      }
   }

}

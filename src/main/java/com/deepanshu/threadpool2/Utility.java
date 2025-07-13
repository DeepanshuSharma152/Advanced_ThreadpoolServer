package com.deepanshu.threadpool2;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utility {
    public static String hashPassword(String password){
        try{
            // Make a SHA‑256 message digest instance
            MessageDigest md=MessageDigest.getInstance("SHA-256");

            // Compute the hash as a byte array
            byte[] hash= md.digest(password.getBytes());

            // Convert each byte to a two‑digit hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();


        } catch (NoSuchAlgorithmException e){
            throw new RuntimeException("Error Hashing Password", e);
        }
    }

}

package com.stringingbackend.backend.accounts.tools;

// for hashing algorithm
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

public class Hashing {

    private static final Argon2 ARGON2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);

    public static String hashPassword(String password){
        System.out.println("[Hashing] hashPassword called");

        char[] passwordChars = password.toCharArray();

        try {
            return ARGON2.hash(3, 65536, 1, passwordChars);
        } 
        
        finally {
            System.out.println("[Hashing] (200) hashPassword succeeded");
            ARGON2.wipeArray(passwordChars);
        }
    }

    public static Boolean verifyPassword(String passwordToVerify, String dbHashedPassword){
        char[] passwordChars = passwordToVerify.toCharArray();

        try {
            return ARGON2.verify(dbHashedPassword, passwordChars);
        } 
        
        finally {
            System.out.println("[Hashing] (200) verifyPassword succeeded");
            ARGON2.wipeArray(passwordChars);
        }
    }
}

package com.stringingbackend.backend.accounts.tools;

// for hashing algorithm
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

public class Hashing {

    private static final Argon2 ARGON2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);

    public static String hashPassword(String password){
        char[] passwordChars = password.toCharArray();

        try {
            return ARGON2.hash(
                3,       // iterations
                65536,   // memory in KB = 64 MB
                1,       // parallelism
                passwordChars
            );
        } finally {
            ARGON2.wipeArray(passwordChars);
        }
    }

    public static Boolean verifyPassword(String passwordToVerify, String dbHashedPassword){
        char[] passwordChars = passwordToVerify.toCharArray();

        try {
            return ARGON2.verify(
                dbHashedPassword,
                passwordChars
            );
        } finally {
            ARGON2.wipeArray(passwordChars);
        }
    }
}

package com.group15.b07project;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

// Store and retrieve a user's PIN using Android Keystore
// Uses Firebase UID as part of the key, so each user gets a unique PIN record.
public class PinManager {
    SharedPreferences sharedPreferences;

    public PinManager(Context context) {
        sharedPreferences = context.getSharedPreferences("secret_shared_prefs", Context.MODE_PRIVATE);
    }

    private String pinCipherKey(String uid) {
        return "pin_encrypted_" + uid;
    }

    private String pinIvKey(String uid) {
        return "pin_iv_" + uid;
    }

    // Generate or retrieve a secret key from the Android Keystore
    private SecretKey CreateOrGetSecretKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        if (!keyStore.containsAlias("projectB07")) {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(
                    "projectB07",
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build();
            keyGenerator.init(keyGenParameterSpec);
            return keyGenerator.generateKey();
        } else {
            return (SecretKey) keyStore.getKey("projectB07", null);
        }
    }

    // Encrypt and store the PIN
    public void storePin(String uid, String pin) throws Exception {
        SecretKey secretKey = CreateOrGetSecretKey();

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        // Encrypt and Get IV
        byte[] encryptedPin = cipher.doFinal(pin.getBytes(StandardCharsets.UTF_8));
        byte[] iv = cipher.getIV();
        // Convert to Base64
        String encodedEncryptedPin = Base64.encodeToString(encryptedPin, Base64.DEFAULT);
        String encodedIv = Base64.encodeToString(iv, Base64.DEFAULT);

        sharedPreferences.edit()
                .putString(pinCipherKey(uid), encodedEncryptedPin)
                .putString(pinIvKey(uid), encodedIv)
                .apply();
    }


     // Retrieve and decrypt the stored PIN for the given user
     // Returns null if not present or on error
    public String getPin(String uid) throws Exception {
        // Retrieve from SharedPreferences
        String encryptedBase64 = sharedPreferences.getString(pinCipherKey(uid), null);
        String ivBase64 = sharedPreferences.getString(pinIvKey(uid), null);
        if (encryptedBase64 == null || ivBase64 == null)
            return null;
        // Decode Base64
        byte[] encryptedPin = Base64.decode(encryptedBase64, Base64.DEFAULT);
        byte[] iv = Base64.decode(ivBase64, Base64.DEFAULT);
        // Decrypt
        Cipher decryptCipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(128, iv);

        SecretKey secretKey = CreateOrGetSecretKey();
        decryptCipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
        byte[] decrypted = decryptCipher.doFinal(encryptedPin);

        return new String(decrypted, StandardCharsets.UTF_8);
    }

    public boolean hasPin(String uid) {
        return sharedPreferences.contains(pinCipherKey(uid)) && sharedPreferences.contains(pinIvKey(uid));
    }

    public boolean verifyPin(String uid, String inputPin) {
        try {
            String storedPin = getPin(uid);
            return (storedPin != null) && (storedPin.equals(inputPin));
        } catch (Exception e) {
            return false;
        }
    }
}


package com.group15.b07project;

import android.content.Context;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

public class ParseJson {
    public static <E> E loadJson(Context context, String jsonFileName, Type type) { // generic method
        try (InputStream is = context.getAssets().open(jsonFileName);
             InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
             Gson gson = new Gson();
             return gson.fromJson(reader, type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

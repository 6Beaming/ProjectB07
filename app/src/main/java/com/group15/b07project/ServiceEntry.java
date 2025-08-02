package com.group15.b07project;

/**
 * Represents a single service entry with name, phone, and URL.
 * Gson will map JSON fields to these properties.
 */
public class ServiceEntry {
    public String name;   // maps to JSON "name"
    public String phone;  // maps to JSON "phone"
    public String url;    // maps to JSON "url"
}
package com.group15.b07project;

//Model class representing a safe location with an address and notes.
public class SafeLocation {
    private String id;
    private String address;
    private String notes;

    //Default constructor required for Firebase deserialization.
    public SafeLocation() {}

    //Create a new SafeLocation entry.

    public SafeLocation(String id, String address, String notes) {
        this.id = id;
        this.address = address;
        this.notes = notes;
    }

    // Getter and setter methods
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
package com.group15.b07project;

public class EmergencyContact {
    private String id;
    private String name;
    private String relationship;
    private String phone;

    //Default constructor required for Firebase deserialization.
    public EmergencyContact() {}

    //Create a new EmergencyContact entry.
    public EmergencyContact(String id, String name, String relationship, String phone) {
        this.id = id;
        this.name = name;
        this.relationship = relationship;
        this.phone = phone;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}

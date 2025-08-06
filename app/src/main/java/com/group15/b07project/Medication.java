package com.group15.b07project;

/**
 * Model class representing a medication with its name and dosage.
 */
public class Medication {
    private String id;
    private String name;
    private String dosage;

    /** Default constructor for Firebase. */
    public Medication() {}

    /**
     * Create a new Medication entry.
     * @param id     Database key
     * @param name   Medication name
     * @param dosage Dosage information
     */
    public Medication(String id, String name, String dosage) {
        this.id = id;
        this.name = name;
        this.dosage = dosage;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }
}
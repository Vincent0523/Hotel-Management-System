package main.java.com.hotel;

public class User {
    private String id;
    private String username;
    private String email;
    private String hashedPassword;
    private String fullName;
    private String phoneNumber;
    
    public User(String username, String email, String hashedPassword, String fullName, String phoneNumber) {
        this.username = username;
        this.email = email;
        this.hashedPassword = hashedPassword;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
    }
    
    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getHashedPassword() { return hashedPassword; }
    public String getFullName() { return fullName; }
    public String getPhoneNumber() { return phoneNumber; }
} 
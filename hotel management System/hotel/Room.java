package main.java.com.hotel;

public class Room {
    private String name;
    private String view;
    private int capacity;
    private double price;
    private String description;
    
    public Room(String name, String view, int capacity, double price, String description) {
        this.name = name;
        this.view = view;
        this.capacity = capacity;
        this.price = price;
        this.description = description;
    }
    
    public String getName() { return name; }
    public String getView() { return view; }
    public int getCapacity() { return capacity; }
    public double getPrice() { return price; }
    public String getDescription() { return description; }
} 
package main.java.com.hotel;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class DatabaseManager {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private static MongoClient mongoClient;
    private static MongoDatabase database;
    private static MongoCollection<Document> usersCollection;
    private static Properties config;
    
    public static void initialize() throws Exception {
        loadConfig();
        connectToMongoDB();
    }
    
    private static void loadConfig() throws IOException {
        config = new Properties();
        try (InputStream input = DatabaseManager.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new IOException("Unable to find config.properties");
            }
            config.load(input);
        }
    }
    
    private static void connectToMongoDB() throws Exception {
        String host = config.getProperty("mongodb.host", "localhost");
        int port = Integer.parseInt(config.getProperty("mongodb.port", "27017"));
        String databaseName = config.getProperty("mongodb.database", "hotel_booking");
        String username = config.getProperty("mongodb.username");
        String password = config.getProperty("mongodb.password");
        
        ConnectionString connectionString;
        if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
            connectionString = new ConnectionString(
                String.format("mongodb://%s:%s@%s:%d/%s",
                    username, password, host, port, databaseName)
            );
        } else {
            connectionString = new ConnectionString(
                String.format("mongodb://%s:%d/%s",
                    host, port, databaseName)
            );
        }
        
        MongoClientSettings settings = MongoClientSettings.builder()
            .applyConnectionString(connectionString)
            .serverApi(ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build())
            .applyToSocketSettings(builder -> 
                builder.connectTimeout(
                    Integer.parseInt(config.getProperty("mongodb.connectTimeoutMS", "5000")),
                    TimeUnit.MILLISECONDS))
            .applyToClusterSettings(builder -> 
                builder.serverSelectionTimeout(
                    Integer.parseInt(config.getProperty("mongodb.serverSelectionTimeoutMS", "5000")),
                    TimeUnit.MILLISECONDS))
            .build();
        
        try {
            mongoClient = MongoClients.create(settings);
            database = mongoClient.getDatabase(databaseName);
            usersCollection = database.getCollection("users");
            
            // Test the connection
            database.runCommand(new Document("ping", 1));
            logger.info("Successfully connected to MongoDB database: {}", databaseName);
        } catch (Exception e) {
            logger.error("Failed to connect to MongoDB: {}", e.getMessage());
            throw new Exception("Failed to connect to MongoDB: " + e.getMessage());
        }
    }
    
    public static void close() {
        if (mongoClient != null) {
            try {
                mongoClient.close();
                logger.info("MongoDB connection closed successfully");
            } catch (Exception e) {
                logger.error("Error closing MongoDB connection: {}", e.getMessage());
            }
        }
    }
    
    public static boolean registerUser(User user) {
        try {
            // Check if username or email already exists
            Document existingUser = usersCollection.find(
                Filters.or(
                    Filters.eq("username", user.getUsername()),
                    Filters.eq("email", user.getEmail())
                )
            ).first();
            
            if (existingUser != null) {
                logger.warn("User registration failed: Username or email already exists");
                return false;
            }
            
            Document userDoc = new Document()
                .append("username", user.getUsername())
                .append("email", user.getEmail())
                .append("hashedPassword", user.getHashedPassword())
                .append("fullName", user.getFullName())
                .append("phoneNumber", user.getPhoneNumber());
            
            usersCollection.insertOne(userDoc);
            ObjectId id = userDoc.getObjectId("_id");
            if (id != null) {
                user.setId(id.toString());
                logger.info("User registered successfully: {}", user.getUsername());
            }
            return true;
        } catch (Exception e) {
            logger.error("Error registering user: {}", e.getMessage());
            return false;
        }
    }
    
    public static User authenticateUser(String username, String password) {
        try {
            Document userDoc = usersCollection.find(Filters.eq("username", username)).first();
            if (userDoc != null) {
                String hashedPassword = userDoc.getString("hashedPassword");
                if (BCrypt.checkpw(password, hashedPassword)) {
                    logger.info("User authenticated successfully: {}", username);
                    return new User(
                        userDoc.getString("username"),
                        userDoc.getString("email"),
                        hashedPassword,
                        userDoc.getString("fullName"),
                        userDoc.getString("phoneNumber")
                    );
                }
            }
            logger.warn("Authentication failed for user: {}", username);
        } catch (Exception e) {
            logger.error("Error authenticating user: {}", e.getMessage());
        }
        return null;
    }
    
    public static String hashPassword(String plainTextPassword) {
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());
    }
} 
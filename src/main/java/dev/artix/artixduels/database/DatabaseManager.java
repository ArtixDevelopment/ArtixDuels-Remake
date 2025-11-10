package dev.artix.artixduels.database;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import dev.artix.artixduels.ArtixDuels;
import org.bson.Document;
import org.bukkit.configuration.file.FileConfiguration;

public class DatabaseManager {
    private ArtixDuels plugin;
    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> statsCollection;

    public DatabaseManager(ArtixDuels plugin) {
        this.plugin = plugin;
    }

    public void connect() {
        FileConfiguration config = plugin.getConfig();
        String connectionString = config.getString("database.connection-string", "mongodb://localhost:27017");
        String databaseName = config.getString("database.database-name", "artixduels");

        MongoClientURI uri = new MongoClientURI(connectionString);
        mongoClient = new MongoClient(uri);
        database = mongoClient.getDatabase(databaseName);
        statsCollection = database.getCollection("player_stats");

        plugin.getLogger().info("Conectado ao MongoDB com sucesso!");
    }

    public void disconnect() {
        if (mongoClient != null) {
            mongoClient.close();
            plugin.getLogger().info("Desconectado do MongoDB.");
        }
    }

    public MongoCollection<Document> getStatsCollection() {
        return statsCollection;
    }

    public MongoCollection<Document> getDuelHistoryCollection() {
        if (database == null) {
            return null;
        }
        return database.getCollection("duel_history");
    }

    public MongoDatabase getDatabase() {
        return database;
    }
}


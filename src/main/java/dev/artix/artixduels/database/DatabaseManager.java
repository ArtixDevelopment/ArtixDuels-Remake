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
    private String storageType;
    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> statsCollection;
    private IStatsDAO statsDAO;
    private IDuelHistoryDAO historyDAO;

    public DatabaseManager(ArtixDuels plugin) {
        this.plugin = plugin;
    }

    public void connect() {
        FileConfiguration config = plugin.getConfig();
        storageType = config.getString("database.type", "mongodb").toLowerCase();

        if (storageType.equals("flatfile") || storageType.equals("flat-file") || storageType.equals("file")) {
            statsDAO = new FlatFileStatsDAO(plugin);
            historyDAO = new FlatFileDuelHistoryDAO(plugin);
            plugin.getLogger().info("Usando armazenamento Flat-File (arquivos YAML)!");
        } else {
            String connectionString = config.getString("database.connection-string", "mongodb://localhost:27017");
            String databaseName = config.getString("database.database-name", "artixduels");

            MongoClientURI uri = new MongoClientURI(connectionString);
            mongoClient = new MongoClient(uri);
            database = mongoClient.getDatabase(databaseName);
            statsCollection = database.getCollection("player_stats");

            statsDAO = new StatsDAO(statsCollection);
            historyDAO = new DuelHistoryDAO(database.getCollection("duel_history"));

            plugin.getLogger().info("Conectado ao MongoDB com sucesso!");
        }
    }

    public void disconnect() {
        if (mongoClient != null) {
            mongoClient.close();
            plugin.getLogger().info("Desconectado do MongoDB.");
        }
    }

    public boolean isMongoDB() {
        return !storageType.equals("flatfile") && !storageType.equals("flat-file") && !storageType.equals("file");
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

    public IStatsDAO getStatsDAO() {
        return statsDAO;
    }

    public IDuelHistoryDAO getHistoryDAO() {
        return historyDAO;
    }
}


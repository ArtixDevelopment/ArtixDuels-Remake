package dev.artix.artixduels.database;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import dev.artix.artixduels.models.DuelHistory;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DuelHistoryDAO {
    private MongoCollection<Document> collection;

    public DuelHistoryDAO(MongoCollection<Document> collection) {
        this.collection = collection;
    }

    public void saveDuelHistory(DuelHistory history) {
        Document doc = historyToDocument(history);
        collection.insertOne(doc);
    }

    public List<DuelHistory> getPlayerHistory(UUID playerId, int limit) {
        List<DuelHistory> history = new ArrayList<>();
        
        for (Document doc : collection.find(
            Filters.or(
                Filters.eq("player1Id", playerId.toString()),
                Filters.eq("player2Id", playerId.toString())
            )
        ).limit(limit).sort(new Document("timestamp", -1))) {
            history.add(documentToHistory(doc));
        }
        
        return history;
    }

    private Document historyToDocument(DuelHistory history) {
        Document doc = new Document();
        doc.append("player1Id", history.getPlayer1Id().toString());
        doc.append("player1Name", history.getPlayer1Name());
        doc.append("player2Id", history.getPlayer2Id().toString());
        doc.append("player2Name", history.getPlayer2Name());
        doc.append("winnerId", history.getWinnerId() != null ? history.getWinnerId().toString() : null);
        doc.append("kitName", history.getKitName());
        doc.append("arenaName", history.getArenaName());
        doc.append("timestamp", history.getTimestamp());
        doc.append("duration", history.getDuration());
        return doc;
    }

    private DuelHistory documentToHistory(Document doc) {
        DuelHistory history = new DuelHistory();
        history.setPlayer1Id(UUID.fromString(doc.getString("player1Id")));
        history.setPlayer1Name(doc.getString("player1Name"));
        history.setPlayer2Id(UUID.fromString(doc.getString("player2Id")));
        history.setPlayer2Name(doc.getString("player2Name"));
        if (doc.getString("winnerId") != null) {
            history.setWinnerId(UUID.fromString(doc.getString("winnerId")));
        }
        history.setKitName(doc.getString("kitName"));
        history.setArenaName(doc.getString("arenaName"));
        history.setTimestamp(doc.getLong("timestamp"));
        history.setDuration(doc.getLong("duration"));
        return history;
    }
}


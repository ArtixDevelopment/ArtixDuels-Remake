package dev.artix.artixduels.listeners;

import dev.artix.artixduels.npcs.DuelNPC;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class NPCListener implements Listener {
    private DuelNPC duelNPC;

    public NPCListener(DuelNPC duelNPC) {
        this.duelNPC = duelNPC;
    }

    @EventHandler
    public void onNPCRightClick(NPCRightClickEvent event) {
        NPC npc = event.getNPC();
        if (npc.data().has("duel-npc") && npc.data().get("duel-npc").equals(true)) {
            duelNPC.onNPCClick(event.getClicker(), npc);
        }
    }
}


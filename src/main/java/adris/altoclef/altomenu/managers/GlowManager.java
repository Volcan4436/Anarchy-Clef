package adris.altoclef.altomenu.managers;


import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.Formatting;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class GlowManager {

    // Logical glow tracking by UUID
    private static final Set<UUID> glowingEntities = new HashSet<>();

    // Map UUID to Entity instance
    private static final Map<UUID, Entity> uuidEntityMap = new HashMap<>();

    // Team names and colors
    private static final String TEAM_PLAYER = "Player";
    private static final String TEAM_HOSTILE = "Hostile";
    private static final String TEAM_PASSIVE = "Passive";

    private static MinecraftClient mc() {
        return MinecraftClient.getInstance();
    }

    private static Scoreboard getScoreboard() {
        if (mc().world == null) return null;
        return mc().world.getScoreboard();
    }

    private static Team getOrCreateTeam(String teamName, Formatting color) {
        Scoreboard scoreboard = getScoreboard();
        if (scoreboard == null) return null;

        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.addTeam(teamName);
            team.setColor(color);
            team.setShowFriendlyInvisibles(true);
            team.setFriendlyFireAllowed(true);
        }
        return team;
    }

    // Register entity so UUID can be mapped to entity instance
    public static void registerEntity(Entity entity) {
        if (entity == null) return;
        uuidEntityMap.put(entity.getUuid(), entity);
    }

    // --- UUID glow tracking methods ---

    public static void addGlow(UUID uuid) {
        glowingEntities.add(uuid);
        Entity entity = uuidEntityMap.get(uuid);
        if (entity != null) {
            addEntityToTeam(entity);
        }
    }

    public static void removeGlow(UUID uuid) {
        glowingEntities.remove(uuid);
        Entity entity = uuidEntityMap.get(uuid);
        if (entity != null) {
            removeEntityFromTeams(entity);
        }
    }

    public static boolean shouldGlow(UUID uuid) {
        return glowingEntities.contains(uuid);
    }

    public static void clear() {
        glowingEntities.clear();
        clearTeams();
        uuidEntityMap.clear();
    }

    // --- Scoreboard team management methods ---

    public static void addEntityToTeam(Entity entity) {
        Scoreboard scoreboard = getScoreboard();
        if (scoreboard == null) return;

        String entityName = entity.getName().toString();

        // Remove from all glow teams first to avoid duplicates
        removeEntityFromTeams(entity);

        Team playerTeam = getOrCreateTeam(TEAM_PLAYER, Formatting.LIGHT_PURPLE);
        Team hostileTeam = getOrCreateTeam(TEAM_HOSTILE, Formatting.RED);
        Team passiveTeam = getOrCreateTeam(TEAM_PASSIVE, Formatting.GREEN);

        if (entity instanceof PlayerEntity && playerTeam != null) {
            Collection<String> players = playerTeam.getPlayerList();
            if (!players.contains(entityName)) {
                players.add(entityName);
            }
        } else if (entity instanceof HostileEntity && hostileTeam != null) {
            Collection<String> players = hostileTeam.getPlayerList();
            if (!players.contains(entityName)) {
                players.add(entityName);
            }
        } else if (entity instanceof PassiveEntity && passiveTeam != null) {
            Collection<String> players = passiveTeam.getPlayerList();
            if (!players.contains(entityName)) {
                players.add(entityName);
            }
        }
    }

    public static void removeEntityFromTeams(Entity entity) {
        Scoreboard scoreboard = getScoreboard();
        if (scoreboard == null) return;

        String entityName = entity.getName().toString();

        Team playerTeam = scoreboard.getTeam(TEAM_PLAYER);
        Team hostileTeam = scoreboard.getTeam(TEAM_HOSTILE);
        Team passiveTeam = scoreboard.getTeam(TEAM_PASSIVE);

        if (playerTeam != null && playerTeam.getPlayerList().contains(entityName)) {
            playerTeam.getPlayerList().remove(entityName);
        }
        if (hostileTeam != null && hostileTeam.getPlayerList().contains(entityName)) {
            hostileTeam.getPlayerList().remove(entityName);
        }
        if (passiveTeam != null && passiveTeam.getPlayerList().contains(entityName)) {
            passiveTeam.getPlayerList().remove(entityName);
        }
    }

    public static void clearTeams() {
        Scoreboard scoreboard = getScoreboard();
        if (scoreboard == null) return;

        Team playerTeam = scoreboard.getTeam(TEAM_PLAYER);
        Team hostileTeam = scoreboard.getTeam(TEAM_HOSTILE);
        Team passiveTeam = scoreboard.getTeam(TEAM_PASSIVE);

        if (playerTeam != null) playerTeam.getPlayerList().clear();
        if (hostileTeam != null) hostileTeam.getPlayerList().clear();
        if (passiveTeam != null) passiveTeam.getPlayerList().clear();
    }
}

package com.iridium.iridiumteams.database;

import com.iridium.iridiumteams.IridiumTeams;
import com.iridium.iridiumteams.enhancements.*;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.*;

@Getter
@DatabaseTable(tableName = "users")
public abstract class IridiumUser<T extends Team> extends DatabaseObject {

    @DatabaseField(columnName = "uuid", canBeNull = false, id = true)
    private @NotNull UUID uuid;

    @DatabaseField(columnName = "name", canBeNull = false)
    private @NotNull String name;

    @DatabaseField(columnName = "active_profile")
    protected int profile =0;

    private BukkitTask bukkitTask;

    private int bukkitTaskTicks = 0;

    private String chatType = "";
    private boolean bypassing;
    private boolean flying;


    public abstract IridiumUserProfile<T> getActiveProfile();
    public abstract void setActiveProfile(IridiumUserProfile<T> profile);

    public Player getPlayer() {
        return Bukkit.getServer().getPlayer(uuid);
    }

    public void setUuid(@NotNull UUID uuid) {
        this.uuid = uuid;
        setChanged(true);
    }

    public void setName(@NotNull String name) {
        this.name = name;
        setChanged(true);
    }

    public void initBukkitTask(IridiumTeams<T, ?> iridiumTeams) {
        if (bukkitTask != null) return;
        bukkitTask = Bukkit.getScheduler().runTaskTimer(iridiumTeams, () -> bukkitTask(iridiumTeams), 0, 20);
    }

    public void bukkitTask(IridiumTeams<T, ?> iridiumTeams) {
        bukkitTaskTicks++;
        applyPotionEffects(iridiumTeams);
    }

    

    public boolean canFly(IridiumTeams<T, ?> iridiumTeams) {
        Player player = getPlayer();
        if (player.hasPermission(iridiumTeams.getCommands().flyCommand.permission)) return true;
        if (isBypassing()) return true;
        Optional<T> team = iridiumTeams.getTeamManager().getTeamViaID(getActiveProfile().getTeamID());
        Optional<T> visitor = iridiumTeams.getTeamManager().getTeamViaPlayerLocation(player);
        return canFly(team.orElse(null), iridiumTeams) || canFly(visitor.orElse(null), iridiumTeams);
    }

    private boolean canFly(T team, IridiumTeams<T, ?> iridiumTeams) {
        if (team == null) return false;
        Enhancement<FlightEnhancementData> flightEnhancement = iridiumTeams.getEnhancements().flightEnhancement;
        TeamEnhancement teamEnhancement = iridiumTeams.getTeamManager().getTeamEnhancement(team, "flight");
        FlightEnhancementData data = flightEnhancement.levels.get(teamEnhancement.getLevel());

        if (!teamEnhancement.isActive(flightEnhancement.type)) return false;
        if (data == null) return false;

        return canApply(iridiumTeams, team, data.enhancementAffectsType);
    }
    public void applyPotionEffects(IridiumTeams<T, ?> iridiumTeams) {
        Player player = getPlayer();
        if (player == null) return;
        iridiumTeams.getTeamManager().getTeamViaLocation(player.getLocation()).ifPresent(t -> applyPotionEffects(iridiumTeams, t));
        iridiumTeams.getTeamManager().getTeamViaID(getActiveProfile().getTeamID()).ifPresent(t -> applyPotionEffects(iridiumTeams, t));
    }

    public void applyPotionEffects(IridiumTeams<T, ?> iridiumTeams, T team) {
        int duration = 10;
        Player player = getPlayer();
        if (player == null) return;
        HashMap<PotionEffectType, Integer> potionEffects = new HashMap<>();

        for (Map.Entry<String, Enhancement<?>> enhancement : iridiumTeams.getEnhancementList().entrySet()) {
            TeamEnhancement teamEnhancement = iridiumTeams.getTeamManager().getTeamEnhancement(team, enhancement.getKey());
            if (!teamEnhancement.isActive(enhancement.getValue().type)) continue;
            EnhancementData enhancementData = enhancement.getValue().levels.get(teamEnhancement.getLevel());
            if (enhancementData instanceof PotionEnhancementData) {
                PotionEnhancementData potionEnhancementData = (PotionEnhancementData) enhancementData;
                if (!canApply(iridiumTeams, team, potionEnhancementData.enhancementAffectsType)) continue;
                PotionEffectType potionEffectType = potionEnhancementData.potion.getPotionEffectType();
                if (!potionEffects.containsKey(potionEffectType)) {
                    potionEffects.put(potionEffectType, potionEnhancementData.strength - 1);
                } else if (potionEffects.get(potionEffectType) < potionEnhancementData.strength - 1) {
                    potionEffects.put(potionEffectType, potionEnhancementData.strength - 1);
                }
            }
        }

        for (Map.Entry<PotionEffectType, Integer> potionEffectType : potionEffects.entrySet()) {
            Optional<PotionEffect> potionEffect = player.getActivePotionEffects().stream()
                    .filter(effect -> effect.getType().equals(potionEffectType.getKey()))
                    .findFirst();
            if (potionEffect.isPresent()) {
                if (potionEffect.get().getAmplifier() <= potionEffectType.getValue() && potionEffect.get().getDuration() <= duration * 20) {
                    player.removePotionEffect(potionEffectType.getKey());
                }
            }
            player.addPotionEffect(potionEffectType.getKey().createEffect(duration * 20, potionEffectType.getValue()));
        }
    }

    public boolean canApply(IridiumTeams<T, ?> iridiumTeams, T team, List<EnhancementAffectsType> enhancementAffectsTypes) {
        Player player =getPlayer();
        if (player == null) return false;
        int teamLocationID = iridiumTeams.getTeamManager().getTeamViaLocation(player.getLocation()).map(T::getId).orElse(0);
        for (EnhancementAffectsType enhancementAffectsType : enhancementAffectsTypes) {
            if (enhancementAffectsType == EnhancementAffectsType.VISITORS && team.getId() == teamLocationID) {
                return true;
            }
            if (enhancementAffectsType == EnhancementAffectsType.MEMBERS_ANYWHERE && team.getId() == getActiveProfile().getTeamID()) {
                return true;
            }
            if (enhancementAffectsType == EnhancementAffectsType.MEMBERS_IN_TERRITORY && team.getId() == getActiveProfile().getTeamID() && team.getId() == teamLocationID) {
                return true;
            }
        }
        return false;
    }

     public void setBypassing(boolean bypassing) {
        this.bypassing=bypassing;
    }

    public void setFlying(boolean flying) {
        this.flying = flying;
    }

    public void setChatType(String chatType) {
        this.chatType = chatType;
    }

}

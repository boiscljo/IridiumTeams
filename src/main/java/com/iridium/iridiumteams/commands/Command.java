package com.iridium.iridiumteams.commands;

import com.iridium.iridiumcore.utils.StringUtils;
import com.iridium.iridiumteams.IridiumTeams;
import com.iridium.iridiumteams.database.IridiumUser;
import com.iridium.iridiumteams.database.Team;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.NotImplementedException;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class Command<T extends Team, U extends IridiumUser<T>> {

    public final @NotNull List<String> aliases;
    public final @NotNull String description;
    public final @NotNull String syntax;
    public final @NotNull String permission;
    public final boolean enabled = true;

    public Command() {
        this.aliases = Collections.emptyList();
        this.description = "";
        this.syntax = "";
        this.permission = "";
    }


    public void execute(CommandSender sender, String[] arguments, IridiumTeams<T, U> iridiumTeams) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(StringUtils.color(iridiumTeams.getMessages().mustBeAPlayer
                    .replace("%prefix%", iridiumTeams.getConfiguration().prefix))
            );
            return;
        }
        execute(iridiumTeams.getUserManager().getUser((OfflinePlayer) sender), arguments, iridiumTeams);
    }

    public void execute(U user, String[] arguments, IridiumTeams<T, U> iridiumTeams) {
        Optional<T> team = iridiumTeams.getTeamManager().getTeamViaID(user.getActiveProfile().getTeamID());
        if (!team.isPresent()) {
            user.getPlayer().sendMessage(StringUtils.color(iridiumTeams.getMessages().dontHaveTeam
                    .replace("%prefix%", iridiumTeams.getConfiguration().prefix))
            );
            return;
        }
        execute(user, team.get(), arguments, iridiumTeams);
    }

    public void execute(U user, T team, String[] arguments, IridiumTeams<T, U> iridiumTeams) {
        throw new NotImplementedException();
    }

    public boolean hasPermission(CommandSender commandSender, IridiumTeams<T, U> iridiumTeams) {
        return commandSender.hasPermission(permission) || permission.equalsIgnoreCase("");
    }

    public List<String> onTabComplete(CommandSender commandSender, String[] args, IridiumTeams<T, U> iridiumTeams) {
        if (commandSender instanceof Player) {
            U user = iridiumTeams.getUserManager().getUser((OfflinePlayer) commandSender);
            Optional<T> team = iridiumTeams.getTeamManager().getTeamViaID(user.getActiveProfile().getTeamID());
            if (team.isPresent()) {
                return onTabComplete(user, team.get(), args, iridiumTeams);
            }
        }
        return Collections.emptyList();
    }

    public List<String> onTabComplete(U user, T team, String[] args, IridiumTeams<T, U> iridiumTeams) {
        return Collections.emptyList();
    }

}

package com.iridium.iridiumteams.commands;

import com.iridium.iridiumcore.utils.StringUtils;
import com.iridium.iridiumteams.IridiumTeams;
import com.iridium.iridiumteams.Rank;
import com.iridium.iridiumteams.database.IridiumUser;
import com.iridium.iridiumteams.database.Team;
import com.iridium.iridiumteams.gui.ConfirmationGUI;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

@NoArgsConstructor
public class DeleteCommand<T extends Team, U extends IridiumUser<T>> extends Command<T, U> {
    public String adminPermission;

    public DeleteCommand(List<String> args, String description, String syntax, String permission, String adminPermission) {
        super(args, description, syntax, permission);
        this.adminPermission = adminPermission;
    }

    @Override
    public void execute(U user, String[] arguments, IridiumTeams<T, U> iridiumTeams) {
        Player player = user.getPlayer();
        if (arguments.length == 1) {
            if (!player.hasPermission(adminPermission)) {
                player.sendMessage(StringUtils.color(iridiumTeams.getMessages().noPermission
                        .replace("%prefix%", iridiumTeams.getConfiguration().prefix)
                ));
                return;
            }
            Optional<T> team = iridiumTeams.getTeamManager().getTeamViaNameOrPlayer(arguments[0]);
            if (!team.isPresent()) {
                player.sendMessage(StringUtils.color(iridiumTeams.getMessages().teamDoesntExistByName
                        .replace("%prefix%", iridiumTeams.getConfiguration().prefix))
                );
                return;
            }
            deleteTeam(user, team.get(), iridiumTeams, true);
            return;
        }
        super.execute(user, arguments, iridiumTeams);
    }

    @Override
    public void execute(U user, T team, String[] arguments, IridiumTeams<T, U> iridiumTeams) {
        Player player = user.getPlayer();
        if (user.getActiveProfile().getUserRank() != Rank.OWNER.getId() && !user.isBypassing()) {
            player.sendMessage(StringUtils.color(iridiumTeams.getMessages().cannotDeleteTeam
                    .replace("%prefix%", iridiumTeams.getConfiguration().prefix)
            ));
            return;
        }
        deleteTeam(user, team, iridiumTeams, false);
    }

    private void deleteTeam(U user, T team, IridiumTeams<T, U> iridiumTeams, boolean admin) {
        Player player = user.getPlayer();
        player.openInventory(new ConfirmationGUI<>(() -> {
            iridiumTeams.getTeamManager().deleteTeam(team, user);
            for (U member : iridiumTeams.getTeamManager().getTeamMembers(team)) {
                member.getActiveProfile().setTeamID(0);
                Player teamMember = member.getPlayer();
                if (teamMember != null) {
                    teamMember.sendMessage(StringUtils.color(iridiumTeams.getMessages().teamDeleted
                            .replace("%prefix%", iridiumTeams.getConfiguration().prefix)
                            .replace("%player%", player.getName())
                    ));
                }
            }
            if (admin) {
                player.sendMessage(StringUtils.color(iridiumTeams.getMessages().deletedPlayerTeam
                        .replace("%prefix%", iridiumTeams.getConfiguration().prefix)
                        .replace("%name%", team.getName())
                ));
            }
        }, iridiumTeams).getInventory());
    }

}

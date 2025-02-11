package com.iridium.iridiumteams.commands;

import com.iridium.iridiumcore.utils.StringUtils;
import com.iridium.iridiumteams.IridiumTeams;
import com.iridium.iridiumteams.Rank;
import com.iridium.iridiumteams.database.IridiumUser;
import com.iridium.iridiumteams.database.Team;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

@NoArgsConstructor
public class LeaveCommand<T extends Team, U extends IridiumUser<T>> extends Command<T, U> {
    public LeaveCommand(List<String> args, String description, String syntax, String permission) {
        super(args, description, syntax, permission);
    }

    @Override
    public void execute(U user, T team, String[] args, IridiumTeams<T, U> iridiumTeams) {
        Player player = user.getPlayer();

        if(user.getActiveProfile().getUserRank()== Rank.OWNER.getId()){
            player.sendMessage(StringUtils.color(iridiumTeams.getMessages().ownerCannotLeave
                    .replace("%prefix%", iridiumTeams.getConfiguration().prefix)
            ));
            return;
        }

        player.sendMessage(StringUtils.color(iridiumTeams.getMessages().leftTeam
                .replace("%prefix%", iridiumTeams.getConfiguration().prefix)
                .replace("%name%", team.getName())
        ));

        iridiumTeams.getTeamManager().getTeamMembers(team).forEach(teamUser -> {
            Player teamPlayer = Bukkit.getPlayer(teamUser.getUuid());
            if (teamPlayer != null && teamPlayer != player) {
                teamPlayer.sendMessage(StringUtils.color(iridiumTeams.getMessages().userLeftTeam
                        .replace("%prefix%", iridiumTeams.getConfiguration().prefix)
                        .replace("%name%", team.getName())
                        .replace("%player%", player.getName())
                ));
            }
        });

        user.getActiveProfile().setTeam(null);
    }

}

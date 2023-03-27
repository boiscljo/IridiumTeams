package com.iridium.iridiumteams.commands;

import com.iridium.iridiumcore.utils.StringUtils;
import com.iridium.iridiumteams.IridiumTeams;
import com.iridium.iridiumteams.database.IridiumUser;
import com.iridium.iridiumteams.database.Team;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.util.List;

@NoArgsConstructor
public class BypassCommand<T extends Team, U extends IridiumUser<T>> extends Command<T, U> {
    public BypassCommand(List<String> args, String description, String syntax, String permission) {
        super(args, description, syntax, permission);
    }

    @Override
    public void execute(U user, String[] arguments, IridiumTeams<T, U> iridiumTeams) {
        Player player = user.getPlayer();
        user.getActiveProfile().setBypassing(!user.getActiveProfile().isBypassing());
        player.sendMessage(StringUtils.color((user.getActiveProfile().isBypassing() ? iridiumTeams.getMessages().nowBypassing : iridiumTeams.getMessages().noLongerBypassing)
                .replace("%prefix%", iridiumTeams.getConfiguration().prefix)
        ));
    }

}

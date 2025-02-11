package com.iridium.iridiumteams.gui;

import com.iridium.iridiumcore.gui.PagedGUI;
import com.iridium.iridiumcore.utils.ItemStackUtils;
import com.iridium.iridiumcore.utils.StringUtils;
import com.iridium.iridiumteams.IridiumTeams;
import com.iridium.iridiumteams.configs.inventories.NoItemGUI;
import com.iridium.iridiumteams.database.IridiumUser;
import com.iridium.iridiumteams.database.Team;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class MembersGUI<T extends Team, U extends IridiumUser<T>> extends PagedGUI<U> {

    private final IridiumTeams<T, U> iridiumTeams;
    private final T team;

    public MembersGUI(T team, Inventory previousInventory, IridiumTeams<T, U> iridiumTeams) {
        super(
                1,
                iridiumTeams.getInventories().membersGUI.size,
                iridiumTeams.getInventories().membersGUI.background,
                iridiumTeams.getInventories().previousPage,
                iridiumTeams.getInventories().nextPage,
                previousInventory,
                iridiumTeams.getInventories().backButton
        );
        this.iridiumTeams = iridiumTeams;
        this.team = team;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        NoItemGUI noItemGUI = iridiumTeams.getInventories().membersGUI;
        Inventory inventory = Bukkit.createInventory(this, getSize(), StringUtils.color(noItemGUI.title));
        addContent(inventory);
        return inventory;
    }

    @Override
    public Collection<U> getPageObjects() {
        return iridiumTeams.getTeamManager().getTeamMembers(team);
    }

    @Override
    public ItemStack getItemStack(U user) {
        return ItemStackUtils.makeItem(iridiumTeams.getInventories().membersGUI.item, iridiumTeams.getUserPlaceholderBuilder().getPlaceholders(user));
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        super.onInventoryClick(event);

        U user = getItem(event.getSlot());
        if (user == null) return;

        switch (event.getClick()) {
            case LEFT:
                if (user.getActiveProfile().getUserRank() != 1) {
                    iridiumTeams.getCommands().demoteCommand.execute(event.getWhoClicked(), new String[]{user.getName()}, iridiumTeams);
                } else {
                    iridiumTeams.getCommands().kickCommand.execute(event.getWhoClicked(), new String[]{user.getName()}, iridiumTeams);
                }
                break;
            case RIGHT:
                iridiumTeams.getCommands().promoteCommand.execute(event.getWhoClicked(), new String[]{user.getName()}, iridiumTeams);
                break;
        }
    }
}

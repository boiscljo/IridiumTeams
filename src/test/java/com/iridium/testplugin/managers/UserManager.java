package com.iridium.testplugin.managers;

import com.iridium.iridiumteams.database.IridiumUserProfile;
import com.iridium.iridiumteams.managers.IridiumUserManager;
import com.iridium.testplugin.TestTeam;
import com.iridium.testplugin.User;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserManager implements IridiumUserManager<TestTeam, User> {
    public static List<User> users;
    public static List<IridiumUserProfile<TestTeam>> profiles;

    public UserManager() {
        users = new ArrayList<>();
        profiles = new ArrayList<>();
    }

    @Override
    public @NotNull User getUser(@NotNull OfflinePlayer offlinePlayer) {
        Optional<User> userOptional = getUserByUUID(offlinePlayer.getUniqueId());
        if (userOptional.isPresent()) {
            return userOptional.get();
        } else {
            Optional<String> name = Optional.ofNullable(offlinePlayer.getName());
            User user = new User(offlinePlayer.getUniqueId(), name.orElse(""));
            users.add(user);
            IridiumUserProfile<TestTeam> profile = new IridiumUserProfile<>(profiles.size() + 1, user.getUuid(),
                    "default");
            profiles.add(profile);
            user.setActiveProfile(profile);
            return user;
        }
    }

    public Optional<User> getUserByUUID(@NotNull UUID uuid) {
        return users.stream().filter(user -> user.getUuid() == uuid).findFirst();
    }

    @Override
    public Optional<IridiumUserProfile<TestTeam>> getUserProfile(int id) {
        return profiles.stream().filter(profile -> profile.getId() == id).findAny();
    }

    @Override
    public List<IridiumUserProfile<TestTeam>> getUserProfiles(User user) {
        return profiles.stream().filter(profile -> profile.getUuid().equals(user.getUuid())).toList();
    }

    @Override
    public IridiumUserProfile<TestTeam> createUserProfile(User user, String name) {
        IridiumUserProfile<TestTeam> profile = new IridiumUserProfile<>(profiles.size() + 1, user.getUuid(), name);
        profiles.add(profile);
        return profile;
    }

    public List<User> getUsers() {
        return users;
    }
}

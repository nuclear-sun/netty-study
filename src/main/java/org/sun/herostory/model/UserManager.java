package org.sun.herostory.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class UserManager {

    private static final Map<Integer, User> userMap = new HashMap<Integer, User>();

    private UserManager() {}

    public static void addUser(User user) {
        if(user == null) {
            return;
        }
        userMap.put(user.getUserId(), user);
    }

    public static void removeUserById(Integer id) {
        if(id == null) {
            return;
        }
        userMap.remove(id);
    }

    public static User getUserById(Integer id) {
        if(id == null) {
            return null;
        }
        return userMap.get(id);
    }

    public static Collection<User> listUsers() {
        return userMap.values();
    }

}

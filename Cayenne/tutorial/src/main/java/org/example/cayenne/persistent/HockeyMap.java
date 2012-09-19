package org.example.cayenne.persistent;

import org.example.cayenne.persistent.auto._HockeyMap;

public class HockeyMap extends _HockeyMap {

    private static HockeyMap instance;

    private HockeyMap() {}

    public static HockeyMap getInstance() {
        if(instance == null) {
            instance = new HockeyMap();
        }

        return instance;
    }
}

package com.mialliance.utils;

import com.mialliance.mind.base.communication.CommDispatcher;

public final class CommUtils {

    public static boolean onSameTeam(CommDispatcher origin, OwnerTeamSupplier obj) {
        if (!(origin instanceof OwnerTeamSupplier supp)) {
            return false;
        }
        return supp.getOwnerTeam() == obj.getOwnerTeam();
    }
}

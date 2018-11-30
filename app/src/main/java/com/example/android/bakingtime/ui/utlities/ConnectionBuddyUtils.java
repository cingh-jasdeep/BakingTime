package com.example.android.bakingtime.ui.utlities;

import android.os.Bundle;

import com.zplesac.connectionbuddy.ConnectionBuddy;
import com.zplesac.connectionbuddy.cache.ConnectionBuddyCache;
import com.zplesac.connectionbuddy.interfaces.ConnectivityChangeListener;

public class ConnectionBuddyUtils {

    public static void clearConnectionBuddyState(Bundle savedInstanceState, Object activityObject) {
        if(savedInstanceState != null){
            if(ConnectionBuddyCache.isLastNetworkStateStored(activityObject)) {
                ConnectionBuddyCache.clearLastNetworkState(activityObject);
            }
        }
    }
    /**
     * @param object   Object which is registered to network change receiver.
     * @param listener Callback listener.
     */
    public static void registerConnectionBuddyEvents(Object object, ConnectivityChangeListener listener) {
        ConnectionBuddy.getInstance().registerForConnectivityEvents(object, listener);
    }

    public static void unregisterConnectionBuddyEvents(Object object) {
        ConnectionBuddy.getInstance().unregisterFromConnectivityEvents(object);
    }
}

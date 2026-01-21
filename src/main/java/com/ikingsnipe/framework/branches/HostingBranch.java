package com.ikingsnipe.framework.branches;

import com.ikingsnipe.framework.core.Branch;
import com.ikingsnipe.casino.managers.LocationManager;

/**
 * Branch that handles all hosting-related activities.
 */
public class HostingBranch extends Branch {
    private final LocationManager locationManager;

    public HostingBranch(LocationManager locationManager) {
        this.locationManager = locationManager;
    }

    @Override
    public boolean isValid() {
        // Hosting is valid if we are at the target location
        return locationManager.isAtLocation();
    }
}

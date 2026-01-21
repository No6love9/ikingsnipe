package com.ikingsnipe.framework.branches;

import com.ikingsnipe.framework.core.Branch;

/**
 * Branch that handles maintenance tasks like banking and muling.
 */
public class MaintenanceBranch extends Branch {
    @Override
    public boolean isValid() {
        // Maintenance is checked if we are not actively hosting or need restock/mule
        return true; 
    }
}

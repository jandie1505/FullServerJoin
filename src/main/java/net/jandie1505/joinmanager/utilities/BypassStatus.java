package net.jandie1505.joinmanager.utilities;

public enum BypassStatus {
    NOT_AVAILABLE(false),
    TEMPORARY(true),
    PERMANENT(true);

    private boolean bypass;

    BypassStatus(boolean bypass) {
        this.bypass = bypass;
    }

    public boolean isBypass() {
        return bypass;
    }
}

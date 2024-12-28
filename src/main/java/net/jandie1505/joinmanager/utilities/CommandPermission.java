package net.jandie1505.joinmanager.utilities;

public enum CommandPermission {
    NONE(false, false),
    INFO(true, false),
    MANAGE(true, true);

    private final boolean info;
    private final boolean manage;

    CommandPermission(boolean info, boolean manage) {
        this.info = info;
        this.manage = manage;
    }

    public final boolean info() {
        return info;
    }

    public final boolean manage() {
        return manage;
    }

}

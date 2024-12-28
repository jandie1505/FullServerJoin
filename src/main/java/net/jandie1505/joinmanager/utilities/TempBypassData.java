package net.jandie1505.joinmanager.utilities;

public final class TempBypassData {
    private final long removeAtEpoch;
    private boolean used;

    public TempBypassData(long removeAtEpoch) {
        this.removeAtEpoch = removeAtEpoch;
        this.used = false;
    }

    public boolean isUsed() {
        return this.used;
    }

    public boolean used() {
        return this.isUsed();
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public long getRemoveAtEpoch() {
        return this.removeAtEpoch;
    }

    public long removeAtEpoch() {
        return this.getRemoveAtEpoch();
    }

}

package io.github.ladysnake.sincereloyalty;

import org.jetbrains.annotations.Contract;

public interface TridentRecaller {
    /**
     * Returns {@code true} if the status was changed through this call
     */
    @Contract(mutates = "this")
    boolean sincereloyalty_updateRecallStatus(RecallStatus recalling);

    @Contract(pure = true)
    boolean isRecallingTrident();

    enum RecallStatus {
        CHARGING, CANCEL, RECALL
    }
}

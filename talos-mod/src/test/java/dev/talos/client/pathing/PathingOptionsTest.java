package dev.talos.client.pathing;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PathingOptionsTest {
    @Test
    void navigateOnlyDisablesMining() {
        assertFalse(PathingOptions.NAVIGATE_ONLY.allowMining());
    }

    @Test
    void defaultOptionsKeepExistingMiningBehavior() {
        assertTrue(PathingOptions.DEFAULT.allowMining());
        assertTrue(new PathingOptions().allowMining());
        assertTrue(new PathingOptions(12).allowMining());
    }
}

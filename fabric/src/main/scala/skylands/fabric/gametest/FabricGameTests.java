package skylands.fabric.gametest;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import skylands.gametest.SkylandsTests;

// Fabric discovers gametest classes via the "fabric-gametest" entrypoint
// in fabric.mod.json. Each entry must implement FabricGameTest.
// @GameTest-annotated methods are instance methods on the implementing class,
// not static — unlike NeoForge's @GameTestHolder pattern.
public final class FabricGameTests implements FabricGameTest {

    @GameTest(template = "skylands:empty")
    public void placeBeanBlock(GameTestHelper helper) {
        SkylandsTests.placeBeanBlock(helper);
    }

    @GameTest(template = "skylands:empty")
    public void placeCloudBlock(GameTestHelper helper) {
        SkylandsTests.placeCloudBlock(helper);
    }

    @GameTest(template = "skylands:empty")
    public void beanstalkCenterPropertyRoundTrips(GameTestHelper helper) {
        SkylandsTests.beanstalkCenterPropertyRoundTrips(helper);
    }
}

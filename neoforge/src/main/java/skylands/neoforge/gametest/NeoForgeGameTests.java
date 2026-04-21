package skylands.neoforge.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import skylands.gametest.SkylandsTests;

// @GameTestHolder + @PrefixGameTestTemplate are NeoForge's gametest
// discovery annotations (net.neoforged.neoforge.gametest.*), not vanilla
// MC annotations. The PrefixGameTestTemplate(false) stops the framework
// from turning "skylands:empty" into "skylands:skylands:empty".
@GameTestHolder("skylands")
@PrefixGameTestTemplate(value = false)
public final class NeoForgeGameTests {
    private NeoForgeGameTests() {}

    @GameTest(template = "skylands:empty")
    public static void placeBeanBlock(GameTestHelper helper) {
        SkylandsTests.placeBeanBlock(helper);
    }

    @GameTest(template = "skylands:empty")
    public static void placeCloudBlock(GameTestHelper helper) {
        SkylandsTests.placeCloudBlock(helper);
    }

    @GameTest(template = "skylands:empty")
    public static void beanstalkCenterPropertyRoundTrips(GameTestHelper helper) {
        SkylandsTests.beanstalkCenterPropertyRoundTrips(helper);
    }
}

package skylands.neoforge.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import skylands.gametest.SkylandsTests;

// NeoForge's GameTestRegistry.turnMethodIntoTestFunction builds the
// structure name as:  namespace + ":" + (prefix ? className + "." : "") + template
// where `namespace` comes from @GameTestHolder and `prefix` is controlled by
// @PrefixGameTestTemplate. The namespace is ALWAYS prefixed — even with
// PrefixGameTestTemplate(false). So `template` must be bare "empty" here;
// a "skylands:empty" template would produce the invalid
// "skylands:skylands:empty" structure lookup.
//
// Fabric's FabricGameTestModInitializer takes `template` verbatim when set,
// which is why FabricGameTests uses "skylands:empty" directly.
@GameTestHolder("skylands")
@PrefixGameTestTemplate(value = false)
public final class NeoForgeGameTests {
    private NeoForgeGameTests() {}

    @GameTest(template = "empty")
    public static void placeBeanBlock(GameTestHelper helper) {
        SkylandsTests.placeBeanBlock(helper);
    }

    @GameTest(template = "empty")
    public static void placeCloudBlock(GameTestHelper helper) {
        SkylandsTests.placeCloudBlock(helper);
    }

    @GameTest(template = "empty")
    public static void beanstalkCenterPropertyRoundTrips(GameTestHelper helper) {
        SkylandsTests.beanstalkCenterPropertyRoundTrips(helper);
    }
}

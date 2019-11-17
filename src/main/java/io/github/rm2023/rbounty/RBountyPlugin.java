package io.github.rm2023.rbounty;

import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameRegistryEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.util.generator.dummy.DummyObjectProvider;
import org.spongepowered.api.util.TypeTokens;

import com.google.inject.Inject;

@Plugin(id = "rbounty", name = "RBounty", version = "1.0.0", description = "A sponge plugin to place bounties on players")
public class RBountyPlugin {
	public static Key<Value<Integer>> BOUNTY = DummyObjectProvider.createExtendedFor(Key.class, "BOUNTY");
    
	@Inject
    private PluginContainer container;
	
	@Listener
	public void onInit(GameInitializationEvent event) {
	  DataRegistration.builder()
	      .dataClass(BountyData.class)
	      .immutableClass(ImmBountyData.class)
	      .builder(new BountyDataBuilder())
	      .manipulatorId("rbounty:bounty")
	      .dataName("Bounty")
	      .buildAndRegister(container);
	}
	
    @Listener
    public void onKeyRegistration(GameRegistryEvent.Register<Key<?>> event) {
        BOUNTY = Key.builder()
        		.type(TypeTokens.INTEGER_VALUE_TOKEN)
                .id("rbounty:bounty")
                .name("Bounty")
                .query(DataQuery.of("Bounty"))
                .build();
    }
}
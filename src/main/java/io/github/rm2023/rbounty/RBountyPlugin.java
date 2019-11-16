package io.github.rm2023.rbounty;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataManager;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.mutable.MapValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameRegistryEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.util.generator.dummy.DummyObjectProvider;

import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;

import io.github.rm2023.rbounty.data.Bounty;
import io.github.rm2023.rbounty.data.impl.BountyBuilder;

@Plugin(id = "rbounty", name = "RBounty", version = "1.0.0", description = "A sponge plugin to place bounties on players")
public class RBountyPlugin {
	public static Key<Value<Bounty>> BOUNTY = DummyObjectProvider.createExtendedFor(Key.class, "BOUNTY");

	@Inject
    private PluginContainer container;
	private DataRegistration<BountyData, ImmutableBountyData> BOUNTY_DATA_REGISTRATION;
	
    public void onKeyRegistration(GameRegistryEvent.Register<Key<?>> event) {
        BOUNTY = Key.builder()
                .type(new TypeToken<Value<Bounty>>() {})
                .id("rbounty:bounty")
                .name("Bounty")
                .query(DataQuery.of("Bounty"))
                .build();
    }
    
    @Listener
    public void onDataRegistration(GameRegistryEvent.Register<DataRegistration<?, ?>> event) {
        final DataManager dataManager = Sponge.getDataManager();
        dataManager.registerBuilder(Bounty.class, new BountyBuilder());
        dataManager.registerContentUpdater(BountyData.class, new BountyDataBuilder.BountyUpdater());

        this.BOUNTY_DATA_REGISTRATION = DataRegistration.builder()
                .dataClass(BountyData.class)
                .immutableClass(ImmutableBountyData.class)
                .dataImplementation(BountDataImpl.class)
                .immutableImplementation(ImmutableBountyDataImpl.class)
                .dataName("Bounty Data")
                .manipulatorId("rbounty:bounty")
                .buildAndRegister(this.container);
    }
}
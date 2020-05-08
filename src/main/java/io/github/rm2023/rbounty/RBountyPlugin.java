/*  RBounty: A plugin allowing the placing and claiming of player bounties.
 *   Copyright (C) 2019 rm2023
 *
 *  This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.rm2023.rbounty;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import io.github.rm2023.rbounty.commands.*;
import io.github.rm2023.rbounty.config.GeneralConfig;
import io.github.rm2023.rbounty.data.BountyData;
import io.github.rm2023.rbounty.data.BountyDataBuilder;
import io.github.rm2023.rbounty.data.ImmBountyData;
import io.github.rm2023.rbounty.listener.EntityDeath;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameRegistryEvent;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.PermissionDescription.Builder;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.TypeTokens;
import org.spongepowered.api.util.generator.dummy.DummyObjectProvider;

import java.io.IOException;
import java.util.Optional;

@Plugin(id = "rbounty",
		name = "RBounty",
		version = "1.0.1",
		description = "A plugin allowing the placing and claiming of player bounties.")
public class RBountyPlugin
{
	private static RBountyPlugin instance;

	public static RBountyPlugin getInstance()
	{
		return instance;
	}

	public static Key<Value<Integer>> BOUNTY = DummyObjectProvider.createExtendedFor(Key.class, "BOUNTY");
    public RBountyData data = null;

    @Inject
    public PluginContainer container;

    @Inject
    private Logger logger;

	public EconomyService economyService;
    public UserStorageService userStorageService;

    @Inject
    @ConfigDir(sharedRoot = false)
    private ConfigurationLoader<CommentedConfigurationNode> configLoader;
    private CommentedConfigurationNode configNode;
    private GeneralConfig config;

    @Listener
    public void onInit(GameInitializationEvent event)
	{
		instance = this;
		DataRegistration.builder()
				.dataClass(BountyData.class)
				.immutableClass(ImmBountyData.class)
				.builder(new BountyDataBuilder())
				.manipulatorId("rbounty:bounty")
				.dataName("Bounty")
				.buildAndRegister(container);

		try {
			loadConfig();
		}
		catch (IOException | ObjectMappingException e)
		{
			e.printStackTrace();
		}
		Sponge.getEventManager().registerListeners(this, new EntityDeath(this));
		Sponge.getCommandManager().register(container, bountyMain, "bounty");
    }
    
    @Listener
    public void onReload(GameReloadEvent event)
	{
		try
		{
			loadConfig();
		}
		catch (IOException | ObjectMappingException e)
		{
			e.printStackTrace();
		}
	}

    @Listener
    public void onRegistration(GameRegistryEvent.Register<Key<?>> event)
	{
		BOUNTY = Key.builder()
				.type(TypeTokens.INTEGER_VALUE_TOKEN)
				.id("bounty")
				.name("Bounty")
				.query(DataQuery.of("Bounty"))
				.build();
		event.register(BOUNTY);
    }

    @Listener
    public void onServerStarted(GameStartedServerEvent event)
	{
		userStorageService = Sponge.getServiceManager().provideUnchecked(UserStorageService.class);
		Optional<EconomyService> economyOpt = Sponge.getServiceManager().provide(EconomyService.class);
		if (!economyOpt.isPresent())
		{
		    logger.error("RBounty REQUIRES a plugin with an economy API in order to function.");
		    Sponge.getEventManager().unregisterPluginListeners(this);
		    Sponge.getCommandManager().getOwnedBy(this).forEach(Sponge.getCommandManager()::removeMapping);
		    logger.info("RBounty is now disabled.");
		    return;
		}
		economyService = economyOpt.get();
		PermissionService permissionService = Sponge.getServiceManager().provide(PermissionService.class).orElse(null);
		if (permissionService != null)
		{
		    Builder adminBuilder = permissionService.newDescriptionBuilder(container);
		    adminBuilder.id("rbounty.command.admin")
					.description(Text.of("Allows the user to set bounties regardless of the bounty's current amount or the player's balance."))
			    	.assign(PermissionDescription.ROLE_ADMIN, true)
					.register();

		    Builder userBuilder = permissionService.newDescriptionBuilder(container);
		    userBuilder.id("rbounty.command.user")
					.description(Text.of("Allows the user to view, add to, and claim bounties."))
					.assign(PermissionDescription.ROLE_USER, true)
					.register();
		}
			data = new RBountyData(logger);
			logger.info("RBounty loaded");
    }

	private void loadConfig() throws IOException, ObjectMappingException
	{
		//Config
		this.configNode = this.configLoader.load();
		@SuppressWarnings("UnstableApiUsage") TypeToken<GeneralConfig> type = TypeToken.of(GeneralConfig.class);
		this.config = configNode.getValue(type, new GeneralConfig());
		configNode.setValue(type, this.config);
		this.configLoader.save(configNode);
		//End config
	}

    private CommandSpec bountySet = CommandSpec.builder()
			.description(Text.of("Sets a player's bounty"))
	    	.permission("rbounty.command.admin")
	    	.arguments(GenericArguments.onlyOne(GenericArguments.user(Text.of("user"))),
		    	GenericArguments.onlyOne(GenericArguments.integer(Text.of("bounty"))))
	    	.executor(new SetBounty(this))
			.build();

    private CommandSpec bountyView = CommandSpec.builder()
			.description(Text.of("Get a player's current bounty"))
	    	.permission("rbounty.command.user")
	    	.arguments(GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.user(Text.of("user")))))
	    	.executor(new ViewBounty(this))
			.build();

    private CommandSpec bountyAdd = CommandSpec.builder()
			.description(Text.of("Add to a player's bounty"))
	    	.permission("rbounty.command.user")
	    	.arguments(GenericArguments.onlyOne(GenericArguments.user(Text.of("user"))),
		    	GenericArguments.onlyOne(GenericArguments.integer(Text.of("bounty"))))
	    	.executor(new AddBounty(this))
			.build();


    private CommandSpec bountyTop = CommandSpec.builder()
			.description(Text.of("Shows the bounty leaderboards"))
	    	.permission("rbounty.command.user")
	    	.arguments(GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.integer(Text.of("page")))))
	    	.executor(new TopBounty(this))
			.build();

    private CommandSpec bountyTopOnline = CommandSpec.builder()
	    	.description(Text.of("Shows the bounty leaderboards for online players."))
	    	.permission("rbounty.command.user")
	    	.arguments(GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.integer(Text.of("page")))))
	    	.executor(new TopOnlineBounty(this)).build();

    private CommandSpec bountyMain = CommandSpec.builder()
			.description(Text.of("Master command for bounty"))
	    	.permission("rbounty.command.user")
			.child(bountySet, "set")
			.child(bountyView, "view")
	    	.child(bountyAdd, "add")
			.child(bountyTop, "top", "leaderboard")
	    	.child(bountyTopOnline, "topOnline", "leaderboardOnline")
			.build();
}
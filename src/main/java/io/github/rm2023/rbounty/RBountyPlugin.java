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

import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.game.GameRegistryEvent;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.PermissionDescription.Builder;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.util.generator.dummy.DummyObjectProvider;
import org.spongepowered.api.util.TypeTokens;

import com.google.inject.Inject;

import io.github.rm2023.rbounty.data.BountyData;
import io.github.rm2023.rbounty.data.BountyDataBuilder;
import io.github.rm2023.rbounty.data.ImmBountyData;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.Map.Entry;

import org.slf4j.Logger;

@Plugin(id = "rbounty", name = "RBounty", version = "1.0.0", description = "A plugin allowing the placing and claiming of player bounties.")
public class RBountyPlugin {
    public static Key<Value<Integer>> BOUNTY = DummyObjectProvider.createExtendedFor(Key.class, "BOUNTY");
    RBountyData data = null;

    @Inject
    private Game game;

    @Inject
    private PluginContainer container;

    @Inject
    private Logger logger;

    private PermissionService permissionService;
    private EconomyService economyService;
    private UserStorageService userStorageService;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDir;

    private Path configFile;

    private ConfigurationLoader<CommentedConfigurationNode> configLoader;
    private CommentedConfigurationNode configNode;
    public boolean broadcasts = true;

    // Creates config file
    public void setup() {
	if (!Files.exists(configFile)) {
	    try {
		Files.createFile(configFile);
		configNode = configLoader.load();
		CommentedConfigurationNode broadcastNode = configNode.getNode("allowBroadcasts");
		broadcastNode.setComment("Should the server broadcast bounty changes/claims?");
		broadcastNode.setValue(true);
		configLoader.save(configNode);
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	} else {
	    load();
	}
    }

    // Loads config file if it already exists
    public void load() {
	try {
	    configNode = configLoader.load();
	    broadcasts = configNode.getNode("allowBroadcasts").getBoolean();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    @Listener
    public void onInit(GameInitializationEvent event) {
	DataRegistration.builder().dataClass(BountyData.class).immutableClass(ImmBountyData.class)
		.builder(new BountyDataBuilder()).manipulatorId("rbounty:bounty").dataName("Bounty")
		.buildAndRegister(container);

	// Creates config directory, your could also create a method for this.
	if (!Files.exists(configDir)) {
	    try {
		Files.createDirectories(configDir);
	    } catch (IOException io) {
		io.printStackTrace();
	    }
	}
	configFile = configDir.resolve("rbounty.conf");
	configLoader = HoconConfigurationLoader.builder().setPath(configFile).build();
	setup();

	Sponge.getCommandManager().register(container, bountyMain, "bounty");
    }
    
    @Listener
    public void onReload(GameReloadEvent event) {
	configFile = configDir.resolve("rbounty.conf");
	configLoader = HoconConfigurationLoader.builder().setPath(configFile).build();
	setup();
    }

    @Listener
    public void onRegistration(GameRegistryEvent.Register<Key<?>> event) {
	BOUNTY = Key.builder().type(TypeTokens.INTEGER_VALUE_TOKEN).id("bounty").name("Bounty")
		.query(DataQuery.of("Bounty")).build();
	event.register(BOUNTY);
    }

    @Listener
    public void onServerStarted(GameStartedServerEvent event) {
	userStorageService = Sponge.getServiceManager().provide(UserStorageService.class).get();
	Optional<EconomyService> economyOpt = Sponge.getServiceManager().provide(EconomyService.class);
	if (!economyOpt.isPresent()) {
	    logger.error("RBounty REQUIRES a plugin with an economy API in order to function.");
	    game.getEventManager().unregisterPluginListeners(this);
	    game.getCommandManager().getOwnedBy(this).forEach(game.getCommandManager()::removeMapping);
	    logger.info("RBounty is now disabled.");
	    return;
	}
	economyService = economyOpt.get();
	permissionService = Sponge.getServiceManager().provide(PermissionService.class).orElse(null);
	if (permissionService != null) {
	    Builder adminBuilder = permissionService.newDescriptionBuilder(container);
	    adminBuilder.id("rbounty.command.admin").description(Text.of(
		    "Allows the user to set bounties regardless of the bounty's current amount or the player's balance."))
		    .assign(PermissionDescription.ROLE_ADMIN, true).register();
	    Builder userBuilder = permissionService.newDescriptionBuilder(container);
	    userBuilder.id("rbounty.command.user")
		    .description(Text.of("Allows the user to view, add to, and claim bounties."))
		    .assign(PermissionDescription.ROLE_USER, true).register();
	}
	data = new RBountyData(logger);
	logger.info("RBounty loaded");
    }

    private void broadcast(String msg, CommandSource src) {
	if (broadcasts) {
	    Sponge.getServer().getBroadcastChannel().send(Text.builder(msg).color(TextColors.BLUE).style(TextStyles.BOLD).build());
	} else if (src != null) {
	    src.sendMessage(Text.builder(msg).color(TextColors.BLUE).build());
	}
    }

    @Listener
    public void onEntityDeath(DestructEntityEvent.Death event) {
	// If an entity is killed, that entity happens to be a player w/ a bounty, and
	// the entitydeath cause has another player in it with a valid currency account,
	// award the killer the bounty.
	if (!event.isCancelled() && event.getTargetEntity() instanceof User) {
	    User killed = (User) event.getTargetEntity();
	    User killer = null;
	    for (Object object : event.getCause().all()) {
		if (object instanceof User && !((User) object).getName().equals(killed.getName())) {
		    killer = (User) object;
		    break;
		}
	    }
	    if (killer != null && !event.getContext().containsKey(EventContextKeys.FAKE_PLAYER)
		    && data.getBounty(killed) > 0) {
		UniqueAccount killerAccount = economyService.getOrCreateAccount(killer.getUniqueId()).orElse(null);
		if (killerAccount != null) {
		    killerAccount.deposit(economyService.getDefaultCurrency(),
			    BigDecimal.valueOf(data.getBounty(killed)),
			    Cause.builder().append(killed).append(killer).append(container)
				    .build(EventContext.builder().add(EventContextKeys.PLUGIN, container).build()));
		    data.setBounty(killed.getUniqueId(), 0);
		    broadcast(killer.getName() + " has claimed " + killed.getName() + "'s bounty!", null);
		}
	    }
	}
    }

    CommandSpec bountySet = CommandSpec.builder().description(Text.of("Sets a player's bounty"))
	    .permission("rbounty.command.admin")
	    .arguments(GenericArguments.onlyOne(GenericArguments.user(Text.of("user"))),
		    GenericArguments.onlyOne(GenericArguments.integer(Text.of("bounty"))))
	    .executor(new SetBounty()).build();

    public class SetBounty implements CommandExecutor {
	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
	    User user = args.<User>getOne("user").get();
	    int bounty = args.<Integer>getOne("bounty").get();
	    if (bounty < 0) {
		src.sendMessage(Text.builder("Bounty must be a non-negative integer").color(TextColors.BLUE).build());
		return CommandResult.empty();
	    }

	    if (data.setBounty(user, bounty)) {
		broadcast(user.getName() + "'s bounty has been set to "
			+ economyService.getDefaultCurrency().format(BigDecimal.valueOf(data.getBounty(user))).toPlain()
			+ "!", src);
		return CommandResult.success();
	    }
	    src.sendMessage(Text.builder("An error occured. Check console log for more information")
		    .color(TextColors.BLUE).build());
	    return CommandResult.empty();
	}
    }

    CommandSpec bountyView = CommandSpec.builder().description(Text.of("Get a player's current bounty"))
	    .permission("rbounty.command.user")
	    .arguments(GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.user(Text.of("user")))))
	    .executor(new ViewBounty()).build();

    public class ViewBounty implements CommandExecutor {
	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
	    User user = args.<User>getOne("user").orElse(null);
	    if (user == null) {
		if (src instanceof Player) {
		    user = (User) src;
		} else {
		    src.sendMessage(Text.builder("This command must target a player").color(TextColors.BLUE).build());
		    return CommandResult.empty();
		}
	    }
	    int bounty = data.getBounty(user);
	    if (bounty > 0) {
		src.sendMessage(Text.builder(user.getName() + "'s bounty is " + bounty).color(TextColors.BLUE).build());
		return CommandResult.success();
	    }
	    src.sendMessage(Text.builder(user.getName() + " doesn't have a bounty").color(TextColors.BLUE).build());
	    return CommandResult.success();
	}
    }

    CommandSpec bountyAdd = CommandSpec.builder().description(Text.of("Add to a player's bounty"))
	    .permission("rbounty.command.user")
	    .arguments(GenericArguments.onlyOne(GenericArguments.user(Text.of("user"))),
		    GenericArguments.onlyOne(GenericArguments.integer(Text.of("bounty"))))
	    .executor(new AddBounty()).build();

    public class AddBounty implements CommandExecutor {
	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
	    User user = args.<User>getOne("user").get();
	    int bounty = args.<Integer>getOne("bounty").get();
	    if (!(src instanceof Player)) {
		src.sendMessage(Text.builder("Only a player may run this command.").color(TextColors.BLUE).build());
		return CommandResult.empty();
	    }
	    if (bounty <= 0) {
		src.sendMessage(Text.builder("Bounty must be a positive integer.").color(TextColors.BLUE).build());
		return CommandResult.empty();
	    }
	    UniqueAccount account = economyService.getOrCreateAccount(((Player) src).getUniqueId()).orElse(null);
	    if (account == null) {
		src.sendMessage(Text.builder("An error occured. Check economy plugin for more information.")
			.color(TextColors.BLUE).build());
		return CommandResult.empty();
	    }
	    if (account.hasBalance(economyService.getDefaultCurrency()) && account
		    .getBalance(economyService.getDefaultCurrency()).compareTo(BigDecimal.valueOf(bounty)) < 0) {
		src.sendMessage(Text.builder("You don't have enough money to bounty " + user.getName() + " for "
			+ economyService.getDefaultCurrency().format(BigDecimal.valueOf(bounty)).toPlain() + ".")
			.color(TextColors.BLUE).build());
		return CommandResult.empty();
	    }

	    if (data.setBounty(user, bounty + data.getBounty(user))) {
		account.withdraw(economyService.getDefaultCurrency(), BigDecimal.valueOf(bounty),
			Cause.builder().append(src).append(container)
				.build(EventContext.builder().add(EventContextKeys.PLUGIN, container).build()));
		if (data.getBounty(user) == bounty) {
		    broadcast("A bounty of "
			    + economyService.getDefaultCurrency().format(BigDecimal.valueOf(bounty)).toPlain()
			    + " has been set on " + user.getName() + "!", src);
		} else {
		    broadcast(user.getName() + "'s bounty has been increased by "
			    + economyService.getDefaultCurrency().format(BigDecimal.valueOf(bounty)).toPlain()
			    + " and is now at " + economyService.getDefaultCurrency()
				    .format(BigDecimal.valueOf(data.getBounty(user))).toPlain()
			    + "!", src);
		}
		return CommandResult.success();
	    }
	    src.sendMessage(Text.builder("An error occured. Check console log for more information.")
		    .color(TextColors.BLUE).build());
	    return CommandResult.empty();
	}
    }

    CommandSpec bountyTop = CommandSpec.builder().description(Text.of("Shows the bounty leaderboards"))
	    .permission("rbounty.command.user")
	    .arguments(GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.integer(Text.of("page")))))
	    .executor(new TopBounty()).build();

    public class TopBounty implements CommandExecutor {
	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
	    int page = args.<Integer>getOne("page").orElse(1);
	    src.sendMessage(parseLeaderboard(page * 10 - 10, page * 10, false));
	    return CommandResult.success();
	}
    }

    CommandSpec bountyTopOnline = CommandSpec.builder()
	    .description(Text.of("Shows the bounty leaderboards for online players."))
	    .permission("rbounty.command.user")
	    .arguments(GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.integer(Text.of("page")))))
	    .executor(new TopOnlineBounty()).build();

    public class TopOnlineBounty implements CommandExecutor {
	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
	    int page = args.<Integer>getOne("page").orElse(1);
	    src.sendMessage(parseLeaderboard(page * 10 - 10, page * 10, true));
	    return CommandResult.success();
	}
    }

    CommandSpec bountyMain = CommandSpec.builder().description(Text.of("Master command for bounty"))
	    .permission("rbounty.command.user").child(bountySet, "set").child(bountyView, "view")
	    .child(bountyAdd, "add").child(bountyTop, "top", "leaderboard")
	    .child(bountyTopOnline, "topOnline", "leaderboardOnline").build();

    /**
     * Parses the bounty leaderboard and returns a chat friendly representation of
     * it.
     * 
     * @param start  the first position to show on the leaderboard.
     * @param end    the last position to show on the leaderboard
     * @param online Whether to only look for online players
     * @return a textual representation of the leaderboard
     */
    public Text parseLeaderboard(int start, int end, boolean online) {
	Text fail = Text.builder("No bounties were found in that range!").color(TextColors.BLUE).build();
	ArrayList<Entry<UUID, Integer>> lb = data.getLeaderboard();
	if (lb.size() <= start || start < 0 || start >= end || lb.get(start).getValue() == 0) {
	    return fail;
	}
	Text.Builder builder = Text.builder();
	int val;
	User user;
	int skip = 0;

	if (online) {
	    int i = -1;
	    while (start > -1) {
		start -= 1;
		i += 1;
		user = userStorageService.get(lb.get(i).getKey()).get();
		val = lb.get(i).getValue();
		while (!user.isOnline()) {
		    i += 1;
		    user = userStorageService.get(lb.get(i).getKey()).get();
		    val = lb.get(i).getValue();
		    if (i >= lb.size() || val == 0) {
			return fail;
		    }
		}
	    }
	    skip = i;
	    start = i;
	}

	builder.append(Text.of("\n---------------------LEADERBOARD---------------------\n"));
	for (int i = start; i < end && i < lb.size() && lb.get(i).getValue() > 0; i++) {
	    val = lb.get(i).getValue();
	    user = userStorageService.get(lb.get(i).getKey()).get();
	    if (online && !user.isOnline()) {
		skip += 1;
		end += 1;
		continue;
	    }
	    builder.append(Text.of((i + 1 - skip) + ". " + user.getName() + ", "
		    + (economyService.getDefaultCurrency().format(BigDecimal.valueOf(val)).toPlain()) + "\n"));
	}
	builder.append(Text.of("-----------------------------------------------------"));
	builder.color(TextColors.BLUE);
	return builder.build();
    }
}
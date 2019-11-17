package io.github.rm2023.rbounty;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameRegistryEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.generator.dummy.DummyObjectProvider;
import org.spongepowered.api.util.TypeTokens;

import com.google.inject.Inject;

import io.github.rm2023.rbounty.data.BountyData;
import io.github.rm2023.rbounty.data.BountyDataBuilder;
import io.github.rm2023.rbounty.data.ImmBountyData;

import org.slf4j.Logger;

@Plugin(id = "rbounty", name = "RBounty", version = "1.0.0", description = "A sponge plugin to place bounties on players")
public class RBountyPlugin {
	public static Key<Value<Integer>> BOUNTY = DummyObjectProvider.createExtendedFor(Key.class, "BOUNTY");
	RBountyData data = null;
    
	@Inject
    private PluginContainer container;
	
	@Inject
	private Logger logger;
	
	@Listener
	public void onInit(GameInitializationEvent event) {
	  DataRegistration.builder()
	      .dataClass(BountyData.class)
	      .immutableClass(ImmBountyData.class)
	      .builder(new BountyDataBuilder())
	      .manipulatorId("rbounty:bounty")
	      .dataName("Bounty")
	      .buildAndRegister(container);
	  
	  Sponge.getCommandManager().register(container, bountyMain, "bounty");
	}
	
    @Listener
    public void onRegistration(GameRegistryEvent.Register<Key<?>> event) {
        BOUNTY = Key.builder()
        		.type(TypeTokens.INTEGER_VALUE_TOKEN)
                .id("bounty")
                .name("Bounty")
                .query(DataQuery.of("Bounty"))
                .build();
        event.register(BOUNTY);
    }
    
	@Listener
	public void onServerStarted(GameStartedServerEvent  event) {
		data = new RBountyData(logger);
		logger.info("RBounty loaded");
	}
	
    CommandSpec bountySet = CommandSpec.builder()
    	    .description(Text.of("Sets a player's bounty"))
    	    .permission("rbounty.command.admin")
            .arguments(
                    GenericArguments.onlyOne(GenericArguments.user(Text.of("user"))),
                    GenericArguments.onlyOne(GenericArguments.integer(Text.of("bounty"))))
    	    .executor(new SetBounty())
    	    .build();
    
    CommandSpec bountyGet = CommandSpec.builder()
    	    .description(Text.of("Get a player's current bounty"))
    	    .permission("rbounty.command.admin")
            .arguments(
                    GenericArguments.onlyOne(GenericArguments.user(Text.of("user"))))
    	    .executor(new GetBounty())
    	    .build();
	
    CommandSpec bountyMain = CommandSpec.builder()
    	    .description(Text.of("Master command for bounty"))
    	    .permission("rbounty.command.user")
    	    .child(bountySet, "set")
    	    .child(bountyGet, "get")
    	    .build();
   
    
    public class SetBounty implements CommandExecutor {
        @Override
        public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            if(data.setBounty(args.<User>getOne("user").get(), args.<Integer>getOne("bounty").get())) {
            	src.sendMessage(Text.of(args.<User>getOne("user").get().getName() + "'s bounty is now " + args.<Integer>getOne("bounty").get()));
            	return CommandResult.success();
            }
            return CommandResult.empty();
        }
    }
    
    public class GetBounty implements CommandExecutor {
        @Override
        public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            User user = args.<User>getOne("user").get();
            int bounty = data.getBounty(user);
            if(bounty > 0) {
            	src.sendMessage(Text.of(user.getName() + "'s bounty is " + bounty));
            	return CommandResult.success();
            }
            src.sendMessage(Text.of(user.getName() + " doesn't have a bounty."));
            return CommandResult.success();
        }
    }
}
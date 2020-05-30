package io.github.rm2023.rbounty.commands;

import io.github.rm2023.rbounty.RBountyPlugin;
import io.github.rm2023.rbounty.Utility.Helper;
import io.github.rm2023.rbounty.config.GeneralConfig;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.math.BigDecimal;

public class AddBounty implements CommandExecutor
{
    private RBountyPlugin instance;

    public AddBounty(RBountyPlugin rBountyPlugin)
    {
        this.instance = rBountyPlugin;
    }

    @Override
    @NonNull
    public CommandResult execute(@NonNull CommandSource src, CommandContext args) throws CommandException
    {
        User user = args.<User>getOne("user").get();
        int bounty = args.<Integer>getOne("bounty").get();
        if (!(src instanceof Player))
        {
            throw new CommandException(Text.builder("Only a player may run this command.").color(TextColors.RED).build());
        }
        if (bounty <= 0)
        {
            throw new CommandException(Text.builder("Bounty must be a positive integer.").color(TextColors.RED).build());
        }
        UniqueAccount account = instance.getEconomyService().getOrCreateAccount(((Player) src).getUniqueId()).orElse(null);
        if (account == null)
        {
            throw new CommandException(Text.builder("An error occured. Check economy plugin for more information.").color(TextColors.RED).build());
        }
        if (account.getBalance(instance.getEconomyService().getDefaultCurrency()).compareTo(BigDecimal.valueOf(bounty)) < 0)
        {
            src.sendMessage(Text.builder("You don't have enough money to bounty " + user.getName() + " for "
                    + instance.getEconomyService().getDefaultCurrency().format(BigDecimal.valueOf(bounty)).toPlain() + ".")
                    .color(TextColors.BLUE)
                    .build());
            return CommandResult.empty();
        }

        if (instance.data.setBounty(user, bounty + instance.data.getBounty(user)))
        {
            account.withdraw(instance.getEconomyService().getDefaultCurrency(),
                    BigDecimal.valueOf(bounty),
                    Cause.builder()
                            .append(src)
                            .append(instance.getContainer())
                            .build(EventContext.builder()
                                    .add(EventContextKeys.PLUGIN, instance.getContainer())
                                    .build()));

            if (instance.data.getBounty(user) == bounty)
            {
                Helper.broadcast(instance.getConfig().bountySetMessage
                        .replace("%bounty%", instance.getEconomyService().getDefaultCurrency().format(BigDecimal.valueOf(instance.data.getBounty(user))).toPlain())
                        .replace("%player%", user.getName()), src);
            }
            else {
                Helper.broadcast(instance.getConfig().bountyIncreasedMessage
                        .replace("%difference%", Integer.toString(bounty))
                        .replace("%bounty%", instance.getEconomyService().getDefaultCurrency().format(BigDecimal.valueOf(instance.data.getBounty(user))).toPlain())
                        .replace("%player%", user.getName()), src);
            }
            return CommandResult.success();
        }
        else{
            throw new CommandException(Text.of(TextColors.RED, "Error while trying to add bounty! Check console for details"));
        }
    }
}
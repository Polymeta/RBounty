package io.github.rm2023.rbounty.commands;

import io.github.rm2023.rbounty.RBountyPlugin;
import io.github.rm2023.rbounty.Utility.Helper;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.math.BigDecimal;

public class SetBounty implements CommandExecutor
{
    private final RBountyPlugin instance;

    public SetBounty(RBountyPlugin rBountyPlugin)
    {
        this.instance = rBountyPlugin;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException
    {
        User user = args.<User>getOne("user").orElseThrow(() -> new CommandException(Text.of("Failed to get user!")));
        int bounty = args.<Integer>getOne("bounty").orElseThrow(() -> new CommandException(Text.of("You need to supply an amount!")));
        if (bounty < 0)
        {
            throw new CommandException(Text.of(TextColors.RED, "Bounty must be > 0!"));
        }

        if (instance.data.setBounty(user, bounty))
        {
            Helper.broadcast(instance.getConfig().bountySetMessage
                    .replace("%bounty%",
                            instance.getEconomyService().getDefaultCurrency().format(BigDecimal.valueOf(instance.data.getBounty(
                                    user))).toPlain())
                    .replace("%player%", user.getName()), src);
            return CommandResult.success();
        }
        else
        {
            throw new CommandException(Text.of(TextColors.RED,
                    "Error while trying to set bounty! Check console for details"));
        }
    }
}
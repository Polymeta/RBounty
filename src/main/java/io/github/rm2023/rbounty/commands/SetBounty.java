package io.github.rm2023.rbounty.commands;

import io.github.rm2023.rbounty.RBountyPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
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
    private RBountyPlugin instance;

    public SetBounty(RBountyPlugin rBountyPlugin)
    {
        this.instance = rBountyPlugin;
    }

    @Override
    @NonNull
    public CommandResult execute(@NonNull CommandSource src, CommandContext args) throws CommandException
    {
        User user = args.<User>getOne("user").get();
        int bounty = args.<Integer>getOne("bounty").get();
        if (bounty < 0)
        {
            throw new CommandException(Text.of(TextColors.RED, "Bounty must be > 0!"));
        }

        if (instance.data.setBounty(user, bounty))
        {
            instance.broadcast(user.getName() + "'s bounty has been set to "
                    + instance.economyService.getDefaultCurrency().format(BigDecimal.valueOf(instance.data.getBounty(user))).toPlain()
                    + "!", src);
            return CommandResult.success();
        }
        else{
            throw new CommandException(Text.of(TextColors.RED, "Error while trying to set bounty! Check console for details"));
        }
    }
}
package io.github.rm2023.rbounty.commands;

import io.github.rm2023.rbounty.RBountyPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class ViewBounty implements CommandExecutor
{

    private RBountyPlugin instance;

    public ViewBounty(RBountyPlugin rBountyPlugin)
    {
        this.instance = rBountyPlugin;
    }

    @Override
    @NonNull
    public CommandResult execute(@NonNull CommandSource src, CommandContext args) throws CommandException
    {
        User user = args.<User>getOne("user").orElse(null);
        if (user == null)
        {
            if (src instanceof Player)
            {
                user = (User) src;
            }
            else {
                throw new CommandException(Text.builder("This command must target a player!").color(TextColors.RED).build());
            }
        }
        int bounty = instance.data.getBounty(user);
        if (bounty > 0)
        {
            src.sendMessage(Text.builder(user.getName() + "'s bounty is " + bounty).color(TextColors.BLUE).build());
            return CommandResult.success();
        }
        src.sendMessage(Text.builder(user.getName() + " doesn't have a bounty").color(TextColors.BLUE).build());
        return CommandResult.success();
    }
}
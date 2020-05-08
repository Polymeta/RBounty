package io.github.rm2023.rbounty.commands;

import io.github.rm2023.rbounty.RBountyPlugin;
import io.github.rm2023.rbounty.Utility.Helper;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

public class TopOnlineBounty implements CommandExecutor
{
    private RBountyPlugin instance;

    public TopOnlineBounty(RBountyPlugin rBountyPlugin)
    {
        this.instance = rBountyPlugin;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        int page = args.<Integer>getOne("page").orElse(1);
        src.sendMessage(Helper.parseLeaderboard(page * 10 - 10, page * 10, true));
        return CommandResult.success();
    }
}

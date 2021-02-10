package io.github.rm2023.rbounty.Utility;

import io.github.rm2023.rbounty.RBountyPlugin;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

public class Helper
{
    public static void broadcast(String msg, CommandSource src)
    {
        if (RBountyPlugin.getInstance().getConfig().doBroadcasts)
        {
            Sponge.getServer().getBroadcastChannel().send(TextSerializers.FORMATTING_CODE.deserialize(msg));
        }
        else if (src != null)
        {
            src.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(msg));
        }
    }

    /**
     * Parses the bounty leaderboard and returns a chat friendly representation of
     * it.
     *
     * @param start  the first position to show on the leaderboard.
     * @param end    the last position to show on the leaderboard
     * @param online Whether to only look for online players
     * @return a textual representation of the leaderboard
     */
    public static Text parseLeaderboard(int start, int end, boolean online)
    {
        Text fail = Text.builder("No bounties were found in that range!")
                .color(TextColors.BLUE)
                .build();

        ArrayList<Map.Entry<UUID, Integer>> lb = RBountyPlugin.getInstance().data.getLeaderboard();

        if (lb.size() <= start || start < 0 || start >= end || lb.get(start).getValue() == 0)
        {
            return fail;
        }

        Text.Builder builder = Text.builder();
        int val;
        User user;
        int skip = 0;

        if (online)
        {
            int i = -1;
            while (start > -1)
            {
                start -= 1;
                i += 1;
                user = RBountyPlugin.getInstance().getUserStorageService().get(lb.get(i).getKey()).get();
                val = lb.get(i).getValue();
                while (!user.isOnline())
                {
                    i += 1;
                    user = RBountyPlugin.getInstance().getUserStorageService().get(lb.get(i).getKey()).get();
                    val = lb.get(i).getValue();
                    if (i >= lb.size() || val == 0)
                    {
                        return fail;
                    }
                }
            }
            skip = i;
            start = i;
        }

        builder.append(Text.of("\n---------------------LEADERBOARD---------------------\n"));
        for (int i = start; i < end && i < lb.size() && lb.get(i).getValue() > 0; i++)
        {
            val = lb.get(i).getValue();
            user = RBountyPlugin.getInstance().getUserStorageService().get(lb.get(i).getKey()).get();
            if (online && !user.isOnline())
            {
                skip += 1;
                end += 1;
                continue;
            }
            builder.append(Text.of((i + 1 - skip) + ". " + user.getName() + ", "
                    + (RBountyPlugin.getInstance().getEconomyService().getDefaultCurrency().format(BigDecimal.valueOf(
                    val)).toPlain()) + "\n"));
        }
        builder.append(Text.of("-----------------------------------------------------"));
        builder.color(TextColors.BLUE);
        return builder.build();
    }
}

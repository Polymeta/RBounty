package io.github.rm2023.rbounty.listener;

import io.github.rm2023.rbounty.RBountyPlugin;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.RespawnPlayerEvent;
import org.spongepowered.api.service.economy.account.UniqueAccount;

import java.math.BigDecimal;

public class EntityDeath
{
    private RBountyPlugin instace;

    public EntityDeath(RBountyPlugin rBountyPlugin)
    {
        this.instace = rBountyPlugin;
    }

    @Listener
    public void onEntityDeath(DestructEntityEvent.Death event)
    {
        // If an entity is killed, that entity happens to be a player w/ a bounty, and
        // the entitydeath cause has another player in it with a valid currency account,
        // award the killer the bounty.
        if (event.getTargetEntity() instanceof User)
        {
            User killed = (User) event.getTargetEntity();
            User killer = null;
            for (Object object : event.getCause().all())
            {
                if (object instanceof User && !((User) object).getName().equals(killed.getName()))
                {
                    killer = (User) object;
                    break;
                }
            }
            if (killer != null
                    && !event.getContext().containsKey(EventContextKeys.FAKE_PLAYER)
                    && instace.data.getBounty(killed) > 0)
            {
                UniqueAccount killerAccount = instace.economyService.getOrCreateAccount(killer.getUniqueId()).orElse(null);
                if (killerAccount != null)
                {
                    killerAccount.deposit(instace.economyService.getDefaultCurrency(),
                            BigDecimal.valueOf(instace.data.getBounty(killed)),
                            Cause.builder()
                                    .append(killed)
                                    .append(killer)
                                    .append(instace.container)
                                    .build(EventContext.builder()
                                            .add(EventContextKeys.PLUGIN, instace.container)
                                            .build()));
                    instace.data.setBounty(killed, 0);
                    instace.broadcast(killer.getName() + " has claimed " + killed.getName() + "'s bounty!", null);
                }
            }
        }
    }

    @Listener
    public void onRespawn(RespawnPlayerEvent event)
    {
        event.getTargetEntity().offer(RBountyPlugin.BOUNTY, event.getOriginalPlayer().get(RBountyPlugin.BOUNTY).get());
    }
}

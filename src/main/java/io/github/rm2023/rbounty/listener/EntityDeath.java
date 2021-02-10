package io.github.rm2023.rbounty.listener;

import io.github.rm2023.rbounty.RBountyPlugin;
import io.github.rm2023.rbounty.Utility.Helper;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.RespawnPlayerEvent;
import org.spongepowered.api.service.economy.account.UniqueAccount;

import java.math.BigDecimal;
import java.util.Optional;

public class EntityDeath
{
    private final RBountyPlugin instace;

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
            Optional<EntityDamageSource> damageOpt = event.getCause().first(EntityDamageSource.class);
            if (damageOpt.isPresent())
            {
                EntityDamageSource damageDone = damageOpt.get();
                if (damageDone.getSource().getType().equals(EntityTypes.PLAYER))
                {
                    killer = (User) damageDone.getSource();
                }
            }

            if (killer == killed)
                return; // prevents getting bounty by killing oneself

            if (killer != null &&
                    !event.getContext().containsKey(EventContextKeys.FAKE_PLAYER) &&
                    instace.data.getBounty(killed) > 0)
            {
                UniqueAccount killerAccount = instace.getEconomyService().getOrCreateAccount(killer.getUniqueId()).orElse(
                        null);
                if (killerAccount != null)
                {
                    killerAccount.deposit(instace.getEconomyService().getDefaultCurrency(),
                            BigDecimal.valueOf(instace.data.getBounty(killed)),
                            Cause.builder()
                                    .append(killed)
                                    .append(killer)
                                    .append(instace.getContainer())
                                    .build(EventContext.builder()
                                            .add(EventContextKeys.PLUGIN, instace.getContainer())
                                            .build()));
                    Helper.broadcast(instace.getConfig().claimMessage
                            .replace("%killer%", killer.getName())
                            .replace("%victim%", killed.getName())
                            .replace("%bounty%",
                                    instace.getEconomyService().getDefaultCurrency().format(BigDecimal.valueOf(instace.data.getBounty(
                                            killed))).toPlain()), null);
                    instace.data.setBounty(killed, 0);
                }
            }
        }
    }

    @Listener
    public void onRespawn(RespawnPlayerEvent event)
    {
        event.getOriginalPlayer().get(RBountyPlugin.BOUNTY).ifPresent(data ->
                event.getTargetEntity().offer(RBountyPlugin.BOUNTY, data));
    }
}

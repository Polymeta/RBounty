package io.github.rm2023.rbounty;

import java.util.Collection;
import java.util.TreeMap;
import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.service.user.UserStorageService;

public class RBountyData {
	//Cache for RBountyData. All bounty gets use cache
	//All bounty sets write both to cache and playerdata
	//Cache is constructed from playerdata on initialization
	TreeMap<UUID, Integer> cache;
	
	public RBountyData() {
		resetCache();
	}
	
	protected void resetCache() {
		UserStorageService userStorage = Sponge.getServiceManager().provide(UserStorageService.class).get();
		Collection<GameProfile> userProfiles = userStorage.getAll();
		
		for(GameProfile userProfile : userProfiles) {
			User user = userStorage.get(userProfile).orElse(null);
			if(user == null)
			{
				continue;
			}
			Integer playerBounty = user.get(RBountyPlugin.BOUNTY).orElse(null);
			if(playerBounty == null)
			{
				continue;
			}
			cache.put(user.getUniqueId(), playerBounty);
		}
	}
	
	public int getBounty(User user) {
		return cache.get(user.getUniqueId());
	}
	
	public boolean setBounty(User user, int bounty) {
		if(user != null && user.offer(RBountyPlugin.BOUNTY, bounty).isSuccessful())
		{
			cache.put(user.getUniqueId(), bounty);
			return true;
		}
		return false;
	}
}
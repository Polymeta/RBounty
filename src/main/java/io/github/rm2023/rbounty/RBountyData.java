/*  RBounty: A plugin allowing the placing and claiming of player bounties.
 *   Copyright (C) 2019 rm2023
 *
 *  This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.rm2023.rbounty;

import java.util.Map;
import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.service.user.UserStorageService;

import io.github.rm2023.rbounty.data.BountyData;

import org.slf4j.Logger;

public class RBountyData {
    // Cache for RBountyData. All bounty gets use cache
    // All bounty sets write both to cache and playerdata
    // Cache is constructed from playerdata on initialization
	private HashMap<UUID, Integer> cache;

    private UserStorageService userStorage;

    private Logger logger;

    private ArrayList<Map.Entry<UUID, Integer>> leaderboard;

    private boolean validLeaderboard = false;

    public RBountyData(Logger logger)
	{
		userStorage = Sponge.getServiceManager().provide(UserStorageService.class).get();
		this.logger = logger;
		resetCache();
		resetLeaderboard();
    }

    /**
     * Sets up the cache for RBountyData by iterating through all users in
     * userStorage and storing their bounties in the cache.
     */
	private void resetCache()
	{
		Collection<GameProfile> userProfiles = userStorage.getAll();

		cache = new HashMap<>();
		for (GameProfile userProfile : userProfiles)
		{
		    User user = userStorage.get(userProfile).orElse(null);
		    if (user == null)
		    {
				continue;
		    }
		    Integer playerBounty = user.get(RBountyPlugin.BOUNTY).orElse(null);
		    if (playerBounty == null)
		    {
				if (user.offer(new BountyData(0)).isSuccessful())
				{
			    	cache.put(user.getUniqueId(), 0);
				}
				else {
			    	logger.error("Error while reading bounty for " + user.getName() + ".");
				}
				continue;
		    }
		    cache.put(user.getUniqueId(), playerBounty);
		}
    }

    public int getBounty(User user)
	{
		if (cache.containsKey(user.getUniqueId()))
		{
	    	return cache.get(user.getUniqueId());
		}
		return 0;
    }

    public boolean setBounty(User user, int bounty)
	{
		if (user == null)
		{
		    return false;
		}
		DataTransactionResult result;
		if (user.get(RBountyPlugin.BOUNTY).isPresent())
		{
		    result = user.offer(RBountyPlugin.BOUNTY, bounty);
		}
		else {
		    result = user.offer(new BountyData(bounty));
		}

		if (result.isSuccessful())
		{
		    cache.put(user.getUniqueId(), bounty);
		    validLeaderboard = false;
		    return true;
		}
		logger.error(result.toString());
		return false;
    }

    /**
     * Converts the cache into a list and sorts it in descending order and sets
     * leaderboard to that.
     */
    private void resetLeaderboard()
	{
		leaderboard = new ArrayList<>();
		leaderboard.addAll(cache.entrySet());
		leaderboard.sort((o1, o2) -> (o2.getValue()).compareTo(o1.getValue()));

		validLeaderboard = true;
    }

    public ArrayList<Entry<UUID, Integer>> getLeaderboard()
	{
		if (!validLeaderboard) {
	    	resetLeaderboard();
		}
		return leaderboard;
    }
}
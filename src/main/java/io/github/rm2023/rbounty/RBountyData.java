package io.github.rm2023.rbounty;
import java.util.TreeMap;
import java.util.UUID;

public class RBountyData {
	//Cache for RBountyData. All bounty getters use cache
	//All bounty setters write both to cache and playerdata
	//Cache is constructed from playerdata on initialization
	TreeMap<UUID, Integer> cache;
	
	public RBountyData()
	{
		
	}
}

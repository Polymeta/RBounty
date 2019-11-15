package io.github.rm2023.rbounty;
import java.util.TreeMap;
import java.util.UUID;

public class RBountyData {
	//Cache for RBountyData. All bounty gets use cache
	//All bounty sets write both to cache and playerdata
	//Cache is constructed from playerdata on initialization
	TreeMap<UUID, Integer> cache;
	
}

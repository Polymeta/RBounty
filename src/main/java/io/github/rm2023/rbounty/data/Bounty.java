package io.github.rm2023.rbounty.data;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataSerializable;
import org.spongepowered.api.data.Queries;

public class Bounty implements DataSerializable {

	public static final DataQuery VALUE_QUERY = DataQuery.of("Value");
	
	private int value;
	
	public Bounty(int value)
	{
		this.value = value;
	}
	
	@Override
	public int getContentVersion() {
		return BountyBuilder.CONTENT_VERSION;
	}
	
	public int getValue() {
		return value;
	}

	@Override
	public DataContainer toContainer() {
		return DataContainer.createNew()
				.set(VALUE_QUERY, value)
				.set(Queries.CONTENT_VERSION, BountyBuilder.CONTENT_VERSION);
	}
}

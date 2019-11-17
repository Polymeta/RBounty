package io.github.rm2023.rbounty.data;

import java.util.Optional;

import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;

public class BountyDataBuilder implements DataManipulatorBuilder<BountyData, ImmBountyData> {
	
	@Override
	public Optional<BountyData> build(DataView container) throws InvalidDataException {
		return new BountyData().from(container.copy());
	}

	@Override
	public BountyData create() {
		return new BountyData();
	}

	@Override
	public Optional<BountyData> createFrom(DataHolder dataHolder) {
		return new BountyData().from(dataHolder.copy().toContainer());
	}
}

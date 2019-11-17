package io.github.rm2023.rbounty.data;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableSingleData;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;

import io.github.rm2023.rbounty.RBountyPlugin;

public class ImmBountyData extends AbstractImmutableSingleData<Integer, ImmBountyData, BountyData>{

	private ImmutableValue<Integer> immutableValue;
	
	public ImmBountyData(Integer value, Key<? extends BaseValue<Integer>> usedKey) {
		super(value, usedKey);
		immutableValue = Sponge.getRegistry().getValueFactory().createValue(RBountyPlugin.BOUNTY, getValue()).asImmutable();
	}
	
	public ImmBountyData(Integer value) {
		super(value, RBountyPlugin.BOUNTY);
		immutableValue = Sponge.getRegistry().getValueFactory().createValue(RBountyPlugin.BOUNTY, value, value).asImmutable();
	}

	public ImmBountyData() {
		super(0, RBountyPlugin.BOUNTY);
		immutableValue = Sponge.getRegistry().getValueFactory().createValue(RBountyPlugin.BOUNTY, value, value).asImmutable();
	}

	
	@Override
	public int getContentVersion() {
		return 1;
	}

	@Override
	public ImmutableValue<Integer> getValueGetter() {
		return immutableValue;
	}
	
	@Override
	public BountyData asMutable() {
		return new BountyData(getValue());
	}
}

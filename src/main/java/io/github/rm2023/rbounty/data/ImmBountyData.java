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

package io.github.rm2023.rbounty.data;

import io.github.rm2023.rbounty.RBountyPlugin;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableSingleData;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;

public class ImmBountyData extends AbstractImmutableSingleData<Integer, ImmBountyData, BountyData>
{
    private final ImmutableValue<Integer> immutableValue;

    public ImmBountyData(Integer value, Key<? extends BaseValue<Integer>> usedKey)
    {
        super(value, usedKey);
        immutableValue = Sponge.getRegistry().getValueFactory().createValue(RBountyPlugin.BOUNTY, getValue())
                .asImmutable();
    }

    public ImmBountyData(Integer value)
    {
        super(value, RBountyPlugin.BOUNTY);
        immutableValue = Sponge.getRegistry().getValueFactory().createValue(RBountyPlugin.BOUNTY, value, value)
                .asImmutable();
    }

    public ImmBountyData()
    {
        super(0, RBountyPlugin.BOUNTY);
        immutableValue = Sponge.getRegistry().getValueFactory().createValue(RBountyPlugin.BOUNTY, value, value)
                .asImmutable();
    }

    @Override
    public int getContentVersion()
    {
        return 1;
    }

    @Override
    public ImmutableValue<Integer> getValueGetter()
    {
        return immutableValue;
    }

    @Override
    public BountyData asMutable()
    {
        return new BountyData(getValue());
    }
}

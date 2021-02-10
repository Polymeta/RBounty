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

import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;

import java.util.Optional;

public class BountyDataBuilder implements DataManipulatorBuilder<BountyData, ImmBountyData>
{
    @Override
    public Optional<BountyData> build(DataView container) throws InvalidDataException
    {
        return new BountyData().from(container.copy());
    }

    @Override
    public BountyData create()
    {
        return new BountyData();
    }

    @Override
    public Optional<BountyData> createFrom(DataHolder dataHolder)
    {
        return new BountyData().from(dataHolder.copy().toContainer());
    }
}

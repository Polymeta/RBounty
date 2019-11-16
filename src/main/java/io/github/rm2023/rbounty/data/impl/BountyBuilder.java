package io.github.rm2023.rbounty.data.impl;

import java.util.Optional;

import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;

import io.github.rm2023.rbounty.data.Bounty;

public class BountyBuilder extends AbstractDataBuilder<Bounty>{
    public static final int CONTENT_VERSION = 2;

    public BountyBuilder() {
        super(Bounty.class, CONTENT_VERSION);
    }

	@Override
	protected Optional<Bounty> buildContent(DataView content) throws InvalidDataException {
        if (!content.contains(Bounty.VALUE_QUERY)) {
            return Optional.empty();
        }

        int value = content.getInt(Bounty.VALUE_QUERY).get();
        
        return Optional.of(new Bounty(value));
	}
}

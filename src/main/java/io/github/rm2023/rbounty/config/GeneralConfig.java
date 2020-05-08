package io.github.rm2023.rbounty.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class GeneralConfig
{
    @Setting(comment = "Whether or not the plugin should broadcast bounty claims and changes.")
    public boolean doBroadcasts = true;

    @Setting(comment = "The message that gets displayed upon bounty claim. Placeholders are  %killer%, %victim% and %bounty%")
    public String claimMessage = "%killer% has claimed %victim%'s bounty of %bounty%!";

    @Setting(comment = "The message that gets displayed when a bounty gets set on a player. Placeholders are %player% and %bounty%")
    public String bountySetMessage = "A bounty of %bounty% has been set on %player%!";

    @Setting(comment = "The message that gets displayed when a bounty gets increased on a player. Placeholders are %player%, %difference% and %bounty%")
    public String bountyIncreasedMessage = "%player%'s bounty has been increased by %difference% and is now at %bounty%!";
}

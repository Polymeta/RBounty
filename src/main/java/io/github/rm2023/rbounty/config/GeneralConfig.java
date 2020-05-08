package io.github.rm2023.rbounty.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class GeneralConfig
{
    @Setting(comment = "Whether or not the plugin should broadcast bounty claims and changes.")
    public static boolean doBroadcasts = true;
}

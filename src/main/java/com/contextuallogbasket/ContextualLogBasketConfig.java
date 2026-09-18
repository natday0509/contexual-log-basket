package com.contextuallogbasket;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("contextuallogbasket")
public interface ContextualLogBasketConfig extends Config
{
    @Range(min = 1, max = 20)
    @ConfigItem(
        keyName = "bankDistance",
        name = "Bank distance",
        description = "Use Fill within this many tiles of a bank (including diagonals), and Empty farther away.",
        position = 0
    )
    default int bankDistance()
    {
        return 2;
    }
}

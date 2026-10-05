package net.runelite.client.plugins.afksaver;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("afksaver")
public interface AfkSaverConfig extends Config
{
	@Range(
		min = 1,
		max = 10
	)
	@ConfigItem(
		keyName = "idleGracePackets",
		name = "Idle packet grace",
		description = "How many idle packets in a row (no activity between them) before the view distance drops to the lowest gated value."
	)
	default int idleGracePackets()
	{
		return 3;
	}
}

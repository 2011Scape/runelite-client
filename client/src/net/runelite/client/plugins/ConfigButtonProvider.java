package net.runelite.client.plugins;

import java.util.List;
import javax.swing.JButton;

/**
 * Implemented by a plugin that wants action buttons in its config panel, alongside the Reset and
 * Back buttons. The buttons are created and rendered by {@code ConfigPanel}; their action listeners
 * run on the Swing event thread, so they should only flip a thread-safe request that the client
 * picks up on its own thread (for example {@code GameClient.forceSceneRebuild}).
 */
public interface ConfigButtonProvider
{
	List<JButton> getConfigButtons();
}

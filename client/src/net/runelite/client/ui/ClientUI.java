/*
 * Copyright (c) 2016-2017, Adam <Adam@sigterm.info>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.ui;

import com.GameClient;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Constants;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.ExpandResizeType;
import net.runelite.client.config.RuneLiteConfig;
import net.runelite.client.config.SidebarSide;
import net.runelite.client.config.WarningOnExit;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ClientShutdown;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.NavigationButtonAdded;
import net.runelite.client.events.NavigationButtonRemoved;
import net.runelite.client.ui.skin.SubstanceRuneLiteLookAndFeel;
import net.runelite.client.util.*;
import org.pushingpixels.substance.internal.SubstanceSynapse;
import org.pushingpixels.substance.internal.utils.SubstanceCoreUtilities;
import org.pushingpixels.substance.internal.utils.SubstanceTitlePaneUtilities;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.swing.*;
import java.applet.Applet;
import java.awt.*;
import net.runelite.api.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static javax.swing.JOptionPane.INFORMATION_MESSAGE;

/**
 * Client UI.
 */
@Slf4j
@Singleton
public class ClientUI
{
	private static final String CONFIG_GROUP = "runelite";
	private static final String CONFIG_CLIENT_BOUNDS = "clientBounds";
	private static final String CONFIG_CLIENT_MAXIMIZED = "clientMaximized";
	private static final String CONFIG_CLIENT_SIDEBAR_CLOSED = "clientSidebarClosed";
	private static final String CONFIG_CLIENT_SIDEBAR_INITIALIZED = "clientSidebarInitialized";
	private static final Dimension DEFAULT_COMPACT_CLIENT_BOUNDS = new Dimension(900, 560);
	private static final int TITLEBAR_CONTROL_GAP = 6;
	private static final int TITLEBAR_BUTTON_SIZE = 23;
	private static final int TITLEBAR_BUTTON_GAP = 2;
	public static final BufferedImage ICON = ImageUtil.loadImageResource(ClientUI.class, "/icon-256.png");
	private static final BufferedImage CLIENT_BACKGROUND = ImageUtil.loadImageResource(ClientUI.class, "client-background.png");

	@Getter
	private TrayIcon trayIcon;

	private final RuneLiteConfig config;

	public RuneLiteConfig getConfig() {
		return config;
	}

	//private final KeyManager keyManager;
	//private final MouseManager mouseManager;
	private Applet client;
	private final ConfigManager configManager;
	//private final Provider<ClientThread> clientThreadProvider;
	private final EventBus eventBus;
	private final boolean safeMode;
	private final String title;

	private final CardLayout cardLayout = new CardLayout();
	private final Rectangle sidebarButtonPosition = new Rectangle();
	private boolean withTitleBar;
	private BufferedImage sidebarLeftIcon;
	private BufferedImage sidebarRightIcon;
	private ContainableFrame frame;
	private JPanel navContainer;
	private PluginPanel pluginPanel;
	private ClientPluginToolbar pluginToolbar;
	private ClientPanel clientPanel;
	private ClientTitleToolbar titleToolbar;
	private JPanel sidebarContainer;
	private JButton currentButton;
	private NavigationButton currentNavButton;
	private boolean sidebarOpen;
	private JPanel container;
	private NavigationButton sidebarNavigationButton;
	private JButton sidebarNavigationJButton;
	private Dimension lastClientSize;
	private Cursor defaultCursor;
	private Rectangle lastNormalClientBounds;
	private boolean applyingStoredBounds;
	private boolean windowPersistenceReady;

	@Inject
	private ClientUI(
		RuneLiteConfig config,
		//KeyManager keyManager,
		//MouseManager mouseManager,
		@Nullable Applet client,
		ConfigManager configManager,
		//Provider<ClientThread> clientThreadProvider,
		EventBus eventBus,
		@Named("safeMode") boolean safeMode,
		@Named("void.title") String title,
		@Named("void.version") String version
	)
	{
		System.out.println("client is: "+client);
		this.config = config;
		//this.keyManager = keyManager;
		//this.mouseManager = mouseManager;
		this.client = client;
		this.configManager = configManager;
		//this.clientThreadProvider = clientThreadProvider;
		this.eventBus = eventBus;
		this.safeMode = safeMode;
		this.title = title + "(" + version + ")";
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals(CONFIG_GROUP) ||
			event.getKey().equals(CONFIG_CLIENT_MAXIMIZED) ||
			event.getKey().equals(CONFIG_CLIENT_BOUNDS))
		{
			return;
		}

		SwingUtilities.invokeLater(() ->
		{
			if (event.getKey().equals("sidebarSide"))
			{
				updateSidebarLayout();
				updateSidebarToggleButton();
				repaintSidebarSideSwitch();
				return;
			}

			updateFrameConfig(event.getKey().equals("lockWindowSize"));
		});
	}

	@Subscribe
	public void onNavigationButtonAdded(final NavigationButtonAdded event)
	{
		SwingUtilities.invokeLater(() ->
		{
			final NavigationButton navigationButton = event.getButton();
			final PluginPanel pluginPanel = navigationButton.getPanel();
			final boolean inTitle = !event.getButton().isTab() && withTitleBar;
			final int iconSize = 16;

			if (pluginPanel != null)
			{
				navContainer.add(pluginPanel.getWrappedPanel(), navigationButton.getTooltip());
			}

			final JButton button = SwingUtil.createSwingButton(navigationButton, iconSize, (navButton, jButton) ->
			{
				final PluginPanel panel = navButton.getPanel();

				if (panel == null)
				{
					return;
				}

				boolean doClose = currentButton != null && currentButton == jButton && currentButton.isSelected();

				if (doClose)
				{
					contract();
					currentButton.setSelected(false);
					currentNavButton.setSelected(false);
					currentButton = null;
				}
				else
				{
					if (currentButton != null)
					{
						currentButton.setSelected(false);
					}

					if (currentNavButton != null)
					{
						currentNavButton.setSelected(false);
					}

					currentButton = jButton;
					currentNavButton = navButton;
					currentButton.setSelected(true);
					currentNavButton.setSelected(true);
					expand(navButton);
				}
			});

			if (inTitle)
			{
				titleToolbar.addComponent(event.getButton(), button);
				titleToolbar.revalidate();
			}
			else
			{
				pluginToolbar.addComponent(event.getButton(), button);
				pluginToolbar.revalidate();
			}
		});
	}

	@Subscribe
	public void onNavigationButtonRemoved(final NavigationButtonRemoved event)
	{
		SwingUtilities.invokeLater(() ->
		{
			pluginToolbar.removeComponent(event.getButton());
			pluginToolbar.revalidate();
			titleToolbar.removeComponent(event.getButton());
			titleToolbar.revalidate();
			final PluginPanel pluginPanel = event.getButton().getPanel();

			if (pluginPanel != null)
			{
				navContainer.remove(pluginPanel.getWrappedPanel());
			}
		});
	}

	/*@Subscribe
	public void onGameStateChanged(final GameStateChanged event)
	{
		if (event.getGameState() != GameState.LOGGED_IN || !(client instanceof Client) || !config.usernameInTitle())
		{
			return;
		}

		final Client client = (Client) this.client;
		final ClientThread clientThread = clientThreadProvider.get();

		// Keep scheduling event until we get our name
		clientThread.invokeLater(() ->
		{
			if (client.getGameState() != GameState.LOGGED_IN)
			{
				return true;
			}

			final Player player = client.getLocalPlayer();

			if (player == null)
			{
				return false;
			}

			final String name = player.getName();

			if (Strings.isNullOrEmpty(name))
			{
				return false;
			}

			frame.setTitle(title + " - " + name);
			return true;
		});
	}*/

	/**
	 * Initialize UI.
	 */
	public void init() throws Exception
	{
		SwingUtilities.invokeAndWait(() ->
		{
			// Set some sensible swing defaults
			SwingUtil.setupDefaults();

			// Use substance look and feel
			SwingUtil.setTheme(new SubstanceRuneLiteLookAndFeel());

			// Use custom UI font
			SwingUtil.setFont(FontManager.getRunescapeFont());

			// On Linux and macOS the look and feel only treats window decorations as custom when its
			// default-decoration flags are set. Without them the frame is left undecorated with no title
			// bar at all, so the sidebar toggle that lives in the title toolbar can never be reached.
			// The frame reads the flag while it is being created, so set it before the frame is made.
			final OSType osType = OSType.getOSType();
			if (config.enableCustomChrome() && (osType == OSType.Linux || osType == OSType.MacOS))
			{
				JFrame.setDefaultLookAndFeelDecorated(true);
				JDialog.setDefaultLookAndFeelDecorated(true);
			}

			// Create main window
			frame = new ContainableFrame();

			// Try to enable fullscreen on OSX
			OSXUtil.tryEnableFullscreen(frame);

			frame.setTitle(title);
			frame.setIconImage(ICON);
			frame.getLayeredPane().setCursor(Cursor.getDefaultCursor()); // Prevent substance from using a resize cursor for pointing
			frame.setLocationRelativeTo(frame.getOwner());
			frame.setResizable(true);

			frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			frame.addWindowListener(new WindowAdapter()
			{
				@Override
				public void windowClosing(WindowEvent event)
				{
					int result = JOptionPane.OK_OPTION;

					if (showWarningOnExit())
					{
						try
						{
							result = JOptionPane.showConfirmDialog(
								frame,
								"Are you sure you want to exit?", "Exit",
								JOptionPane.OK_CANCEL_OPTION,
								JOptionPane.QUESTION_MESSAGE);
						}
						catch (Exception e)
						{
							log.warn("Unexpected exception occurred while check for confirm required", e);
						}
					}

					if (result == JOptionPane.OK_OPTION)
					{
						shutdownClient();
					}
				}
			});
			frame.addComponentListener(new ComponentAdapter()
			{
				@Override
				public void componentMoved(ComponentEvent event)
				{
					saveClientBoundsConfig();
				}

				@Override
				public void componentResized(ComponentEvent event)
				{
					saveClientBoundsConfig();
				}
			});
			frame.addWindowStateListener(event -> saveClientBoundsConfig());

			container = new BackgroundPanel(new BorderLayout(), CLIENT_BACKGROUND);
			container.setBackground(ColorScheme.DARKER_GRAY_COLOR);


			// Decorate window with custom chrome and titlebar if needed
			withTitleBar = config.enableCustomChrome();
			frame.setUndecorated(withTitleBar);
			if (withTitleBar)
			{
				// Install the look-and-feel title pane before the frame is realised. The look and feel (and
				// its Linux/macOS window-decoration handling) expects the decoration style to be set on the
				// undecorated frame before it is packed, so do it here rather than after pack().
				frame.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);

				// Some platforms (notably macOS Aqua) hand window decorations to the OS and leave the look
				// and feel without a title pane. Setting the decoration style on a frame that is already
				// undecorated would then leave a window with no title bar and no way to reach the sidebar
				// toggle, so detect that now - before pack() makes the frame displayable - and fall back to
				// the native decorations instead.
				if (osType != OSType.Windows && SubstanceCoreUtilities.getTitlePaneComponent(frame) == null)
				{
					log.warn("Custom window chrome requested but the look-and-feel title pane is unavailable; "
						+ "falling back to native window decorations");
					try
					{
						frame.setUndecorated(false);
						withTitleBar = false;
					}
					catch (IllegalComponentStateException ex)
					{
						log.warn("Unable to restore native window decorations", ex);
					}
				}
			}

			// Layout frame
			frame.pack();
			frame.revalidateMinimumSize();


			navContainer = new BackgroundPanel(CLIENT_BACKGROUND);
			navContainer.setLayout(cardLayout);
			navContainer.setMinimumSize(new Dimension(0, 0));
			navContainer.setMaximumSize(new Dimension(0, 0));
			navContainer.setPreferredSize(new Dimension(0, 0));
			navContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);

			// To reduce substance's colorization (tinting)
			navContainer.putClientProperty(SubstanceSynapse.COLORIZATION_FACTOR, 1.0);
			clientPanel = new ClientPanel(client, CLIENT_BACKGROUND);

			pluginToolbar = new ClientPluginToolbar(CLIENT_BACKGROUND);
			sidebarContainer = new BackgroundPanel(CLIENT_BACKGROUND);
			sidebarContainer.setLayout(new BoxLayout(sidebarContainer, BoxLayout.X_AXIS));
			titleToolbar = new ClientTitleToolbar();
			updateSidebarLayout();

			frame.add(container);

			// Add key listener
			/*final HotkeyListener sidebarListener = new HotkeyListener(config::sidebarToggleKey)
			{
				@Override
				public void hotkeyPressed()
				{
					toggleSidebar();
				}
			};
			sidebarListener.setEnabledOnLoginScreen(true);
			keyManager.registerKeyListener(sidebarListener);

			final HotkeyListener pluginPanelListener = new HotkeyListener(config::panelToggleKey)
			{
				@Override
				public void hotkeyPressed()
				{
					togglePluginPanel();
				}
			};
			pluginPanelListener.setEnabledOnLoginScreen(true);
			keyManager.registerKeyListener(pluginPanelListener);

			// Add mouse listener
			final MouseListener mouseListener = new MouseAdapter()
			{
				@Override
				public MouseEvent mousePressed(MouseEvent mouseEvent)
				{
					if (SwingUtilities.isLeftMouseButton(mouseEvent) && sidebarButtonPosition.contains(mouseEvent.getPoint()))
					{
						SwingUtilities.invokeLater(ClientUI.this::toggleSidebar);
						mouseEvent.consume();
					}

					return mouseEvent;
				}
			};
			mouseManager.registerMouseListener(mouseListener);*/


			if (withTitleBar)
			{
				final JComponent titleBar = SubstanceCoreUtilities.getTitlePaneComponent(frame);
				if (titleBar == null)
				{
					// The look and feel did not provide a title pane. On Linux that is what happens when its
					// default-decoration flags are not set - init() sets them above. Rather than fail the whole
					// UI, keep going without the custom title bar.
					log.warn("Custom window chrome requested but the look-and-feel title pane is unavailable");
				}
				else
				{
					installTitleToolbar(titleBar);
				}
			}

			// Update config
			updateFrameConfig(true);

			// Create hide sidebar button

			sidebarLeftIcon = ImageUtil.alphaOffset(ImageUtil.luminanceScale(ImageUtil.loadImageResource(ClientUI.class, "sidebar_toggle.png"), 0.68f), 0.88f);
			sidebarRightIcon = ImageUtil.flipImage(sidebarLeftIcon, true, false);

			sidebarNavigationButton = NavigationButton
				.builder()
				.priority(100)
				.icon(getSidebarToggleIcon())
				.tooltip("Open SideBar")
				.onClick(this::toggleSidebar)
				.build();

			sidebarNavigationJButton = SwingUtil.createSwingButton(
				sidebarNavigationButton,
				0,
				null);

			titleToolbar.addComponent(sidebarNavigationButton, sidebarNavigationJButton);

			restoreSidebarState();
		});
	}

	/**
	 * Adds the title toolbar (and, through it, the sidebar toggle button) to the look-and-feel title
	 * pane, and widens the pane's layout so the toolbar is laid out beside the window controls. Split
	 * out of {@link #init()} so a look and feel that provides no title pane can be handled without
	 * failing the whole UI.
	 */
	private void installTitleToolbar(JComponent titleBar)
	{
		titleToolbar.putClientProperty(SubstanceTitlePaneUtilities.EXTRA_COMPONENT_KIND, SubstanceTitlePaneUtilities.ExtraComponentKind.TRAILING);
		titleBar.add(titleToolbar);

		// Substance's default layout manager for the title bar only lays out substance's components
		// This wraps the default manager and lays out the TitleToolbar as well.
		LayoutManager delegate = titleBar.getLayout();
		titleBar.setLayout(new LayoutManager()
		{
			@Override
			public void addLayoutComponent(String name, Component comp)
			{
				delegate.addLayoutComponent(name, comp);
			}

			@Override
			public void removeLayoutComponent(Component comp)
			{
				delegate.removeLayoutComponent(comp);
			}

			@Override
			public Dimension preferredLayoutSize(Container parent)
			{
				return delegate.preferredLayoutSize(parent);
			}

			@Override
			public Dimension minimumLayoutSize(Container parent)
			{
				return delegate.minimumLayoutSize(parent);
			}

			@Override
			public void layoutContainer(Container parent)
			{
				delegate.layoutContainer(parent);
				normalizeTitlebarControlButtons(titleBar, titleToolbar);
				final int width = titleToolbar.getPreferredSize().width;
				final int availableWidth = Math.max(0, getTitlebarControlsLeft(titleBar, titleToolbar) - TITLEBAR_CONTROL_GAP);
				final int height = Math.min(titleToolbar.getPreferredSize().height, titleBar.getHeight());
				final int y = Math.max(0, (titleBar.getHeight() - height) / 2);
				titleToolbar.setBounds(Math.max(0, availableWidth - width), y, Math.min(width, availableWidth), height);
			}
		});
	}

	private static int getTitlebarControlsLeft(Container titleBar, Component extraToolbar)
	{
		int controlsLeft = titleBar.getWidth();
		int rightHalf = titleBar.getWidth() / 2;

		for (Component component : titleBar.getComponents())
		{
			if (component == extraToolbar || !component.isVisible())
			{
				continue;
			}

			Rectangle bounds = component.getBounds();
			if (bounds.width > 0 && bounds.x >= rightHalf)
			{
				controlsLeft = Math.min(controlsLeft, bounds.x);
			}
		}

		return Math.max(0, controlsLeft);
	}

	private static void normalizeTitlebarControlButtons(Container titleBar, Component extraToolbar)
	{
		List<Component> controls = new ArrayList<>();
		int minX = Integer.MAX_VALUE;
		int maxRight = 0;

		for (Component component : titleBar.getComponents())
		{
			if (component == extraToolbar || !component.isVisible() || !isSubstanceTitleButton(component))
			{
				continue;
			}

			controls.add(component);
			minX = Math.min(minX, component.getX());
			maxRight = Math.max(maxRight, component.getX() + component.getWidth());
		}

		if (controls.isEmpty())
		{
			return;
		}

		controls.sort((a, b) -> Integer.compare(a.getX(), b.getX()));

		final int y = Math.max(0, (titleBar.getHeight() - TITLEBAR_BUTTON_SIZE) / 2);
		if (minX >= titleBar.getWidth() / 2)
		{
			int x = maxRight - TITLEBAR_BUTTON_SIZE;
			for (int i = controls.size() - 1; i >= 0; i--)
			{
				setTitlebarControlBounds(controls.get(i), x, y);
				x -= TITLEBAR_BUTTON_SIZE + TITLEBAR_BUTTON_GAP;
			}
		}
		else
		{
			int x = minX;
			for (Component control : controls)
			{
				setTitlebarControlBounds(control, x, y);
				x += TITLEBAR_BUTTON_SIZE + TITLEBAR_BUTTON_GAP;
			}
		}
	}

	private static boolean isSubstanceTitleButton(Component component)
	{
		return component instanceof AbstractButton &&
			component.getClass().getName().equals("org.pushingpixels.substance.internal.utils.SubstanceTitleButton");
	}

	private static void setTitlebarControlBounds(Component control, int x, int y)
	{
		Dimension size = new Dimension(TITLEBAR_BUTTON_SIZE, TITLEBAR_BUTTON_SIZE);
		control.setMinimumSize(size);
		control.setPreferredSize(size);
		control.setMaximumSize(size);
		control.setBounds(x, y, TITLEBAR_BUTTON_SIZE, TITLEBAR_BUTTON_SIZE);

		AbstractButton button = (AbstractButton) control;
		button.setMargin(new Insets(0, 0, 0, 0));
		button.setHorizontalAlignment(AbstractButton.CENTER);
		button.setVerticalAlignment(AbstractButton.CENTER);
	}

	public void show()
	{
		SwingUtilities.invokeLater(() ->
		{
			// Layout frame
			frame.pack();
			frame.revalidateMinimumSize();

			// Create tray icon (needs to be created after frame is packed)
			trayIcon = SwingUtil.createTrayIcon(ICON, title, frame);

			// Move frame around (needs to be done after frame is packed)
			if (config.rememberScreenBounds() && !safeMode)
			{
				try
				{
					Rectangle clientBounds = configManager.getConfiguration(
						CONFIG_GROUP, CONFIG_CLIENT_BOUNDS, Rectangle.class);
					if (clientBounds != null)
					{
						applyWindowBounds(clientBounds);

						// frame.getGraphicsConfiguration().getBounds() returns the bounds for the primary display.
						// We have to find the correct graphics configuration by using the client boundaries.
						GraphicsConfiguration gc = findDisplayFromBounds(clientBounds);
						if (gc != null)
						{
							double scale = gc.getDefaultTransform().getScaleX();

							// When Windows screen scaling is on, the position/bounds will be wrong when they are set.
							// The bounds saved in shutdown are the full, non-scaled co-ordinates.
							if (scale != 1)
							{
								clientBounds.setRect(
									clientBounds.getX() / scale,
									clientBounds.getY() / scale,
									clientBounds.getWidth() / scale,
									clientBounds.getHeight() / scale);

								frame.setMinimumSize(clientBounds.getSize());
								applyWindowBounds(clientBounds);
							}
						}
						else
						{
							applyDefaultCompactBounds();
						}
					}
					else
					{
						applyDefaultCompactBounds();
					}

					if (Boolean.TRUE.equals(configManager.getConfiguration(CONFIG_GROUP, CONFIG_CLIENT_MAXIMIZED, Boolean.class)))
					{
						frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
					}
				}
				catch (Exception ex)
				{
					log.warn("Failed to set window bounds", ex);
					applyDefaultCompactBounds();
				}
			}
			else
			{
				applyDefaultCompactBounds();
			}



			// Show frame
			frame.setVisible(true);
			if ((frame.getExtendedState() & JFrame.MAXIMIZED_BOTH) != JFrame.MAXIMIZED_BOTH && lastNormalClientBounds != null)
			{
				applyWindowBounds(lastNormalClientBounds);
			}
			windowPersistenceReady = true;
			saveClientBoundsConfig();
			frame.toFront();
			requestFocus();
			log.info("Showing frame {} null: "+client, frame);
			frame.revalidateMinimumSize();

		});

		/*// Show out of date dialog if needed
		if (client != null && client != ClientScriptMap.anApplet6044)
		{
			SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame,
				"RuneLite has not yet been updated to work with the latest\n"
					+ "game update, it will work with reduced functionality until then.",
				"RuneLite is outdated", INFORMATION_MESSAGE));
		}*/
	}

	private GraphicsConfiguration findDisplayFromBounds(final Rectangle bounds)
	{
		GraphicsDevice[] gds = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();

		for (GraphicsDevice gd : gds)
		{
			GraphicsConfiguration gc = gd.getDefaultConfiguration();

			final Rectangle displayBounds = gc.getBounds();
			if (displayBounds.contains(bounds))
			{
				return gc;
			}
		}

		return null;
	}

	private void applyWindowBounds(Rectangle bounds)
	{
		applyingStoredBounds = true;
		try
		{
			Rectangle sanitizedBounds = new Rectangle(bounds);
			Dimension minimumSize = frame.getMinimumSize();
			if (minimumSize != null)
			{
				sanitizedBounds.width = Math.max(sanitizedBounds.width, minimumSize.width);
				sanitizedBounds.height = Math.max(sanitizedBounds.height, minimumSize.height);
			}
			frame.setBounds(sanitizedBounds);
			lastNormalClientBounds = new Rectangle(sanitizedBounds);
		}
		finally
		{
			applyingStoredBounds = false;
		}
	}

	private void applyDefaultCompactBounds()
	{
		Dimension minimumSize = frame.getMinimumSize();
		int width = Math.max(DEFAULT_COMPACT_CLIENT_BOUNDS.width, minimumSize == null ? 0 : minimumSize.width);
		int height = Math.max(DEFAULT_COMPACT_CLIENT_BOUNDS.height, minimumSize == null ? 0 : minimumSize.height);
		Rectangle screenBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		Rectangle compactBounds = new Rectangle(
			screenBounds.x + Math.max(0, (screenBounds.width - width) / 2),
			screenBounds.y + Math.max(0, (screenBounds.height - height) / 2),
			Math.min(width, screenBounds.width),
			Math.min(height, screenBounds.height));
		applyWindowBounds(compactBounds);
	}

	private void restoreSidebarState()
	{
		boolean firstRun = configManager.getConfiguration(CONFIG_GROUP, CONFIG_CLIENT_SIDEBAR_INITIALIZED) == null;
		if (firstRun)
		{
			configManager.setConfiguration(CONFIG_GROUP, CONFIG_CLIENT_SIDEBAR_INITIALIZED, true);
			configManager.setConfiguration(CONFIG_GROUP, CONFIG_CLIENT_SIDEBAR_CLOSED, true);
			sidebarOpen = false;
			pluginToolbar.setVisible(false);
			updateSidebarToggleButton();
			updateSidebarLayout();
			return;
		}

		if (configManager.getConfiguration(CONFIG_GROUP, CONFIG_CLIENT_SIDEBAR_CLOSED) == null)
		{
			toggleSidebar();
			return;
		}

		sidebarOpen = false;
		pluginToolbar.setVisible(false);
		updateSidebarToggleButton();
		updateSidebarLayout();
	}

	private boolean showWarningOnExit()
	{
		if (config.warningOnExit() == WarningOnExit.ALWAYS)
		{
			return true;
		}

		if (config.warningOnExit() == WarningOnExit.LOGGED_IN && client instanceof GameClient)
		{
			return ((GameClient) client).hasLocalPlayer();
		}

		return false;
	}

	private void shutdownClient()
	{
		saveClientBoundsConfig();
		ClientShutdown csev = new ClientShutdown();
		eventBus.post(csev);
		new Thread(() ->
		{
			csev.waitForAllConsumers(Duration.ofSeconds(10));

			if (client != null)
			{
				// The client can call System.exit when it's done shutting down
				// if it doesn't though, we want to exit anyway, so race it
				int clientShutdownWaitMS;
				if (client != null)
				{
					//((GameClient) client).stopNow();
					clientShutdownWaitMS = 1000;
				}
				else
				{
					// it will continue rendering for about 4 seconds before attempting shutdown if its vanilla
					client.stop();
					frame.setVisible(false);
					clientShutdownWaitMS = 6000;
				}

				try
				{
					Thread.sleep(clientShutdownWaitMS);
				}
				catch (InterruptedException ignored)
				{
				}
			}
			System.exit(0);
		}, "RuneLite Shutdown").start();
	}

	/**
	 * Paint this component to target graphics
	 *
	 * @param graphics the graphics
	 */
	public void paint(final Graphics graphics)
	{
		assert SwingUtilities.isEventDispatchThread() : "paint must be called on EDT";
		frame.paint(graphics);
	}

	/**
	 * Gets component width.
	 *
	 * @return the width
	 */
	public int getWidth()
	{
		return frame.getWidth();
	}

	/**
	 * Gets component height.
	 *
	 * @return the height
	 */
	public int getHeight()
	{
		return frame.getHeight();
	}

	/**
	 * Returns true if this component has focus.
	 *
	 * @return true if component has focus
	 */
	public boolean isFocused()
	{
		return frame.isFocused();
	}

	/**
	 * Request focus on this component and then on client component
	 */
	public void requestFocus()
	{
		switch (OSType.getOSType())
		{
			case MacOS:
				// On OSX Component::requestFocus has no visible effect, so we use our OSX-specific
				// requestUserAttention()
				OSXUtil.requestUserAttention();
				break;
			default:
				frame.requestFocus();
		}

		giveClientFocus();
	}

	/**
	 * Attempt to forcibly bring the client frame to front
	 */
	public void forceFocus()
	{
		switch (OSType.getOSType())
		{
			case MacOS:
				OSXUtil.requestForeground();
				break;
			case Windows:
				WinUtil.requestForeground(frame);
				break;
			default:
				frame.requestFocus();
				break;
		}

		giveClientFocus();
	}

	/**
	 * Returns current cursor set on game container
	 * @return awt cursor
	 */
	public Cursor getCurrentCursor()
	{
		return container.getCursor();
	}

	/**
	 * Returns current custom cursor or default system cursor if cursor is not set
	 * @return awt cursor
	 */
	public Cursor getDefaultCursor()
	{
		return defaultCursor != null ? defaultCursor : Cursor.getDefaultCursor();
	}

	/**
	 * Changes cursor for client window. Requires ${@link ClientUI#init()} to be called first.
	 * FIXME: This is working properly only on Windows, Linux and Mac are displaying cursor incorrectly
	 * @param image cursor image
	 * @param name  cursor name
	 */
	public void setCursor(final BufferedImage image, final String name)
	{
		if (container == null)
		{
			return;
		}

		final
		java.awt.Point hotspot = new
				java.awt.Point(0, 0);
		final Cursor cursorAwt = Toolkit.getDefaultToolkit().createCustomCursor(image, hotspot, name);
		defaultCursor = cursorAwt;
		setCursor(cursorAwt);
	}

	/**
	 * Changes cursor for client window. Requires ${@link ClientUI#init()} to be called first.
	 * @param cursor awt cursor
	 */
	public void setCursor(final Cursor cursor)
	{
		container.setCursor(cursor);
	}

	/**
	 * Resets client window cursor to default one.
	 * @see ClientUI#setCursor(BufferedImage, String)
	 */
	public void resetCursor()
	{
		if (container == null)
		{
			return;
		}

		defaultCursor = null;
		container.setCursor(Cursor.getDefaultCursor());
	}

	/**
	 * Get pos of game canvas in game window
	 *
	 * @return game canvas pos
	 */
	public Point getCanvasOffset()
	{
		/*if (client == ClientScriptMap.anApplet6044)
		{
			final Canvas canvas = client.getCanvas();
			if (canvas != null)
			{
				final Point point = SwingUtilities.convertPoint(canvas, 0, 0, frame);
				return new Point(point.x, point.y);
			}
		}*/

		return new Point(0, 0);
	}
	/**
	 * Paint UI related overlays to target graphics
	 * @param graphics target graphics
	 */
	public void paintOverlays(final Graphics2D graphics)
	{
		/*if (!(client instanceof GameClient) || withTitleBar)
		{
			return;
		}

		final GameClient client = (GameClient) this.client;
		final int x = client.getRealDimensions().width - sidebarOpenIcon.getWidth() - 5;

		// pos sidebar button if resizable mode logout is visible
//		final Widget logoutButton = client.getWidget(WidgetInfo.RESIZABLE_VIEWPORT_BOTTOM_LINE_LOGOUT_BUTTON);
		final int y = /*logoutButton != null && !logoutButton.isHidden() && logoutButton.getParent() != null
				? logoutButton.getHeight() + logoutButton.getRelativeY()
				:*/ /*5;

		final BufferedImage image = sidebarOpen ? sidebarClosedIcon : sidebarOpenIcon;

		final Rectangle sidebarButtonRange = new Rectangle(x - 15, 0, image.getWidth() + 25, client.getRealDimensions().height);
		final Point mousePosition = new Point(
				client.getMouseCanvasPosition().getX() + client.getViewportXOffset(),
				client.getMouseCanvasPosition().getY() + client.getViewportYOffset());

		if (sidebarButtonRange.contains(mousePosition.getX(), mousePosition.getY()))
		{
			graphics.drawImage(image, x, y, null);
		}

		// Update button dimensions
		sidebarButtonPosition.setBounds(x, y, image.getWidth(), image.getHeight());*/
	}

	public GraphicsConfiguration getGraphicsConfiguration()
	{
		return frame.getGraphicsConfiguration();
	}

	private void toggleSidebar()
	{
		// Toggle sidebar open
		boolean isSidebarOpen = sidebarOpen;
		sidebarOpen = !sidebarOpen;

		// Select/deselect buttons
		if (currentButton != null)
		{
			currentButton.setSelected(sidebarOpen);
		}

		if (currentNavButton != null)
		{
			currentNavButton.setSelected(sidebarOpen);
		}

		if (isSidebarOpen)
		{
			updateSidebarToggleButton();
			configManager.setConfiguration(CONFIG_GROUP, CONFIG_CLIENT_SIDEBAR_CLOSED, true);

			contract();

			pluginToolbar.setVisible(false);
		}
		else
		{
			updateSidebarToggleButton();
			configManager.unsetConfiguration(CONFIG_GROUP, CONFIG_CLIENT_SIDEBAR_CLOSED);

			// Try to restore last panel
			expand(currentNavButton);

			pluginToolbar.setVisible(true);
			updateSidebarLayout();
		}

		// Revalidate sizes of affected Swing components
		updateSidebarLayout();
		giveClientFocus();

		if (sidebarOpen)
		{
			frame.expandBy(pluginToolbar.getWidth());
		}
		else
		{
			frame.contractBy(pluginToolbar.getWidth());
		}

		finishClientResize();
	}

	private void finishClientResize()
	{
		frame.refreshNativePeer();
		redrawClientNow();
		redrawClient();
		SwingUtilities.invokeLater(() ->
		{
			frame.refreshNativePeer();
			redrawClientNow();
			Timer timer = new Timer(75, event ->
			{
				frame.refreshNativePeer();
				redrawClientNow();
			});
			timer.setRepeats(false);
			timer.start();
		});
	}

	private void repaintSidebarSideSwitch()
	{
		container.revalidate();
		container.repaint();
		clientPanel.revalidate();
		clientPanel.repaint();
		frame.revalidate();
		frame.repaint();

		SwingUtilities.invokeLater(() ->
		{
			container.repaint();
			clientPanel.repaint();
			frame.repaint();
			redrawClientNow();
		});
	}

	private void redrawClient()
	{
		if (!(client instanceof GameClient))
		{
			return;
		}

		final GameClient gameClient = (GameClient) client;
		SwingUtilities.invokeLater(() ->
		{
			redrawClient(gameClient, true);
			SwingUtilities.invokeLater(() -> redrawClient(gameClient, true));
		});
	}

	private void redrawClientNow()
	{
		if (client instanceof GameClient)
		{
			redrawClient((GameClient) client, false);
		}
	}

	private void redrawClient(GameClient gameClient, boolean requestFocus)
	{
		try
		{
			gameClient.invalidateStretching(true);

			Canvas canvas = gameClient.getCanvas();
			if (canvas != null)
			{
				canvas.invalidate();
				canvas.validate();
				canvas.repaint();

				Container parent = canvas.getParent();
				while (parent != null)
				{
					parent.invalidate();
					parent.validate();
					parent.doLayout();
					parent.repaint();
					parent = parent.getParent();
				}
			}

			client.invalidate();
			client.validate();
			client.repaint();
			frame.invalidate();
			frame.validate();
			frame.repaint();
			if (requestFocus)
			{
				giveClientFocus();
			}
			Toolkit.getDefaultToolkit().sync();
		}
		catch (RuntimeException ex)
		{
			log.debug("Unable to redraw client after sidebar resize", ex);
		}
	}

	private void togglePluginPanel()
	{
		// Toggle plugin panel open
		final boolean pluginPanelOpen = pluginPanel != null;

		if (currentButton != null)
		{
			currentButton.setSelected(!pluginPanelOpen);
		}

		if (pluginPanelOpen)
		{
			contract();
		}
		else
		{
			expand(currentNavButton);
		}
	}

	private boolean isSidebarOnLeft()
	{
		return config.sidebarSide() == SidebarSide.LEFT;
	}

	private BufferedImage getSidebarToggleIcon()
	{
		return isSidebarOnLeft() ? sidebarLeftIcon : sidebarRightIcon;
	}

	private void updateSidebarToggleButton()
	{
		if (sidebarNavigationJButton == null || sidebarLeftIcon == null || sidebarRightIcon == null)
		{
			return;
		}

		sidebarNavigationJButton.setIcon(new ImageIcon(getSidebarToggleIcon()));
		sidebarNavigationJButton.setToolTipText(sidebarOpen ? "Close SideBar" : "Open SideBar");
	}

	private void updateSidebarLayout()
	{
		if (container == null || navContainer == null || clientPanel == null)
		{
			return;
		}

		final boolean toolbarVisible = pluginToolbar != null && pluginToolbar.isVisible();

		sidebarContainer.removeAll();
		if (isSidebarOnLeft())
		{
			if (toolbarVisible)
			{
				sidebarContainer.add(pluginToolbar);
			}
			sidebarContainer.add(navContainer);
		}
		else
		{
			sidebarContainer.add(navContainer);
			if (toolbarVisible)
			{
				sidebarContainer.add(pluginToolbar);
			}
		}

		if (clientPanel.getParent() != container)
		{
			container.add(clientPanel, BorderLayout.CENTER);
		}

		if (sidebarContainer.getParent() == container)
		{
			container.remove(sidebarContainer);
		}
		container.add(sidebarContainer, isSidebarOnLeft() ? BorderLayout.WEST : BorderLayout.EAST);

		sidebarContainer.revalidate();
		sidebarContainer.repaint();
		container.revalidate();
		container.repaint();
	}

	private void expand(@Nullable NavigationButton button)
	{
		if (button == null)
		{
			return;
		}

		final PluginPanel panel = button.getPanel();

		if (panel == null)
		{
			return;
		}

		if (!sidebarOpen)
		{
			toggleSidebar();
		}

		int width = panel.getWrappedPanel().getPreferredSize().width;
		int expandBy = pluginPanel != null ? pluginPanel.getWrappedPanel().getPreferredSize().width - width : width;
		pluginPanel = panel;

		// Expand sidebar
		navContainer.setMinimumSize(new Dimension(width, 0));
		navContainer.setMaximumSize(new Dimension(width, Integer.MAX_VALUE));
		navContainer.setPreferredSize(new Dimension(width, 0));
		navContainer.revalidate();
		cardLayout.show(navContainer, button.getTooltip());

		// panel.onActivate has to go after giveClientFocus so it can get focus if it needs.
		giveClientFocus();
		panel.onActivate();

		// Check if frame was really expanded or contracted
		if (expandBy > 0)
		{
			frame.expandBy(expandBy);
		}
		else if (expandBy < 0)
		{
			frame.contractBy(expandBy);
		}
	}

	private void contract()
	{
		if (pluginPanel == null)
		{
			return;
		}

		pluginPanel.onDeactivate();
		navContainer.setMinimumSize(new Dimension(0, 0));
		navContainer.setMaximumSize(new Dimension(0, 0));
		navContainer.setPreferredSize(new Dimension(0, 0));
		navContainer.revalidate();
		giveClientFocus();
		frame.contractBy(pluginPanel.getWrappedPanel().getPreferredSize().width);
		pluginPanel = null;
	}

	private void giveClientFocus()
	{
		/*if (client instanceof GameClient)
		{
			final Canvas c = ((GameClient) client).getCanvas();
			if (c != null)
			{
				c.requestFocusInWindow();
			}
		}
		else if (client != null)
		{
			client.requestFocusInWindow();
		}*/
	}

	private void updateFrameConfig(boolean updateResizable)
	{
		if (frame == null)
		{
			return;
		}

		// Update window opacity if the frame is undecorated, translucency capable and not fullscreen
		if (frame.isUndecorated() &&
			frame.getGraphicsConfiguration().isTranslucencyCapable() &&
			frame.getGraphicsConfiguration().getDevice().getFullScreenWindow() == null)
		{
			frame.setOpacity(((float) config.windowOpacity()) / 100.0f);
		}

	/*	if (config.usernameInTitle() && (client instanceof GameClient))
		{
			final Player player = ((GameClient)client).getLocalPlayer();

			if (player != null && player.getName() != null)
			{
				frame.setTitle(title + " - " + player.getName());
			}
		}*/
		else
		{
			frame.setTitle(title);
		}

		if (frame.isAlwaysOnTopSupported())
		{
			frame.setAlwaysOnTop(config.gameAlwaysOnTop());
		}

		if (updateResizable)
		{
			frame.setResizable(!config.lockWindowSize());
		}

		frame.setExpandResizeType(config.automaticResizeType());

		ContainableFrame.Mode containMode = config.containInScreen();
		if (containMode == ContainableFrame.Mode.ALWAYS && !withTitleBar)
		{
			// When native window decorations are enabled we don't have a way to receive window move events
			// so we can't contain to screen always.
			containMode = ContainableFrame.Mode.RESIZING;
		}
		frame.setContainedInScreen(containMode);

		if (!config.rememberScreenBounds())
		{
			configManager.unsetConfiguration(CONFIG_GROUP, CONFIG_CLIENT_MAXIMIZED);
			configManager.unsetConfiguration(CONFIG_GROUP, CONFIG_CLIENT_BOUNDS);
		}

		if (client == null)
		{
			return;
		}

		// The upper bounds are defined by the applet's max size
		// The lower bounds are defined by the client's fixed size
		int width = Math.max(Math.min(config.gameSize().width, 7680), Constants.GAME_FIXED_WIDTH);
		int height = Math.max(Math.min(config.gameSize().height, 2160), Constants.GAME_FIXED_HEIGHT);
		final Dimension size = new Dimension(width, height);

		if (!size.equals(lastClientSize))
		{
			lastClientSize = size;
			client.setSize(size);
			client.setPreferredSize(size);
			client.getParent().setPreferredSize(size);
			client.getParent().setSize(size);

			if (frame.isVisible())
			{
				frame.pack();
			}
		}
	}

	private void saveClientBoundsConfig()
	{
		if (frame == null || !windowPersistenceReady || applyingStoredBounds || safeMode || !config.rememberScreenBounds())
		{
			return;
		}

		boolean maximized = (frame.getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH;
		boolean iconified = (frame.getExtendedState() & JFrame.ICONIFIED) == JFrame.ICONIFIED;
		if (maximized)
		{
			configManager.setConfiguration(CONFIG_GROUP, CONFIG_CLIENT_MAXIMIZED, true);
			if (lastNormalClientBounds != null)
			{
				configManager.setConfiguration(CONFIG_GROUP, CONFIG_CLIENT_BOUNDS, lastNormalClientBounds);
			}
			return;
		}

		configManager.unsetConfiguration(CONFIG_GROUP, CONFIG_CLIENT_MAXIMIZED);
		if (iconified)
		{
			return;
		}

		final Rectangle bounds = frame.getBounds();
		if (bounds.width <= 0 || bounds.height <= 0)
		{
			return;
		}

		Rectangle normalBounds = new Rectangle(bounds);
		if (config.automaticResizeType() == ExpandResizeType.KEEP_GAME_SIZE)
		{
			// Try to contract plugin panel
			if (pluginPanel != null)
			{
				normalBounds.width -= pluginPanel.getWrappedPanel().getPreferredSize().width;
			}
		}

		lastNormalClientBounds = new Rectangle(normalBounds);
		configManager.setConfiguration(CONFIG_GROUP, CONFIG_CLIENT_BOUNDS, normalBounds);
	}
}

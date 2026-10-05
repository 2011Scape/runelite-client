/*
 * Copyright (c) 2017, Kronos <https://github.com/KronosDesign>
 * Copyright (c) 2017, Adam <Adam@sigterm.info>
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
package net.runelite.client.plugins.devtools;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import javax.inject.Inject;
import javax.inject.Singleton;


import com.GameClient;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;

@Singleton
class DevToolsOverlay extends Overlay
{
	private static final Font FONT = FontManager.getRunescapeFont().deriveFont(Font.BOLD, 16);
	private static final Color RED = new Color(221, 44, 0);
	private static final Color GREEN = new Color(0, 200, 83);
	private static final Color ORANGE = new Color(255, 109, 0);
	private static final Color YELLOW = new Color(255, 214, 0);
	private static final Color CYAN = new Color(0, 184, 212);
	private static final Color BLUE = new Color(41, 98, 255);
	private static final Color DEEP_PURPLE = new Color(98, 0, 234);
	private static final Color PURPLE = new Color(170, 0, 255);
	private static final Color GRAY = new Color(158, 158, 158);

	private static final int MAX_DISTANCE = 2400;

	private final GameClient client;
	private final DevToolsPlugin plugin;
	private final TooltipManager toolTipManager;

	@Inject
	private DevToolsOverlay(GameClient client, DevToolsPlugin plugin, TooltipManager toolTipManager)
	{
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		setPriority(OverlayPriority.HIGHEST);
		this.client = client;
		this.plugin = plugin;
		this.toolTipManager = toolTipManager;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		graphics.setFont(FONT);

		if (plugin.getPlayers().isActive())
		{
			renderPlayers(graphics);
		}

		if (plugin.getNpcs().isActive())
		{
			renderNpcs(graphics);
		}

		if (plugin.getGroundItems().isActive() || plugin.getGroundObjects().isActive() || plugin.getGameObjects().isActive() || plugin.getWalls().isActive() || plugin.getDecorations().isActive() || plugin.getTileLocation().isActive() || plugin.getMovementFlags().isActive())
		{
			renderTileObjects(graphics);
		}

		if (plugin.getInventory().isActive())
		{
			renderInventory(graphics);
		}

		if (plugin.getProjectiles().isActive())
		{
			renderProjectiles(graphics);
		}

		if (plugin.getGraphicsObjects().isActive())
		{
			renderGraphicsObjects(graphics);
		}

		if (plugin.getRoofs().isActive())
		{
			renderRoofs(graphics);
		}

		renderMinimapDiagnostics(graphics);

		return null;
	}

	/**
	 * The one line that explains a minimap that is not showing its own current image.
	 * <p>
	 * The map has no packet behind it - it is a render of the scene the client keeps -
	 * so when it is showing the last good image instead of a fresh one, or the region is
	 * still loading, there is nothing anywhere else in the client to look at. It is
	 * drawn only while that is true, so a healthy client has nothing here.
	 */
	private void renderMinimapDiagnostics(Graphics2D graphics)
	{
		String diagnostics = client.getMinimapDiagnostics();
		if (diagnostics == null)
		{
			return;
		}

		int height = graphics.getFontMetrics().getHeight();
		int width = graphics.getFontMetrics().stringWidth(diagnostics);
		graphics.setColor(new Color(0, 0, 0, 160));
		graphics.fillRect(0, 0, width + 8, height + 4);
		graphics.setColor(ORANGE);
		graphics.drawString(diagnostics, 4, height);
	}

	private void renderRoofs(Graphics2D graphics)
	{
	/*	WorldController scene = client.getScene();
		Tile[][][] tiles = scene.getTiles();
		byte[][][] settings = client.getTileSettings();
		int z = client.getPlane();
		String text = "R";

		for (int x = 0; x < Constants.SCENE_SIZE; ++x)
		{
			for (int y = 0; y < Constants.SCENE_SIZE; ++y)
			{
				Tile tile = tiles[z][x][y];

				if (tile == null)
				{
					continue;
				}

				int flag = settings[z][x][y];
				if ((flag & Constants.TILE_FLAG_UNDER_ROOF) == 0)
				{
					continue;
				}

				Point loc = Perspective.getCanvasTextLocation(client, graphics, tile.getLocalLocation(), text, z);
				if (loc == null)
				{
					continue;
				}

				OverlayUtil.renderTextLocation(graphics, loc, text, Color.RED);
			}
		}*/
	}

	private void renderPlayers(Graphics2D graphics)
	{
	/*	List<Player> players = client.getPlayers();
		Player local = client.getLocalPlayer();

		for (Player p : players)
		{
			if (p != local)
			{
				String text = p.getName() + " (A: " + p.getAnimation() + ") (P: " + p.getPoseAnimation() + ") (G: " + p.getGraphic() + ")";
				OverlayUtil.renderActorOverlay(graphics, p, text, BLUE);
			}
		}

		String text = local.getName() + " (A: " + local.getAnimation() + ") (P: " + local.getPoseAnimation() + ") (G: " + local.getGraphic() + ")";
		OverlayUtil.renderActorOverlay(graphics, local, text, CYAN);*/
	}

	private void renderNpcs(Graphics2D graphics)
	{
	/*	List<NPC> npcs = client.getNpcs();
		for (NPC npc : npcs)
		{
			NPCType composition = npc.getComposition();
			Color color = composition.getCombatLevel() > 1 ? YELLOW : ORANGE;
			if (composition.getConfigs() != null)
			{
				NPCType transformedComposition = composition.transform();
				if (transformedComposition == null)
				{
					color = GRAY;
				}
				else
				{
					composition = transformedComposition;
				}
			}

			String text = composition.getName() + " (ID:" + composition.getId() + ")" +
				" (A: " + npc.getAnimation() + ") (P: " + npc.getPoseAnimation() + ") (G: " + npc.getGraphic() + ")";
			OverlayUtil.renderActorOverlay(graphics, npc, text, color);
		}*/
	}

	private void renderTileObjects(Graphics2D graphics)
	{
	/*	WorldController scene = client.getScene();
		Tile[][][] tiles = scene.getTiles();

		int z = client.getPlane();

		for (int x = 0; x < Constants.SCENE_SIZE; ++x)
		{
			for (int y = 0; y < Constants.SCENE_SIZE; ++y)
			{
				Tile tile = tiles[z][x][y];

				if (tile == null)
				{
					continue;
				}

				Player player = client.getLocalPlayer();
				if (player == null)
				{
					continue;
				}

				if (plugin.getGroundItems().isActive())
				{
					renderGroundItems(graphics, tile, player);
				}

				if (plugin.getGroundObjects().isActive())
				{
					renderTileObject(graphics, tile.getGroundObject(), player, PURPLE);
				}

				if (plugin.getGameObjects().isActive())
				{
					renderGameObjects(graphics, tile, player);
				}

				if (plugin.getWalls().isActive())
				{
					renderTileObject(graphics, tile.getWallObject(), player, GRAY);
				}

				if (plugin.getDecorations().isActive())
				{
					renderDecorObject(graphics, tile, player);
				}

				if (plugin.getTileLocation().isActive())
				{
					renderTileTooltip(graphics, tile);
				}

				if (plugin.getMovementFlags().isActive())
				{
					renderMovementInfo(graphics, tile);
				}
			}
		}*/
	}

	private void renderTileTooltip(Graphics2D graphics, Object tile)
	{
	/*	Polygon poly = Perspective.getCanvasTilePoly(client, tile.getLocalLocation());
		if (poly != null && poly.contains(client.getMouseCanvasPosition().getX(), client.getMouseCanvasPosition().getY()))
		{
			WorldPoint worldLocation = tile.getWorldLocation();
			String tooltip = String.format("World location: %d, %d, %d</br>" +
					"Region ID: %d location: %d, %d", worldLocation.getX(), worldLocation.getY(), worldLocation.getPlane(),
				worldLocation.getRegionID(), worldLocation.getRegionX(), worldLocation.getRegionY());
			toolTipManager.add(new Tooltip(tooltip));
			OverlayUtil.renderPolygon(graphics, poly, GREEN);
		}*/
	}

	private void renderMovementInfo(Graphics2D graphics, Object tile)
	{
	/*	Polygon poly = Perspective.getCanvasTilePoly(client, tile.getLocalLocation());

		if (poly == null || !poly.contains(client.getMouseCanvasPosition().getX(), client.getMouseCanvasPosition().getY()))
		{
			return;
		}

		if (client.getCollisionMaps() != null)
		{
			int[][] flags = client.getCollisionMaps()[client.getPlane()].getFlags();
			int data = flags[tile.getSceneLocation().getX()][tile.getSceneLocation().getY()];

			Set<MovementFlag> movementFlags = MovementFlag.getSetFlags(data);

			if (movementFlags.isEmpty())
			{
				toolTipManager.add(new Tooltip("No movement flags"));
			}
			else
			{
				movementFlags.forEach(flag -> toolTipManager.add(new Tooltip(flag.toString())));
			}

			OverlayUtil.renderPolygon(graphics, poly, GREEN);
		}*/
	}

	private void renderGroundItems(Graphics2D graphics, Object tile, Object player)
	{
		/*GroundObjEntity itemLayer = tile.getItemLayer();
		if (itemLayer != null)
		{
			if (player.getLocalLocation().distanceTo(itemLayer.getLocalLocation()) <= MAX_DISTANCE)
			{
				Linkable current = itemLayer.getBottom();
				while (current instanceof TileItem)
				{
					TileItem item = (TileItem) current;
					OverlayUtil.renderTileOverlay(graphics, itemLayer, "ID: " + item.getId() + " Qty:" + item.getQuantity(), RED);
					current = current.getNext();
				}
			}
		}*/
	}

	private void renderGameObjects(Graphics2D graphics, Object tile, Object player)
	{
	/*	InteractiveEntity[] gameObjects = tile.getGameObjects();
		if (gameObjects != null)
		{
			for (InteractiveEntity gameObject : gameObjects)
			{
				if (gameObject != null && gameObject.getSceneMinLocation().equals(tile.getSceneLocation()))
				{
					renderTileObject(graphics, gameObject, player, GREEN);
				}
			}
		}*/
	}

	private void renderTileObject(Graphics2D graphics, Object tileObject, Object player, Color color)
	{
		/* (tileObject != null)
		{
			if (player.getLocalLocation().distanceTo(tileObject.getLocalLocation()) <= MAX_DISTANCE)
			{
				OverlayUtil.renderTileOverlay(graphics, tileObject, "ID: " + tileObject.getId(), color);
			}
		}*/
	}

	private void renderDecorObject(Graphics2D graphics, Object tile, Object player)
	{
		/*WallDecoration decorObject = tile.getDecorativeObject();
		if (decorObject != null)
		{
			if (player.getLocalLocation().distanceTo(decorObject.getLocalLocation()) <= MAX_DISTANCE)
			{
				OverlayUtil.renderTileOverlay(graphics, decorObject, "ID: " + decorObject.getId(), DEEP_PURPLE);
			}

			Shape p = decorObject.getConvexHull();
			if (p != null)
			{
				graphics.draw(p);
			}

			p = decorObject.getConvexHull2();
			if (p != null)
			{
				graphics.draw(p);
			}
		}*/
	}

	private void renderInventory(Graphics2D graphics)
	{
//		Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
//		if (inventoryWidget == null || inventoryWidget.isHidden())
//		{
//			return;
//		}
//
//		for (WidgetItem item : inventoryWidget.getWidgetItems())
//		{
//			Rectangle slotBounds = item.getCanvasBounds();
//
//			String idText = "" + item.getId();
//			FontMetrics fm = graphics.getFontMetrics();
//			Rectangle2D textBounds = fm.getStringBounds(idText, graphics);
//
//			int textX = (int) (slotBounds.getX() + (slotBounds.getWidth() / 2) - (textBounds.getWidth() / 2));
//			int textY = (int) (slotBounds.getY() + (slotBounds.getHeight() / 2) + (textBounds.getHeight() / 2));
//
//			graphics.setColor(new Color(255, 255, 255, 65));
//			graphics.fill(slotBounds);
//
//			graphics.setColor(Color.BLACK);
//			graphics.drawString(idText, textX + 1, textY + 1);
//			graphics.setColor(YELLOW);
//			graphics.drawString(idText, textX, textY);
//		}
	}

	private void renderProjectiles(Graphics2D graphics)
	{
/*		List<ProjectileEntity> projectiles = client.getProjectiles();

		for (ProjectileEntity projectile : projectiles)
		{
			int projectileId = projectile.getId();
			String text = "(ID: " + projectileId + ")";
			int x = (int) projectile.getX();
			int y = (int) projectile.getY();
			LocalPoint projectilePoint = new LocalPoint(x, y);
			Point textLocation = Perspective.getCanvasTextLocation(client, graphics, projectilePoint, text, 0);
			if (textLocation != null)
			{
				OverlayUtil.renderTextLocation(graphics, textLocation, text, Color.RED);
			}
		}*/
	}

	private void renderGraphicsObjects(Graphics2D graphics)
	{
	/*	List<SpotanimEntity> graphicsObjects = client.getGraphicsObjects();

		for (SpotanimEntity graphicsObject : graphicsObjects)
		{
			LocalPoint lp = graphicsObject.getLocation();
			Polygon poly = Perspective.getCanvasTilePoly(client, lp);

			if (poly != null)
			{
				OverlayUtil.renderPolygon(graphics, poly, Color.MAGENTA);
			}

			String infoString = "(ID: " + graphicsObject.getId() + ")";
			Point textLocation = Perspective.getCanvasTextLocation(
				client, graphics, lp, infoString, 0);
			if (textLocation != null)
			{
				OverlayUtil.renderTextLocation(graphics, textLocation, infoString, Color.WHITE);
			}
		}*/
	}
}

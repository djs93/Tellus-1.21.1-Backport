package com.yucareux.tellus.client.widget.map;

import com.yucareux.tellus.client.widget.map.component.MapComponent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class SlippyMapWidget extends AbstractWidget {
	private static final String ATTRIBUTION = "(c) OpenStreetMap Contributors";

	private final SlippyMap map;
	private final List<MapComponent> components = new ArrayList<>();

	private boolean mouseDown;
	private boolean mouseDragged;
	private int attributionBottomPadding;

	public SlippyMapWidget(int x, int y, int width, int height) {
		super(x, y, width, height, Component.empty());
		this.map = new SlippyMap(width, height);
	}

	public SlippyMap getMap() {
		return this.map;
	}

	public <T extends MapComponent> T addComponent(T component) {
		this.components.add(component);
		return component;
	}

	public void setAttributionBottomPadding(int padding) {
		this.attributionBottomPadding = Math.max(0, padding);
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		this.drawBackground(graphics);

		graphics.enableScissor(this.getX() + 4, this.getY() + 4, this.getX() + this.width - 4,
				this.getY() + this.height - 4);

		int cameraX = this.map.getCameraX();
		int cameraY = this.map.getCameraY();
		int cameraZoom = this.map.getCameraZoom();

		List<SlippyMapTilePos> tiles = this.map.getVisibleTiles();
		List<SlippyMapTilePos> cascadedTiles = this.map.cascadeTiles(tiles);
		cascadedTiles.sort(Comparator.comparingInt(SlippyMapTilePos::getZoom));

		for (SlippyMapTilePos pos : cascadedTiles) {
			SlippyMapTile tile = this.map.getTile(pos);
			this.renderTile(graphics, cameraX, cameraY, cameraZoom, pos, tile, delta);
		}

		SlippyMapPoint mouse = this.getPointUnderMouse(mouseX, mouseY);
		graphics.pose().pushPose();
		graphics.pose().translate((float) this.getX(), (float) this.getY(), 0);
		for (MapComponent component : this.components) {
			component.onDrawMap(this.map, graphics, mouseX, mouseY, mouse);
		}
		graphics.pose().popPose();

		graphics.disableScissor();

		int maxX = this.getX() + this.width - 4;
		int maxY = this.getY() + this.height - 4 - this.attributionBottomPadding;
		int attributionWidth = Minecraft.getInstance().font.width(ATTRIBUTION) + 20;
		int attributionOriginX = maxX - attributionWidth;
		int attributionOriginY = maxY - Minecraft.getInstance().font.lineHeight - 4;
		graphics.fill(attributionOriginX, attributionOriginY, maxX, maxY, 0xC0101010);
		graphics.drawString(Minecraft.getInstance().font, ATTRIBUTION, attributionOriginX + 10, attributionOriginY + 2,
				0xFFFFFFFF);

	}

	private void renderTile(
			GuiGraphics graphics,
			int cameraX,
			int cameraY,
			int cameraZoom,
			SlippyMapTilePos pos,
			SlippyMapTile image,
			float delta
	) {
		image.update(delta);

		ResourceLocation location = image.getLocation();
		if (location != null) {
			int deltaZoom = cameraZoom - pos.getZoom();
			double zoomScale = Math.pow(2.0, deltaZoom);
			int size = Mth.floor(SlippyMap.TILE_SIZE * zoomScale);
			int renderX = (pos.getX() << deltaZoom) * SlippyMap.TILE_SIZE - cameraX;
			int renderY = (pos.getY() << deltaZoom) * SlippyMap.TILE_SIZE - cameraY;
			int textureSize = Math.max(SlippyMap.TILE_SIZE, size);

			graphics.pose().pushPose();
			graphics.pose().translate((float) this.getX(), (float) this.getY(), 0);
			int scaleFactor = Math.max(1, (int) Math.round(Minecraft.getInstance().getWindow().getGuiScale()));
			float scale = 1.0F / scaleFactor;
			graphics.pose().scale(scale, scale, 1.0F);

			graphics.blit(Objects.requireNonNull(location, "tileLocation"), renderX,
					renderY, 0.0F, 0.0F, size, size,
					textureSize, textureSize);

			graphics.pose().popPose();
		}
	}

	private void drawBackground(GuiGraphics graphics) {
		graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0xFF202020);
		graphics.renderOutline(this.getX(), this.getY(), this.width, this.height, 0xFF000000);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (this.isMouseOver(mouseX, mouseY)) {
			this.mouseDown = true;

			if (button == 0) {
				SlippyMapPoint mouse = this.getPointUnderMouse(mouseX, mouseY);
				for (MapComponent component : this.components) {
					if (component.onMouseClicked(this.map, mouse, button)) {
						return true;
					}
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (this.mouseDown) {
			this.map.drag((int) -dragX, (int) -dragY);
			this.mouseDragged = true;
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (button == 0) {
			if (this.mouseDown && !this.mouseDragged && this.isMouseOver(mouseX, mouseY)) {
				SlippyMapPoint mouse = this.getPointUnderMouse(mouseX, mouseY);
				for (MapComponent component : this.components) {
					if (component.onMouseReleased(this.map, mouse, button)) {
						return true;
					}
				}
			}
		}

		this.mouseDown = false;
		this.mouseDragged = false;
		return false;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (this.isMouseOver(mouseX, mouseY)) {
			int zoom = (int) Math.signum(scrollY);
			if (zoom != 0) {
				this.map.zoom(zoom, (int) (mouseX - this.getX()), (int) (mouseY - this.getY()));
			}
			return true;
		}
		return false;
	}

	public void close() {
		this.map.shutdown();
	}

	private SlippyMapPoint getPointUnderMouse(double mouseX, double mouseY) {
		int scale = Math.max(1, (int) Minecraft.getInstance().getWindow().getGuiScale());
		int mapX = (int) ((mouseX - this.getX()) * scale) + this.map.getCameraX();
		int mapY = (int) ((mouseY - this.getY()) * scale) + this.map.getCameraY();
		return new SlippyMapPoint(mapX, mapY, this.map.getCameraZoom());
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput narration) {
	}
}

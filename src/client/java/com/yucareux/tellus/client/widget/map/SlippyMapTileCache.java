package com.yucareux.tellus.client.widget.map;

import com.yucareux.tellus.Tellus;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import net.minecraft.client.Minecraft;

public class SlippyMapTileCache {
	private static final int CACHE_SIZE = 1024;

	private final ExecutorService loadingService = Executors.newFixedThreadPool(4, new ThreadFactoryBuilder()
			.setDaemon(true)
			.setNameFormat("tellus-map-load-%d")
			.build());

	private final Queue<InputStream> loadingStreams = new LinkedBlockingQueue<>();
	private final Path cacheRoot;
	private final LoadingCache<SlippyMapTilePos, SlippyMapTile> tileCache;

	public SlippyMapTileCache() {
		this.cacheRoot = Minecraft.getInstance().gameDirectory.toPath().resolve("tellus/cache/map");
		this.tileCache = CacheBuilder.newBuilder()
				.maximumSize(CACHE_SIZE)
				.removalListener(notification -> {
					SlippyMapTile tile = (SlippyMapTile) notification.getValue();
					if (tile != null) {
						tile.delete();
					}
				})
				.build(new CacheLoader<>() {
					@Override
					public SlippyMapTile load(SlippyMapTilePos key) {
						SlippyMapTile tile = new SlippyMapTile(key);
						SlippyMapTileCache.this.loadingService
								.submit(() -> tile.supplyImage(SlippyMapTileCache.this.downloadImage(key)));
						return tile;
					}
				});
	}

	public SlippyMapTile getTile(SlippyMapTilePos pos) {
		try {
			return this.tileCache.get(pos);
		} catch (Exception e) {
			SlippyMapTile tile = new SlippyMapTile(pos);
			tile.supplyImage(this.createErrorImage());
			return tile;
		}
	}

	public void shutdown() {
		for (SlippyMapTile tile : this.tileCache.asMap().values()) {
			tile.delete();
		}

		this.tileCache.invalidateAll();
		this.loadingService.shutdown();

		while (!this.loadingStreams.isEmpty()) {
			try {
				InputStream poll = this.loadingStreams.poll();
				if (poll != null) {
					poll.close();
				}
			} catch (IOException e) {
				Tellus.LOGGER.warn("Failed to close loading map stream", e);
			}
		}
	}

	private NativeImage downloadImage(SlippyMapTilePos pos) {
		try (InputStream input = Objects.requireNonNull(this.getStream(pos), "tileStream")) {
			return NativeImage.read(input);
		} catch (IOException e) {
			Tellus.LOGGER.error("Failed to load map tile {}", e.getClass().getName());
		}
		return this.createErrorImage();
	}

	private InputStream getStream(SlippyMapTilePos pos) throws IOException {
		Path cachePath = this.cacheRoot.resolve(pos.getCacheName());
		if (Files.exists(cachePath)) {
			return new BufferedInputStream(Files.newInputStream(cachePath));
		}

		URI uri = URI.create(
				String.format("https://tile.openstreetmap.org/%s/%s/%s.png", pos.getZoom(), pos.getX(), pos.getY()));
		URL url = uri.toURL();
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setConnectTimeout(5000);
		connection.setReadTimeout(5000);
		connection.setRequestProperty("User-Agent", "Tellus/2.0.0 (Minecraft Mod)");
		InputStream stream = connection.getInputStream();
		this.loadingStreams.add(stream);
		try (InputStream input = new BufferedInputStream(stream)) {
			byte[] data = input.readAllBytes();
			this.cacheData(cachePath, data);
			this.loadingStreams.remove(stream);
			return new ByteArrayInputStream(data);
		}
	}

	private void cacheData(Path cachePath, byte[] data) {
		try {
			Files.createDirectories(this.cacheRoot);
		} catch (IOException e) {
			Tellus.LOGGER.error("Failed to create cache root", e);
		}

		try (OutputStream output = Files.newOutputStream(cachePath)) {
			output.write(data);
		} catch (IOException e) {
			Tellus.LOGGER.error("Failed to cache map tile", e);
		}
	}

	private NativeImage createErrorImage() {
		NativeImage result = new NativeImage(SlippyMap.TILE_SIZE, SlippyMap.TILE_SIZE, false);
		for (int x = 0; x < SlippyMap.TILE_SIZE; x++) {
			for (int y = 0; y < SlippyMap.TILE_SIZE; y++) {
				result.setPixelRGBA(x, y, 0xFF0000FF);
			}
		}
		return result;
	}
}

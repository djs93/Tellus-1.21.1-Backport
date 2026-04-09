package com.yucareux.tellus.world.data.elevation;

import java.util.Objects;

record NormalizedElevationTile(NormalizedElevationTileKey key, ShortRaster heights, TellusElevationProvenance provenance) {
   NormalizedElevationTile {
      Objects.requireNonNull(key, "key");
      Objects.requireNonNull(heights, "heights");
      Objects.requireNonNull(provenance, "provenance");
      if (heights.width() != NormalizedElevationTileKey.TILE_SIZE || heights.height() != NormalizedElevationTileKey.TILE_SIZE) {
         throw new IllegalArgumentException("Unexpected normalized tile dimensions");
      }

      if (provenance.width() != NormalizedElevationTileKey.TILE_SIZE || provenance.height() != NormalizedElevationTileKey.TILE_SIZE) {
         throw new IllegalArgumentException("Unexpected normalized provenance dimensions");
      }
   }
}

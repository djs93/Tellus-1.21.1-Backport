package com.yucareux.tellus.worldgen.building;

public final class BuildingPlacementSupport {
   private BuildingPlacementSupport() {
   }

   public static int capLowerColumnTopY(int floorY, int columnTopY, int overlyingFloorY) {
      if (overlyingFloorY == Integer.MAX_VALUE) {
         return columnTopY;
      } else if (overlyingFloorY <= floorY) {
         return Integer.MIN_VALUE;
      } else {
         return Math.min(columnTopY, overlyingFloorY - 1);
      }
   }
}

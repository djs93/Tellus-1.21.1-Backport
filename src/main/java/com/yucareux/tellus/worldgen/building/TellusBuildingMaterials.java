package com.yucareux.tellus.worldgen.building;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;

public final class TellusBuildingMaterials {
   private static final BlockState BUILDING_STATE = Blocks.GRAY_CONCRETE.defaultBlockState();
   private static final BlockState BUILDING_WINDOW_STATE = Blocks.LIGHT_GRAY_STAINED_GLASS.defaultBlockState();
   private static final BlockState BUILDING_TOWER_WINDOW_STATE = Blocks.LIGHT_BLUE_STAINED_GLASS.defaultBlockState();
   private static final BlockState BUILDING_ROOF_STATE = Blocks.GRAY_CONCRETE.defaultBlockState();
   private static final BlockState BUILDING_SLATE_ROOF_STATE = Blocks.DEEPSLATE_TILES.defaultBlockState();
   private static final BlockState BUILDING_CLAY_TILE_ROOF_STATE = Blocks.BRICKS.defaultBlockState();
   private static final BlockState BUILDING_STONE_ROOF_STATE = Blocks.STONE_BRICKS.defaultBlockState();
   private static final BlockState BUILDING_RESIDENTIAL_WALL_STATE = Blocks.WHITE_TERRACOTTA.defaultBlockState();
   private static final BlockState BUILDING_ARID_WALL_STATE = Blocks.SANDSTONE.defaultBlockState();
   private static final BlockState BUILDING_SANDSTONE_WALL_STATE = Blocks.SMOOTH_SANDSTONE.defaultBlockState();
   private static final BlockState BUILDING_COLD_WALL_STATE = Blocks.LIGHT_GRAY_TERRACOTTA.defaultBlockState();
   private static final BlockState BUILDING_TROPICAL_WALL_STATE = Blocks.BIRCH_PLANKS.defaultBlockState();
   private static final BlockState BUILDING_BRICK_WALL_STATE = Blocks.BRICKS.defaultBlockState();
   private static final BlockState BUILDING_PALE_STONE_WALL_STATE = Blocks.CALCITE.defaultBlockState();
   private static final BlockState BUILDING_COMMERCIAL_WALL_STATE = Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState();
   private static final BlockState BUILDING_INDUSTRIAL_WALL_STATE = Blocks.ANDESITE.defaultBlockState();
   private static final BlockState BUILDING_TOWER_WALL_STATE = Blocks.CYAN_TERRACOTTA.defaultBlockState();
   private static final BlockState BUILDING_TRIM_STATE = Blocks.POLISHED_ANDESITE.defaultBlockState();
   private static final BlockState BUILDING_WHITE_TRIM_STATE = Blocks.SMOOTH_QUARTZ.defaultBlockState();
   private static final BlockState BUILDING_SANDSTONE_TRIM_STATE = Blocks.CUT_SANDSTONE.defaultBlockState();
   private static final BlockState BUILDING_BRICK_TRIM_STATE = Blocks.STONE_BRICKS.defaultBlockState();
   private static final BlockState BUILDING_FLOOR_STATE = Blocks.POLISHED_ANDESITE.defaultBlockState();
   private static final BlockState BUILDING_RESIDENTIAL_FLOOR_STATE = Blocks.OAK_PLANKS.defaultBlockState();
   private static final BlockState BUILDING_PARTITION_STATE = Blocks.SMOOTH_STONE.defaultBlockState();
   private static final BlockState BUILDING_STAIR_STATE = Blocks.OAK_STAIRS.defaultBlockState();
   private static final BlockState BUILDING_COMMERCIAL_STAIR_STATE = Blocks.STONE_BRICK_STAIRS.defaultBlockState();
   private static final BlockState BUILDING_SLAB_STATE = Blocks.SMOOTH_STONE_SLAB.defaultBlockState();
   private static final BlockState BUILDING_RESIDENTIAL_SLAB_STATE = Blocks.OAK_SLAB.defaultBlockState();
   private static final BlockState BUILDING_LIGHT_STATE = Blocks.SEA_LANTERN.defaultBlockState();

   private TellusBuildingMaterials() {
   }

   public static TellusBuildingMaterials.BuildingMaterialPalette resolvePalette(BuildingBlueprint blueprint) {
      return resolvePalette(blueprint.profile(), blueprint.blueprintSeed());
   }

   public static TellusBuildingMaterials.BuildingMaterialPalette resolvePalette(BuildingProfile profile, long blueprintSeed) {
      BlockState wall = switch (profile.archetype()) {
         case HOUSE, APARTMENT -> switch (profile.climateFamily()) {
            case COLD -> BUILDING_COLD_WALL_STATE;
            case ARID -> BUILDING_ARID_WALL_STATE;
            case TROPICAL -> BUILDING_TROPICAL_WALL_STATE;
            case TEMPERATE -> BUILDING_RESIDENTIAL_WALL_STATE;
         };
         case COMMERCIAL -> BUILDING_COMMERCIAL_WALL_STATE;
         case INDUSTRIAL -> BUILDING_INDUSTRIAL_WALL_STATE;
         case TOWER -> BUILDING_TOWER_WALL_STATE;
         case GENERIC -> BUILDING_STATE;
      };
      BlockState trim = BUILDING_TRIM_STATE;
      BlockState roof = BUILDING_ROOF_STATE;
      if (profile.archetype() == BuildingProfile.Archetype.HOUSE) {
         TellusBuildingStyles.HouseStyle houseStyle = TellusBuildingStyles.resolveHouseStyle(profile, blueprintSeed);
         wall = switch (houseStyle) {
            case WHITE_SLATE -> BUILDING_RESIDENTIAL_WALL_STATE;
            case WARM_CLAY -> BUILDING_SANDSTONE_WALL_STATE;
            case GRAY_CHARCOAL -> BUILDING_COLD_WALL_STATE;
            case BRICK_SLATE -> BUILDING_BRICK_WALL_STATE;
            case PALE_STONE -> BUILDING_PALE_STONE_WALL_STATE;
         };
         trim = switch (houseStyle) {
            case WHITE_SLATE -> BUILDING_WHITE_TRIM_STATE;
            case WARM_CLAY -> BUILDING_SANDSTONE_TRIM_STATE;
            case GRAY_CHARCOAL -> BUILDING_TRIM_STATE;
            case BRICK_SLATE, PALE_STONE -> BUILDING_BRICK_TRIM_STATE;
         };
         roof = switch (houseStyle) {
            case WHITE_SLATE, BRICK_SLATE -> BUILDING_SLATE_ROOF_STATE;
            case WARM_CLAY -> BUILDING_CLAY_TILE_ROOF_STATE;
            case GRAY_CHARCOAL -> BUILDING_ROOF_STATE;
            case PALE_STONE -> BUILDING_STONE_ROOF_STATE;
         };
      }

      BlockState floor = switch (profile.archetype()) {
         case HOUSE, APARTMENT -> BUILDING_RESIDENTIAL_FLOOR_STATE;
         default -> BUILDING_FLOOR_STATE;
      };
      BlockState stair = switch (profile.archetype()) {
         case HOUSE, APARTMENT -> BUILDING_STAIR_STATE;
         default -> BUILDING_COMMERCIAL_STAIR_STATE;
      };
      BlockState slab = switch (profile.archetype()) {
         case HOUSE, APARTMENT -> BUILDING_RESIDENTIAL_SLAB_STATE.setValue(BlockStateProperties.SLAB_TYPE, SlabType.BOTTOM);
         default -> BUILDING_SLAB_STATE.setValue(BlockStateProperties.SLAB_TYPE, SlabType.BOTTOM);
      };
      BlockState window = profile.archetype() == BuildingProfile.Archetype.TOWER || profile.archetype() == BuildingProfile.Archetype.COMMERCIAL
         ? BUILDING_TOWER_WINDOW_STATE
         : BUILDING_WINDOW_STATE;
      return new TellusBuildingMaterials.BuildingMaterialPalette(wall, trim, roof, window, floor, BUILDING_PARTITION_STATE, stair, slab, BUILDING_LIGHT_STATE);
   }

   public static BlockState resolveLodFacadeBlock(
      BuildingBlueprint blueprint, TellusBuildingMaterials.BuildingMaterialPalette palette, int boundaryDistance, int floorIndex
   ) {
      if (!blueprint.isFacadeCell(boundaryDistance, floorIndex)) {
         return palette.wall();
      }

      return switch (blueprint.profile().archetype()) {
         case TOWER, COMMERCIAL -> palette.window();
         case APARTMENT -> floorIndex > 0 ? palette.window() : palette.wall();
         case HOUSE, INDUSTRIAL, GENERIC -> palette.wall();
      };
   }

   public static BlockState resolveLodRoofBlock(TellusBuildingMaterials.BuildingMaterialPalette palette, boolean roofEdge) {
      return roofEdge ? palette.trim() : palette.roof();
   }

   public record BuildingMaterialPalette(
      BlockState wall,
      BlockState trim,
      BlockState roof,
      BlockState window,
      BlockState floor,
      BlockState partition,
      BlockState stair,
      BlockState slab,
      BlockState light
   ) {
   }
}

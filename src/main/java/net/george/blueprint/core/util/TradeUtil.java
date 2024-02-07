package net.george.blueprint.core.util;

import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerProfession;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Random;

/**
 * A utility class containing some useful stuff for village trades.
 *
 * @author bageldotjpg
 */
@SuppressWarnings("unused")
@ApiStatus.NonExtendable
public final class TradeUtil {
    public static final int NOVICE = 1;
    public static final int APPRENTICE = 2;
    public static final int JOURNEYMAN = 3;
    public static final int EXPERT = 4;
    public static final int MASTER = 5;

    /**
     * Adds an array of {@link TradeOffers.Factory}s for a specific profession and level.
     *
     * @param profession     A {@link VillagerProfession} to target.
     * @param level          The level for the trades.
     * @param tradeFactories An array of {@link TradeOffers.Factory}s.
     */
    public static void addVillagerTrades(VillagerProfession profession, int level, TradeOffers.Factory... tradeFactories) {
        TradeOfferHelper.registerVillagerOffers(profession, level, factories -> factories.addAll(Arrays.asList(tradeFactories)));
    }

    /**
     * Adds an array of {@link TradeOffers.Factory}s to wanderer trade list.
     *
     * @param tradeFactories An array of {@link TradeOffers.Factory}s to add.
     */
    public static void addWandererTrades(TradeOffers.Factory... tradeFactories) {
        TradeOfferHelper.registerWanderingTraderOffers(1, factories -> factories.addAll(Arrays.asList(tradeFactories)));
    }

    /**
     * Adds an array of {@link TradeOffers.Factory}s for a specific profession and level only if a given Mod ID is loaded.
     *
     * @param modid          A Mod ID to check.
     * @param profession     A {@link VillagerProfession} to target.
     * @param level          The level for the trades.
     * @param tradeFactories An array of {@link TradeOffers.Factory}s.
     */
    public static void addCompatVillagerTrades(String modid, VillagerProfession profession, int level, TradeOffers.Factory... tradeFactories) {
        if (FabricLoader.getInstance().isModLoaded(modid)) {
            addVillagerTrades(profession, level, tradeFactories);
        }
    }

    /**
     * Adds an array of {@link TradeOffers.Factory}s to wanderer trade list only if a given Mod ID is loaded.
     *
     * @param modid  A Mod ID to check.
     * @param trades An array of {@link TradeOffers.Factory}s to add.
     */
    public static void addCompatWandererTrades(String modid, TradeOffers.Factory... trades) {
        if (FabricLoader.getInstance().isModLoaded(modid)) {
            addWandererTrades(trades);
        }
    }

    /**
     * A {@link BasicTradeFactory} extension offering more constructors.
     *
     * @author bageldotjpg
     */
    public static class BlueprintTrade extends BasicTradeFactory  {
        public BlueprintTrade(ItemStack input, ItemStack input2, ItemStack output, int maxTrades, int experience, float priceMultiply) {
            super(input, input2, output, maxTrades, experience, priceMultiply);
        }

        public BlueprintTrade(Item input, int inputCount, Item output, int outputCount, int maxTrades, int experience, float priceMultiply) {
            this(new ItemStack(input, inputCount), ItemStack.EMPTY, new ItemStack(output, outputCount), maxTrades, experience, priceMultiply);
        }

        public BlueprintTrade(Item input, int inputCount, Item output, int outputCount, int maxTrades, int experience) {
            this(input, inputCount, output, outputCount, maxTrades, experience, 0.15F);
        }

        public BlueprintTrade(Item input, int inputCount, int emeraldCount, int maxTrades, int experience, float priceMultiply) {
            this(new ItemStack(input, inputCount), ItemStack.EMPTY, new ItemStack(Items.EMERALD, emeraldCount), maxTrades, experience, priceMultiply);
        }

        public BlueprintTrade(Item input, int inputCount, int emeraldCount, int maxTrades, int experience) {
            this(input, inputCount, emeraldCount, maxTrades, experience, 0.15F);
        }

        public BlueprintTrade(int emeraldCount, Item output, int outputCount, int maxTrades, int experience, float priceMultiply) {
            this(new ItemStack(Items.EMERALD, emeraldCount), ItemStack.EMPTY, new ItemStack(output, outputCount), maxTrades, experience, priceMultiply);
        }

        public BlueprintTrade(int emeraldCount, Item output, int outputCount, int maxTrades, int experience) {
            this(emeraldCount, output, outputCount, maxTrades, experience, 0.15F);
        }
    }

    public static abstract class BasicTradeFactory implements TradeOffers.Factory {
        protected final ItemStack price;
        protected final ItemStack price2;
        protected final ItemStack forSale;
        protected final int maxTrades;
        protected final int experience;
        protected final float priceMultiply;

        public BasicTradeFactory(ItemStack price, ItemStack price2, ItemStack forSale, int maxTrades, int experience, float priceMultiply) {
            this.price = price;
            this.price2 = price2;
            this.forSale = forSale;
            this.maxTrades = maxTrades;
            this.experience = experience;
            this.priceMultiply = priceMultiply;
        }

        public BasicTradeFactory(ItemStack price, ItemStack forSale, int maxTrades, int experience, float priceMult) {
            this(price, ItemStack.EMPTY, forSale, maxTrades, experience, priceMult);
        }

        public BasicTradeFactory(int emeralds, ItemStack forSale, int maxTrades, int experience, float mult) {
            this(new ItemStack(Items.EMERALD, emeralds), forSale, maxTrades, experience, mult);
        }

        public BasicTradeFactory(int emeralds, ItemStack forSale, int maxTrades, int experience) {
            this(new ItemStack(Items.EMERALD, emeralds), forSale, maxTrades, experience, 1);
        }

        @Nullable
        @Override
        public TradeOffer create(Entity entity, Random random) {
            return new TradeOffer(this.price, this.price2, this.forSale, this.maxTrades, this.experience, this.priceMultiply);
        }
    }
}

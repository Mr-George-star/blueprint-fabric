package net.george.blueprint.core;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.george.blueprint.client.BlueprintShaders;
import net.george.blueprint.client.ChestManager;
import net.george.blueprint.client.RewardHandler;
import net.george.blueprint.client.renderer.BlueprintBoatRenderer;
import net.george.blueprint.client.renderer.block.BlueprintChestBlockEntityRenderer;
import net.george.blueprint.client.renderer.block.BlueprintSignBlockEntityRenderer;
import net.george.blueprint.client.screen.shake.ScreenShakeHandler;
import net.george.blueprint.common.command.ConfigCommand;
import net.george.blueprint.common.command.argument.EnumArgument;
import net.george.blueprint.common.command.argument.ModIdArgument;
import net.george.blueprint.common.network.MessageC2SUpdateSlabfishHat;
import net.george.blueprint.common.network.entity.MessageS2CEndimation;
import net.george.blueprint.common.network.entity.MessageS2CTeleportEntity;
import net.george.blueprint.common.network.entity.MessageS2CUpdateEntityData;
import net.george.blueprint.common.network.entity.SpawnEntityS2CPacket;
import net.george.blueprint.common.network.praticle.MessageS2CSpawnParticle;
import net.george.blueprint.common.server.ServerLifecycleHooks;
import net.george.blueprint.common.world.modification.ModdedBiomeSource;
import net.george.blueprint.common.world.storage.tracking.DataProcessors;
import net.george.blueprint.common.world.storage.tracking.TrackedData;
import net.george.blueprint.common.world.storage.tracking.TrackedDataManager;
import net.george.blueprint.core.api.ModLoadingContext;
import net.george.blueprint.core.api.biome.BiomeDictionary;
import net.george.blueprint.core.api.conditions.BlueprintAndCondition;
import net.george.blueprint.core.api.conditions.config.*;
import net.george.blueprint.core.api.config.ModConfig;
import net.george.blueprint.core.api.config.network.ConfigSync;
import net.george.blueprint.core.api.config.util.ConfigLoader;
import net.george.blueprint.core.api.network.SimpleChannel;
import net.george.blueprint.core.api.recipe.CraftingHelper;
import net.george.blueprint.core.endimator.EndimationLoader;
import net.george.blueprint.core.events.ModConfigEvents;
import net.george.blueprint.core.other.BlueprintEvents;
import net.george.blueprint.core.registry.*;
import net.george.blueprint.core.util.DataUtil;
import net.george.blueprint.core.util.NetworkUtil;
import net.george.blueprint.core.util.registry.RegistryHelper;
import net.minecraft.client.render.block.entity.SignBlockEntityRenderer;
import net.minecraft.command.argument.ArgumentTypes;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

public class   Blueprint implements ModInitializer {
	public static final String MOD_ID = "blueprint";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	public static final Marker CORE = MarkerManager.getMarker(MOD_ID);
	public static final EndimationLoader ENDIMATION_LOADER = new EndimationLoader();
	public static final RegistryHelper REGISTRY_HELPER = new RegistryHelper(MOD_ID);
	public static final SimpleChannel CHANNEL = new SimpleChannel(new Identifier(MOD_ID, "net"));
	public static final SimpleChannel PLAY_CHANNEL = new SimpleChannel(new Identifier("net"));
	public static final TrackedData<Byte> SLABFISH_SETTINGS = TrackedData.Builder.create(DataProcessors.BYTE, () -> (byte) 8).enablePersistence().build();

	public void onInitialize() {
		ConfigSync.INSTANCE.init();
		ConfigLoader.loadDefaultConfigPath();
		CraftingHelper.init();
		RewardHandler.registerEvents();
		BiomeDictionary.init();

		ModLoadingContext.registerConfig(MOD_ID, ModConfig.Type.CLIENT, BlueprintConfig.CLIENT_SPEC);
		ModLoadingContext.registerConfig(MOD_ID, ModConfig.Type.COMMON, BlueprintConfig.COMMON_SPEC);

		BlueprintEntityTypes.register();
		BlueprintBiomes.register();
		BlueprintBlockEntityTypes.register();
		BlueprintLootConditions.register();
		BlueprintShaders.registerShaders();
		BlueprintEvents.registerEvents();

		this.registerArgumentTypes();
		this.registerCallbacks();
		this.registerConfigPredicates();
		this.registerMessages();
		this.registerOthers();
		this.commonSetup();
	}

	private void commonSetup() {
		TrackedDataManager.INSTANCE.registerData(new Identifier(MOD_ID, "slabfish_head"), SLABFISH_SETTINGS);

		EntityRendererRegistry.register(BlueprintEntityTypes.BOAT.get(), BlueprintBoatRenderer::new);

		BlockEntityRendererRegistry.register(BlueprintBlockEntityTypes.CHEST.get(), BlueprintChestBlockEntityRenderer::new);
		BlockEntityRendererRegistry.register(BlueprintBlockEntityTypes.TRAPPED_CHEST.get(), BlueprintChestBlockEntityRenderer::new);
		BlockEntityRendererRegistry.register(BlueprintBlockEntityTypes.SIGN.get(), BlueprintSignBlockEntityRenderer::new);

		DataUtil.getSortedAlternativeDispenseBehaviors().forEach(DataUtil.AlternativeDispenseBehavior::register);
		BlueprintEvents.SORTED_CUSTOM_NOTE_BLOCK_INSTRUMENTS = DataUtil.getSortedCustomNoteBlockInstruments();
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private void registerArgumentTypes() {
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			ArgumentTypes.register(new Identifier(MOD_ID, "enum").toString(), EnumArgument.class, (ArgumentSerializer)new EnumArgument.Serializer());
			ArgumentTypes.register(new Identifier(MOD_ID, "modid").toString(), ModIdArgument.class, new ConstantArgumentSerializer<>(ModIdArgument::modIdArgument));
		}
	}

	private void registerCallbacks() {
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
			if (!dedicated) {
				ConfigCommand.register(dispatcher);
			}

		});
		ServerLifecycleEvents.SERVER_STARTING.register(ServerLifecycleHooks::handleServerAboutToStart);
		ServerLifecycleEvents.SERVER_STOPPED.register(ServerLifecycleHooks::handleServerStopped);

		ModConfigEvents.RELOADING.register(config -> {
			if (config.getModId().equals(Blueprint.MOD_ID)) {
				NetworkUtil.updateSlabfish(RewardHandler.SlabfishSetting.getConfig());
			}
		});
	}

	private void registerConfigPredicates() {
		CraftingHelper.register(new BlueprintAndCondition.Serializer());
		DataUtil.registerConfigPredicate(new EqualsPredicate.Serializer());
		DataUtil.registerConfigPredicate(new GreaterThanOrEqualPredicate.Serializer());
		DataUtil.registerConfigPredicate(new GreaterThanPredicate.Serializer());
		DataUtil.registerConfigPredicate(new LessThanOrEqualPredicate.Serializer());
		DataUtil.registerConfigPredicate(new LessThanPredicate.Serializer());
		DataUtil.registerConfigPredicate(new ContainsPredicate.Serializer());
		DataUtil.registerConfigPredicate(new MatchesPredicate.Serializer());
	}

	private void registerMessages() {
		int id = -1;
		CHANNEL.registerS2CPacket(MessageS2CEndimation.class, ++id, MessageS2CEndimation::decode);
		CHANNEL.registerS2CPacket(MessageS2CSpawnParticle.class, ++id, MessageS2CSpawnParticle::decode);
		CHANNEL.registerS2CPacket(MessageS2CTeleportEntity.class, ++id, MessageS2CTeleportEntity::decode);
		CHANNEL.registerS2CPacket(MessageS2CUpdateEntityData.class, ++id, MessageS2CUpdateEntityData::decode);
		CHANNEL.registerC2SPacket(MessageC2SUpdateSlabfishHat.class, ++id, MessageC2SUpdateSlabfishHat::decode);

		int default_id = -1;
		PLAY_CHANNEL.registerS2CPacket(SpawnEntityS2CPacket.class, ++default_id, SpawnEntityS2CPacket::decode);
	}

	private void registerOthers() {
		DataUtil.registerConfigCondition(Blueprint.MOD_ID, BlueprintConfig.CLIENT, BlueprintConfig.CLIENT.slabfishSettings);
		BuiltinRegistries.add(Registry.BIOME_SOURCE, new Identifier(MOD_ID, "modded"), ModdedBiomeSource.CODEC);
		BlueprintSurfaceRules.register();
	}
}

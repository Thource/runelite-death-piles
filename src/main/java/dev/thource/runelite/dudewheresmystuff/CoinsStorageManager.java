package dev.thource.runelite.dudewheresmystuff;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.thource.runelite.dudewheresmystuff.coins.GrandExchange;
import dev.thource.runelite.dudewheresmystuff.coins.ServantsMoneybag;
import dev.thource.runelite.dudewheresmystuff.coins.ShiloFurnace;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;

@Slf4j
@Singleton
public class CoinsStorageManager extends StorageManager<CoinsStorageType, CoinsStorage> {
    @Inject
    CoinsStorageManager(Client client, ItemManager itemManager, ConfigManager configManager, DudeWheresMyStuffConfig config, Notifier notifier, DudeWheresMyStuffPlugin plugin) {
        super(client, itemManager, configManager, config, notifier, plugin);

        for (CoinsStorageType type : CoinsStorageType.values()) {
            if (type == CoinsStorageType.SERVANT_MONEYBAG
                    || type == CoinsStorageType.SHILO_FURNACE
                    || type == CoinsStorageType.GRAND_EXCHANGE) continue;

            storages.add(new CoinsStorage(type, client, itemManager));
        }

        storages.add(new ServantsMoneybag(client, itemManager));
        storages.add(new ShiloFurnace(client, itemManager));
        storages.add(new GrandExchange(client, itemManager));
    }

    @Override
    public String getConfigKey() {
        return "coins";
    }

    @Override
    public Tab getTab() {
        return Tab.COINS;
    }
}

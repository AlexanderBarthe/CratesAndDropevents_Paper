package dev.upscairs.cratesAndDropevents.hooks.shopguiplus;

import dev.upscairs.cratesAndDropevents.dropevents.Dropevent;
import dev.upscairs.cratesAndDropevents.resc.CrateStorage;
import dev.upscairs.cratesAndDropevents.resc.DropeventStorage;
import net.brcdev.shopgui.provider.item.ItemProvider;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;

public class ShopGUIPlusItemProvider extends ItemProvider {
  public ShopGUIPlusItemProvider() {
    super("CratesAndDropEvents");
  }

  @Override
  public boolean isValidItem(ItemStack itemStack) {
    return getDropEventId(itemStack) != null;
  }

  @Override
  public ItemStack loadItem(ConfigurationSection configurationSection) {
    ConfigurationSection cadSection = configurationSection.getConfigurationSection("cad");
    if (cadSection == null) return null;

    String dropEventName = cadSection.getString("dropEvent");
    if (dropEventName == null) return null;

    Dropevent dropevent = DropeventStorage.getDropeventByName(dropEventName);
    if (dropevent == null) return null;

    return dropevent.getDropStarterItem();
  }

  @Override
  public boolean compare(ItemStack itemStack1, ItemStack itemStack2) {
    String itemStack1Id = getDropEventId(itemStack1);
    String itemStack2Id = getDropEventId(itemStack2);

    // Ensure dropevent id exists for both and are the same
    return itemStack1Id != null && Objects.equals(itemStack1Id, itemStack2Id);
  }

  private String getDropEventId(ItemStack itemStack) {
    if (itemStack == null) return null;
    if (!itemStack.hasItemMeta()) return null;

    PersistentDataContainer itemStackContainer = itemStack.getItemMeta().getPersistentDataContainer();

    boolean hasKey = itemStackContainer.has(Dropevent.EVENT_KEY);
    if (!hasKey) return null;

    return itemStackContainer.get(Dropevent.EVENT_KEY, PersistentDataType.STRING);
  }
}

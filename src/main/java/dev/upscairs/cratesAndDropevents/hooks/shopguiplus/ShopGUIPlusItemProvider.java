package dev.upscairs.cratesAndDropevents.hooks.shopguiplus;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import dev.upscairs.cratesAndDropevents.db.services.DropeventService;
import dev.upscairs.cratesAndDropevents.dropevents.Dropevent;
import net.brcdev.shopgui.provider.item.ItemProvider;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;

public class ShopGUIPlusItemProvider extends ItemProvider {

  private final DropeventService dropeventService;

  public ShopGUIPlusItemProvider() {
    super("CratesAndDropEvents");
    dropeventService = CratesAndDropevents.getInstance().getDbServices().getDropeventService();
  }

  @Override
  public boolean isValidItem(ItemStack itemStack) {
    return getDropEventId(itemStack) != null;
  }

  @Override
  public ItemStack loadItem(ConfigurationSection configurationSection) {
    ConfigurationSection cadSection = configurationSection.getConfigurationSection("cad");
    if (cadSection == null) return null;

    int dropEventId = cadSection.getInt("dropEventId");

    Dropevent dropevent = dropeventService.getById(dropEventId);
    if (dropevent == null) return null;

    return dropevent.getDropStarterItem();
  }

  @Override
  public boolean compare(ItemStack itemStack1, ItemStack itemStack2) {
    Integer itemStack1Id = getDropEventId(itemStack1);
    Integer itemStack2Id = getDropEventId(itemStack2);

    // Ensure dropevent id exists for both and are the same
    return itemStack1Id != null && Objects.equals(itemStack1Id, itemStack2Id);
  }

  private Integer getDropEventId(ItemStack itemStack) {
    if (itemStack == null) return null;
    if (!itemStack.hasItemMeta()) return null;

    PersistentDataContainer itemStackContainer = itemStack.getItemMeta().getPersistentDataContainer();

    if (!itemStackContainer.has(Dropevent.EVENT_KEY, PersistentDataType.INTEGER))
      return null;

    return itemStackContainer.get(Dropevent.EVENT_KEY, PersistentDataType.INTEGER);
  }
}

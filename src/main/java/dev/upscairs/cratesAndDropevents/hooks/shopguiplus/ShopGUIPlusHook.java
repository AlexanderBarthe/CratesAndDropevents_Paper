package dev.upscairs.cratesAndDropevents.hooks.shopguiplus;

import dev.upscairs.cratesAndDropevents.CratesAndDropevents;
import net.brcdev.shopgui.ShopGuiPlusApi;
import net.brcdev.shopgui.event.ShopGUIPlusPostEnableEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ShopGUIPlusHook implements Listener {
  private CratesAndDropevents plugin;
  private ShopGUIPlusItemProvider itemProvider;

  public ShopGUIPlusHook(CratesAndDropevents plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onShopGUIPlusPostEnable(ShopGUIPlusPostEnableEvent event) {
    this.itemProvider = new ShopGUIPlusItemProvider();
    ShopGuiPlusApi.registerItemProvider(itemProvider);
    plugin.getLogger().info("Registered item provider in ShopGUI+!");
  }
}

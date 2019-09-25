package mitw.lobby.gui;

import java.util.HashMap;
import java.util.Map;

import mitw.lobby.ItemStackBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import net.development.mitw.Mitw;
import net.development.mitw.menu.Button;
import net.development.mitw.menu.Menu;

public class LanguageGUI extends Menu {

	@Override
	public Map<Integer, Button> getButtons(final Player arg0) {
		final Map<Integer, Button> buttons = new HashMap<>();

		buttons.put(0, new LanguageButton("&6&lChinese 中文", "zh_tw"));
		buttons.put(1, new LanguageButton("&6&lEnglish", "en_us"));

		return buttons;
	}

	@Override
	public String getTitle(final Player arg0) {
		return "§6§lLanguages §7| §f語言";
	}

	private class LanguageButton extends Button {

		private final String displayName;
		private final String language;

		public LanguageButton(final String displayName, final String language) {
			this.displayName = displayName;
			this.language = language;
		}

		@Override
		public ItemStack getButtonItem(final Player player) {
			return ItemStackBuilder.createItem1(Material.BOOK, 1, 0, displayName, "&7click me to select this language!", "&7點我選擇該語言!");
		}

		@Override
		public void clicked(final Player player, final int slot, final ClickType clickType, final int hotbarButton) {
			Mitw.getInstance().getLanguageData().sendLangRedis(player, language);
		}

	}

}

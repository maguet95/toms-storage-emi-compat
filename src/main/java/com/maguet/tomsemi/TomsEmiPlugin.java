package com.maguet.tomsemi;

import com.tom.storagemod.Content;
import com.tom.storagemod.screen.AbstractStorageTerminalScreen;
import com.tom.storagemod.screen.IScreen;
import com.tom.storagemod.util.IAutoFillTerminal;
import com.tom.storagemod.util.IAutoFillTerminal.ISearchHandler;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.api.widget.Bounds;

import net.minecraft.world.inventory.Slot;

/**
 * Integración de Tom's Simple Storage con EMI.
 *
 * Basada en com.tom.storagemod.emi.EMIPlugin de tom5454 (Tom's Storage, licencia MIT), que la versión
 * Fabric de Tom's excluye al compilar aunque su fabric.mod.json la sigue declarando.
 */
public class TomsEmiPlugin implements EmiPlugin {

	@Override
	public void register(EmiRegistry registry) {
		// La terminal de crafteo aparece como estación de las recetas de mesa de crafteo.
		registry.addWorkstation(VanillaEmiRecipeCategories.CRAFTING, EmiStack.of(Content.craftingTerminal.get()));
		// El botón + de EMI: llena la cuadrícula con lo que haya en el almacenamiento y en el inventario.
		registry.addRecipeHandler(Content.craftingTerminalMenu.get(), new TerminalRecipeHandler());
		// Pasar el ratón por un objeto de la terminal y pulsar R/U muestra sus recetas en EMI.
		registry.addGenericStackProvider((screen, x, y) -> {
			if (screen instanceof AbstractStorageTerminalScreen<?> t) {
				Slot slot = t.getSlotUnderMouse();
				if (slot != null) return new EmiStackInteraction(EmiStack.of(slot.getItem()), null, false);
			}
			return EmiStackInteraction.EMPTY;
		});
		// Que el panel de EMI no tape los botones laterales de la terminal.
		registry.addGenericExclusionArea((screen, out) -> {
			if (screen instanceof IScreen s) {
				s.getExclusionAreas(b -> out.accept(new Bounds(b.x(), b.y(), b.width(), b.height())));
			}
		});
	}

	static {
		// El modo de búsqueda «sincronizar con EMI» de la terminal.
		IAutoFillTerminal.updateSearch.add(new ISearchHandler() {
			@Override public void setSearch(String text) { EmiApi.setSearchText(text); }
			@Override public String getSearch() { return EmiApi.getSearchText(); }
			@Override public String getName() { return "EMI"; }
		});
	}
}

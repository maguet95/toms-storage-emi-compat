package com.maguet.tomsemi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.tom.storagemod.inventory.StoredItemStack;
import com.tom.storagemod.menu.CraftingTerminalMenu;
import com.tom.storagemod.util.IAutoFillTerminal;

import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.Widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Botón + de EMI en la terminal de crafteo.
 *
 * Basado en com.tom.storagemod.emi.EmiTransferHandler de tom5454 (MIT). Diferencias:
 * - Cuenta SIEMPRE lo que hay en el almacenamiento, no solo con la «búsqueda inteligente» activada.
 * - Llena de verdad: manda a la terminal el mismo mensaje {fill: <receta>} que usa la integración de JEI de Tom;
 *   el servidor de Tom's saca los ingredientes del almacenamiento y del inventario. En el original ese paso estaba comentado.
 */
public class TerminalRecipeHandler implements StandardRecipeHandler<CraftingTerminalMenu> {

	@Override
	public List<Slot> getInputSources(CraftingTerminalMenu menu) {
		return Collections.emptyList();
	}

	@Override
	public List<Slot> getCraftingSlots(CraftingTerminalMenu menu) {
		return Collections.emptyList();
	}

	@Override
	public EmiPlayerInventory getInventory(AbstractContainerScreen<CraftingTerminalMenu> screen) {
		List<EmiStack> stacks = new ArrayList<>();
		CraftingTerminalMenu menu = screen.getMenu();
		// Cuadrícula + inventario del jugador (el slot 0 es el resultado).
		menu.slots.subList(1, menu.slots.size()).stream().map(Slot::getItem).map(EmiStack::of).forEach(stacks::add);
		// Todo lo que la terminal ve en el almacenamiento.
		menu.getStoredItems().forEach(s -> stacks.add(EmiStack.of(s.getStack(), s.getQuantity())));
		return new EmiPlayerInventory(stacks);
	}

	@Override
	public boolean supportsRecipe(EmiRecipe recipe) {
		return recipe.getCategory() == VanillaEmiRecipeCategories.CRAFTING && recipe.getId() != null;
	}

	@Override
	public boolean canCraft(EmiRecipe recipe, EmiCraftContext<CraftingTerminalMenu> context) {
		// Como en JEI con Tom's: se permite aunque falte algo; se coloca lo que haya y lo que falta se marca en rojo.
		return true;
	}

	@Override
	public boolean craft(EmiRecipe recipe, EmiCraftContext<CraftingTerminalMenu> context) {
		AbstractContainerScreen<CraftingTerminalMenu> screen = context.getScreen();
		CompoundTag tag = new CompoundTag();
		tag.putString("fill", recipe.getId().toString());
		((IAutoFillTerminal) screen.getMenu()).sendMessage(tag);
		Minecraft.getInstance().setScreen(screen);
		return true;
	}

	@Override
	public void render(EmiRecipe recipe, EmiCraftContext<CraftingTerminalMenu> context, List<Widget> widgets, GuiGraphics g) {
		List<Integer> missing = missingSlots(recipe, context.getScreen().getMenu());
		int i = 0;
		for (Widget w : widgets) {
			if (w instanceof SlotWidget sw) {
				int j = i++;
				EmiIngredient stack = sw.getStack();
				Bounds b = sw.getBounds();
				if (sw.getRecipe() == null && !stack.isEmpty() && missing.contains(j)) {
					g.fill(b.x(), b.y(), b.x() + b.width(), b.y() + b.height(), 0x44FF0000);
				}
			}
		}
	}

	/** Casillas de la receta (en la cuadrícula de 3x3) cuyo ingrediente no está ni en el inventario ni en el almacenamiento. */
	private static List<Integer> missingSlots(EmiRecipe recipe, CraftingTerminalMenu menu) {
		int width = recipe.getDisplayWidth();
		Set<StoredItemStack> stored = new HashSet<>(menu.getStoredItems());
		List<Integer> missing = new ArrayList<>();
		int i = 0;
		for (EmiIngredient ing : recipe.getInputs()) {
			List<ItemStack> options = ing.getEmiStacks().stream().map(EmiStack::getItemStack).filter(s -> !s.isEmpty()).toList();
			if (!options.isEmpty()) {
				boolean found = false;
				for (ItemStack s : options) {
					if (Minecraft.getInstance().player.getInventory().findSlotMatchingItem(s) != -1 || stored.contains(new StoredItemStack(s))) {
						found = true;
						break;
					}
				}
				if (!found) missing.add(width == 1 ? i * 3 : width == 2 ? ((i % 2) + i / 2 * 3) : i);
			}
			i++;
		}
		return missing;
	}
}

package fr.utt.simpleItemStorage;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.profiles.builder.XSkull;
import com.mojang.authlib.GameProfile;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class SISRecipes {

    public static void initRecipes() {
        initTerminalBlockRecipe();
    }

    private static void initTerminalBlockRecipe() {
        ItemStack head = SISTerminal.getTerminalItemStack();

        NamespacedKey recipeKey = new NamespacedKey(SimpleItemStorage.getInstance(), "terminal");
        ShapedRecipe recipe = new ShapedRecipe(recipeKey, head);

        recipe.shape("TPT",
                     "E#E",
                     "CQC");
        recipe.setIngredient('T', XMaterial.REDSTONE_TORCH.parseMaterial());
        recipe.setIngredient('P', XMaterial.GREEN_STAINED_GLASS_PANE.parseMaterial());
        recipe.setIngredient('E', XMaterial.EMERALD.parseMaterial());
        recipe.setIngredient('#', XMaterial.COMPARATOR.parseMaterial());
        recipe.setIngredient('C', XMaterial.TRAPPED_CHEST.parseMaterial());
        recipe.setIngredient('Q', XMaterial.QUARTZ.parseMaterial());

        SimpleItemStorage.getInstance().getServer().addRecipe(recipe);
    }
}

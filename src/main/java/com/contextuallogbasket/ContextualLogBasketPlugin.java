package com.contextuallogbasket;

import com.google.inject.Provides;
import javax.inject.Inject;
import net.runelite.api.*;
import net.runelite.api.events.PostMenuSort;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@PluginDescriptor(name = "Contextual Log Basket",
    description = "Left-click Fill within a configurable distance of a bank, and Empty elsewhere",
    tags = {"log", "basket", "bank", "woodcutting", "menu"})
public class ContextualLogBasketPlugin extends Plugin
{
    @Inject
    private Client client;

    @Inject
    private ContextualLogBasketConfig config;

    @Provides
    ContextualLogBasketConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(ContextualLogBasketConfig.class);
    }

    // Run after the built-in Menu Entry Swapper (priority 0).
    @Subscribe(priority = -1)
    public void onPostMenuSort(PostMenuSort event)
    {
        if (client.isMenuOpen() || client.getGameState() != GameState.LOGGED_IN
            || client.isKeyPressed(KeyCode.KC_SHIFT) || client.getLocalPlayer() == null)
        {
            return;
        }
        MenuEntry[] entries = client.getMenuEntries();
        if (entries.length == 0)
        {
            return;
        }
        MenuEntry top = entries[entries.length - 1];
        int group = top.getParam1() >>> 16;
        if ((group != InterfaceID.INVENTORY && group != InterfaceID.BANKSIDE)
            || !isBasket(top.getItemId()) || !isItemAction(top))
        {
            return;
        }

        String option = nearBank() ? "Fill" : "Empty";
        MenuEntry chosen = findAction(entries, top, option);
        if (chosen == null)
        {
            return; // Never invent an action that the game did not offer.
        }
        for (int i = 0; i < entries.length; i++)
        {
            if (entries[i] == chosen)
            {
                entries[i] = top;
                entries[entries.length - 1] = chosen;
                chosen.setType(MenuAction.CC_OP);
                client.setMenuEntries(entries);
                return;
            }
        }
        // Submenus cannot be reparented; copy the real action to the root menu.
        client.createMenuEntry(-1)
            .setOption(chosen.getOption()).setTarget(chosen.getTarget())
            .setIdentifier(chosen.getIdentifier()).setType(MenuAction.CC_OP)
            .setParam0(chosen.getParam0()).setParam1(chosen.getParam1())
            .setItemId(chosen.getItemId()).setWorldViewId(chosen.getWorldViewId())
            .onClick(chosen.onClick());
    }

    static boolean isBasket(int id)
    {
        return id == ItemID.LOG_BASKET_CLOSED || id == ItemID.LOG_BASKET_OPEN;
    }

    private static boolean isItemAction(MenuEntry entry)
    {
        return entry.getType() == MenuAction.CC_OP
            || entry.getType() == MenuAction.CC_OP_LOW_PRIORITY;
    }

    static MenuEntry findAction(MenuEntry[] entries, MenuEntry target, String option)
    {
        for (MenuEntry entry : entries)
        {
            if (entry.getParam0() != target.getParam0() || entry.getParam1() != target.getParam1()
                || entry.getItemId() != target.getItemId())
            {
                continue;
            }
            if (isItemAction(entry) && option.equalsIgnoreCase(entry.getOption()))
            {
                return entry;
            }
            if (entry.getSubMenu() != null)
            {
                MenuEntry child = findAction(entry.getSubMenu().getMenuEntries(), target, option);
                if (child != null)
                {
                    return child;
                }
            }
        }
        return null;
    }

    private boolean nearBank()
    {
        int distance = Math.max(1, Math.min(20, config.bankDistance()));
        Player player = client.getLocalPlayer();
        WorldView world = player.getWorldView();
        int x = player.getLocalLocation().getSceneX();
        int y = player.getLocalLocation().getSceneY();
        for (NPC npc : world.npcs())
        {
            NPCComposition definition = npc.getTransformedComposition();
            if (definition != null && hasAction(definition.getActions(), "Bank")
                && npc.getWorldArea().distanceTo(player.getWorldLocation()) <= distance)
            {
                return true;
            }
        }
        Tile[][] tiles = world.getScene().getTiles()[world.getPlane()];
        // Objects occupy every tile in their footprint, so large bank chests work too.
        for (int tx = Math.max(0, x - distance); tx <= Math.min(tiles.length - 1, x + distance); tx++)
        {
            for (int ty = Math.max(0, y - distance); ty <= Math.min(tiles[tx].length - 1, y + distance); ty++)
            {
                Tile tile = tiles[tx][ty];
                if (tile == null)
                {
                    continue;
                }
                if (isBank(tile.getWallObject()) || isBank(tile.getDecorativeObject())
                    || isBank(tile.getGroundObject()))
                {
                    return true;
                }
                for (GameObject object : tile.getGameObjects())
                {
                    if (isBank(object))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isBank(TileObject object)
    {
        if (object == null)
        {
            return false;
        }
        ObjectComposition definition = client.getObjectDefinition(object.getId());
        if (definition.getImpostorIds() != null)
        {
            definition = definition.getImpostor();
        }
        return definition != null && isBankDefinition(definition.getName(), definition.getActions());
    }

    static boolean isBankDefinition(String name, String[] actions)
    {
        return hasAction(actions, "Bank") || (name != null
            && name.toLowerCase(java.util.Locale.ROOT).contains("bank")
            && (hasAction(actions, "Use") || hasAction(actions, "Collect")));
    }

    private static boolean hasAction(String[] actions, String option)
    {
        if (actions != null)
        {
            for (String action : actions)
            {
                if (option.equalsIgnoreCase(action))
                {
                    return true;
                }
            }
        }
        return false;
    }
}

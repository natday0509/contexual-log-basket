package com.contextuallogbasket;

import java.lang.reflect.Field;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.PostMenuSort;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.ItemID;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ContextualLogBasketPluginTest
{
    private MenuEntry entry(String option, int slot)
    {
        MenuEntry entry = mock(MenuEntry.class);
        when(entry.getOption()).thenReturn(option);
        when(entry.getParam0()).thenReturn(slot);
        when(entry.getParam1()).thenReturn(InterfaceID.INVENTORY << 16);
        when(entry.getItemId()).thenReturn(ItemID.LOG_BASKET_CLOSED);
        when(entry.getType()).thenReturn(MenuAction.CC_OP);
        return entry;
    }

    @Test
    public void findsSubmenuWithoutChoosingAnotherBasket()
    {
        MenuEntry fill = entry("Fill", 0);
        MenuEntry check = entry("Check", 0);
        MenuEntry empty = entry("Empty", 0);
        Menu menu = mock(Menu.class);
        when(check.getSubMenu()).thenReturn(menu);
        when(menu.getMenuEntries()).thenReturn(new MenuEntry[]{empty});
        assertSame(empty, ContextualLogBasketPlugin.findAction(
            new MenuEntry[]{entry("Empty", 1), check, fill}, fill, "Empty"));
        assertNull(ContextualLogBasketPlugin.findAction(new MenuEntry[]{fill}, fill, "Empty"));
    }

    @Test
    public void recognizesBanksButNotDepositBoxesOrClosedChests()
    {
        assertTrue(ContextualLogBasketPlugin.isBankDefinition("Bank booth", new String[]{null, "Bank"}));
        assertTrue(ContextualLogBasketPlugin.isBankDefinition("Bank chest", new String[]{"Use"}));
        assertFalse(ContextualLogBasketPlugin.isBankDefinition("Bank deposit box", new String[]{"Deposit"}));
        assertFalse(ContextualLogBasketPlugin.isBankDefinition("Bank chest", new String[]{"Open"}));
        assertFalse(ContextualLogBasketPlugin.isBankDefinition("Chest", new String[]{"Use"}));
        assertFalse(ContextualLogBasketPlugin.isBankDefinition(null, null));
        assertTrue(ContextualLogBasketPlugin.isBasket(ItemID.LOG_BASKET_OPEN));
        assertFalse(ContextualLogBasketPlugin.isBasket(1));
    }

    @Test
    public void fillsAtTwoTilesIncludingDiagonalAndEmptiesAtThree() throws Exception
    {
        assertEquals(2, new ContextualLogBasketConfig() {}.bankDistance());
        checkDistance(2, 0, 2, "Fill");
        checkDistance(2, 2, 2, "Fill");
        checkDistance(3, 0, 2, "Empty");
    }

    @Test
    public void usesConfiguredDistance() throws Exception
    {
        checkDistance(5, 5, 5, "Fill");
        checkDistance(6, 0, 5, "Empty");
        checkDistance(1, 1, 1, "Fill");
        checkDistance(2, 0, 1, "Empty");
        checkDistance(20, 20, 20, "Fill");
        checkDistance(21, 0, 20, "Empty");
    }

    @Test
    public void promotesSubmenuPreservingTheGameAction() throws Exception
    {
        Client client = mock(Client.class, RETURNS_DEEP_STUBS);
        when(client.getGameState()).thenReturn(GameState.LOGGED_IN);
        WorldView world = client.getLocalPlayer().getWorldView();
        when(world.npcs().iterator()).thenReturn(java.util.Collections.emptyIterator());
        when(world.getScene().getTiles()).thenReturn(new Tile[4][104][104]);
        when(client.getLocalPlayer().getLocalLocation()).thenReturn(new LocalPoint(1344, 1344));
        MenuEntry fill = entry("Fill", 4);
        MenuEntry check = entry("Check", 4);
        MenuEntry empty = entry("Empty", 4);
        when(empty.getIdentifier()).thenReturn(12345);
        when(empty.getWorldViewId()).thenReturn(-1);
        when(empty.getTarget()).thenReturn("Log basket");
        java.util.function.Consumer<MenuEntry> callback = e -> {};
        when(empty.onClick()).thenReturn(callback);
        Menu sub = mock(Menu.class);
        when(check.getSubMenu()).thenReturn(sub);
        when(sub.getMenuEntries()).thenReturn(new MenuEntry[]{empty});
        when(client.getMenuEntries()).thenReturn(new MenuEntry[]{check, fill});
        MenuEntry promoted = mock(MenuEntry.class, RETURNS_SELF);
        when(client.createMenuEntry(-1)).thenReturn(promoted);
        plugin(client, 2).onPostMenuSort(new PostMenuSort());
        verify(promoted).setOption("Empty");
        verify(promoted).setTarget("Log basket");
        verify(promoted).setIdentifier(12345);
        verify(promoted).setParam0(4);
        verify(promoted).setParam1(InterfaceID.INVENTORY << 16);
        verify(promoted).setItemId(ItemID.LOG_BASKET_CLOSED);
        verify(promoted).setWorldViewId(-1);
        verify(promoted).setType(MenuAction.CC_OP);
        verify(promoted).onClick(callback);
    }

    @Test
    public void leavesOpenMenusAndShiftClickUntouched() throws Exception
    {
        Client client = mock(Client.class);
        ContextualLogBasketPlugin plugin = plugin(client, 2);
        when(client.isMenuOpen()).thenReturn(true);
        plugin.onPostMenuSort(new PostMenuSort());
        when(client.isMenuOpen()).thenReturn(false);
        when(client.getGameState()).thenReturn(GameState.LOGGED_IN);
        when(client.isKeyPressed(KeyCode.KC_SHIFT)).thenReturn(true);
        plugin.onPostMenuSort(new PostMenuSort());
        verify(client, never()).getMenuEntries();
        verify(client, never()).createMenuEntry(anyInt());
    }

    private ContextualLogBasketPlugin plugin(Client client, int distance) throws Exception
    {
        ContextualLogBasketPlugin plugin = new ContextualLogBasketPlugin();
        Field field = ContextualLogBasketPlugin.class.getDeclaredField("client");
        field.setAccessible(true);
        field.set(plugin, client);
        ContextualLogBasketConfig config = mock(ContextualLogBasketConfig.class);
        when(config.bankDistance()).thenReturn(distance);
        Field configField = ContextualLogBasketPlugin.class.getDeclaredField("config");
        configField.setAccessible(true);
        configField.set(plugin, config);
        return plugin;
    }

    private void checkDistance(int dx, int dy, int distance, String expected) throws Exception
    {
        Client client = mock(Client.class);
        Player player = mock(Player.class);
        WorldView world = mock(WorldView.class, RETURNS_DEEP_STUBS);
        when(client.getGameState()).thenReturn(GameState.LOGGED_IN);
        when(client.getLocalPlayer()).thenReturn(player);
        when(player.getWorldView()).thenReturn(world);
        when(player.getLocalLocation()).thenReturn(new LocalPoint(10 * 128 + 64, 10 * 128 + 64));
        when(world.npcs().iterator()).thenReturn(java.util.Collections.emptyIterator());
        Tile[][][] tiles = new Tile[4][40][40];
        Tile tile = mock(Tile.class);
        tiles[0][10 + dx][10 + dy] = tile;
        GameObject chest = mock(GameObject.class);
        when(chest.getId()).thenReturn(123);
        when(tile.getGameObjects()).thenReturn(new GameObject[]{chest});
        ObjectComposition definition = mock(ObjectComposition.class);
        when(client.getObjectDefinition(123)).thenReturn(definition);
        when(definition.getActions()).thenReturn(new String[]{"Bank"});
        when(world.getScene().getTiles()).thenReturn(tiles);
        MenuEntry fill = entry("Fill", 0);
        MenuEntry empty = entry("Empty", 0);
        MenuEntry[] entries = {empty, fill};
        when(client.getMenuEntries()).thenReturn(entries);
        plugin(client, distance).onPostMenuSort(new PostMenuSort());
        assertEquals(expected, entries[entries.length - 1].getOption());
        verify(client).setMenuEntries(entries);
    }
}

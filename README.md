# Contextual Log Basket

A RuneLite plugin that makes the inventory log basket's default left-click action:

- **Fill** within your configured distance of a banker, bank booth, or usable bank chest (default: two tiles).
- **Empty** farther away.

Distance includes diagonals and is measured from the bank object's occupied tiles
or the bank NPC's footprint. This is proximity, not walking distance: walls do not
change the result. Deposit boxes and closed chests do not count as banks.

Click the cog next to **Contextual Log Basket** in RuneLite's plugin list and set
**Bank distance** to 1–20 tiles. Changes apply immediately when hovering the basket
with the menu closed. For example, a setting of 5 uses Fill up to and including
five tiles away, and Empty from six tiles away.

Supports the open and closed log basket in the inventory, including the inventory
panel while banking when the game offers the action. It promotes the existing
Empty submenu action; it does not automate clicks or invent unavailable actions.
Equipped baskets and combined forestry baskets are outside this plugin's scope.
Shift-click retains your existing settings. The plugin runs after RuneLite's
built-in Menu Entry Swapper; other third-party menu swappers may conflict.

## Build and run

Install JDK 17, then in PowerShell from this folder:

```powershell
.\gradlew.bat --gradle-user-home .gradle-user-home test build
.\gradlew.bat --gradle-user-home .gradle-user-home run
```

The second command opens a RuneLite development client with this plugin loaded.
Enable **Contextual Log Basket** in the plugin list. The first build downloads
Gradle and RuneLite dependencies. On macOS/Linux, use `bash ./gradlew` instead.
This project is not published to the Plugin Hub; building a jar does not install
it into the ordinary RuneLite launcher.

Submission instructions are in [SUBMISSION.md](SUBMISSION.md). Original plugin
code is licensed under [BSD 2-Clause](LICENSE).

## In-game verification

With a log basket containing logs, hover it one and two tiles from a booth, chest,
and banker: left-click should be Fill. Move three tiles away: it should be Empty,
and clicking should perform the game's Empty action. Repeat diagonally and with
the open basket. Check Shift-click and the banking inventory separately.
Set Bank distance to 1, 5, and 20 and verify Fill at the chosen distance and Empty
one tile beyond it. Test at a booth, usable chest, and banker, and verify that
changing the setting takes effect without restarting the plugin.

API references: [RuneLite example plugin](https://github.com/runelite/example-plugin)
and [RuneLite menu entry swapper](https://github.com/runelite/runelite/blob/master/runelite-client/src/main/java/net/runelite/client/plugins/menuentryswapper/MenuEntrySwapperPlugin.java).

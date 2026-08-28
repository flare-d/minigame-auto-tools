# minigame-auto-tools

 **Minigame Auto Tools**

A lightweight Fabric client-side mod for Minecraft 1.21.4 designed specifically for minigame enthusiasts. Automates tedious inventory and building tasks so you can focus on the game.

---

## Features

### Auto Tool Switch
Automatically selects the best tool from your hotbar when you start breaking a block. Shears for wool, pickaxes for stone, axes for wood — no more manual swapping. Your original item is restored as soon as you stop mining.

### Smart Scaffold / Bridge Building
Hold a block and walk. The mod places blocks under your feet and in front of you to prevent falls. It only builds off existing solid blocks, never on thin air. Includes server-side ghost-block protection — if a block is placed but not confirmed by the server, the mod detects the unsafe air and places again until you have a solid surface.

### Auto Replenish
Running out of blocks while building? If you have more of the same block elsewhere in your inventory, the mod automatically refills your active hotbar slot. Build with two stacks without ever opening your inventory.

### Wool Safety Cushion
Falling with wool in your hand? The mod looks straight down and rapidly places wool blocks beneath you to break your fall, as long as you have wool remaining.

### Auto Water Drop
If you are falling from a lethal height and carry a water bucket, the mod automatically switches to it and places water at the perfect moment to cancel the fall damage — unless you are already being saved by scaffold.

### Resource HUD
A minimal, clean overlay at the top of your screen tracking your key resources: Iron, Gold, Diamonds, and Emeralds. Automatically shifts downward when boss health bars are present so nothing overlaps.

### Pickup Notifier
Item pickup notifications slide in from the left side of the screen, showing the item icon, name, and amount collected. They fade out after a few seconds.

---

## Requirements

- **Minecraft:** 1.21.4
- **Mod Loader:** [Fabric Loader](https://fabricmc.net/use/) >= 0.16.0
- **Fabric API:** Any 1.21.4-compatible build
- **Java:** 21 or newer

---

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 1.21.4.
2. Download the latest `auto-tools-*.jar` from the [Releases](../../releases) page.
3. Place the `.jar` file into your `.minecraft/mods` folder.
4. Launch the game through the Fabric profile.

> This is a **client-side only** mod. It does not need to be installed on the server and works on any multiplayer server.

---

## Safety & Fair Play

This mod is intended for **minigame and casual play** where client-side automation is permitted. It does not modify packets, use exploits, or interact with the server in any non-standard way — everything is accomplished through normal player actions (slot changes, block placements, and attacks).

Always check your server's rules before using any client-side automation.

---

## Contributing

Contributions are welcome. If you find a bug or want to suggest a feature, open an [Issue](../../issues) or submit a [Pull Request](../../pulls).

## License

This project is released under the MIT License. See [LICENSE](LICENSE) for details.

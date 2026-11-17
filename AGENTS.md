# InventorySorterUltimate (Minecraft 26.3 / Fabric)

Standalone client-side Fabric mod for Minecraft 26.3. Original implementation, MIT licensed.

## Environment & Build

- **Minecraft**: `26.3` (unobfuscated official Mojang names via `net.fabricmc.fabric-loom` `1.18-SNAPSHOT`)
- **Fabric Loader**: `0.19.5`
- **Gradle**: `9.7.1`
- **Java**: JDK `25` (available locally at `C:\Users\Madcept\.devin\extensions\redhat.java-1.58.0-win32-x64\jre\25.0.4.1-win32-x86_64`)

### Build Command (PowerShell)

```powershell
$env:JAVA_HOME = "C:\Users\Madcept\.devin\extensions\redhat.java-1.58.0-win32-x64\jre\25.0.4.1-win32-x86_64"
.\gradlew.bat build
```

Output JAR is produced in `build/libs/`:
- `inventory-sorter-ultimate-1.0.0+26.3.jar`

## Architecture

- `dev.inventorysorter.InventorySorterMod`: Client entrypoint, active sort lifecycle (`trySort`, `onTick`, `cancelSort`).
- `dev.inventorysorter.util.ContainerSorter`: Section detection, stack-consolidation + ordering move planner, and tick executor.
- `dev.inventorysorter.util.SlotClicks`: Container click packet helpers (single click, stack transfer, empty player-slot lookup, drop carried).
- `dev.inventorysorter.input.SorterKeybinds`: Registers `key.inventory-sorter-ultimate.sort` (default: Middle Mouse Button) into vanilla `Options.keyMappings`.
- `dev.inventorysorter.config.SorterConfig` / `SorterConfigScreen`: JSON config (`config/inventory-sorter-ultimate.json`) and optional ModMenu settings screen (`dev.inventorysorter.compat.ModMenuIntegration`).

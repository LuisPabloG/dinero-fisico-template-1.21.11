# Ratonera Mod - Progress Summary

## Objective
Add a ShopBlock system (buy/sell GUI) to the "Ratonera" mod for Minecraft Fabric 1.21.11.

## Current State

### What's Created
All Java files, resources, and JSONs for the ShopBlock system are written.

### Blocking Issue (ValueOutput/ValueInput)
**Root cause found:** In MC 1.21.11, `BlockEntity.saveAdditional`/`loadAdditional` use
`net.minecraft.world.level.storage.ValueOutput`/`ValueInput` (NOT `net.minecraft.nbt`).

These interfaces only expose 3 methods each:
- `ValueOutput`: `child(String)`, `childrenList(String)`, `list(String, Codec)`
- `ValueInput`: `childOrEmpty(String)`, `childrenListOrEmpty(String)`, `listOrEmpty(String, Codec)`

There are NO primitive read/write methods (no writeInt, readString, etc.).

### How to Write Primitive Data
**Option A (Codec list):** Use `output.list("key", Codec.INT).add(value)` — but creates a list, not a single value.

**Option B (CompoundTag intermediary):** Build a `CompoundTag` manually, then use
`tag.store(output, "key")` to write it. On read, extract via `tag.getXxx("key")` from the ValueInput's
underlying CompoundTag — but ValueInput doesn't expose a getter.

**Option C (new API):** The interface methods are intentionally sparse; primitive writing
goes through Codec-based serialization. Investigate how vanilla does it.

### Next Steps Needed
1. Determine how vanilla block entities write int/String/UUID to ValueOutput
2. Fix the ShopBlockEntity serialization accordingly
3. Fix `player.getServer()` -> `player.level().getServer()`
4. Compile and test

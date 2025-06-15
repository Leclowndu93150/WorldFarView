**Inspired by FarPlane2**, this mod enhances render distance using Level of Detail (LOD) planes.

## 🚀 Features
- Dynamic LOD-based rendering
- Configurable render distance and quality settings
- Server-side compatibility controls
- Extensible API for custom chunk generators

## 🔧 Installation & Usage
1. Install the mod on both **client and server**.
2. Join a world - the mod activates automatically.
   - *Note:* High resolutions may require longer loading times.
3. **Currently supports only noise-based chunk generators** (vanilla-compatible).
   - The mod will notify you if any compatibility issues arise.

> ⚠️ **Development Notice**: This mod is still in active development and may have stability issues.

---

## ⚙️ Configuration

### **Client Settings** *(Editable in-game via Mods menu)*
| Setting | Description | Recommended Value        |  
|---------|-------------|--------------------------|  
| **LOD0 Start Size** | Base plane size (rendering starts at half this distance) | `Render distance * 32`   |  
| **Number of LODs** | Total LOD planes (each 2× larger than the previous) | -                        |  
| **Resolution Quality** | Vertices per plane (higher = sharper but heavier) | `70` |  

**Render Distance Formula**:  
`Total Distance = LOD0 Start Size × (2 ^ Number of LODs)`

*Example*: If `LOD0 Start Size = 1000` and `Number of LODs = 3`, the max distance is `1000 × 8 = 8000` blocks.

⚠️ If the server rejects client settings (e.g., unsupported resolution), the mod will auto-disable.

---

### **Server Settings** *(Can be found in world/server files)*
| Setting | Description |  
|---------|-------------|  
| **clientSide** | If `true`, sends seed/generator data to clients (*unsupported in current version*). If `false`, forces server-side generation. |  
| **maxResolution** | Rejects clients requesting resolutions beyond this limit (if `clientSide = false`). |  
| **maxLevel** | Rejects clients requesting higher LOD levels than allowed (if `clientSide = false`). |  

---

## 🛠️ For Developers: Adding Compatibility
To integrate your custom world generator:
1. Implement `FarChunkGenerator` and register it in `FarChunkGenerators`.
2. For **client-side support**, create:
   - `ClientFarChunkGenerator`
   - `SerializableFarChunkGenerator<YourClientFarChunkGenerator>`

📌 **Performance Note**: Ensure your chunk generator is optimized for speed!

---
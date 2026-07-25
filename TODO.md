# CodeChickenLib TODO

CodeChickenLib `1.12.x` / `3.3.7` TODO summary. The inventory covers 51 TODO markers in active source code and 1 deferred marker in `archive/`; TODOs emitted by external runtime logs are excluded.  

### High Priority / Functional

- [ ] Implement `Matrix4.inverse()` instead of always throwing `IrreversibleTransformationException` (`src/main/java/codechicken/lib/vec/Matrix4.java:567`).  
  - [ ] Prefer an optimized affine-matrix inverse where the matrix representation permits it.  
- [ ] Restore the `CCModelBase` and `CCModelRenderer` buffer-rendering implementations (`CCModelBase.java:63`, `CCModelRenderer.java:110`).  
  - [ ] Bind the supplied `BufferBuilder` through `CCRenderState`.  
  - [ ] Render baked models while preserving the caller's drawing state.  
  - [ ] Add a regression test or runtime example proving that the legacy render API emits geometry.  
- [ ] Complete vanilla transform parsing in `TransformUtils.parseFromJson()` (`src/main/java/codechicken/lib/util/TransformUtils.java:36,214,257`).  
  - [ ] Match vanilla model JSON transform values and semantics.  
  - [ ] Preserve support for `x`/`y`, CCL defaults, and hand transforms.  
  - [ ] Move the inline transform parsing at line 316 into a transform factory.  
- [ ] Extend `CCBlockStateLoader` JSON compatibility (`src/main/java/codechicken/lib/model/loader/blockstate/CCBlockStateLoader.java:56-58`).  
  - [ ] Support vanilla predicates and predicate variants.  
  - [ ] Pass unhandled BlockState JSON data to custom `IModel` implementations.  
  - [ ] Support custom sided particle definitions loaded from JSON.  
- [ ] Complete proper `BakedQuad` / `CCBakedQuad` piping in `CCRenderState` (`src/main/java/codechicken/lib/render/CCRenderState.java:36`).  
- [ ] Add an optional no-lighting path to `BlockRenderer` (`src/main/java/codechicken/lib/render/BlockRenderer.java:11`).  
- [ ] Extend `BakingVertexBuffer` to support triangles and quadulation (`src/main/java/codechicken/lib/render/buffer/BakingVertexBuffer.java:27`).  
- [ ] Add automatic block-breaking texture support for `ICCBlockRenderer` (`src/main/java/codechicken/lib/render/block/ICCBlockRenderer.java:14`).  
  - [ ] Define how custom renderers receive and apply damage-stage textures.  
  - [ ] Optimize the temporary baking and re-rendering path in `BlockRenderingRegistry.renderBlockDamage()` (`BlockRenderingRegistry.java:91`).  

### Rendering / Performance

- [ ] Cache UV ranges in `VertexDataUtils` instead of scanning every texture sprite for each lookup (`src/main/java/codechicken/lib/util/VertexDataUtils.java:92`).  
  - [ ] Define cache invalidation behavior around texture stitching and resource reloads.  
  - [ ] Keep the lookup safe for its documented threading constraints.  
- [ ] Cache shader resource reads in `ShaderHelper` where dynamic shader loading permits it (`src/main/java/codechicken/lib/render/shader/ShaderHelper.java:10`).  
  - [ ] Key cached content by resource identity and reload/version state.  
  - [ ] Avoid stale resources during development reloads.  
- [ ] Expand `GlStateTracker` into a useful GL leak analyzer (`src/main/java/codechicken/lib/render/state/GlStateTracker.java:14-16`).  
  - [ ] Compare saved pre/post states automatically.  
  - [ ] Detect changes made through both raw OpenGL and `GlStateManager`.  
  - [ ] Cover the remaining relevant GL state categories.  
- [ ] Improve shader failure reporting in `ShaderProgram` with Minecraft crash-report categories (`src/main/java/codechicken/lib/render/shader/ShaderProgram.java:24`).  
- [ ] Verify and optimize the custom block-damage rendering path before adding caching or reuse (`src/main/java/codechicken/lib/render/block/BlockRenderingRegistry.java:91`).  

### Model / Format Features

- [ ] Add material-library (`.mtl`) support to `OBJParser` (`src/main/java/codechicken/lib/render/OBJParser.java:21`).  
  - [ ] Handle material declarations, `usemtl`, and referenced textures.  
  - [ ] Define behavior for unsupported material properties.  
- [ ] Add JSON support to `DummyBakedModel` if callers still need configurable fallback models (`src/main/java/codechicken/lib/model/DummyBakedModel.java:17`).  
- [ ] Add face-based models to `SimpleBlockRenderer` (`src/main/java/codechicken/lib/model/bakery/SimpleBlockRenderer.java:31`).  
- [ ] Complete the `CCModelBox` model conversion/API (`src/main/java/codechicken/lib/model/modelbase/CCModelBox.java:10`).  
- [ ] Consider adding more Platonic solids to `CCModelLibrary` (`src/main/java/codechicken/lib/render/CCModelLibrary.java:9`).  
- [ ] Make `DistanceRayTraceResult` copyable (`src/main/java/codechicken/lib/raytracer/DistanceRayTraceResult.java:9`).  

### API Safety / Refactoring

- [ ] Validate the type returned by `ReflectionManager.newInstance_Unsafe()` (`src/main/java/codechicken/lib/reflect/ReflectionManager.java:190`).  
  - [ ] Reject mappings whose implementation class is not assignable to the requested return type.  
  - [ ] Provide an actionable exception message.  
- [ ] Review and simplify the defensive null checks in `FluidUtils` (`src/main/java/codechicken/lib/fluid/FluidUtils.java:16`).  
  - [ ] Confirm Forge capability and legacy fluid contracts before removing checks.  
- [ ] Rework `TextureUtils` icon registration API (`src/main/java/codechicken/lib/texture/TextureUtils.java:35,42,48`).  
  - [ ] Move `IIconRegister` to a more appropriate type/package if API compatibility permits.  
  - [ ] Expose the collection as `List<IIconRegister>` rather than `ArrayList<IIconRegister>`.  
  - [ ] Rename `addIconRegister()` to better describe its registration behavior, with a compatibility bridge if needed.  
- [ ] Rename `LambdaUtils.forEach()` to `forAll()` to match its all-elements predicate semantics (`src/main/java/codechicken/lib/util/LambdaUtils.java:22`).  
- [ ] Evaluate whether `CCRSConsumer` should be merged with or replaced by `CCRenderState` (`src/main/java/codechicken/lib/render/consumer/CCRSConsumer.java:15`).  
- [ ] Add a copy operation to the model-base compatibility layer (`src/main/java/codechicken/lib/model/modelbase/CCModelBase.java:17`, `CCModelRenderer.java:18`).  
  - [ ] Decide whether the intended wrapper should pull data from existing vanilla `ModelBase` instances.  
  - [ ] Define the animation-callback API before implementing the wrapper.  

### Documentation

- [ ] Document the configuration translation-key generation rules (`src/main/java/codechicken/lib/configuration/IConfigTag.java:56`).  
- [ ] Document the public texture/resource APIs (`src/main/java/codechicken/lib/texture/IItemBlockTextureProvider.java:9`, `CustomIResource.java:17`).  
- [ ] Document the vertex consumers and particle rendering behavior (`src/main/java/codechicken/lib/render/consumer/UnpackingVertexConsumer.java:10`, `CCRSConsumer.java:15`, `src/main/java/codechicken/lib/render/particle/DigIconParticle.java:50`).  
- [ ] Document the model bakery and loader APIs (`src/main/java/codechicken/lib/model/bakery/SimpleBlockRenderer.java:27`, `SubBlockBakery.java:33`, `SubBlockStateKeyGenerator.java:12`, `SubItemStackKeyGenerator.java:12`, `src/main/java/codechicken/lib/model/loader/cube/CCModelCube.java:25`).  
- [ ] Document and reorder the utility methods in `RenderUtils` (`src/main/java/codechicken/lib/render/RenderUtils.java:22`).  

### Compatibility / Cleanup

- [ ] Decide whether the `1.13` compatibility TODOs are still relevant to the maintained `1.12.x` branch (`src/main/java/codechicken/lib/render/item/CCRenderItem.java:100`, `src/main/java/codechicken/lib/model/bakedmodels/ModelProperties.java:64`).  
  - [ ] Remove stale migration comments if the behavior is intentionally 1.12-only.  
  - [ ] Otherwise move the compatibility work to the appropriate version branch or issue.  
- [ ] Decide whether individual `ICCBlockRenderer` implementations should own icon registration rather than `BlockRenderingRegistry` (`src/main/java/codechicken/lib/render/block/BlockRenderingRegistry.java:31`).  
- [ ] Review the tooltip clipping workaround in `GuiDraw` and implement correct top-bound clipping (`src/main/java/codechicken/lib/gui/GuiDraw.java:160`).  
- [ ] Clarify the intended scope of automatic block-model and texture registration before changing the public API (`src/main/java/codechicken/lib/texture/TextureUtils.java:35`, `BlockRenderingRegistry.java:31`).  

### Deferred / Archive

- [ ] Decide whether the archived QB importer is still needed (`archive/codechicken/lib/render/QBImporter.java:20`).  
  - [ ] Remove it from the archive if no supported code uses it.  
  - [ ] Otherwise document its supported format and maintenance status.  

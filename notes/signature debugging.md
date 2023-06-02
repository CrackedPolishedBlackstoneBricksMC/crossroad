Here's a class signature from Minecraft 1.16.5, on the VillagerProfessionLayer class: `<T:Lnet/minecraft/world/entity/LivingEntity;:Lnet/minecraft/world/entity/npc/VillagerDataHolder;M:Lnet/minecraft/client/model/EntityModel<TT;>;:Lnet/minecraft/client/model/VillagerHeadModel;>Lnet/minecraft/client/renderer/entity/layers/RenderLayer<TT;TM;>;Lnet/minecraft/server/packs/resources/ResourceManagerReloadListener;`

Here's how it looks like when visited:

```text
	visitFormalTypeParameter: T
	visitClassBound
		visitClassType: net/minecraft/world/entity/LivingEntity
	visitEnd
	visitInterfaceBound
		visitClassType: net/minecraft/world/entity/npc/VillagerDataHolder
	visitEnd
	visitFormalTypeParameter: M
	visitClassBound
		visitClassType: net/minecraft/client/model/EntityModel
		visitTypeArgumentWildcard: =
		visitTypeVariable: T
	visitEnd
	visitInterfaceBound
		visitClassType: net/minecraft/client/model/VillagerHeadModel
	visitEnd
	visitSuperclass
		visitClassType: net/minecraft/client/renderer/entity/layers/RenderLayer
		visitTypeArgumentWildcard: =
		visitTypeVariable: T
		visitTypeArgumentWildcard: =
		visitTypeVariable: M
	visitEnd
	visitInterface
		visitClassType: net/minecraft/server/packs/resources/ResourceManagerReloadListener
	visitEnd
```

Another example, on the ConfiguredFeature class: `<FC::Lnet/minecraft/world/level/levelgen/feature/configurations/FeatureConfiguration;F:Lnet/minecraft/world/level/levelgen/feature/Feature<TFC;>;>Ljava/lang/Object;Lnet/minecraft/world/level/levelgen/Decoratable<Lnet/minecraft/world/level/levelgen/feature/ConfiguredFeature<**>;>;`

```
	visitFormalTypeParameter: FC
	visitInterfaceBound
		visitClassType: net/minecraft/world/level/levelgen/feature/configurations/FeatureConfiguration
	visitEnd
	visitFormalTypeParameter: F
	visitClassBound
		visitClassType: net/minecraft/world/level/levelgen/feature/Feature
		visitTypeArgumentWildcard: =
		visitTypeVariable: FC
	visitEnd
	visitSuperclass
		visitClassType: java/lang/Object
	visitEnd
	visitInterface
		visitClassType: net/minecraft/world/level/levelgen/Decoratable
		visitTypeArgumentWildcard: =
		visitClassType: net/minecraft/world/level/levelgen/feature/ConfiguredFeature
		visitTypeArgument
		visitTypeArgument
	visitEnd
visitEnd
```

The structure is

1. Formal type parameters (optional, enclosed in angle brackets if it exists)
2. Superclass
3. Interfaces implemented

*Formal type parameters* are any new type variables that the class introduces, like the `T` in `public class MyClass<T> {}`.

I think it's best illustrated by some examples

|class definition|signature|
|---|---|
|`class MyClass1<T> {}`|`<T:Ljava/lang/Object;>Ljava/lang/Object;`|
|`class MyClass2<T extends Math> {}`|`<T:Ljava/lang/Math;>Ljava/lang/Object;`|
|`class MyClass3<T extends Runnable> {}`|`<T::Ljava/lang/Runnable;>Ljava/lang/Object;`|
|`class MyClass4<T extends Runnable & Action> {}`|`<T::Ljava/lang/Runnable;:Ljavax/swing/Action;>Ljava/lang/Object;`|
|`class MyClass5<T extends Math & Runnable & Action> {}`|`<T:Ljava/lang/Math;:Ljava/lang/Runnable;:Ljavax/swing/Action;>Ljava/lang/Object;`|
|`class MyClass6<T, U> {}`|`<T:Ljava/lang/Object;U:Ljava/lang/Object;>Ljava/lang/Object;`|
|`class MyClass7<X, Y extends X> {}`|`<X:Ljava/lang/Object;Y:TX;>Ljava/lang/Object;`|

These classes introduce new formal type parameters, so their signatures start with an angle bracket block. The superclass of all the `MyClass`es is `Object`, so that comes after the angle bracket block. Inside the block we have the formal parameter name, a colon, the super*class* (if one exists) of the type parameter, and if superinterfaces exist they come after more colons. Also, there is a new type of type: `Tblah;` refers to the formal parameter named `blah`, much like how `Lblah;` refers to the class `blah`.

Imagine parsing character-by-character:

* if you see `<`, the signature is describing a formal parameter block
* munch characters until you see a `:` to find the formal parameter name
* after consuming the `:`, the options are `L` (for a superclass bound) or `:` (for a superinterface bound)
  * if the formal parameter is completely unconstrained (neither a superclass nor a superinterface bound), javac would rather emit `<T:Ljava/lang/Object;>` than `<T>` or `<T:>` or `<T::>` 
* after that, the options are `:` (after which another superinterface bound follows), `>` (to close the formal parameter block), or something else (the start of the next formal parameter name)
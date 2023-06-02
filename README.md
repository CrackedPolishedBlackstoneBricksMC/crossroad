# crossroad

`crossroad` is a Gradle plugin for generating headers that are the *intersection* of two or more input jars: the headers only contain classes, methods, and fields accessible in every input jar, under the same name & with the same types.

For the time being, `crossroad` only emits "header jars" - method bodies do not contain any code.

The intended use-case is for writing software against the *common subset* of several, radically-different versions of a dependency:

* Simply dumping both versions in the same subproject won't have the intended effect.
* You can use one subproject for each version, but if the dependencies are similar, it requires a lot of cut-and-pasting code.
* You can write code against the lower version, and have another subproject include its source-code together with the higher version on the compile classpath, but the only way to tell if you're using something *removed* in the update is "try compiling and see if it fails", which is not ideal. (This also confuses the hell out of IDEs.)

When the dependencies are merged: you statically know what exists in all versions, and are statically prevented from compiling code that doesn't use things extant in all versions.

## Usage

<details><summary>Apply the plugin...</summary>

```gradle
buildscript {
	repositories {
		mavenCentral()
		maven { url "https://repo.sleeping.town/" }
	}
	dependencies {
		classpath "agency.highlysuspect:crossroad:0.3"
	}
}

apply plugin: "java"
apply plugin: "agency.highlysuspect.crossroad"
```

</details>

This makes a `crossroad.merge` function available in your scripts, which you can use as such:

```gradle
def hybridGson = crossroad.merge(
	"com.google.code.gson:gson:2.10.1",
	"com.google.code.gson:gson:1.7.2"
);

dependencies {
	compileOnly files(hybridGson);
	//Now, only the common subset of GSON 1 and 2 is available in my IDE.
	//Yes this is a contrived example
}

//Or like this, it doesn't matter:
dependencies {
	compileOnly files(crossroad.merge("...", "...", "..."));
}
```

* You can pass `Path`s, `File`s, dependencies with one artifact, and strings (they will be resolved to maven dependencies).
* `merge` returns a `Path`. If you'd like a dependency, convert it to one with `project.files`.
* `merge` accepts a variable number of arguments.

## Usage with [`minivan`](https://github.com/CrackedPolishedBlackstoneBricksMC/minivan)

<details><summary>Apply both plugins...</summary>

```gradle
buildscript {
	repositories {
		maven { url = "https://maven.fabricmc.net/"}
		maven { url = "https://repo.sleeping.town/" }
		gradlePluginPortal()
	}
	dependencies {
		classpath "agency.highlysuspect:minivan:0.2"
		classpath "agency.highlysuspect:crossroad:0.3"
	}
}

apply plugin: "java"
apply plugin: "agency.highlysuspect.minivan"
apply plugin: "agency.highlysuspect.crossroad"
```

</details>

The process is as follows:

* Set up an instance of Minecraft using `minivan`'s manual API: `minivan.getMinecraft("1.19.4")`
* Access the raw Minecraft jar using the `.minecraft` property
* Pass them to `crossroad`
* Add the merged jar to the project

We then add the oldest version's Maven `.dependencies` as well - I guess we could try to match up the Maven deps and merge them with `crossroad` as well, but in practice it's not really an issue, the third-party libraries have 100000% less churn than vanilla Minecraft and modders rarely use them anyway (other than google gson).

```gradle
dependencies {
	def a = minivan.getMinecraft("1.16.5")
	def b = minivan.getMinecraft("1.18.2")
	def c = minivan.getMinecraft("1.19.2")
	def d = minivan.getMinecraft("1.19.4")
	
	compileOnly project.files(crossroad.merge(a.minecraft, b.minecraft, c.minecraft, d.minecraft))
	a.dependencies.each { compileOnly it }
}
```

A complete Minecraft example, including a dependency on a MC-independent "core" module, is available in [Apathy's codebase](https://github.com/quat1024/apathy/blob/844593e07da3b6597147c0dc8924f8a1ee40fe8b/core-plus-minecraft-1.16-thru-1.19.4/build.gradle).
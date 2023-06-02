# crossroad

"Intersects" jars - generates header jars that contain only classes, fields, and methods that exist in all input jars.

by header jars I mean the method bodies are missing. (It'd be nice to include them if they don't differ between all inputs?)

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
def hybridGson = crossroad.merge("com.google.code.gson:gson:2.10.1", "com.google.code.gson:gson:1.7.2");
dependencies {
	compileOnly files(hybridGson)
}

//or like this, it doesn't matter:
dependencies {
	compileOnly files(crossroad.merge("..."))
}
```

⭐ You can pass `Path`s, `File`s, dependencies with one artifact, and strings (they will be resolved to maven dependencies). The function returns a `Path`.

## Usage with `minivan`

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
* Add the merged dependency to the project

We then add the oldest version's Maven `.dependencies` as well - I guess we could try to match up the Maven deps and merge them with `crossroad` as well, but in practice it's not really an issue, the third-party libraries have 100000% less churn than vanilla Minecraft, and modders rarely need to use them (other than google gson I guess).

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
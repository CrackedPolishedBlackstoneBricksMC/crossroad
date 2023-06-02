# crossroad

"Intersects" jars - generates header jars that contain only classes, fields, and methods that exist in all input jars.

by header jars I mean the method bodies are missing. (It'd be nice to include them if they don't differ between all inputs?)

## Usage

```gradle
buildscript {
	repositories {
		mavenCentral()
		maven { url "https://repo.sleeping.town/" }
	}
	dependencies {
		classpath "agency.highlysuspect:crossroad:0.1"
	}
}

apply plugin: "agency.highlysuspect.crossroad"

//Idk write an example later lol

//the intended use case is with Minivan
def mc1194 = minivan.getMinecraft("1.19.4")
def mc1192 = minivan.getMinecraft("1.19.2")
def merged = crossroad.merge(mc1194.minecraft, mc1192.minecraft)
project.dependencies.add("compileOnly", project.files(merged))
```
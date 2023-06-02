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
		classpath "agency.highlysuspect:crossroad:0.3"
	}
}

apply plugin: "agency.highlysuspect.crossroad"

//(Idk write a more basic example later lol)

//the intended use case is with Minivan
dependencies {
  def a = minivan.getMinecraft("1.16.5");
  def b = minivan.getMinecraft("1.19.4");
  def merged = crossroad.merge(a.minecraft, b.minecraft);
  
  compileOnly files(merged);
  
  //doesn't acutally "merge" the minivan-discovered libraries, but generally is good enough
  a.dependencies.each { compileOnly it }
}
```
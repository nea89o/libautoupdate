# libautoupdate

A java library for creating auto updates.

## Installation

First, include the library in your build using a shadow plugin or similar. This is mostly intended for fat jars,
since multi jar updates are not handled right now.

```
repositories {
    maven("https://repo.nea.moe/releases")
}

dependencies {
    shadowImpl("moe.nea:libautoupdate:0.1.0")
}
```

You may want to exclude transitive dependencies if your target platform already bundles google's gson library.

## Usage

In your application, create an `UpdateContext` and store it somewhere:

```java
UpdateContext updateContext=new UpdateContext(
        UpdateSource.gistSource("romangraef","9b62fe32bc41c09d2d7e2d3153f14ee8"),
        UpdateTarget.deleteAndSaveInTheSameFolder(Main.class),
        CurrentVersion.of(10000),
        "test"
        );
```

You will have to specify an update source,
an update target (the file to replace), the version that is currently being ran, and a string id to prevent
files from being overwritten.

Upon startup you probably want to call 'updateContext.cleanup()' to remove leftover data from any updates of your
application.


### Sources

 - Gist Source

Uses a gist with multiple (or just one) files called `<upstream>.json` in the format
```json5
{ "versionNumber": 10001 // The version number of the new update
, "versionName": "1.0.1 - Hello" // The version name, for display to the user.
, "sha256": "3ca21a6c1bfd26b4987f2342f00b50e8bf6a87d7449c3951b91f97c7d46a8570" // The sha256 hash of the file
, "download": "https://github.com/NotEnoughUpdates/NotEnoughUpdates/releases/download/v2.1-rc7/NotEnoughUpdates-2.1-blahaj-rc7.jar" // The download url of this version
}
```

 - GitHub Releases Source

Uses a GitHub release to either source a pre-release or full-release JAR. This release source does not support hashes or
newer-than-latest local versions. The current version also has to be the tag name, unlike the gist source which can have
any version type. The GitHub release needs to have a tag that is also present in the jar itself, and there should only 
be one JAR in each GitHub release. Subclasses of this source may provide custom logic for choosing the jar / version.


### Targets

 - DeleteAndSaveInSameFolder

Deletes the original jar and downloads the new jar into the same directory, while keeping the name specified by the download url.

 - ReplaceJar

Deletes the original jar and downloads the new jar into the same directory, while keeping the name of the original (deleted) jar.


### Checking for an update

You can check for an update by calling `updateContext.checkUpdate("upstream")`. 
You then can check if an update is present using `potentialUpdate.isUpdateAvailable()`.
After that check you can call `potentialUpdate.launchUpdate()`. 
This will cause your update to be downloaded now, and be executed after you application exits.



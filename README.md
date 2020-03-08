# Sincere Loyalty

[![Curseforge](https://curse.nikky.moe/api/img/359522?logo)](https://www.curseforge.com/projects/359522) [![](https://jitpack.io/v/Ladysnake/SincereLoyalty.svg)](https://jitpack.io/#Ladysnake/SincereLoyalty)

Make your trident even more loyal !

A trident with Loyalty III can be combined with Heart of the Sea to obtain Loyalty IV. 
At this level, whenever it is dropped it will drop as an entity. Tridents abandoned in this way can be brought back to you
by keeping right click down with an empty hand, no matter the distance. No one will be able to steal them from you either,
as when thrown Loyalty II tridents will always get back to you. Tridents with Loyalty IV cannot be destroyed by normal means.

## Installing
### Players
You can download this mod through curseforge. It requires the [Fabric Modloader](https://fabricmc.net/) and Fabric API.

### Developers
You can add this mod to a dev environment by inserting the following in your `build.gradle` :

```gradle
repositories {
	maven { 
        name = "Ladysnake Mods"
        url = 'https://dl.bintray.com/ladysnake/mods'
    }
}

dependencies {
    modImplementation "io.github.ladysnake:SincereLoyalty:${pal_version}"
    include "io.github.ladysnake:SincereLoyalty:${pal_version}"
}
```

You can find the current version of Sincere Loyalty in the [releases](https://github.com/Ladysnake/PlayerAbilityLib/releases) tab of the repository on Github.

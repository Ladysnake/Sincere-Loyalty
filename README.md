# Sincere Loyalty

[![Curseforge](https://curse.nikky.moe/api/img/366420?logo)](https://www.curseforge.com/projects/366420) [![](https://jitpack.io/v/Ladysnake/Sincere-Loyalty.svg)](https://jitpack.io/#Ladysnake/Sincere-Loyalty)

Make your trident even more loyal !

A trident with Loyalty III can be combined with Heart of the Sea to obtain Loyalty IV.

![Craft](https://cdn.discordapp.com/attachments/523251999899385875/686333307947974707/unknown.png)

At this level, whenever it is dropped it will drop as an entity. Tridents abandoned in this way can be brought back to you
by keeping right click down with an empty hand, no matter the distance. No one will be able to steal them from you for long either,
as Loyalty IV tridents can always be recalled by you when thrown or dropped. Tridents with Loyalty IV cannot be destroyed by normal means.

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
    modImplementation "io.github.ladysnake:SincereLoyalty:${sl_version}"
    include "io.github.ladysnake:Sincere-Loyalty:${sl_version}"
}
```

You can find the current version of Sincere Loyalty in the [releases](https://github.com/Ladysnake/Sincere-Loyalty/releases) tab of the repository on Github.

# The Stalker: Six Nights (Forge 1.20.1)

A Minecraft Forge horror mod for Java 17.

## Build
Use Gradle 8.8 with Java 17:
`gradle build --no-daemon`

The built mod JAR is written to `build/libs/`.

## Current features in this source
- Custom 3D Stalker entity with role-specific texture/scale variants.
- Six-night event director and final letter.
- The Watcher remains visible longer and only vanishes after a sustained stare.
- Random smaller Stalkers appear around nearby doors/windows and linger before disappearing.
- Night 3 Hide and Seek and Night 4 Don't Look Back survival encounters.
- Client-side dark fog near Stalkers and rare PNG jumpscares using bundled horror art.
- `/stalker spawn` and `/stalker remove` commands (requires permission level 2).

## Notes
This source has been edited for the requested feature set but has not been built or tested in Minecraft in this environment. Verify the GitHub Actions build before replacing the existing JAR. Keep backups of the current working source and JAR.

# Plugin Hub submission

This project is prepared for submission, not yet accepted into the Plugin Hub.
The authoritative process is the
[RuneLite submission guide](https://github.com/runelite/plugin-hub#submitting-a-plugin).

## Publish the source

The configured repository is
https://github.com/natday0509/contexual-log-basket (the spelling matches the existing remote).
It must be public so RuneLite can build and review the source.

After reviewing the local commit, publish it with `git push -u origin main`.
Do not force-push if the remote has different history; reconcile that first.

## Create the Plugin Hub pull request

1. Fork https://github.com/runelite/plugin-hub and create a branch named
   `add-contextual-log-basket` in that fork.
2. Add a single file named `plugins/contextual-log-basket`, without an extension.
   Copy the prepared `.submission/plugins/contextual-log-basket` file into it.
   Its two fields are the source repository URL and exact tested commit hash.
3. Open a pull request to `runelite/plugin-hub` on `master`, using the title and
   body in `.submission/pull-request.md`.
4. Check the Plugin Hub build and review results. If source changes are needed,
   commit and push them to this repository and update the marker's `commit=` field
   in the same pull request.

The local `.submission` folder is intentionally ignored: its marker references
the completed source commit and belongs in the Plugin Hub repository.
If the source changes after preparation, rebuild and update the marker using
the full hash from `git rev-parse HEAD` before submitting.

## Build and review scope

`build=standard` allows Plugin Hub to replace the local Gradle build with its
standard build. Production code uses only RuneLite and its provided dependencies.
JUnit and Mockito are local test dependencies, not runtime dependencies.
No custom build steps or external runtime services are required.

The plugin copies an existing Empty submenu action into the root menu, preserving
the action identifier, item, slot, widget, world view, and callback. This matches
the approach used for submenu promotion in RuneLite's built-in Menu Entry Swapper.
The distance condition and submenu promotion should both be included in review;
this document does not claim approval by RuneLite or Jagex.

## Manual validation

The author reported that the original two-tile version works in-game. Automated
tests cover configurable boundaries and menu handling. Before submitting, verify
the configurable setting in-game using the checklist in README.md; this has not
been independently verified by an automated game session.

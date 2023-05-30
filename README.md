# DrivenByKate

This is an experimental, work-in-progress layer atop [DrivenByMoss](http://www.mossgrabers.de/Software/Bitwig/Bitwig.html).
In a way, it's an extension to that extension.

The primary goal is to add full graphics support and improved UI to the variety of devices that I (Kate)
use without adding maintenance surface to Moss' incredible DrivenByMoss plugin.

Targeted devices currently include:

- The Ableton Push2
- NI Komplete Kontrol mkII devices.
- NI Maschine mk3 devices.


### Building and Installing the extension

1. Install Maven and dependences, either [from here](https://maven.apache.org/install.html)
or if on Linux, using the distro package manager, e.g. `yum install maven` or
`apt-get install maven`.
2. Run `mvn install` in this repo's root.
3. Follow [installation instructions] in the included manual for further steps.

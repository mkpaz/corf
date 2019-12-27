# Telekit

Download from [releases page](https://github.com/mkpaz/telekit/releases).

Base app provides the following tools:

- **API Client**

  API requests automation tool. It's not intended to provide complex API testing, but rather to perform bulk requests to
  automate routine configurations tasks.

- **Base64 Encoder**

  Base64 batch encoder & decoder;

- **Import File Builder**

  Generates text files (like CSV, XML, configs, custom scripts etc) by predefined template.

- **IPv4 Calculator**

  Simple IPv4 calc. Supports subnet splitting and IP format conversion.

- **Password Generator**

  Batch password generator that supports three different types of passwords.

- **Sequence Generator**

  Generates complex numeric sequences by predefined template.

- **SS7 Utils**
    * CIC Table - ISUP channel identification code helper;
    * SPC Converter - MTP3 signalling point code converter; supports ITU and ANSI formats.

- **Transliterator**

  Converts cyrillic geo-area names to ASCII.
  See [Romanization of Russian](https://en.wikipedia.org/wiki/Wikipedia:Romanization_of_Russian).

## System requirements

Telekit is a Java app that uses [jlink](https://docs.oracle.com/javase/9/tools/jlink.htm) to build native runtime
image, so there is no need to install Java.

Right now, only Windows 7+ systems (both 32 & 64 bit) are supported.

## Plugins

App features can be extended via plugins. If you want to write your own one, clone this repo and use
`telekit-plugin-example` as starting point.

## Credits

Application icon made by Freepik from [www.flaticon.com](https://flaticon.com).

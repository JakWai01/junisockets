# junisockets

![Maven CI](https://github.com/JakWai01/junisockets/workflows/Maven%20CI/badge.svg)
![Mirror](https://github.com/JakWai01/junisockets/workflows/Mirror/badge.svg)

## Overview

A WebRTC **signaling server** for [unisockets](https://github.com/alphahorizonio/unisockets) to allow nodes to discover each other and exchange candidates. The signaling server is not involved in any actual connections between clients.

### Components

TODO: Add Components

### Signaling Protocol

The signaling components use the following protocol:

![Sequence Diagram](https://alphahorizonio.github.io/unisockets/media/sequence.svg)

A public signaling server instance is running on `wss://signaler.webnetes.dev` and used in the demo.

### Related Resources

Interested in an implementation of the [Go `net` package](https://golang.org/pkg/net/) based on the unisockets package, with TinyGo and WASM support? You might be interested in [tinynet](https://github.com/alphahorizonio/tinynet)!

You want a Kubernetes-style system for WASM, running in the browser and in node? You might be interested in [webnetes](https://github.com/alphahorizonio/webnetes), which uses unisockets for it's networking layer.

## License

junisockets (c) 2021 Jakob Waibel and contributors

SPDX-License-Identifier: AGPL-3.0

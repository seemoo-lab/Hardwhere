# LARS

Large Accessories Retrieval System

This repo contains the mobile app, the required snipe-it patches and the "lent-by" backend that works in tandem with [snipe-it](https://github.com/snipe/snipe-it)

The backend daemon ist expected to handle all of the example.com/LARS/* requests and exposes its own API and webview at this path.

# Building the daemon
Get rustc and run
`cargo build --release`
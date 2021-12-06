# HardWhere

SEEMOO Accessories Retrieval System (HardWhere)

App + additional Functions based on [snipe-it]

This repo contains the mobile app, the required snipe-it patches and the "lent-by" backend that works in tandem with [snipe-it]

The backend daemon ist expected to handle all of the example.com/HardWhere/* requests and exposes its own API and webview at this path.

# Building the daemon
install requirements:
`apt install libssl-dev pkg-config`
For other distributions [see here](https://docs.rs/openssl/0.10.35/openssl/#automatic)

Get rustc and run
`cargo build --release`

[#snipe-it]: https://github.com/snipe/snipe-it

# Example apache config for backend

```apache2
<VirtualHost *:443>
[...]
ProxyPass "/HardWhere" http://127.0.0.1:8000
ProxyPassReverse "/HardWhere" http://127.0.0.1:8000
ProxyPreserveHost Off
```
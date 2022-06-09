# HardWhere

SEEMOO Accessories Retrieval System (HardWhere)

App + who-lent-to-whom functions based on [snipe-it]

This repo contains the mobile app, the required snipe-it patches and the "lent-by" backend that works in tandem with [snipe-it]

The backend daemon ist expected to handle all of the example.com/HardWhere/* requests and exposes its own API and webview at this path.

Currently compatible with snipeit v5.4.3

## Building the daemon
install requirements:
`apt install libssl-dev pkg-config`
For other distributions [see here](https://docs.rs/openssl/0.10.35/openssl/#automatic)

Get rustc and run
`cargo build --release`

[#snipe-it]: https://github.com/snipe/snipe-it

## Log settings for the daemon

The `RUST_LOG` environment vairable can be set. Examples:
`RUST_LOG=hardwhere_backend=trace` or for the full show `RUST_LOG=trace`
You can also specify it for multiple moduls `RUST_LOG=hardwhere_backend=debug,actix_web=trace`.

Possible values are trace,debug,info,warn,error

## Example apache config for backend

Running the daemon together on the same machine with snipeit.
The daemon is expected to be present on <domain>/HardWhere for the app.
```apache2
<VirtualHost *:443>
[...]
  <Location /HardWhere>
      ProxyPass http://127.0.0.1:8000/HardWhere
      ProxyPassReverse http://127.0.0.1:8000/HardWhere
      ProxyPreserveHost On
  </Location>
```

# Structure
- `src/` contains the additional backend, referred to as HardWhere-Backend or daemon, used for
  - indexing who lent to whom
  - providing return/lent APIs for the app, so the index in the backend is updated at the same time
  - regularly making sure the default fieldset is applied to all models that have none
  - providing a webview on https://<domain>/HardWhere/ for inspecting lent assets without using the app
- `static/` is part of the HardWhere-Backend, containing templates, CSS icons and other stuff for the webview, it's required to be relative to the daemon application
- `hardwhere.patch` contains code applied to snipeit to provide app login via QR code and auto-login from the snipeit menu into the HardWhere-Backend webview
- `app/` contains the sourcecode for the android app
- `res/` app resources

## Workflow
You can open this folder as an android App in android studio and in VS Code for the HardWhere-Backend. It is recommended to use VS Code with the rust-analyzer plugin for it.

You can build the app via gradle or in android studio. HardWhere-Backend can be build using `cargo build --release`, `--release` being optional.

For the integration with snipeit you'll have to upgrade hardwhere.patch for new snipeit versions.
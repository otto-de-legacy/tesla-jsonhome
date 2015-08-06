# tesla-jsonhome

This library provides a component, that adds an http endpoint 
to [tesla-microservice](https://github.com/otto-de/tesla-microservice)
that will produce a [json-home](http://tools.ietf.org/html/draft-nottingham-json-home-02) document
with links to resources of your microservice. 

[![Build Status](https://travis-ci.org/otto-de/tesla-jsonhome.svg?branch=master)](https://travis-ci.org/otto-de/tesla-jsonhome)

[![Clojars Project](http://clojars.org/de.otto/tesla-jsonhome/latest-version.svg)](http://clojars.org/de.otto/tesla-jsonhome)
[![Dependencies Status](http://jarkeeper.com/otto-de/tesla-jsonhome/status.svg)](http://jarkeeper.com/otto-de/tesla-jsonhome)

## Usage

Because tesla-microservice is a provided dependency, you must always specify two dependencies in your project clj:

```clojure
:dependencies [[de.otto/tesla-microservice "0.1.15"]
               [de.otto/tesla-jsonhome "0.1.0"]]
```

Add the `jsonhome`component to the base-system before starting it. 
```clojure
(assoc (system/base-system runtime-config)
       :jsonhome (component/using (jsonhome/new-jsonhome "/jsonhome/") [:config :handler]))
```
### Adding Resources to the JSON-Home document

Some resources will be added to your Json-Home document by default.

If you have a tesla-microservice config like that:

```INI
status.url=/your-status
health.url=/your-health
```

This Json-Home document would be rendered:

```JSON
{
  "resources": {
    "healthcheck": {
      "href": "/your-health"
    },
    "status": {
      "href": "/your-status"
    }
  }
}
```

You can add additional resources be providing a hash-map as an argument while creating the component:

```clojure
(jsonhome/new-jsonhome "/jsonhome/"  {:doc {:href "http://doc.example.com"
                                            :title "Online Documentation"}})
```

And the rendered Json-Home document would look like this:

```JSON
{
  "resources": {
    "healthcheck": {
      "href": "/your-health"
    },
    "status": {
      "href": "/your-status"
    },
    "doc": {
      "href": "http://doc.example.com",
      "title" "Online Documentation"}
  }
}
```
### Using Link-Relation prefixes

It is common practice to use full-qualified names as keys in the resources hash.
To prefix every resource, you can set `jsonhome.link-rel-prefix` in your config.

For Example this config:

```INI
jsonhome.link-rel-prefix=http://spec.example.com/link-rel/
```

will produce this Json-Home document:

```JSON
{
  "resources": {
    "http://spec.example.com/link-rel/healthcheck": {
      "href": "/health"
    },
    "http://spec.example.com/link-rel/status": {
      "href": "/status"
    }
  }
}
```

## Initial Contributors

Torsten Mangner, Tobias Radtke, Kai Brandes

## Compatibility
Versions `0.1.0` and above of tesla-jsonhome are compatible with versions `0.1.15` and above of tesla-microservice.

## License
Apache License

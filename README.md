# rfqservice

Toy request for quote service.

## Installation

Download from http://example.com/FIXME.

## Usage

Open a repl and evaluate statements in comment in namespaces as
required. For example:

```
$ lein repl
rfqservice.core=> (use 'rfqservice.core)
nil
rfqservice.core=> (.orElse (quote-for (MyRfqService. 0.02M orders) :usd 200) :unfulfilled)
#rfqservice.core.Quote{:bid 232.69, :ask 232.75}
rfqservice.core=>
```

You can run tests using

```
$ lein test
```


## License

Copyright © 2016 Chris Howe-Jones

Distributed under the Eclipse Public License version 1.0

datadog-metrics-java
====================

Metadata
--------
Status
  Under Development
Type
  Library
Versioning
  Semantic Versioning
Maintainers
  `CODEOWNERS.txt <CODEOWNERS.txt>`_

Overview
--------

This library attempts to bridge the gap between the metric functionality
provided by `Dropwizard metrics <https://github.com/dropwizard/metrics>`_ and
Datadog approach to tagging in a way that less convoluted than that provided by the
`metrics-datadog <https://github.com/coursera/metrics-datadog>`_ library.

dropwizard/metrics vs.java-dogstatsd-client
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

There are already two existing libraries for sending metrics to Datadog
from JVM apps which the role and approach of this library are best
expressed in terms of.
`java-dogstatsd-client <https://github.com/Datadog/java-dogstatsd-client>`_
provides a simple and clean way to send tagged events to the Datadog agent where the agent will handle aggregation.
The key benefits that are absent from that library
but which are provided by the dropwizard library are:

- Scheduled publishing of gauge metrics
- A standard set of metrics and instrumentation
- Convenience functions to instrument code for metrics collection (i.e. timing)

This library therefore aims to add those features on top of the dogstatsd
client (often by _borrowing_ them from Dropwizard).

Lightweight
"""""""""""
This library aims to add those specific pieces of functionality and convenience
that are missing from java-dogstatsd-client, it does _not_ attempt to
provide a rich framework for metrics. The design will revolve
around using and exposing the interface provided by that client rather than
foisting structure and opinions on top of it.

Time Aggregation
""""""""""""""""

Dropwizard also does time aggregation internally so that all 
metrics are published on the same schedule in the form which will
be sent to Datadog. Such aggregation will be left to the Datadog
agent because:

- it's there
- it makes this piece a lot simpler
- it encourages apples-to-apples aggregations

Contribute
----------

- Issue Tracker: https://github.com/brightcove/datadog-metrics-java/issues
- Source Code: https://github.com/brightcove/datadog-metrics-java

Support
-------

If you are having issues, please file an
`issue <https://github.com/brightcove/datadog-metrics-java/issues>`_.

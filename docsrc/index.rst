datadog-metrics-java
####################

.. toctree::
   :maxdepth: 2
   :caption: Contents:

Introduction
************

.. todo:: Move most of this block to a page for specifics
	  
The most powerful means of organizing and manipulating metrics data in Datadog
is through the use of the *tags* mechanism that is provided. However the more
polished Java metric libraries (such as Dropwizard) are oriented more towards
using variations in metric names rather than tags. Datadog itself is also not a
Java shop, so their provided Java functionality is fairly rudimentary. This has
resulted in a choice between using the basic Datadog library and foresaking the
conveniences of the Dropwizard library, or adapting Dropwizard to
conform to the tag based model (such as the Coursera library) thereby introducing
additional complexity on top of what is already there.
The concerns around waste are heightened by the fact that much of the
functionality provided by the Dropwizard library itself are not needed when
using the Datadog agent, and so the final product is far heavier than required
and distant from the standard Datadog client.

**Caveat:** This library and the motivations described above are focused on
Datadog being the sole destination for metrics. The abstraction provided by
the Dropwizard metrics library would be very valuable when targeting
heterogeneous metric stores.

Installation
************

Gradle
======

Add the BC Nexus repository and a compile time dependency on this project:

.. code-block:: groovy

   repository {
     maven { url 'http://nexus.vidmark.local:8081/nexus/content/groups/public' }
   }

   dependencies {
     compile 'com.brightcove.com:datadog-metrics-java:$release'
   }

Where ``$release`` is replaced by the desired release, for this version::

.. parsed-literal::

   com.brightcove.com:datadog-metrics-java:|release|
   
   
Indices and tables
******************

* :ref:`genindex`
* :ref:`modindex`
* :ref:`search`

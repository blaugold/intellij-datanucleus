# IntelliJ Plugin for DataNucleus Runtime Enhancement

This plugin adds a tab to java run configurations where DataNucleus runtime
enhancement can be enabled. The plugin looks for the `datanucleus-core` jar
in the classpath of the module which is the context for the run configuration. 

## Installation

Click on `Browse Repositories` in `Plugins` and search for `DataNucleus Runtime Enhancement`.

## Usage

First make sure to read the docs on 
[runtime enhancement](http://www.datanucleus.org/products/accessplatform/jdo/enhancer.html#runtime).

Per default enhancement is disabled.
To enable runtime enhancement, create a run configuration and go to the tab `Datanucleues Enahncer`.
Here you can enable runtime enhancement.

To speed up enhancement and startup time, reduce the number of classes scanned by specifying the
root packages of all your persistence capable classes.
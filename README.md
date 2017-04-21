# JacORB #

[![Build Status](https://travis-ci.org/JacORB/JacORB.svg?branch=master)](https://travis-ci.org/JacORB/JacORB)
[![Maven Central](https://img.shields.io/maven-central/v/org.jacorb/jacorb.svg)](https://maven-badges.herokuapp.com/maven-central/org.jacorb/jacorb)
[![Javadocs](http://www.javadoc.io/badge/org.jacorb/jacorb.svg)](http://www.javadoc.io/doc/org.jacorb/jacorb)

JacORB is a [freely licensed](https://raw.githubusercontent.com/JacORB/JacORB/master/doc/LICENSE) java implementation of the OMG's [CORBA](http://www.omg.org) standard. The main home page is [here](http://www.jacorb.org). Mailing lists are available [here](http://www.jacorb.org/contact.html) and the Bugzilla for issues is [here](http://www.jacorb.org/bugzilla).



## Installation
JacORB should work under all supported Java platforms (See [here](http://www.oracle.com/technetwork/java/javase/system-configurations-135212.html) and [here](http://www.oracle.com/technetwork/java/javase/config-417990.html)). This release has been tested on Linux, Mac, and Windows.

See also the current [ProgrammingGuide.pdf](http://www.jacorb.org/documentation.html) for more details.

### Prerequisities
 * Java 1.6 or later
 * Maven 3.0.4 or later for building JacORB / running the tests

### Libraries and Scripts
Useful scripts are available in the 'bin' directory. The JacORB libraries are stored in the lib directory for the binary distribution.

### Configuration File
For more details see Chapter 3 of the ProgrammingGuide. In the simplest case, the template `etc/jacorb.properties_template` file may be copied to a directory on your classpath. The paths and logging information should be updated.

### Getting started
There are a number of examples in the demo directory. For more information look at their individual README files and the ProgrammingGuide.

## Building Source

JacORB may be built via Maven using standard Maven commands e.g. `mvn clean install -DskipTests=true`. Note that the install phase must be executed so subsequent module builds find the results of preceeding modules. The project may be
imported into Eclipse or IntelliJ using standard import commands.

:bulb: The JacORB Core and Regression Test module utilises Endorsed Directories to build within the maven-compiler-plugin. In Eclipse, the JBoss Tools Endorsed Libraries Plugin must be installed for this to work.

## Contributing

See the [CONTRIBUTING.md](CONTRIBUTING.md) document.

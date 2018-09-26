# Prototype Gene Homology Service

This repo contains a prototype of a service for searching a protein database given a protein
sequence.

Build status (master):
[![Build Status](https://travis-ci.org/kbaseIncubator/GeneHomologyPrototype.svg?branch=master)](https://travis-ci.org/kbaseIncubator/GeneHomologyPrototype)
[![codecov](https://codecov.io/gh/kbaseIncubator/GeneHomologyPrototype/branch/master/graph/badge.svg)](https://codecov.io/gh/kbaseIncubator/GeneHomologyPrototype)

## Usage

Protein sequence databases are organized by namespaces, where there is a 1:1 relationship between
a database and a namespace. A namespace has the following properties:
* A unique string ID set by the creator of the namespace
* A string uniquely identifying the source of the data (e.g. JGI, KBase, etc)
* A string uniquely identifying the source database within the data source
* An optional free text description
* The implementation used to create the sequence database (e.g. LAST, BLAST)
  
Note that searches against a namespace **may not be reproducible over time**. 

The service is expected to contain <1000 namespaces, although there is no hard limit.

Most input strings do not allow empty strings and have a maximum size of 256 unicode code points.

## Requirements

Java 8 (OpenJDK OK)  
Apache Ant (http://ant.apache.org/)  
Jetty 9.3+ (http://www.eclipse.org/jetty/download.html)
    (see jetty-config.md for version used for testing)  
This repo (git clone https://github.com/kbaseIncubator/GeneHomologyService)  
The jars repo (git clone https://github.com/kbase/jars)  
The two repos above need to be in the same parent folder.

## Build

```
cd [gene homology repo directory]
ant build
```

## Sequence database

The LAST sequence database to use and the namespace details are configured in the 'deploy.cfg'
file. The namespace details file expects a YAML format and has the following keys:

```
id: mynamespace
datasource: KBase
sourcedatabase: CI Refdata
description: some reference data
moddate: 1537919196
```

`id` is the id of the namespace. This is an arbitrary string consisting of ASCII alphanumeric
characters and the underscore, with a maximum length of 256 Unicode code points.

`datasource` is an identifier for the source of the data, like KBase or JGI.

`sourcedatabase` (optional) is an identifier for the database within the `datasource` from
which the sketch database was generated. If `sourcedatabase` is omitted the value `default`
is used.

`description` (optional) is a free text description of the namespace.

`lastmod` is the last modification date of the database in epoch seconds. This information is
persisted here due to the lack of a database in this prototype. It will be removed in the future.
It should be manually updated when the LAST database is updated.

On startup, the service reads the namespace file and LAST database file.

## Start service

ensure `lastal` is available on the system path  
cd into the gene homology repo  
`ant build`  
copy `deploy.cfg.example` to `deploy.cfg` and fill in appropriately  
`export GENE_HOMOLOGY_CONFIG=<path to deploy.cfg>`  
OR  
`export KB_DEPLOYMENT_CONFIG=<path to deploy.cfg>`  

`cd jettybase`  
`./jettybase$ java -jar -Djetty.http.port=<port> <path to jetty install>/start.jar`  

## API

`GET /`

General server information including git commit, version, and server time.

`GET /namespace`

List all namespaces.

`GET /namespace/<namespace id>`

Returns information about a specific namespace.

```
POST /namespace/<namespace id>/search
```

Performs a search with a sequence in FASTA format provided in the `POST` body against the
database associated with the given namespace. `curl -T` is useful for this:  
`curl -X POST -T UniRef50_A0A257EYX4.fasta http://localhost:8080/namespace/lastns/search`  
Currently the input FASTA must contain only one sequence.  

## What makes this a prototype?

* There's no database support
* There's no data loader
* It currently only supports one namespace, and therefore one sequence database
  * The API is structured to allow fixing this
* It only supports LAST
  * The API is structured to allow fixing this
* There are virtually no tests
* The documentation is minimal
* The code was written in a hurry and may not follow best practices.
 
 ## Developer notes

### Adding and releasing code

* Adding code
  * All code additions and updates must be made as pull requests directed at the develop branch.
    * All tests must pass and all new code must be covered by tests.
    * All new code must be documented appropriately
      * Javadoc
      * General documentation if appropriate
      * Release notes
* Releases
  * The master branch is the stable branch. Releases are made from the develop branch to the master
    branch.
  * Update the version as per the semantic version rules in `src/us/kbase/genehomoloy/api/Root.java`.
  * Tag the version in git and github.

### Running tests

* There aren't any, really, but...
* `ant test`

### UI

Most text fields are arbitrary text entered by a data uploader. These fields should be
HTML-escaped prior to display.
  
Use common sense when displaying a field from the server regarding whether the field should be
html escaped or not.
  
### Exception mapping

In `us.kbase.Genehomology.core.exceptions`:  
`GeneHomologyException` and subclasses other than the below - 400  
`NoDataException` and subclasses - 404  

`JsonMappingException` (from [Jackson](https://github.com/FasterXML/jackson)) - 400  

Anything else is mapped to 500.

## TODO

* Productionize
  * max return count with upper limit
* Search namespaces (no free text search)
* HTTP2 support
* Other seqsearch implementations?

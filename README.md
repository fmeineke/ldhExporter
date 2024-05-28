***SeekExporter***

Simple REST-API for transforming FAIRDOM Seek investigations with NFDI4Health defined MDS extended metadata

**Parameter**
- format=[seek|csh|xml|cshval] (Default:csh)
- id=[investigations|studies|projects]/<number>

All data is retrieved from https://ldh.zks.uni-leipzig.de.

**Examples**
- https://ldh-test.zks.uni-leipzig.de/export?id=projects/2
- https://ldh-test.zks.uni-leipzig.de/export?id=projects/2&format=seek
- https://ldh-test.zks.uni-leipzig.de/export?id=projects/2&format=seekxml
- https://ldh-test.zks.uni-leipzig.de/export?id=projects/2&format=cshxml
- https://ldh-test.zks.uni-leipzig.de/export?id=projects/2&format=csh
- https://ldh-test.zks.uni-leipzig.de/export?id=projects/2&format=cshval
- https://ldh-test.zks.uni-leipzig.de/export?id=projects/2&format=xml

Version 12.2023 © F. Meineke for NFDI4Health

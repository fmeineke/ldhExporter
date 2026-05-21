***SeekExporter***

Simple REST-API for transforming FAIRDOM Seek projects with NFDI4Health defined MDS extended metadata
This service is intended to be used with https://github.com/nfdi4health/ldh-deployment

**Parameter**
- format=[seek|csh|xml|cshval] (Default:csh)
- id=[investigations|studies|projects]/<number>


**Examples**
All example data is retrieved from https://ldh.zks.uni-leipzig.de.

- https://ldh-test.zks.uni-leipzig.de/export/projects/21
- https://ldh-test.zks.uni-leipzig.de/export/projects/21&format=seek
- https://ldh-test.zks.uni-leipzig.de/export/projects/21&format=seekxml
- https://ldh-test.zks.uni-leipzig.de/export/projects/21&format=cshxml
- https://ldh-test.zks.uni-leipzig.de/export/projects/21&format=csh
- https://ldh-test.zks.uni-leipzig.de/export/projects/21&format=cshval
- https://ldh-test.zks.uni-leipzig.de/export/projects/21&format=xml

Version 27.02.2026 © F. Meineke for NFDI4Health




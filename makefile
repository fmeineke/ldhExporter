IMAGE_TAG=fmeineke/ldh-exp:v2.8
.PHONY: run-compose

jar:
	mvn install dependency:copy-dependencies -DoutputDirectory=target/lib
	#mvn install dependency:copy-dependencies -Dmaven.test.skip=true -DoutputDirectory=target/lib

docker: 
	make clean all build-image push-image

run-class:
	export LDH_SOURCE="https://ldh.zks-mhh.imise.uni-leipzig.de" && java -cp 'target/classes:target/lib/*' imise.LDHExport

run-jar: jar
	export LDH_SOURCE="https://ldh.zks-mhh.imise.uni-leipzig.de" && java -jar target/LDHExport-1.0.jar

image: jar
	docker build . -t $(IMAGE_TAG)

push-image:
	docker push $(IMAGE_TAG)

run-image:
	docker run -p 8083:8083 $(IMAGE_TAG)

run-compose:
	echo "IMAGE=${IMAGE_TAG}" > .env
	docker compose up

unused:
	mvn dependency:analyze -DignoreUnusedRuntime=true

update:
	mvn versions:display-dependency-updates

login: 
	docker login -u fmeineke

clean: 
	rm -rf target/*

IMAGE_TAG=fmeineke/ldh-exp:v2.7

build-jar:
	mvn install dependency:copy-dependencies -DoutputDirectory=target/lib
	#mvn install dependency:copy-dependencies -Dmaven.test.skip=true -DoutputDirectory=target/lib

docker: 
	make clean all build-image push-image

run-server:
	#export LDH_SOURCE=https://ldh.zks.uni-leipzig.de && java -cp 'target/classes:target/lib/*' imise.LDHExport
	export LDH_SOURCE="https://ldh.zks-mhh.imise.uni-leipzig.de" && java -cp 'target/classes:target/lib/*' imise.LDHExport
run-jar: build-jar
	export LDH_SOURCE="https://ldh.zks-mhh.imise.uni-leipzig.de" && java -jar target/LDHExport-1.0.jar
build-image: build-jar
	docker build . -t $(IMAGE_TAG)
push-image:
	docker push $(IMAGE_TAG)

run-image:
	docker run $(IMAGE_TAG)

run-compose:
	docker compose up

unused:
	mvn dependency:analyze -DignoreUnusedRuntime=true
update:
	mvn versions:display-dependency-updates

login: 
	docker login -u fmeineke
clean: 
	rm -f target/lib/*
	rm -rf target/*

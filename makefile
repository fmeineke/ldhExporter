all:
	mvn install dependency:copy-dependencies -Dmaven.test.skip=true -DoutputDirectory=target/lib

run-server:
	export LDH_SOURCE=https://ldh.zks.uni-leipzig.de && java -cp 'target/classes:target/lib/*' imise.MyServer
run-jar:
	java -jar target/LDHExport-1.0.jar
build-image: 
	docker build . -t fmeineke/ldh-exp:v1
push-image:
	docker push fmeineke/ldh-exp:v1
run-image:
	docker run -p 8321:8321 -e LDH_EXP=http://localhost:8321 -e LDH_SOURCE=https://ldh.zks.uni-leipzig.de fmeineke/ldh-exp:v1

unused:
	mvn dependency:analyze -DignoreUnusedRuntime=true
update:
	mvn versions:display-dependency-updates
clean: 
	rm -f target/lib/*

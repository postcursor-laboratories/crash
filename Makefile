PB=protoc
JC=javac
JAVA=java
CLASSPATH=./lib/jbox2d-library-2.1.2.2.jar:./lib/slf4j-api-1.6.4.jar:./lib/protobuf-java-2.6.1.jar:./bin
JCFLAGS=-cp $(CLASSPATH) -d ./bin

all: build

build: buildjava

buildproto: .protobuf-generated
	$(PB) -I=protobuf/ --java_out=.protobuf-generated/ protobuf/*.proto

buildjava: bin buildproto
	$(JC) $(JCFLAGS) src/*.java .protobuf-generated/*/*.java

.protobuf-generated:
	mkdir .protobuf-generated

bin:
	mkdir bin

run: build
	$(JAVA) -cp $(CLASSPATH) CrashClient

runserver: build
	$(JAVA) -cp $(CLASSPATH) CrashServer

clean:
	rm -rf bin
	rm -rf .protobuf-generated


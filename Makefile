PB=protoc
JC=javac
JAR=jar
JAVA=java
CLASSPATH=./lib/jbox2d-library-2.1.2.2.jar:./lib/slf4j-api-1.6.4.jar:./lib/protobuf-java-2.6.1.jar:./bin
JCFLAGS=-cp $(CLASSPATH) -d ./bin
JARFLAGS=cf
JARFILE=out/crash.jar
APP=Crash.app
APPRES=$(APP)/Contents/Resources
APPJAVA=$(APPRES)/Java

# Building targets

all: build

build: buildjava

buildproto: .protobuf-generated
	$(PB) -I=protobuf/ --java_out=.protobuf-generated/ protobuf/*.proto

buildjava: bin clearbin buildproto
	$(JC) $(JCFLAGS) src/*.java .protobuf-generated/*/*.java

app: jar cleanapp
	unzip -d $(APP) .crashapp.zip
	cp OSXIcon.icns $(APPRES)/icon.icns || true # we don't care if there's no icon
	cp $(JARFILE) $(APPJAVA)/app.jar
	cp -r lib $(APPJAVA)/
	python makehelp/finishapp.py $(CLASSPATH) CrashClient

jar: buildjava cleanjar out
	$(JAR) $(JARFLAGS) $(JARFILE) -C bin .

# Running targets

run: build
	$(JAVA) -cp $(CLASSPATH) CrashClient

runserver: build
	$(JAVA) -cp $(CLASSPATH) CrashServer

# Directory creation targets

.protobuf-generated:
	mkdir .protobuf-generated

bin:
	mkdir bin

out:
	mkdir out

# Clearing/Cleaning targets

clearbin: # avoids leaving renamed/deleted classes
	rm -rf bin/*

clean: cleanjar cleanapp
	rm -rf bin
	rm -rf .protobuf-generated

cleanjar:
	rm -f $(JARFILE)

cleanapp:
	rm -rf $(APP)


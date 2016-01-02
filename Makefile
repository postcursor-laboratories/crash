JC=javac
JAVA=java
CLASSPATH=./lib/jbox2d-library-2.1.2.2.jar:./lib/slf4j-api-1.6.4.jar:./bin
JCFLAGS=-cp $(CLASSPATH) -d ./bin

all: bin
	$(JC) $(JCFLAGS) src/*.java

bin:
	mkdir bin

run:
	$(JAVA) -cp $(CLASSPATH) CrashClient

runserver:
	$(JAVA) -cp $(CLASSPATH) CrashServer

clean:
	rm -rf bin


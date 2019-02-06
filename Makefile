all: CSftp.jar
CSftp.jar: CSftp.java
	javac CSftp.java
	jar cvfe CSftp.jar CSftp *.class


run: CSftp.jar  
	java -jar CSftp.jar ftp.cs.ubc.ca  21

clean:
	rm -f *.class
	rm -f CSftp.jar

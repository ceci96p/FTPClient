# FTPClient
Simple FTP Client Program that provides FTP UDP client functionality through the command-line interface implemented in 
Java and Java Socket related classes. Project developed for a computer science course.

After compiling in the command-line please run:  java -jar CSftp.jar (ip # or domain name of the server) (port # to connect from),for example:

```java -jar CSftp.jar 127.0.0.1 21```

The client can send commands to the server like:

Username: ```USER (any username)```

Password: ```PW (any password)```

Retrieve file: ```GET (file name)```

Request set of features/extensions: ```FEATURES```

Change working directory: ```CD (directory name)```

Retrieve list of files: ```DIR```

Quit conection: ```QUIT```

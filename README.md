# FTPClient
Simple FTP Client Program that provides FTP UDP client functionality through the command-line interface implemented in 
Java and Java Socket related classes. Project developed for computer science course.

After compiling in the command-line please run:  java CSftp (ipv4 or domain name of the server) (port # to connect from) 
for example:

```java CSftp 127.0.0.1 21```

The client accepts to execute commands like:

Username: ```USER cs317```

Change working directory: ```CWD (directory name)```

Change to parent directory: ```CDUP```

Name list of current working directory: ```NLST```

Quit conection: ```QUIT```

File transfer type: ```TYPE (a or i)``` - ascii or binary type only

Transmission mode: ```MODE (s)``` - stream mode only

File structure: ```STRU (f)``` - file structure only

Retrieve file: ```RETR (file name)```

Passive mode: ```PASV```



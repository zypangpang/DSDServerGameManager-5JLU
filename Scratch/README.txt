How to start up the server and connect as a client

Uses:
- ClientText.java
- ServerGameManager.java

*Starting Up The Server*
1) Open up a Terminal window and cd into the /Scratch directory in our repo
2) Compile ServerGameManager.java with `javac ServerGameManager.java`
3) It should compile with no print statements
4) Start up the server with `java ServerGameManager`

*Connecting as the Client*
1) Now that the server is running, you need to open a new terminal window
2) cd to the /Scratch directory again
3) Compile ClientText.java with `javac ClientText.java`
3) It should compile with no print statements
4) Run with `java ClientText`
5) It will prompt you for an IP Address. Leave it blank and press enter to connect locally.
6) This should prompt you with a client window that says, "Hello, you are client #..."
7) Verify that the window running the server has printed out "New connection with client#..."
8) In the client window, you can ping the server with a string and it will be returned in all uppercase
9) Send "." to end your session with the server
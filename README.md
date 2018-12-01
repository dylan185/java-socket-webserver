# java-socket-webserver
Computer Networks Lab #1 - Create a Java Web Server using Sockets

In this assignment I created a Java Web Socket server that will take incoming http requests 
on local host. Depending on the incoming request it will send the corrisponding response. 
The only three responses required for this assignment were the base "OK - 200, 
Moved Permanently - 301, and Object Not Found - 404". When sending the right request
which is a valid picture name (`/testing_pic.png`) the "OK - 200" response will be sent and 
the requested picture will be shown. If a request for another valid picture except with 
a invalid path, like `/testing/foo/testing_pic.png`, the "Moved Permanently - 301" response 
will be sent and the user will be redirected to the correct address. If a invalid request is
recieved, like `/foo_pic.png`, then the "Object Not Found - 404" response will be sent.

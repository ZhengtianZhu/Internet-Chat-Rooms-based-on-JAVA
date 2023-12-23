# 2022-10-31

注意：本项目只在单主机运行调试过，没试过在局域网和不同主机之间接发消息和文件（估计不行），有需要的自行查阅资料。

Run the server first and then the client. The client should receive the "" message from the server and display it on the console.
We have two files including Server.java and Client.java

As for the Client,
symbol means different function.
I use the sql to process the data of the string.
Maybe I need to initialize something to start some other functions, anyway.

1. Overview of the Java Chat Application
The Java Chat application I am going to build is a console application that is launched from the command line. I use MySQL to store the client login information, and the server will participate in each function. Besides, each meassage will be processed by the server. 

2. The programme runs in the console in the same computer.

多线程实现Server和Client的收发同步：
A symbol means 标志位-num to label different functions, i.e., functions in the requirement list.
Server主循环接收Client的Socket连接， the server will use multiple threads to connect each Client.
Client启动的客户端，依据题目需要来运行程序；
client通过多线程创建新的客户端

Requirement list: 

/login: If it is the first time that the client logins, then the client will register its information, otherwise the server will check whether the login information of the client is right.
*login.length()>=7)//首先名字长度得有, it must be larger than 7; there is no need to input the password, because this program does not have this function.

我的设计理念：上线了，构造函数自动给client 赋值生命Connected=true;
该属性随着client的quit()消失而消失；

/to : meaning broadcast the message to all other clients.

/to (client N, +message): meaning two clients talk privately. For example, client A starts the message "/to B I want to date with you", then the message will send to the client B, and it will receive the message " A: I want to date with you".

/quit: the client go offline.



history:

who: calculate the total number of the online clients.


SOCKET：
3.1. What Is “Connection Timed Out”?
For establishing a connection to the server from the client-side, the socket constructor is invoked, which instantiates a socket object. The constructor takes the remote host address and the port number as input arguments. After that, it attempts to establish a connection to the remote host based on the given parameters.

The operation blocks all other processes until a successful connection is made. However, if the connection isn’t successful after a certain time, the program throws a ConnectionException with a “Connection timed out” message:

https://www.baeldung.com/java-socket-connection-read-timeout

time_out is made by JAVA

https://reintech.io/blog/java-network-programming-creating-managing-sockets










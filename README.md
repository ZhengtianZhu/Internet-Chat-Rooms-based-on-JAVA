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

我更是按照视频，做了逻辑字段的处理，主要的代码，都还是借鉴尚硅谷的旧版视频的；

原理解释：
从连接上，TCP/IP established, then the client write DataStream into the buff; Server 端的accept方法一直到客户端启动并向服务器发出请求为止一直在等待。但注意，此方法不支持多个客户端同时访问 simultaneously. //据说，即使server不accpet(), TCP has established. There is data transmitting.
Java这里是怎么捕获client的socket的，为啥不会捕获错误？如何识别client1和client2，咱的client port设置的不都是一样的？多线程的client怎么标记的呢？

1. 函数原型
accept函数允许在套接字上进行传入连接尝试。

SOCKET WSAAPI accept(
  SOCKET   s,
  sockaddr *addr,
  int      *addrlen
);

listen监听客户端来的链接，accept将客户端的信息绑定到一个socket上，也就是给客户端创建一个socket，通过返回值返回给我们客户端的socket。//这里提到的是，信息绑定

一次只能创建一个，有几个客户端链接，就要调用几次。
————————————————
版权声明：本文为CSDN博主「超级D洋葱」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
原文链接：https://blog.csdn.net/u014779536/article/details/115834445


We use the socket to locate each client;
We have a list to accomdate clients 










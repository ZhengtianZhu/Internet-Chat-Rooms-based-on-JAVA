# 2022-10-31
考研比如2022年12月考完，2023年3月初试，几个月的时间，不太需要本科毕业生做这么高难度的、功能完善的事儿。

注意：本项目只在单主机运行调试过，没试过在局域网和不同主机之间接发消息和文件（），有需要的自行查阅资料。

For the sake of simplicity, we’ll run our client and server programs on the same computer. If we were to execute them on different networked computers, the only thing that would change is the IP address. In this case, we’ll use localhost on 127.0.0.1.
https://www.baeldung.com/a-guide-to-java-sockets#:~:text=Socket%20clientSocket%20%3D%20serverSocket.accept%20%28%29%3B%20When%20the%20server,everything%20goes%20well%2C%20the%20server%20accepts%20the%20connection.

# How to Run the application

Run the server first and then the client. The client should receive the "" message from the server and display it on the console.
We have two files including Server.java and Client.java

## As for the Client,
symbol means different function.
I use the sql to process the data of the string.
~~Maybe I need to initialize something to start some other functions, anyway.~~

1. Overview of the Java Chat Application
The Java Chat application I am going to build is a console application that is launched from the command line. I use MySQL to store the client login information, and the server will participate in each function. Besides, each meassage will be processed by the server. 

2. The program runs in the console in the same computer.

多线程实现Server和Client的收发同步：
A symbol means 标志位-num to label different functions, i.e., functions in the requirement list.
Server主循环接收Client的Socket连接， the server will use multiple threads to connect each Client.
Client启动的客户端，依据题目需要来运行程序；
client通过多线程创建新的客户端

# Requirements list: 

消息都是以空格隔开的，所以我这里用readUTF()来读一行行消息
eg. Bob hi 1 Alice,
"Bob" is the message sender, "hi" is the message,"1" means to ,"Alice" means the target receiver.
For the client "Alice", "hi" should appear on her console screen.

/login: If it is the first time that the client logins, then the client will register its information, otherwise the server will check whether the login information of the client is right.
*login.length()>=7)//首先名字长度得有, it must be larger than 7; there is no need to input the password, because this program does not have this function. 
//如果输错，要一直循环等待输入，且quit指令还没放入login;配合mysql


我的设计理念：上线了，构造函数自动给client 赋值生命Connected=true;
该属性随着client的quit()消失而消失；
查询client是否在线，则每次都去数据库里看看当前client是Online or quit；

/to : meaning broadcast the message to all other clients.

/to (client N, +message): meaning two clients talk privately. For example, client A starts the message "/to B I want to date with you", then the message will send to the client B, and it will receive the message " A: I want to date with you".

/quit: the client go offline.

/history:

who: calculate the total number of the online clients.

# My design
每出现一个新功能，就多一个sym标志位的数字，来检验新功能，于是有了很多if else

## SOCKET：
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
jdbc connects the local database to store the status of the client, like whether the client is online or has quitted.

For the server,      
we use the following methods to read and write for the data stream.
dis = new DataInputStream(socket.getInputStream());            
dos = new DataOutputStream(socket.getOutputStream());

We use bConnected to judge whether a thread is alive at present.

## JDBC连接
Server and client all need to connect to the database. They all need to register and connect, thus, TCP/IP has some function here, and we can easily understand why we need to write down those codes.  <br>

一：JDBC

 sun：提供了一套通用性的接口：可以连接任何的数据库： 
 连接数据库的具体得到实例，具体的数据库厂商实现的。 
 
 连接数据的步骤（别忘了复制jar包）:( 
 1）注册驱动： Class.forName（）：DriverManager 
 （2）获得链接对象：Connection 
 （3）创建sql容器：语句： 
 （4）执行sql语句:: stmt
 （5）查询操作：遍历结果集：ResultSet 
 （6）关闭资源： 


 如果是软编码的话步骤如下：

1个方言文件内容：
————————————————
版权声明：本文为CSDN博主「东方-教育技术博主」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
原文链接：https://blog.csdn.net/qq_41517071/article/details/84615765
# 搜狐北京 运维开发校招面试
4. Read Timed Out  <br>
4.1. What Is “Read Timed Out”?
The read() method call in the InputStream blocks until it finishes reading data bytes from the socket. The operation waits until it reads at least one data byte from the socket. However, if the method doesn’t return anything after an unspecified time, it throws an InterrupedIOException with a “Read timed out” error message:













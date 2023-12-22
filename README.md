# 2022-10-31

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
*login.length()>=7)//首先名字长度得有, it must be larger than 7

我的设计理念：上线了，构造函数自动给client 赋值生命Connected=true;
该属性随着client的quit()消失而消失；

/to : meaning broadcast the message to all other clients.

/to (client N, +message): meaning two clients talk privately. For example, client A starts the message "/to B I want to date with you", then the message will send to the client B, and it will receive the message " A: I want to date with you".

/quit: the client go offline.



history:

who: calculate the total number of the online clients.











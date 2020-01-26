package tk;


import java.awt.event.ActionEvent;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;

//主线程监听，此线程收发消息；客户端下线，届时再说？
public class Server {
    public static void main(String[] args){
        new Server().listenClient();
    }

    private Map<Integer, Socket> clients1 = new HashMap<Integer, Socket>();
    private List<mythread> clients=new ArrayList<>() ;
    private boolean started=false;
    private DataOutputStream dos=null;

    public Server() {
       started=true;
    }

    //非常熟悉的监听
    public void listenClient(){
        int port = 12345;
        String temp = "";
        try {
            ServerSocket server = new ServerSocket(port);
            // server尝试接收其他Socket的连接请求，server的accept方法是阻塞式的
            int num=0;
            while (started) {
                System.out.printf("服务器端正在监听 %d\n",num++);
                Socket socket = server.accept();
                clients1.put(socket.getPort(), socket);
                temp = "客户端"+socket.getPort()+":连接";
                System.out.println(temp);
                mythread c = new mythread(socket);
                clients.add(c);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void write(String str){
        try {
            dos.writeUTF(str);
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void sendMsgToAll(Socket fromSocket, String msg,String name) {
        Set<Integer> keset = this.clients1.keySet();
        java.util.Iterator<Integer> iter = keset.iterator();

        while(iter.hasNext()){
            //循环，依次找出非本客户端的来
            int key = iter.next();
            Socket socket = clients1.get(key);
            if(socket != fromSocket){
                try {
                    if(socket.isClosed() == false){
                        if(socket.isOutputShutdown() == false){

                            dos = new DataOutputStream(socket.getOutputStream());
                            write(name);
                            write(msg);
                            /*Writer writer = new OutputStreamWriter(
                                    socket.getOutputStream());
                            writer.write(msg);
                            writer.flush();*/

                        }

                    }
                } catch (SocketException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

        }
    }
    //需要行动的，
    public void actionPerformed(ActionEvent e) {
        String temp = "";
        if("sendMsg".equals(e.getActionCommand())){
            System.out.println("开始向客户端群发消息");
            Set<Integer> keset = this.clients1.keySet();
            java.util.Iterator<Integer> iter = keset.iterator();
            while(iter.hasNext()){
                int key = iter.next();
                Socket socket = clients1.get(key);
                try {

                    if(socket.isClosed() == false){
                        if(socket.isOutputShutdown() == false){
                            //想客户端写东西
                            temp = "向客户端"+socket.getPort()+"发送消息";
                            System.out.println(temp);
                            Writer writer = new OutputStreamWriter(
                                    socket.getOutputStream());
                           /* this.apppendMsg(temp);*/
                            writer.write("来自服务器的问候");
                            writer.flush();
                        }
                    }
                } catch (SocketException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    class mythread implements Runnable{
        private HashMap<String, String> mp = new HashMap<>();
        private Socket socket = null;
        private Server server = null;
        private DataInputStream dis = null;
        private DataOutputStream dos=null;
        private String temp = null;
        private String str = null;
        private String name = null;
        private boolean bConnected=false;

        //先记住吧，
        public mythread(Socket socket) {
            bConnected=true;
            this.socket = socket;
//            this.server = server;

        }

        private void init(){
//System.out.println("init 1");
            try {
                dis = new DataInputStream(socket.getInputStream());
                dos = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            mp.put("hi","向大家打招呼，“Hi，大家好！我来咯~”");
            mp.put("hi ", "打招呼：“Hi，你好啊~”");
        }

        private String read(){
            try {
                str=dis.readUTF();
//System.out.println(temp);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return str;
        }
        private void write(String str){
            try {
                dos.writeUTF(str);
                dos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        private void checkName(){
            boolean ok=false;
            temp=read();
//mysql设计了
            write("true");

        }
        private void sendToAll(String str){

            write(name);
            write(str);
        }
        @Override
        public void run() {
            init();
            checkName();
            System.out.println("子线程开始工作");
            while(bConnected){
                try {
//                    System.out.println("线程"+this.getId()+":开始从客户端读取数据——>");
                    //不停读取数据
                    while ( (name=dis.readUTF())!= null) {
                        temp=read();
                        if(temp.length()>2){
                            if(temp.charAt(0)=='/'&&temp.charAt(1)=='/'){
                                str=temp.substring(2);
                                //扩展，那么就是遇到空格自动回退的字符串切割；再者就是换行符的影响
                                if(str.substring(0,2).equals("hi")){
                                    if(str.length()>2){

                                    }else {
                                        write(mp.get("hi"));
                                    }
                                }
                            }else if(temp.charAt(0)=='/'&&temp.charAt(1)!='/'){

                            }else {

                            }
                        }else {
                            //广播的消息
                        }
                        //发给所有客户端先
                        server.sendMsgToAll(null,temp,name);
                        System.out.println("来自客户端"+socket.getPort()+"的消息:" +temp);
//                        server.sendMsgToAll(this.socket, "客户端"+socket.getPort()+"的说:" +temp);
                    }

                    //关闭死亡线程
                    if(socket.getKeepAlive() == false){
                        dis.close();
                        socket.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        dis.close();
                        socket.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
    }
}



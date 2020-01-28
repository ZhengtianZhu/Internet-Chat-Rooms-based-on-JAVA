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
    private ServerSocket ss=null;
    public Server() {
       started=true;
    }

    //非常熟悉的监听
    public void listenClient(){
        int port = 12345;
        String temp = "";
        try {
             ss= new ServerSocket(port);
            // server尝试接收其他Socket的连接请求，server的accept方法是阻塞式的
            int num=0;
            while (started) {
                System.out.printf("服务器端正在监听 %d\n",num++);
                Socket socket = ss.accept();
//                clients1.put(socket.getPort(), socket);
                System.out.println("客户端"+socket.getPort()+":连接");
                mythread c = new mythread(socket);
                clients.add(c);
                new Thread(c).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                ss.close();
            } catch (IOException e) {
                e.printStackTrace();
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

    //所有读写操作都放线程里了
    class mythread implements Runnable{
        private HashMap<String, String> mp = new HashMap<>();
        private Socket socket = null;
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

        private String checkName(){
            boolean ok=false;
            temp=read();
//mysql设计了
            write("true");
            return temp;
        }
        private void close(){
            try {
                dos.close();
                dis.close();
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        @Override
        public void run() {
            init();
            //如果输错，要一直循环等待输入，且quit指令还没放入login
            str=checkName();
            write(str);
            System.out.println("子线程开始工作");

            try {
            //不停读取数据

                while ( bConnected) {
                    name=read();
                    temp=read();

                    //发给所有客户端先
                    for (int i = 0; i <clients.size() ; i++) {
                        mythread c = clients.get(i);
                        c.write(name);
                        c.write(temp);
                    }

                    System.out.println(clients.size()+"来自客户端"+socket.getPort()+"的消息:" +temp);
                }

                //关闭死亡线程

                } catch (Exception e) {
                    e.printStackTrace();
            }finally {
                close();
            }
        }
    }
}




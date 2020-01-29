package tk;


import java.awt.event.ActionEvent;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.sql.*;
import java.util.*;

//主线程监听，此线程收发消息；客户端下线，届时再说？
public class Server {
    public static void main(String[] args){
        new Server().listenClient();
    }
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
    public String doSql(String name,String str){
        String flag="false";

        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
        }catch (ClassNotFoundException cne){
            cne.printStackTrace();
        }
        String dbur = "jdbc:mysql://127.0.0.1:3306/world?&useSSL=false&serverTimezone=UTC";
        String sql = "SELECT * FROM chat";
        Connection conn=null;
        Statement stmt=null;
        ResultSet rs=null;

        try {
            //管家注册
            conn= DriverManager.getConnection(dbur,"root","1qaz2wsx");
            stmt=conn.createStatement();

            rs= stmt.executeQuery(sql);
            while (rs.next()) {
                if(name.equals(rs.getString(str))){
                    sql=sql+" where name=\'"+name+"\'";
                    rs=stmt.executeQuery(sql);rs.next();
                    str=rs.getString("online");
                    if(str.equals("0")){
                        flag="true";
                        sql="update chat set online=1 where name=\'"+name+"\'";
                        System.out.println(sql);
                        stmt.execute(sql);
                    }
                    break;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            try {
                if(rs!=null)
                    rs.close();
                if(stmt!=null)
                    stmt.close();
                if(conn!=null)
                    conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return flag;
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
        private String tarName = null;
        private String sym = null;
        private boolean bConnected=false;

        //先记住吧，
        public mythread(Socket socket) {
            bConnected=true;
            this.socket = socket;
        }

        private void init(){
            try {
                dis = new DataInputStream(socket.getInputStream());
                dos = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
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
            String name=null;
            String ok="false";
            name=read();
            ok=doSql(name,"name");
//mysql设计了
            write(ok);
            return name;
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
            //如果输错，要一直循环等待输入，且quit指令还没放入login;配合mysql
            str=checkName();
            write(str);
            System.out.println("子线程开始工作");

            try {
            //不停读取数据
                while (bConnected) {
                    name=read();
                    temp=read();
                    sym=read();
                    tarName=read();
                    //发给所有客户端先

                    //命令异常，字符不对；长度不够，客户端离线解决断了
                    if(sym.equals("1")){
                        if(temp.equals("who")){//都是在客户端显示的
                            //mysql数据查询了
                        }else if(temp.substring(0,7).equals("history")){
                            temp=temp.substring(8);

                        }
                    }
                    for (int i = 0; i <clients.size() ; i++) {
                        mythread c = clients.get(i);
                        c.write(name);
                        c.write(temp);
                        c.write(sym);
                        c.write(tarName);
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




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
    private Connection conn=null;
    private Statement stmt=null;
    private ResultSet rs=null;
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
        private mythread(Socket socket) {
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
        private void initSql(){
            try{
                Class.forName("com.mysql.cj.jdbc.Driver");
            }catch (ClassNotFoundException cne){
                cne.printStackTrace();
            }
            String dbur = "jdbc:mysql://127.0.0.1:3306/world?&useSSL=false&serverTimezone=UTC";
            try {
                conn= DriverManager.getConnection(dbur,"root","1qaz2wsx");
                stmt=conn.createStatement();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        //需要行动的，
        private String doSql(String name,String sqlStr,String opt){//name是拆除后的，sqlStr用来指示好了
            initSql();
            String flag="false";
            String sql = "SELECT * FROM chat";
            int num=0;
            try {
                //管家注册
                rs= stmt.executeQuery(sql);
                if(sqlStr.equals("quit")){
                    sql="update chat set online=0 where name=\'"+name+"\'";
                    stmt.execute(sql);
                }else {
                    while (rs.next()) {//全部查一遍，经常操作，用个标志位来
                        if(sqlStr.equals("who")){
                            num++;
                            System.out.println(rs.getString("name"));//who，客户端显示的话就
                        }

                        if(sqlStr.equals("name")){
                            if(name.equals(rs.getString(sqlStr))){
                                sql=sql+" where name=\'"+name+"\'";
                                rs=stmt.executeQuery(sql);rs.next();
                                sqlStr=rs.getString("online");
                                if(sqlStr.equals("0")){
                                    flag="true";
                                    if(opt.equals("login")){
                                        sql="update chat set online=1 where name=\'"+name+"\'";
                                        System.out.println(sql);
                                        stmt.execute(sql);
                                    }

                                }
                                break;
                            }
                        }
                        //name

                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }finally {
                closeSql();
            }
            if(sqlStr.equals("who")) {
                System.out.printf("Total online user: %d\n",num);
            }
            return flag;
        }

        private void closeSql(){
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
        private String checkName(){
            String name=null;
            String ok="false";
            name=read();
            ok=doSql(name,"name","login");
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
                        if(temp.substring(0,2).equals("to")){
                            //用户名不带空格，且必须英文字符，否则
                            int mark=0;
                            for (int i = 3; i <temp.length() ; i++) {
                                if(temp.charAt(i)==' '){
                                    mark=i;break;
                                }
                            }

                            tarName=temp.substring(3,mark);
//    System.out.println(mark+" name "+tarName);
                            if(!doSql(tarName,"name","").equals("true")){//不在线的考虑呢？
                                sym="5"; temp=tarName+" is not online.";
                            }else {
                                sym="4";//仅有的人可以显示
                                temp=temp.substring(mark+1);
                            }
                        }else if(temp.equals("who")){//都是在客户端显示的
                            doSql(null,"who","");
                            //mysql数据查询了
                            sym="4";//假设了下
                        }else if(temp.equals("quit")){
                            doSql(name,"quit","");
                            clients.remove(this);
                            bConnected=false;
                            break;
                        }else if(temp.substring(0,7).equals("history")){
                            temp=temp.substring(8);
                        }
                    }

                    spread();
                    System.out.println(clients.size()+"来自客户端"+socket.getPort()+"的消息:" +temp);
                }
                //关闭死亡线程
                } catch (Exception e) {
                    e.printStackTrace();
            }finally {
                close();
            }
        }

        private void spread(){
            for (int i = 0; i <clients.size() ; i++) {
                mythread c = clients.get(i);
                c.write(name);
                c.write(temp);
                c.write(sym);
                c.write(tarName);
            }
        }
    }
}




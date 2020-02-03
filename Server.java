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

    //所有读写操作都放线程里了
    class mythread implements Runnable{
        private Connection conn=null;
        private Statement stmt=null;
        private ResultSet rs=null;

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
        private boolean readBool(){
            boolean ok=false;
            try {
                ok=dis.readBoolean();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return ok ;
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
            /*原本name就是真实名字，sqlStr可以当做查询的功能模块，有时当做其他功能选择模块，用于if判断；
            * opt是由于/to私聊时代的到来而进一步发展的，主要用于更进一步的选择*/
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
//                System.out.println("inside the sql "+sql);
                                rs=stmt.executeQuery(sql);rs.next();
                                sqlStr=rs.getString("online");
                                if(opt.equals("check")){
                                    if(sqlStr.equals("1")){
                                        flag="true";
                                    }
                                }
                                if(opt.equals("login")){
                                    if(sqlStr.equals("0")){
                                        flag="true";
                                        sql="update chat set online=1 where name=\'"+name+"\'";
//                            System.out.println(sql);
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
        private int countSpace(int start,String temp){
            int mark=0;
            for (int i = start; i <temp.length() ; i++) {
                if(temp.charAt(i)==' '){
                    mark=i;break;
                }
            }
            return mark;
        }

        @Override
        public void run() {
            String str="";
            boolean ok=false;
            init();
            //如果输错，要一直循环等待输入，且quit指令还没放入login;配合mysql

            while (!ok){
                str=checkName();
                ok=readBool();
            }
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
                            int mark=countSpace(3,temp);
                            tarName=temp.substring(3,mark);
//    System.out.println(mark+" name "+tarName);
                            if(doSql(tarName,"name","check").equals("false")){
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
                        }else if(temp.substring(0,7).equals("history")){
                            if(temp.length()>7){
                                temp=temp.substring(8);
                            System.out.println("1 "+temp);
                                int mark=countSpace(0,temp);
                            System.out.println("mark "+mark);
                                tarName=temp.substring(mark+1);
                                temp=temp.substring(0,mark);
                                sym="7";
                            }else {
                                sym="6";
                            }

                        }
                    }
                    spread();
                    System.out.println(clients.size()+"来自客户端"+socket.getPort()+"的消息:" +temp+" sym is "+sym);
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




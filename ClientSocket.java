package tk;



import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
/*
input() login里面有东西的
* */
public class ClientSocket {
    public static void main(String[] args) {
       new ClientSocket().input();
    }
    private HashMap<String, String> mp = new HashMap<>();

    private String login;
    private Socket s;
    private DataOutputStream dos;
    private DataInputStream dis;
    private Scanner in = new Scanner(System.in);

    private boolean started=false;
    private boolean online=false;
    private String name=null;
    private String tarName="";
    private String str=null;

    public ClientSocket(){

    }
    private void writeBool(boolean ok){
        try {
            dos.writeBoolean(ok);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    //就像范例代码，主只有写，读放在线程里
    public void input(){
        init();
        System.out.printf("Please login\n");
        in = new Scanner(System.in);
        login =in.nextLine();
//        login="/login a";
        while (!login.equals("/quit")&&!online){
            if(login.length()>=7){//首先名字长度得有
                try {
                    name= login.substring(7);
                } catch (NullPointerException e) {
                    System.out.println("null name string");
                }
                System.out.println(name);

                if(login.substring(0,6).equals("/login")){
                    boolean ok=submitName(name);
                    writeBool(ok);
                    if(ok){//原本可以和下面的 submit(name);句子合并
                        online=true;//需要进入库进行查询名字，全字符匹配
                        RecvClient c=new RecvClient();
                        new Thread(c).start();
                        start();
                    }else {
                        System.out.println("Name exist, please choose another name.");
                    }
                }else if(login.equals("/quit")){
                    online=true;
                }else{
                    System.out.printf("Invalid command\n");
                }
            }else{
                if(login.equals("/quit")){
                    online=true;
                }else{
                    System.out.printf("Invalid command\n");
                }
            }
            login =in.nextLine();
        }
        close();
    }

    public void init(){
        started=true;
        //初始化一个socket
        try {
            s= new Socket("127.0.0.1", 12345);
            //通过socket获取字符流
            dos = new DataOutputStream(s.getOutputStream());
            //通过标准输入流获取字符流
            dis = new DataInputStream(s.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        mp.put("hi","向大家打招呼，“Hi，大家好！我来咯~”");
        mp.put("hi ", "打招呼：“Hi，你好啊~”");
    }

    public void submit(String str){
        try {
            dos.writeUTF(str);
            dos.flush();
//System.out.printf("1 dos %s\n",name);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String receive(){
        str=null;
        try {
            str=dis.readUTF();
//System.out.printf("2 dis %s\n",str);
        }catch (SocketException e) {
            System.out.println("退出了，bye!");
        }catch (EOFException e) {
            System.out.println("normal exit");
        } catch (IOException e) {
            System.out.println("receive problem");
            e.printStackTrace();
        }
        return  str;
    }

    public boolean submitName(String name){
        submit(name);
//mysql匹配
        try {
            str=receive();
        } catch (NullPointerException e) {
            System.out.println("null string");
        }

        if(str.equals("true")){
            return true;
        }else {
            return false;
        }
    }

    public void sendMsgAndName(String str,String num){
        submit(name);
        submit(str);
        submit(num);
        submit(tarName);
    }

    public void login(){
         String tName=null;
        tName=receive();
        if (tName.equals(name)) {
            System.out.printf("You ");
        }else {
            System.out.printf("%s ",tName);
        }
        str = "have logined";
        //消息是统一的
        System.out.println(str);
    }

    public void start(){
        try {
            //广播的消息
            while (started){
                tarName="";
                str=in.nextLine();
                if(str.length()>2){
                    if(str.charAt(0)=='/'&&str.charAt(1)=='/'){
                        str=str.substring(2);
                        //扩展，那么就是遇到空格自动回退的字符串切割；再者就是换行符的影响
                        if(str.substring(0,2).equals("hi")){
                            if(str.length()>2){
                                tarName=str.substring(3);
                                str = mp.get("hi ");
                                sendMsgAndName(str,"3");
                            }else {
                                str=mp.get("hi");
                                sendMsgAndName(str,"2");
                            }
                        }
                    }else if(str.charAt(0)=='/'&&str.charAt(1)!='/'){
                        str=str.substring(1);
    System.out.println(str);
                        sendMsgAndName(str,"1");
                        if(str.equals("quit")){
                            break;
                        }
                    }else {
                        sendMsgAndName(str,"0");
                    }
                }else {
                    sendMsgAndName(str,"0");
                }
                //否则就是广播的消息
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close(){
        try {
            dos.close();
            dis.close();
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class RecvClient implements Runnable {
        private boolean Connected=false;
        private String tName=null;
        private String temp=null;
        private String sym=null;
        private String tarName="";

        //database
        private Connection conn=null;
        private Statement stmt=null;
        private ResultSet rs=null;

        private List<String> strList = new ArrayList<>();
        private int num=0;
        public RecvClient(){
            Connected=true;
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
        private void listHistory(int begin,int end){
            initSql();
            int i=begin;
//        System.out.println("begin "+begin+" e nd "+end);
            try {
                rs.absolute(begin-1);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            try {
                while (rs.next()) {
                    if(end>0&&i>end){
                        break;
                    }
                    i++;
                    System.out.printf(i+" "+rs.getString("sentence")+"\n");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }finally {
                closeSql();
            }
        }
        private String searchSql(String start,String cnt,String name){
            /*start作为一定的功能模块，cnt为数目，name为表名*/
            String str=" ";
            int num;
            initSql();
            String sql="SELECT * FROM "+name;
            try {//insert the data
                for(String s:strList){
                    sql="insert into %s (sentence) values (\'%s\')";
                    sql= String.format(sql, name,s);
                    stmt.executeUpdate(sql);
                }
                strList.clear();
                rs=stmt.executeQuery("SELECT * FROM "+name);rs.last();//末行，然后直接
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if(start.equals("")){
                    num=rs.getRow();
                    if(num>=50){
                        num-=49;
                        listHistory(num,num+49);
                    }else {
//                        rs.first();
                        listHistory(1,-1);
                    }
                }else {
                    listHistory(Integer.parseInt(start),Integer.parseInt(cnt)+Integer.parseInt(start)-1);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }finally {
                closeSql();
            }

            return str;
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
        /*强制退出无法存储数据*/
        public void run(){
            login();
            try {
            while (Connected) {
                    //主要负责接收服务端的
                    tName=receive();
                    temp=receive();
                    sym=receive();
                    tarName=receive();
                if(sym.equals("0")){
                    temp="说："+temp;
                    if (tName.equals(name)) {
                        temp="你"+temp;
                    }else {
                        temp=tName+temp;
                    }
                }else  if(sym.equals("1")){

                    if(temp.equals("quit")){
                        if (!tName.equals(name)) {
                            temp=tName+" has quit.";
                        }else {
                            close();
                            System.exit(0);
                            Connected=false;
                            break;
                        }
                    }
                        //用户名不带空格，且必须英文字符，否则
                }else if(sym.equals("2")){// "//"无后缀
                    if (tName.equals(name)) {
                        temp="你"+temp;
                    }else {
                        temp=tName+temp;
                    }
                }else if(sym.equals("3")){// "//"有后缀
                    if (tarName.equals(name)) {
                        temp=tName+"向你"+temp;
                    }else if(tName.equals(name)){
                        temp="你向"+tarName+temp;
                    }else {
                        temp=tName+"向"+tarName;
                    }
                }else if(sym.equals("4")){
                    if(name.equals(tName)){//发送主角
                        temp="你对"+tarName+"说："+temp;
                        System.out.println(temp);
                    } else if (tName.equals(tarName)) {//接收方
                        temp=name+"对你说："+temp;
                        System.out.println(temp);
                    }
                }else if(sym.equals("5")){
                    if (name.equals(tName)) {
                        System.out.println(temp);
                    }
                    sym="4";
                }else if(sym.equals("6")){
                    searchSql("", "50", tName);

                    sym="4";
                }else if(sym.equals("7")){

                    searchSql(temp, tarName, tName);
                    sym="4";
                }

                if(!sym.equals("6")){
                    num++;
                    strList.add(temp);
                }
                //消息是统一的
                if(!sym.equals("4")){
                    System.out.println(temp);
                }

                tarName="";
                tName="";
                temp="";
                sym="";
                }
                //退出后仍然存储数据
                searchSql("0", "0", name);
            }catch (NullPointerException e){
                System.out.println("null string");
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
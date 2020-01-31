package tk;



import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
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
    private String tName=null;
    private String tarName="";
    private String str=null;

    public ClientSocket(){

    }

    //就像范例代码，主只有写，读放在线程里
    public void input(){
        init();
        System.out.printf("Please login\n");
        in = new Scanner(System.in);
//        login =in.nextLine();
        login="/login a";
        while (!login.equals("/quit")&&!online){
            if(login.length()>=7){//首先名字长度得有
                try {
                    name= login.substring(7);
                } catch (NullPointerException e) {
                    System.out.println("null name string");
                }

                if(login.substring(0,6).equals("/login")){
                    if(submitName(name)){//原本可以和下面的 submit(name);句子合并
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

        public RecvClient(){
            Connected=true;
        }

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
                        System.out.printf("你");
                    }else {
                        System.out.printf("%s",tName);
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
                        System.out.printf("你");
                    }else {
                        System.out.printf("%s",tName);
                    }
                }else if(sym.equals("3")){// "//"有后缀
                    if (tarName.equals(name)) {
                        System.out.printf("%s向你",tName);
                    }else if(tName.equals(name)){
                        System.out.printf("你向%s",tarName);
                    }else {
                        System.out.printf("%s向%s",tName,tarName);
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
            }catch (NullPointerException e){
                System.out.println("null string");
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
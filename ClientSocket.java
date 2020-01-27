package tk;



import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Scanner;

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
    private String str=null;

    public ClientSocket(){

    }

    //就像范例代码，主只有写，读放在线程里
    public void input(){
        init();
        System.out.printf("Please login\n");
        in = new Scanner(System.in);
        login =in.nextLine();

        while (!login.equals("/quit")&&!online){
            if(login.length()>=7){//首先名字长度得有
                try {
                    name= login.substring(7);
                } catch (NullPointerException e) {
                    System.out.println("null name string");
                }

                if(login.substring(0,6).equals("/login")){
//System.out.println("init");
                    if(submitName(name)){//原本可以和下面的 submit(name);句子合并
                        online=true;//需要进入库进行查询名字，全字符匹配
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
        } catch (IOException e) {
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

    public void sendMsgAndName(String str){
        submit(name);
        submit(str);
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
            login();
            //广播的消息
            while (started){
                str=in.nextLine();
                if(str.length()>2){
                    if(str.charAt(0)=='/'&&str.charAt(1)=='/'){
                        str=str.substring(2);
                        //扩展，那么就是遇到空格自动回退的字符串切割；再者就是换行符的影响
                        if(str.substring(0,2).equals("hi")){
                            if(str.length()>2){
                                str = mp.get("hi ");
                                sendMsgAndName(str);
                            }else {
                                str=mp.get("hi");
                                sendMsgAndName(str);
                            }
                        }
                    }else if(str.charAt(0)=='/'&&str.charAt(1)!='/'){

                    }else {
                        sendMsgAndName(str);
                    }
                }else {
                    sendMsgAndName(str);
                }
                //否则就是广播的消息
                RecvClient r=new RecvClient();
                new Thread(r).start();
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

        public RecvClient(){
            Connected=true;
        }

        public void run(){
            try {
            while (Connected) {
                    //主要负责接收服务端的

                tName=receive();
                str=receive();
                if (tName.equals(name)) {
                    System.out.printf("你向大家");
                }else {
                    System.out.printf("%s向大家",tName);
                }
                //消息是统一的
                System.out.println(str);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
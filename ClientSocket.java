package tk;



import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

public class ClientSocket {
    public static void main(String[] args) {
       new ClientSocket().input();
    }

    private String login;
    private  Socket s;
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

    public void input(){
        init();
        System.out.printf("Please login\n");
        in = new Scanner(System.in);
        login =in.nextLine();
        while (!login.equals("/quit")){
            if(login.length()>=7){//首先名字长度得有
                name= login.substring(7);
                if(login.substring(0,6).equals("/login")){
//System.out.println("init");
                    if(submitName(name)){
                        System.out.println("You have logined");
                        online=true;//需要进入库进行查询名字，全字符匹配
                        broadcast(name);
                        start();
                    }else {
                        System.out.println("Name exist, please choose another name.");
                    }
                }else if(login.equals("/quit")){
                    close();
                }else{
                    System.out.printf("Invalid command 1\n");
                }
            }else{
                if(login.equals("/quit")){
                    close();
                }else{
                    System.out.printf("Invalid command 2\n");
                }
            }
        }
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
        str=receive();
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

    public void recvMsg(){
        String tmpName=null;
        tmpName=receive();
        str=receive();
        if (tmpName.equals(name)) {
            System.out.printf("你说：%s\n",str );
        }else {
            System.out.printf("%s说：%s\n",tmpName,str );
        }
    }

    public void start(){
        try {
            while (started) {
                str=in.nextLine();

                if(str.length()>2){
                    if(str.charAt(0)=='/'&&str.charAt(1)=='/'){
                        str=str.substring(2);
                        //扩展，那么就是遇到空格自动回退的字符串切割；再者就是换行符的影响
                        if(str.substring(0,2).equals("hi")){
                            if(str.length()>2){

                            }else {
                                /*write(mp.get("hi"));*/
                            }
                        }
                    }else if(str.charAt(0)=='/'&&str.charAt(1)!='/'){

                    }else {

                    }
                }else {
                    sendMsgAndName(str);
                }
                //否则就是广播的消息

                //广播的消息
                RecvClient r=new RecvClient();
                new Thread(r).start();


            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            close();
        }
    }

    public void broadcast(String name){

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

        }

    }
}
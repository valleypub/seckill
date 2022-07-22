package com.xxxx.seckill.utils;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.xxxx.seckill.pojo.User;
import com.xxxx.seckill.vo.RespBean;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserUtil {


    private static void createUser(int count) throws Exception {

        List<User> users = new ArrayList<>(count);
        for(int i = 0; i<count; i++){
            User user = new User();
            user.setId(13000000000L + i); //因为手机号码11位
            user.setNickname("autoCreated");
            user.setPassword(MD5Util.inputPassToDBPass("123456", "1a2b3c4d"));
            user.setSalt("1a2b3c4d");
            user.setRegisterDate(new Date());
            user.setLoginCount(1);
            users.add(user);
        }
        System.out.println("批量创建用户完成");

        //插入数据库：使用JDBC版的
        Connection conn = getConn();
        String sql = "insert into t_user(login_count,nickname,register_date,salt,password,id) value(?,?,?,?,?,?)";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        for(int i = 0; i<users.size(); i++){
            User user = users.get(i);
            pstmt.setInt(1,user.getLoginCount());
            pstmt.setString(2,user.getNickname());
            pstmt.setTimestamp(3,new Timestamp(user.getRegisterDate().getTime()));
            pstmt.setString(4,user.getSalt());
            pstmt.setString(5,user.getPassword());
            pstmt.setLong(6,user.getId());
            pstmt.addBatch();
        }
        pstmt.executeBatch();
        pstmt.clearParameters();;
        conn.close();
        System.out.println("insert to db ok!!!!");

        //登录，生成UserTicket 但是并没有生成redis中的数据，why???
        String urlString = "http://localhost:8080/login/doLogin";
        File file = new File("C:\\Users\\king-kong\\Desktop\\config.txt");
        if(file.exists()){
            file.delete();
        }
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        raf.seek(0);
        for(int i = 0; i <users.size(); i++){
            User user = users.get(i);
            URL url = new URL(urlString);
            HttpURLConnection co = (HttpURLConnection) url.openConnection();
            co.setRequestMethod("POST");
            co.setDoOutput(true);
            OutputStream outputStream = co.getOutputStream(); //对于url这个output input是相对于本机程序而言的，所以output就是离开本机向url，input就是从url进入本机的
            String params = "mobile="+user.getId()+"&password="+MD5Util.inputPassToFormPass("123456");
            outputStream.write(params.getBytes());
            outputStream.flush();

            InputStream inputStream = co.getInputStream();
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            byte[] buff = new byte[1024];
            int len = 0;
            while((len=inputStream.read(buff))>=0){
                bout.write(buff, 0, len);
            }
            inputStream.close();
            bout.close();
            //这里可以拿到响应结果
            String response = new String(bout.toByteArray());
            ObjectMapper mapper = new ObjectMapper();
            RespBean respBean = mapper.readValue(response, RespBean.class);
            String userTicket = ((String)respBean.getObj());
            System.out.println("create userTicket："+user.getId());//看看谁拿到了userTicket
            String row = user.getId()+","+userTicket;//config.txt中存的一行的数据
            raf.seek(raf.length());
            raf.write(row.getBytes());
            raf.write("\r\n".getBytes()); //写入一个换行符，在windows下是\r\n表示换行
            System.out.println("write to file："+user.getId());

        }
        raf.close();
        System.out.println("over");

    }

    private static  Connection getConn() throws Exception {
        String url = "jdbc:mysql://localhost:3306/seckill?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai";
        String username = "root";
        String password = "Kang.81163"; //为了保持统一，我的windows本地，云服务器的root账户的密码都是Kang.81163
        String driver = "com.mysql.cj.jdbc.Driver";
        Class.forName(driver);
        return DriverManager.getConnection(url,username,password);
    }

    public static void main(String... args) throws Exception {
        createUser(5000);
    }

}

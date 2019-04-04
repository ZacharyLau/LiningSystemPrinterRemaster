package com.queue.queuing.Beans;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by Administrator on 2017/6/21.
 */
@DatabaseTable(tableName = "setting")
public class Setting {
    @DatabaseField(id = true)
    private int id;

    @DatabaseField(columnName = "remPassword")
    private String remPassword;

    @DatabaseField(columnName = "autoLogin")
    private String autoLogin;

    @DatabaseField(columnName = "username")
    private String userName;

    @DatabaseField(columnName = "password")
    private String password;

    @DatabaseField(columnName = "token")
    private String token;

    @DatabaseField(columnName = "printerIP")
    private String printerIP;

    @DatabaseField(columnName = "userId")
    private String userId;

    @DatabaseField(columnName="networkprinter")
    private int networkprinter;

    @DatabaseField(columnName="papersize")
    private int papersize;

    public Setting(){

    }

    public Setting(int id, String remPassword, String autoLogin,int papersize,int networkprinter) {
        this.id = id;
        this.remPassword = remPassword;
        this.autoLogin = autoLogin;
        this.papersize=papersize;
        this.networkprinter=networkprinter;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRemPassword() {
        return remPassword;
    }

    public void setRemPassword(String remPassword) {
        this.remPassword = remPassword;
    }

    public String getAutoLogin() {
        return autoLogin;
    }

    public void setAutoLogin(String autoLogin) {
        this.autoLogin = autoLogin;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPrinterIP() {
        return printerIP;
    }

    public void setPrinterIP(String ip) {
        this.printerIP = ip;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

   public int getnetworkprinter() {
   return networkprinter;
   }

   public void setnetworkprinter(int networkprinter) {
        this.networkprinter = networkprinter; }

   public int getPaperSize() {
        return papersize;
    }

    public void setPaperSize(int papersize) {
        this.papersize = papersize;
    }

}



package com.example.purich.test;

/**
 * Created by Purich on 8/4/2558.
 */
public class RowItem {
    private String des;
    private String mac;
    private Integer id;



    public RowItem(Integer id, String des, String mac) {
        this.id = id;
        this.des = des;
        this.mac = mac;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    @Override
    public String toString() {
        return des + "\n" + mac;
    }
}

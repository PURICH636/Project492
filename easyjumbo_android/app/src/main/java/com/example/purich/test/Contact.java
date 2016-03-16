package com.example.purich.test;

/**
 * Created by Purich on 15/2/2558.
 */
public class Contact {
    //private variables
    int _id;
    String _username;
    String _key;

    // Empty constructor
    public Contact(){

    }
    // constructor
    public Contact(int id, String username, String key){
        this._id = id;
        this._username = username;
        this._key = key;
    }

    // constructor
    public Contact(String username, String key){
        this._username = username;
        this._key = key;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int id) {
        this._id = id;
    }

    public String get_username() {
        return _username;
    }

    public void set_username(String username) {
        this._username = username;
    }

    public String get_key() {
        return _key;
    }

    public void set_key(String key) {
        this._key = key;
    }

}

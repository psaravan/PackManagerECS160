package com.ecs160.packmanager.utils;

import java.util.ArrayList;

/**
 * User class that holds all user data (excluding sensitive info such as
 * passwords).
 */
public class User {

    private String username;
    private String password;
    private String realName;
    private String phoneNumber;
    private int reliabilityIndex;
    private ArrayList<Transaction> transactions;
    private ArrayList<User> friends;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String userRealName) {
        this.realName = userRealName;
    }

    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public int getReliabilityIndex() {
        return this.reliabilityIndex;
    }

    public void setReliabilityIndex(int reliabilityIndex) {
        this.reliabilityIndex = reliabilityIndex;
    }

    public ArrayList<Transaction> getTransactions() {
        return this.transactions;
    }

    public void setTransactions(ArrayList<Transaction> transactions) {
        this.transactions = transactions;
    }

    public void appendTransactions(ArrayList<Transaction> transactions) {
        this.transactions.addAll(transactions);
    }

    public ArrayList<User> getFriends() {
        return friends;
    }

    public void setFriends(ArrayList<User> friends) {
        this.friends = friends;
    }

    public void appendFriends(ArrayList<User> friends) {
        this.friends.addAll(friends);
    }

}

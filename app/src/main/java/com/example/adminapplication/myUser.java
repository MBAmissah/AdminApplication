package com.example.adminapplication;

public class myUser {
    private String Email;
    private String First_Name;
    private String Last_Name;
    private String Phone_Number;
    private String Student_ID;
    private String Card_Id;
    private int Account_Balance;

    public String getCard_Id() {
        return Card_Id;
    }

    public void setCard_Id(String card_Id) {
        Card_Id = card_Id;
    }



    public int getAccount_Balance() {
        return Account_Balance;
    }

    public void setAccount_Balance(int account_Balance) {
        Account_Balance = account_Balance;
    }




    public String getEmail() {
        return Email;
    }


    @CSVAnnotation.CSVSetter(info = "Email")
    public void setEmail(String email) {
        Email = email;
    }

    public String getFirst_Name() {
        return First_Name;
    }


    @CSVAnnotation.CSVSetter(info = "First_Name")
    public void setFirst_Name(String first_Name) {
        First_Name = first_Name;
    }

    public String getLast_Name() {
        return Last_Name;
    }


    @CSVAnnotation.CSVSetter(info = "Last_Name")
    public void setLast_Name(String last_Name) {
        Last_Name = last_Name;
    }

    public String getPhone_Number() {
        return Phone_Number;
    }


    @CSVAnnotation.CSVSetter(info = "Phone_Number")
    public void setPhone_Number(String phone_Number) {
        Phone_Number = phone_Number;
    }

    public String getStudent_ID() {
        return Student_ID;
    }


    @CSVAnnotation.CSVSetter(info = "Student_ID")
    public void setStudent_ID(String student_ID) {
        Student_ID = student_ID;
    }

    @Override
    public String toString() {
        return  "Account Balance: " + Account_Balance +
                "\nEmail: " + Email +
                "\nFirst Name: " + First_Name +
                "\nLast Name: " + Last_Name +
                "\nPhone Number: " + Phone_Number+
                "\nStudent ID: " + Student_ID +
                "\nCard ID: "+ Card_Id;
    }
}

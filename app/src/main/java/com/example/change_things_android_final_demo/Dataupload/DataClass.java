package com.example.change_things_android_final_demo.Dataupload;

public class DataClass {
    private  String imageURL,caption,text,Location,itemchange,itemprice;

    public DataClass(){
        //空建構子
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getText() {
        return text;
    }

    public String getItemchange() {
        return itemchange;
    }

    public void setItemchange(String itemchange) {
        this.itemchange = itemchange;
    }

    public void setItemprice(String itemprice) {
        this.itemprice = itemprice;
    }

    public String getItemprice() {
        return itemprice;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getLocation() {
        return Location;
    }

    public void setLocation(String location) {
        Location = location;
    }


    public DataClass(String imageURL, String caption, String text,String itemchange, String itemprice ,String location) {
        this.imageURL = imageURL;
        this.caption = caption;
        this.text = text;
        this.itemchange = itemchange;
        this.itemprice = itemprice;
        Location = location;
    }
}

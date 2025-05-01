package com.example.change_things_android_final_demo.recyclerfile;

public class itme_recycler {
    private String name;            // 商品名稱
    private String exchangeItem;    // 希望交換物
    private String price;           // 價格
    private String status;          // 商品狀態（例如：可交換、已交換）
    private String image;              // 圖片資源 ID
    private String Location;

    public itme_recycler(){}    // 空建構子

    public itme_recycler(String name, String exchangeItem, String price, String status, String image, String location) {
        this.name = name;
        this.exchangeItem = exchangeItem;
        this.price = price;
        this.status = status;
        this.image = image;
        Location = location;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExchangeItem() {
        return exchangeItem;
    }

    public void setExchangeItem(String exchangeItem) {
        this.exchangeItem = exchangeItem;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getLocation() {
        return Location;
    }

    public void setLocation(String location) {
        Location = location;
    }
}

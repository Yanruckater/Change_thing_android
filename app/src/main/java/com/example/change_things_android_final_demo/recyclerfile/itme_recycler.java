package com.example.change_things_android_final_demo.recyclerfile;

public class itme_recycler {
    private String name;            // 商品名稱
    private String exchangeItem;    // 希望交換物
    private String price;           // 價格
    private String status;          // 商品狀態（例如：可交換、已交換）
    private int image;              // 圖片資源 ID

    public itme_recycler(String name, String exchangeItem, String price, String status, int image) {
        this.name = name;
        this.exchangeItem = exchangeItem;
        this.price = price;
        this.status = status;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public String getExchangeItem() {
        return exchangeItem;
    }

    public String getPrice() {
        return price;
    }

    public String getStatus() {
        return status;
    }

    public int getImage() {
        return image;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setExchangeItem(String exchangeItem) {
        this.exchangeItem = exchangeItem;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setImage(int image) {
        this.image = image;
    }
}

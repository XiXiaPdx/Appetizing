package com.xixia.appetizing.Models;

/**
 * Created by macbook on 8/29/17.
 */

public class UserDescription {
    private String picID;
    private String foodDescription;

    public UserDescription(){}

    public UserDescription(String picID, String foodDescription){
        this.picID = picID;
        this.foodDescription = foodDescription;
    }

    public String getPicID() {
        return picID;
    }

    public void setPicID(String picID) {
        this.picID = picID;
    }

    public String getFoodDescription() {
        return foodDescription;
    }

    public void setFoodDescription(String foodDescription) {
        this.foodDescription = foodDescription;
    }
}

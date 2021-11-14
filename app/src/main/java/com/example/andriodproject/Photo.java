package com.example.andriodproject;

import java.io.Serializable;

public class Photo implements Serializable {
    private String photoString;
    private String name;
    private String tags;

    public Photo(String name, String photoString) {
        this.name = name;
        this.photoString = photoString;
    }

    public String getName() {
        return this.name;
    }

    public String getPhotoString() {
        return this.photoString;
    }


}

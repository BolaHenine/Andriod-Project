package com.example.andriodproject;

import java.io.Serializable;
import java.util.ArrayList;

public class Album implements Serializable {
    private String name;
    private ArrayList<Photo> photos;

    public Album(String name) {
        this.name = name;
        photos = new ArrayList<Photo>();
    }

    public String getName() {
        return this.name;
    }

    public ArrayList<Photo> getPhotos() {
        return this.photos;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addPhoto(Photo img) {
        if (photos == null) {
            photos = new ArrayList<Photo>();
            photos.add(img);
        } else {
            photos.add(img);
        }
    }

    @Override
    public String toString() {
        return name;
    }


}

package com.example.andriodproject;

import java.io.Serializable;
import java.util.ArrayList;

public class Photo implements Serializable {
    private String photoString;
    private String name;
    private ArrayList<String> pTag;
    private ArrayList<String> lTag;
    private ArrayList<Photo> photos;


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

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getPTag() {
        return this.pTag;
    }

    public ArrayList<Photo> getPhotos() {
        return this.photos;
    }


    public ArrayList<String> getlTag() {
        return this.lTag;
    }

    public void addPTag(String tagName) {
        if (pTag == null) {
            pTag = new ArrayList<String>();
        }
        pTag.add(tagName);
    }

    public void addLTag(String tagName) {
        if (lTag == null) {
            lTag = new ArrayList<String>();
        }
        lTag.add(tagName);
    }


}

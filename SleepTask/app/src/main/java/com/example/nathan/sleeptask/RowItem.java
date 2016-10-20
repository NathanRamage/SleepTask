package com.example.nathan.sleeptask;

import android.graphics.Color;

/**
 * Created by nathan on 20/10/16.
 */

public class RowItem {
    private int imageId;
    private String title;
    private int color;

    public RowItem(int imageId, String title, int color) {
        this.imageId = imageId;
        this.title = title;
        this.color = color;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return title;
    }
}
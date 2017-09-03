package com.example.patearn.patearnfirebase;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by #1 patearn on 2017-08-31.
 */

public class ImageDTO {

    public String imageUrl;
    public String imageName;
    public String title;
    public String description;
    public String uid;
    public String userId;
    public int starCount = 0;
    public Map<String, Boolean> stars = new HashMap<>();

}

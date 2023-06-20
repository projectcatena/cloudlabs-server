package com.cloudlabs.server.image;

public class ImageHelper {
    public static String getFileExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        if(index > 0) {
            String extension = fileName.substring(index + 1);
            return extension;
        }
        return null;
    }
}

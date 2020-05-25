package com.chonbonstudios.smartplaylists.ModelData;

public class StreamingServices {
    private String name;
    private boolean signedIn;

    public StreamingServices(String name, boolean signedIn){
        this.name = name;
        this.signedIn = signedIn;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSignedIn() {
        return signedIn;
    }

    public void setSignedIn(boolean signedIn) {
        this.signedIn = signedIn;
    }
}

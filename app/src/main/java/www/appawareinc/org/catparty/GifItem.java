package www.appawareinc.org.catparty;

/* This class creates "Gif Items". these items contain all the information we need to display
* the gifs, such as the url of the video and the width of the video.*/

//TODO: Add a height parameter so we can show full screen gifs
public class GifItem {
    private String [] guestInfo;

    /*constructor for initializing a complete GifItem*/
    public GifItem(String audition, String height, String width, String ID) {
        super();
        guestInfo = new String [4];
        guestInfo[0] = audition;
        guestInfo[1] = height;
        guestInfo[2] = width;
        guestInfo[3] = ID;
    }

    /*constructor for initializing a null GifItem*/
    public GifItem() {
        guestInfo = new String [4];
        guestInfo[0] = "";
        guestInfo[1] = "";
        guestInfo[2] = "";
        guestInfo[3] = "";
    }

    /*getters and setters for the state variables*/
    public String getGuestAudition() {
        return guestInfo[0];
    }

    public void setGuestAudition(String audition) {
        guestInfo[0] = audition;
    }

    public String getGuestHeight() {
        return guestInfo[1];
    }

    public void setGuestHeight(String height) {
        guestInfo[1] = height;
    }

    public String getGuestWidth() {
        return guestInfo[2];
    }

    public void setGuestWidth(String width) {
        guestInfo[2] = width;
    }

    public String getGuestID() {
        return guestInfo[3];
    }

    public void setGuestID(String ID) {
        guestInfo[3] = ID;
    }

}
package www.appawareinc.org.catparty;

/* This class creates "Gif Items". these items contain all the information we need to display
* the gifs, such as the url of the video and the width of the video.*/

//TODO: Add a height parameter so we can show full screen gifs
public class GifItem {
    private String guestAudition;
    private String guestHeight;
    private String guestWidth;
    private String guestID;

    /*constructor for initializing a complete GifItem*/
    public GifItem(String audition, String height, String width, String ID) {
        super();
        guestAudition = audition;
        guestHeight = height;
        guestWidth = width;
        guestID = ID;
    }

    /*constructor for initializing a null GifItem*/
    public GifItem() {
        guestAudition = "";
        guestHeight = "";
        guestWidth = "";
        guestID = "";
    }

    /*getters and setters for the state variables*/
    public String getGuestAudition() {
        return guestAudition;
    }

    public void setGuestAudition(String audition) {
        guestAudition = audition;
    }

    public String getGuestHeight() {
        return guestHeight;
    }

    public void setGuestHeight(String height) {
        guestHeight = height;
    }

    public String getGuestWidth() {
        return guestWidth;
    }

    public void setGuestWidth(String width) {
        guestWidth = width;
    }

    public String getGuestID() {
        return guestID;
    }

    public void setGuestID(String ID) {
        guestID = ID;
    }

}
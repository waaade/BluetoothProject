package androidRecyclerView;

import android.net.Uri;

public class Image {

    protected int id;
    protected Uri uri;
    protected String senderName;

    public Image(int id, Uri uri, String senderName) {
        this.id = id;
        this.uri = uri;
        this.senderName = senderName;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}

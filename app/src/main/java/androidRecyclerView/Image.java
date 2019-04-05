package androidRecyclerView;

import android.graphics.Bitmap;

public class Image {

    protected int id;
    protected Bitmap bm;
    protected String senderName;

    public Image(int id, Bitmap bm, String senderName) {
        this.id = id;
        this.bm = bm;
        this.senderName = senderName;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public Bitmap getBitmap() {
        return bm;
    }

    public void setBitMap(Bitmap bm) {
        this.bm = bm;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}

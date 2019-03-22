package john.example.bluetoothproject;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.List;

import androidRecyclerView.ImageAdapter;

/**
 * Loads an image from phone storage and loads it onto the screen.
 * Will be able to share image files to other phones via bluetooth
 */
public class ImageTransfer extends AppCompatActivity {

    private static final int READ_REQUEST_CODE = 42;
    private static final String TAG = "Image";
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private ImageAdapter mAdapter;

    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;

    public Uri previewImage = null;
    public int counter = 0;

    private List imageList = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_transfer);

        mRecyclerView = findViewById(R.id.image_recycle_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new ImageAdapter(getBaseContext(), imageList);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }

    /**
     * Fires an intent to spin up the "file chooser" UI and select an image.
     */
    public void performFileSearch(View view) {

        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
        // browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter to show only images, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers,
        // it would be "*/*".
        intent.setType("image/*");

        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    /**
     * Loads all of the image files in the phone storage and displays them.
     * Sends the chosen image to the getBitmapFromUri class to load the image
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            //Uri uri = null;
            if (resultData != null) {
                previewImage = resultData.getData();
                Log.i(TAG, "Uri: " + previewImage.toString());
                //new getBitmapFromUri().execute(uri);
                ImageView iv = findViewById(R.id.image);
                iv.setImageURI(previewImage);
            }
        }
    }

    /**
     * Loads a uri image file and converts it into a bitmap so the app can load it
     */
    private class getBitmapFromUri extends AsyncTask<Uri, Void, Bitmap> {
        /**
         * Converts a uri file into a bitmap file
         *
         * @param uri the uri image file that will be converted
         * @return a bitmap image
         */
        protected Bitmap doInBackground(Uri... uri) {
            Bitmap image;
            try {
                ParcelFileDescriptor parcelFileDescriptor =
                        getContentResolver().openFileDescriptor(uri[0], "r");
                FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                parcelFileDescriptor.close();
                return image;
            } catch (Exception e) {
                Log.i(TAG, "Error Loading image");
            }

            return null;
        }

        /**
         * loads a bitmap image to the app screen
         *
         * @param bm image to be loaded to the screen
         */
        protected void onPostExecute(Bitmap bm) {
            ImageView iv = findViewById(R.id.image);
            iv.setImageBitmap(bm);
        }
    }

    public void sendImage(View view) {
        mAdapter.notifyDataSetChanged();
        imageList.add(new androidRecyclerView.Image(counter++, previewImage, "Me"));
        previewImage = null;
        ImageView iv = findViewById(R.id.image);
        iv.setImageURI(null);
    }
}


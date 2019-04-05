package john.example.bluetoothproject;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Name of the connected device
    private String mConnectedDeviceName = null;

    // Member object for the chat services
    private BluetoothDataService mChatService = null;

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

    @Override
    public void onStart() {
        super.onStart();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            if (mChatService == null) setupChat();
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if (mChatService != null) {
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                mChatService.start();
            }
        }
    }

    /**
     * Properly sets up the chat and listens for the send button and calls the proper
     * function when the button is pushed
     */
    private void setupChat() {
        Button mSendButton = findViewById(R.id.send_button);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendImage();
            }
        });

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothDataService(this, mHandler);
    }

    @Override
    public synchronized void onPause() {
        super.onPause();

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (mChatService != null) mChatService.stop();
    }

    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
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

        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    String address = resultData.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // Get the BluetoothDevice object
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    // Attempt to connect to the device
                    mChatService.connect(device);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occured
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            if (resultData != null) {
                previewImage = resultData.getData();
                Log.i(TAG, "Uri: " + previewImage.toString());
                ImageView iv = findViewById(R.id.image);
                iv.setImageURI(previewImage);
            }
        }
    }

    /**
     * Sends the image that is being previewed to another phone via bluetooth
     */
    public void sendImage() {

        ImageView iv = findViewById(R.id.image);

        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        if(previewImage != null)
        {
            try
            {
                InputStream inputStream = getContentResolver().openInputStream(previewImage);
                byte[] inputData = getBytes(inputStream);

                //resize data to under 1024 bytes to send over bluetooth
                Log.d(TAG, "original size = " + inputData.length);
                Bitmap bm = null;
                for(int size = 25; inputData.length > 1024; size = size - 1)
                {
                    bm = decodeSampledBitmapFromByte(inputData, size, size);
                    ByteArrayOutputStream blob = new ByteArrayOutputStream();
                    bm.compress(Bitmap.CompressFormat.PNG, 0 /* Ignored for PNGs */, blob);
                    inputData = blob.toByteArray();
                    Log.i(TAG, "shortened length = " + size);
                }
                Log.d(TAG, "shortened size = " + inputData.length);

                //send data
                mChatService.write(inputData);
            } catch (Exception e) {
                Log.i(TAG, "Error sending image");
            }
        }
        Log.i(TAG, "previewImage uri: " + previewImage.toString());
        previewImage = null;
        iv.setImageURI(null);
    }

    /**
     * Used to convert a uri to a byte array. Uri must first be convert to an  inputStream
     *
     * @param inputStream
     * @return
     * @throws IOException
     */
    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    /**
     * Determines how much bigger a bitmap is that what is needs to be
     * in order to fit it within the parameters. Works in powers of 2
     *
     * @param options
     * @param reqWidth max width
     * @param reqHeight max height
     * @return the bitmap is this many times bigger than it needs to be
     */
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * Convert a byte array to a bitmap that is within the parameters
     *
     * @param buf byte array to be convert to bitmap
     * @param reqWidth max width of image
     * @param reqHeight max height of image
     * @return created bitmap
     */
    public static Bitmap decodeSampledBitmapFromByte(byte[] buf, int reqWidth,
                                                         int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(buf, 0, buf.length, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(buf, 0, buf.length, options);
    }

    /**
     * This handles all of the information that is being received from
     * BluetoothDataService and displays info to UI
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a bitmap from the buffer
                    Bitmap writeBm = decodeSampledBitmapFromByte(writeBuf, 200, 200);
                    mAdapter.notifyDataSetChanged();
                    imageList.add(new androidRecyclerView.Image(counter++, Bitmap.createScaledBitmap(writeBm, 200, 200, true), "Me"));
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a bitmap from the valid bytes in the buffer
                    Log.i(TAG, "size = " + readBuf.length);
                    //ensures bitmap is within readable size
//                    Bitmap readBm = BitmapFactory.decodeByteArray(readBuf, 0, readBuf.length);
//                    Bitmap read = Bitmap.createScaledBitmap(readBm, 200, 200, true);
                    Bitmap readBm = decodeSampledBitmapFromByte(readBuf, 200, 200);
                    mAdapter.notifyDataSetChanged();
                    //if bitmap is too small it will convert it to a viewable size and
                    //insert it
                    imageList.add(new androidRecyclerView.Image(counter++, Bitmap.createScaledBitmap(readBm, 200, 200, true), mConnectedDeviceName));
                    Log.d(TAG, "Successful received image");
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    /**
     * Starts DeviceListActivity in order to connect to another phone
     * @param v
     */
    public void connect(View v) {
        Intent serverIntent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
    }

    /**
     * Makes a the phone discoverable to other phones via bluetooth
     * @param v
     */
    public void discoverable(View v) {
        ensureDiscoverable();
    }
}


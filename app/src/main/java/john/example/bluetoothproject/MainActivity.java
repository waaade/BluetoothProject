package john.example.bluetoothproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void launchChat(View view) {
        Intent intent = new Intent(this, BluetoothChat.class);
        startActivity(intent);
    }

    public void launchImages(View view) {
        Intent intent = new Intent(this, Image.class);
        startActivity(intent);
    }
}

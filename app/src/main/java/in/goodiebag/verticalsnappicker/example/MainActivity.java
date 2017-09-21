package in.goodiebag.verticalsnappicker.example;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import in.goodiebag.verticalsnappicker.VerticalSnapPicker;

public class MainActivity extends AppCompatActivity {
    VerticalSnapPicker picker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        picker = (VerticalSnapPicker) findViewById(R.id.picker);
        List<VerticalSnapPicker.TextItem> items = new ArrayList<>();
        items.add(new VerticalSnapPicker.TextItem("One","Roboto-Black.ttf"));
        items.add(new VerticalSnapPicker.TextItem("Two","Roboto-BlackItalic.ttf"));
        items.add(new VerticalSnapPicker.TextItem("Three","Roboto-Bold.ttf"));
        items.add(new VerticalSnapPicker.TextItem("Four","Roboto-Black.ttf"));
        items.add(new VerticalSnapPicker.TextItem("Five","Roboto-BlackItalic.ttf"));
        items.add(new VerticalSnapPicker.TextItem("Six","Roboto-Bold.ttf"));
        items.add(new VerticalSnapPicker.TextItem("Seven","Roboto-Black.ttf"));
        items.add(new VerticalSnapPicker.TextItem("Eight","Roboto-BlackItalic.ttf"));
        items.add(new VerticalSnapPicker.TextItem("Nine","Roboto-Bold.ttf"));
        items.add(new VerticalSnapPicker.TextItem("Ten","Roboto-Black.ttf"));
        items.add(new VerticalSnapPicker.TextItem("Eleven","Roboto-BlackItalic.ttf"));
        items.add(new VerticalSnapPicker.TextItem("Twelve","Roboto-Bold.ttf"));
        //items.add(new VerticalSnapPicker.TextItem("Four","Roboto-BoldCondensed.ttf"));
        picker.setList(items);
        picker.setOnSnapListener(new VerticalSnapPicker.VerticalSnapPickerListener() {
            @Override
            public void onSnap(int position) {
                Log.d("Position", position+"");
            }
        });

        picker.postDelayed(new Runnable() {
            @Override
            public void run() {
                picker.setSelectedIndex(10);
            }
        }, 3000);

    }
}

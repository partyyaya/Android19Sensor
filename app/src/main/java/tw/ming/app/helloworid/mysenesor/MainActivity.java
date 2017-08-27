package tw.ming.app.helloworid.mysenesor;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener2;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private SensorManager sensorManager;
    private Sensor sensor;
    private MySensorListener mySensorListener;
    private TextView x,y,z;
    private MyView myView;
    private File sdroot;
    private File photoFile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myView = new MyView();
        setContentView(myView);

        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        for(Sensor sensor:sensors){
            String sensorName = sensor.getName();
            String sensorType = sensor.getStringType();
            Log.i("ming",sensorName+"+"+sensorType);
        }
        //TYPE_LIGHT:測試光亮度
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mySensorListener = new MySensorListener();
//        x = (TextView)findViewById(R.id.x);
//        y = (TextView)findViewById(R.id.y);
//        z = (TextView)findViewById(R.id.z);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    123);
        }else {
            init();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        init();
    }

    private void init(){
        sdroot = Environment.getExternalStorageDirectory();

        myView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePic1();
            }
        });
    }

    private void takePic1(){
        photoFile = new File(sdroot, "brad.jpg");
        Uri uri = Uri.fromFile(new File(sdroot, "brad.jpg"));
//        grantUriPermission(getPackageName(), uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        revokeUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

        Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        it.putExtra(MediaStore.EXTRA_OUTPUT, uri);
//        it.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(it,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==1){
            Drawable drawable = BitmapDrawable.createFromPath(photoFile.getAbsolutePath());
            myView.setBackground(drawable);
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        sensorManager.registerListener(mySensorListener,sensor,SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    protected void onStop() {
        super.onStop();
        sensorManager.unregisterListener(mySensorListener);
    }

    private class MySensorListener implements SensorEventListener2{
        @Override
        public void onFlushCompleted(Sensor sensor) {

        }

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            float[] values = sensorEvent.values;
            //Log.i("ming",""+values.length);
//            x.setText("x:"+(int)(values[0]*100));
//            y.setText("y:"+(int)(values[1]*100));
//            z.setText("z:"+(int)(values[2]*100));
            myView.setXY(values[0], values[1]);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    }
    private class MyView extends View {
        private boolean isInit;
        private float viewW, viewH, ballX, ballY;
        private Paint paint, paintAxis;

        MyView(){
            super(MainActivity.this);
            setBackgroundColor(Color.BLACK);
            paint = new Paint();
            paint.setColor(Color.YELLOW);
            paintAxis = new Paint();
            paintAxis.setColor(Color.RED);
            paintAxis.setStrokeWidth(2);

        }

        private void init(){
            isInit = true;
            viewW = getWidth(); viewH = getHeight();
            ballX = viewW / 2f; ballY = viewH / 2f;
        }
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (!isInit) init();

            canvas.drawCircle(ballX, ballY, 80, paint);
            canvas.drawLine(0,viewH/2f,viewW, viewH/2f, paintAxis);
            canvas.drawLine(viewW/2f, 0, viewW/2f, viewH, paintAxis);

        }

        void setXY(float x, float y){
            ballX = viewW/2f - (x * viewW/(2f * 9.8f));
            ballY = viewH/2f + (y * viewH/(2f * 9.8f));
            invalidate();
        }


    }

}

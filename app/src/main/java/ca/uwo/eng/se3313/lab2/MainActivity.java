package ca.uwo.eng.se3313.lab2;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    /**
     * View that showcases the image
     */
    private ImageView ivDisplay;

    /**
     * Skip button
     */
    private ImageButton skipBtn;

    /**
     * Progress bar showing how many seconds left (percentage).
     */
    private ProgressBar pbTimeLeft;

    /**
     * Label showing the seconds left.
     */
    private TextView tvTimeLeft;

    /**
     * Control to change the interval between switching images.
     */
    private SeekBar sbWaitTime;

    /**
     * Editable text to change the interval with {@link #sbWaitTime}.
     */
    private EditText etWaitTime;


    /**
     * Used to download images from the {@link #urlList}.
     */
    private IImageDownloader imgDownloader;

    /**
     * List of image URLs of cute animals that will be displayed.
     */
    private static final List<String> urlList = new ArrayList<String>() {{
        add("http://i.imgur.com/CPqbVW8.jpg");
        add("http://i.imgur.com/Ckf5OeO.jpg");
        add("http://i.imgur.com/3jq1bv7.jpg");
        add("http://i.imgur.com/8bSITuc.jpg");
        add("http://i.imgur.com/JfKH8wd.jpg");
        add("http://i.imgur.com/KDfJruL.jpg");
        add("http://i.imgur.com/o6c6dVb.jpg");
        add("http://i.imgur.com/B1bUG2K.jpg");
        add("http://i.imgur.com/AfxvVuq.jpg");
        add("http://i.imgur.com/DSDtm.jpg");
        add("http://i.imgur.com/SAVYw7S.jpg");
        add("http://i.imgur.com/4HznKil.jpg");
        add("http://i.imgur.com/meeB00V.jpg");
        add("http://i.imgur.com/CPh0SRT.jpg");
        add("http://i.imgur.com/8niPBvE.jpg");
        add("http://i.imgur.com/dci41f3.jpg");
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Insert your code here (and within the class!)
        //connect all variables
        this.sbWaitTime = (SeekBar) findViewById(R.id.sbWaitTime);
        sbWaitTime.setMax(55);
        this.etWaitTime = (EditText) findViewById(R.id.etWaitTime);
        etWaitTime.setMaxEms(60);
        etWaitTime.setMinEms(5);
        this.ivDisplay = (ImageView) findViewById(R.id.ivDisplay);
        this.pbTimeLeft = (ProgressBar) findViewById(R.id.pbTimeLeft);
        this.tvTimeLeft = (TextView) findViewById(R.id.tvTimeLeft);
        this.skipBtn = (ImageButton) findViewById(R.id.btnSkip);
        countDown count = new countDown();
        downloadNext();


        //set up tvTimeLeft
        tvTimeLeft.setText(etWaitTime.getText().toString());
        tvTimeLeft.setMaxEms(60);
        pbTimeLeft.setMax(Integer.valueOf(etWaitTime.getText().toString()));
        pbTimeLeft.setProgress(Integer.valueOf(tvTimeLeft.getText().toString()));
        pbTimeLeft.setMax(60);

        //seekBar set up
        sbWaitTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;
            int MIN = 5;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                progress = progressValue+MIN;

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                etWaitTime.setText(Integer.toString(progress));
            }
        });

        //set up EditText
        etWaitTime.addTextChangedListener(new TextWatcher() {
            String value;
            String oldValue;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                //hold last valid time for user input errors
                oldValue = etWaitTime.getText().toString();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                value = etWaitTime.getText().toString();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                value = etWaitTime.getText().toString();
                if(!value.equals("")) {
                    int num = Integer.valueOf(value);
                    //prevent user from entering value out of range
                    if (num > 60 || num < 5) {
                        CharSequence text = "Invalid Entry";

                        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();

                        //set to old valid time
                        etWaitTime.setText(oldValue);

                    } else {
                        //if value entered is in range update all timers
                        sbWaitTime.setProgress(Integer.valueOf(value));
                        pbTimeLeft.setMax(Integer.valueOf(etWaitTime.getText().toString()));
                        count.restart();
                    }
                }
                else
                    //if the value in the text field is null, set it to the last valid time
                    etWaitTime.setText(oldValue);

            }
        });

        //set up skip button
        skipBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                downloadNext();
                count.restart();
            }
        });
    }


    //timer for downloading pictures
    private class countDown{
        CountDownTimer count;

        public countDown() {
            count = new CountDownTimer(Long.valueOf(etWaitTime.getText().toString()) * 1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    tvTimeLeft.setText(String.valueOf(millisUntilFinished / 1000));
                    pbTimeLeft.setProgress(Integer.valueOf(tvTimeLeft.getText().toString()));
                }

                @Override
                public void onFinish() {
                    restart();
                }
            }.start();
        }

        private void restart(){
            //restart the countdown when either the previous is finished, or the skip button is pressed
            count.cancel();

            count = new CountDownTimer(Long.valueOf(etWaitTime.getText().toString()) * 1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    if(millisUntilFinished/1000 > 60){
                        tvTimeLeft.setText(String.valueOf(60));
                    }
                    else if (millisUntilFinished/100 < 5){
                        tvTimeLeft.setText(String.valueOf(5));
                    }
                    else
                        tvTimeLeft.setText(String.valueOf(millisUntilFinished/1000));
                    pbTimeLeft.setProgress(Integer.valueOf(tvTimeLeft.getText().toString()));
                }

                @Override
                public void onFinish() {
                    downloadNext();
                    restart();
                }
            }.start();
        }
    }

    private class DownloadImage extends AsyncTask<String, Bitmap, Bitmap> implements IImageDownloader{
        SuccessHandler shandler;
        ErrorHandler ehandler;
        Bitmap img;

        public DownloadImage(SuccessHandler handler, ErrorHandler ehandler){
            this.shandler = handler;
            this.ehandler = ehandler;
        }


        @Override
        public void download(@NonNull String imageUrl, @NonNull SuccessHandler handler) throws IOException {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();

            img  = BitmapFactory.decodeStream(input);
        }


        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if(bitmap == null)
                //if image was not downloaded call error handlers
                ehandler.onError(new IOException("Download incomplete"));
            else
                //if image was downloaded display it
                shandler.onComplete(bitmap);
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                //try to download image
                this.download(strings[0], this.shandler);
            } catch (IOException e) {
                //if error return null
                return null;
            }
            //return image if downloaded
            return img;
        }
    }

    private class displayImage implements IImageDownloader.SuccessHandler{
        //display downloaded image
        @Override
        public void onComplete(@NonNull Bitmap image) {
            ivDisplay.setImageBitmap(image);
        }
    }


    private class errorCat implements IImageDownloader.ErrorHandler{
        //on download error print the cat picture
        @Override
        public void onError(@NonNull Throwable error) {
            ivDisplay.setImageResource(R.drawable.cat_error);
        }
    }

    //download random image
    private void downloadNext(){
        Random rand = new Random();
        int num = rand.nextInt(16);

        DownloadImage downloadImage = new DownloadImage(new displayImage(), new errorCat());
        downloadImage.execute(urlList.get(num));
    }

}
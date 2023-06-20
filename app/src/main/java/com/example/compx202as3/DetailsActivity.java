package com.example.compx202as3;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
/**
*Details activity displays the displays the picture,
* title of the clicked camera marker,  it's region, and city.
*/
public class DetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        //Setting up the bundle to be able to access the intents that were sent
        Bundle detailedIntent = getIntent().getExtras();

        //Accessing the intents that are send from the maps activity
        String img  = detailedIntent.getString("img"); //Storing the image url of the preview image into the img string
        String regionAndCity = detailedIntent.getString("cityAndRegion"); //Storing the region and city into the regionAndCity string
        String title = detailedIntent.getString("title");//Storing the title into the title string

        ImageView imgIV = (ImageView)findViewById(R.id.CamImage); //Trying to access the image from the details xml
        Glide.with(this).load(img).into(imgIV);//Display the image of the clicked camera in the Details activity
        TextView titleTV = (TextView)findViewById(R.id.title);//Getting access the title in the Details activity
        TextView regionAndCityTV = (TextView)findViewById(R.id.RegionAndCity);//Getting access to region and city text
        titleTV.setText(title); //setting up the title of the clicked camera
        regionAndCityTV.setText(regionAndCity);//setting up the region and the city of the clicked camera
    }
}
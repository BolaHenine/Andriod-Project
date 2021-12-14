package com.example.andriodproject;

import android.app.Person;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Gallery;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OpenPhoto extends AppCompatActivity {

    private static final int GET_FROM_GALLERY = 1;
    private ArrayList<Photo> photosList = new ArrayList<Photo>();
    private ArrayList<String> personList = new ArrayList<String>();
    private ArrayList<String> locationList = new ArrayList<String>();
    private ArrayList<Album> albumList;
    private ArrayAdapter<String> person;
    private ArrayAdapter<String> location;
    private Album album;
    private Photo photo;
    private int albumpos;
    private int photopos;
    private ImageView imageview;
    private EditText ptagInput;
    private EditText ltagInput;
    private ListView pTag;
    private ListView lTag;
    private Button deletePerson;
    private Button deleteLocation;
    private Bitmap map;
    Type listType = new TypeToken<ArrayList<Album>>() {
    }.getType();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.open_photo);


        Bundle bundle = getIntent().getExtras();
        albumpos = Integer.parseInt(bundle.getString(MainActivity.ALBUM_POS));
        photopos = Integer.parseInt(bundle.getString(OpenAlbum.PHOTO_POS));
        Log.w("test", String.valueOf(albumpos));
        Log.w("test", String.valueOf(photopos));

        String albumsJson = read(this, "storage.json");
        albumList = new Gson().fromJson(albumsJson, listType);
        setTitle(albumList.get(albumpos).getPhotos().get(photopos).getName());
        imageview = findViewById(R.id.imageView2);
        ptagInput = findViewById(R.id.inputpTag);
        deletePerson = findViewById(R.id.deleteperson);
        deleteLocation = findViewById(R.id.deletelocation);
        ltagInput= findViewById(R.id.inputlTag);
        pTag = findViewById(R.id.pTagName);
        lTag = findViewById(R.id.lTagName);
//        ArrayList<String> test = new ArrayList<String>();
//        test.add("hello");
//        test.add("world");
//        ArrayAdapter<String> person = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1, test);
//        pTag.setAdapter(person);
        map = StringToBitMap(albumList.get(albumpos).getPhotos().get(photopos).getPhotoString());
        imageview.setImageBitmap(map);
        if (photosList == null) {
            photosList = new ArrayList<Photo>();
            photosList = albumList.get(albumpos).getPhotos().get(photopos).getPhotos();
        } else {
            photosList = albumList.get(albumpos).getPhotos().get(photopos).getPhotos();
        }
        if (personList == null) {
            personList = new ArrayList<String>();
            personList = albumList.get(albumpos).getPhotos().get(photopos).getPTag();
        } else {
            personList = albumList.get(albumpos).getPhotos().get(photopos).getPTag();
        }
        if (locationList == null) {
            locationList = new ArrayList<String>();
            locationList = albumList.get(albumpos).getPhotos().get(photopos).getLTag();
        } else {
            locationList = albumList.get(albumpos).getPhotos().get(photopos).getLTag();
        }


        Log.w("test", "hello world");
    }


    public void goBackPhoto(View view) {
        if(photopos>0)
    {
        photopos=photopos-1;

        }
        setTitle(albumList.get(albumpos).getPhotos().get(photopos).getName());
        map = StringToBitMap(albumList.get(albumpos).getPhotos().get(photopos).getPhotoString());
        imageview.setImageBitmap(map);
    }

    public void goToNextPhoto(View view) {
        if(photopos<albumList.get(albumpos).getPhotos().size()-1)
        {
            photopos=photopos+1;

        }
        setTitle(albumList.get(albumpos).getPhotos().get(photopos).getName());
        map = StringToBitMap(albumList.get(albumpos).getPhotos().get(photopos).getPhotoString());
        imageview.setImageBitmap(map);
    }
    public void insertTag(View view)
    {

        if(!ptagInput.getText().toString().trim().equals("")) {
            albumList.get(albumpos).getPhotos().get(photopos).addPTag(ptagInput.getText().toString().trim());
            personList = albumList.get(albumpos).getPhotos().get(photopos).getPTag();
            person = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1, personList);
            person.notifyDataSetChanged();
            pTag.setAdapter(person);
            ptagInput.getText().clear();
        }

        if(!ltagInput.getText().toString().trim().equals("")) {
            albumList.get(albumpos).getPhotos().get(photopos).addLTag(ltagInput.getText().toString().trim());
            locationList = albumList.get(albumpos).getPhotos().get(photopos).getLTag();
            location = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1, locationList);
            location.notifyDataSetChanged();
            lTag.setAdapter(location);
            ltagInput.getText().clear();
        }

        if (ptagInput.getText().toString().trim().equals("") && ltagInput.getText().toString().trim().equals(""))
        {
            Log.w("test","hello world");
            Context context = getApplicationContext();
            CharSequence text = "Enter valid input";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }

        pTag.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = (String) pTag.getItemAtPosition(position);
                deleteTag(item);
            }
        });
        lTag.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = (String) lTag.getItemAtPosition(position);
                deleteLocationTag(item);
            }
        });
        Log.w("test", String.valueOf(pTag));
        Log.w("test", String.valueOf(lTag));

    }
    public void deleteTag(String tag)
    {

        deletePerson.setOnClickListener(v -> {
            AlertDialog.Builder adb=new AlertDialog.Builder(OpenPhoto.this);
            adb.setTitle("Delete?");
            adb.setMessage("Are you sure you want to delete " + tag);
            final int positionToRemove = personList.indexOf(tag);
            adb.setNegativeButton("Cancel", null);
            adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    personList.remove(positionToRemove);
                    person.notifyDataSetChanged();
                }});
            adb.show();
        });
    }
    public void deleteLocationTag(String tag)
    {

        deleteLocation.setOnClickListener(v -> {
            AlertDialog.Builder adb=new AlertDialog.Builder(OpenPhoto.this);
            adb.setTitle("Delete?");
            adb.setMessage("Are you sure you want to delete " + tag);
            final int positionToRemove = locationList.indexOf(tag);
            adb.setNegativeButton("Cancel", null);
            adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    locationList.remove(positionToRemove);
                    location.notifyDataSetChanged();
                }});
            adb.show();
        });
    }

    public String BitMapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String temp = Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    public Bitmap StringToBitMap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }




    private boolean create(Context context, String fileName, String jsonString) {
        String FILENAME = "storage.json";
        try {
            FileOutputStream fos = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
            if (jsonString != null) {
                fos.write(jsonString.getBytes());
            }
            fos.close();
            return true;
        } catch (FileNotFoundException fileNotFound) {
            return false;
        } catch (IOException ioException) {
            return false;
        }

    }
    private String read(Context context, String fileName) {
        try {
            FileInputStream fis = context.openFileInput(fileName);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (FileNotFoundException fileNotFound) {
            return null;
        } catch (IOException ioException) {
            return null;
        }
    }
}
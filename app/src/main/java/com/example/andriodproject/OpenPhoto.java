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
    private ArrayList<String> personList;
    private ArrayList<String> locationList;
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
    private int ptagPositionDel;
    private int ltagPositionDel;
    Type listType = new TypeToken<ArrayList<Album>>() {
    }.getType();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.open_photo);


        Bundle bundle = getIntent().getExtras();
        albumpos = Integer.parseInt(bundle.getString(MainActivity.ALBUM_POS));
        photopos = Integer.parseInt(bundle.getString(OpenAlbum.PHOTO_POS));

        String albumsJson = read(this, "storage.json");
        albumList = new Gson().fromJson(albumsJson, listType);
        setTitle(albumList.get(albumpos).getPhotos().get(photopos).getName());
        imageview = findViewById(R.id.imageView2);
        ptagInput = findViewById(R.id.inputpTag);
        deletePerson = findViewById(R.id.deleteperson);
        deleteLocation = findViewById(R.id.deletelocation);
        ltagInput = findViewById(R.id.inputlTag);
        pTag = findViewById(R.id.pTagName);
        lTag = findViewById(R.id.lTagName);


        map = StringToBitMap(albumList.get(albumpos).getPhotos().get(photopos).getPhotoString());
        imageview.setImageBitmap(map);
        if (photosList == null) {
            photosList = new ArrayList<Photo>();
            photosList = albumList.get(albumpos).getPhotos().get(photopos).getPhotos();
        } else {
            photosList = albumList.get(albumpos).getPhotos().get(photopos).getPhotos();
        }

        if (personList == null && albumList.get(albumpos).getPhotos().get(photopos).getPTag() == null) {
            personList = new ArrayList<String>();
            personList = albumList.get(albumpos).getPhotos().get(photopos).getPTag();
        } else {
            personList = albumList.get(albumpos).getPhotos().get(photopos).getPTag();
        }

        if (locationList == null && albumList.get(albumpos).getPhotos().get(photopos).getLTag() == null) {
            locationList = new ArrayList<String>();
            locationList = albumList.get(albumpos).getPhotos().get(photopos).getLTag();
        } else {
            locationList = albumList.get(albumpos).getPhotos().get(photopos).getLTag();
        }


        person = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, personList);
        pTag.setAdapter(person);

        location = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, locationList);
        lTag.setAdapter(location);


        pTag.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ptagPositionDel = position;
            }
        });

        lTag.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ltagPositionDel = position;
            }
        });

    }


    public void goBackPhoto(View view) {
        if (photopos > 0) {
            photopos = photopos - 1;
            if (personList == null && albumList.get(albumpos).getPhotos().get(photopos).getPTag() == null) {
                personList = new ArrayList<String>();
                personList = albumList.get(albumpos).getPhotos().get(photopos).getPTag();
            } else {
                personList = albumList.get(albumpos).getPhotos().get(photopos).getPTag();
            }

            if (locationList == null && albumList.get(albumpos).getPhotos().get(photopos).getLTag() == null) {
                locationList = new ArrayList<String>();
                locationList = albumList.get(albumpos).getPhotos().get(photopos).getLTag();
            } else {
                locationList = albumList.get(albumpos).getPhotos().get(photopos).getLTag();
            }

            person = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, personList);
            pTag.setAdapter(person);

            location = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, locationList);
            lTag.setAdapter(location);
        }
        setTitle(albumList.get(albumpos).getPhotos().get(photopos).getName());
        map = StringToBitMap(albumList.get(albumpos).getPhotos().get(photopos).getPhotoString());
        imageview.setImageBitmap(map);
    }

    public void goToNextPhoto(View view) {

        if (photopos < albumList.get(albumpos).getPhotos().size() - 1) {
            photopos = photopos + 1;
            if (personList == null && albumList.get(albumpos).getPhotos().get(photopos).getPTag() == null) {
                personList = new ArrayList<String>();
                personList = albumList.get(albumpos).getPhotos().get(photopos).getPTag();
            } else {
                personList = albumList.get(albumpos).getPhotos().get(photopos).getPTag();
            }

            if (locationList == null && albumList.get(albumpos).getPhotos().get(photopos).getLTag() == null) {
                locationList = new ArrayList<String>();
                locationList = albumList.get(albumpos).getPhotos().get(photopos).getLTag();
            } else {
                locationList = albumList.get(albumpos).getPhotos().get(photopos).getLTag();
            }

            person = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, personList);
            pTag.setAdapter(person);

            location = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, locationList);
            lTag.setAdapter(location);

        }
        setTitle(albumList.get(albumpos).getPhotos().get(photopos).getName());
        map = StringToBitMap(albumList.get(albumpos).getPhotos().get(photopos).getPhotoString());
        imageview.setImageBitmap(map);
    }

    public void insertTag(View view) {

        if (ptagInput.getText().toString().trim().equals("") && ltagInput.getText().toString().trim().equals("")) {
            Context context = getApplicationContext();
            CharSequence text = "Enter valid input";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }

        if (!ptagInput.getText().toString().trim().equals("")) {
            personList.add(ptagInput.getText().toString().trim());
            person.notifyDataSetChanged();
            ptagInput.getText().clear();

            SharedPreferences mPrefs = getPreferences(MODE_PRIVATE);
            SharedPreferences.Editor prefsEditor = mPrefs.edit();
            Gson gson = new Gson();
            String json = gson.toJson(albumList);
            prefsEditor.putString("albumList", json);
            prefsEditor.commit();
            create(OpenPhoto.this, "storage.json", json);
        }

        if (!ltagInput.getText().toString().trim().equals("")) {
            locationList.add(ltagInput.getText().toString().trim());
            location.notifyDataSetChanged();
            ltagInput.getText().clear();

            SharedPreferences mPrefs = getPreferences(MODE_PRIVATE);
            SharedPreferences.Editor prefsEditor = mPrefs.edit();
            Gson gson = new Gson();
            String json = gson.toJson(albumList);
            prefsEditor.putString("albumList", json);
            prefsEditor.commit();
            create(OpenPhoto.this, "storage.json", json);
        }


    }

    public void deletePTag(View v) {
        if (personList.size() == 0) {
            Context context = getApplicationContext();
            CharSequence text = "There is nothing to delete";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        } else {
            AlertDialog.Builder adb = new AlertDialog.Builder(OpenPhoto.this);
            String tag = personList.get(ptagPositionDel);
            adb.setTitle("Delete?");
            adb.setMessage("Are you sure you want to delete " + tag);
            adb.setNegativeButton("Cancel", null);
            adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    personList.remove(ptagPositionDel);
                    person.notifyDataSetChanged();

                    SharedPreferences mPrefs = getPreferences(MODE_PRIVATE);
                    SharedPreferences.Editor prefsEditor = mPrefs.edit();
                    Gson gson = new Gson();
                    String json = gson.toJson(albumList);
                    prefsEditor.putString("albumList", json);
                    prefsEditor.commit();
                    create(OpenPhoto.this, "storage.json", json);
                }
            });
            adb.show();
        }

    }

    public void deleteLTag(View v) {
        if (locationList.size() == 0) {
            Context context = getApplicationContext();
            CharSequence text = "There is nothing to delete";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        } else {
            AlertDialog.Builder adb = new AlertDialog.Builder(OpenPhoto.this);
            String tag = locationList.get(ltagPositionDel);
            adb.setTitle("Delete?");
            adb.setMessage("Are you sure you want to delete " + tag);
            adb.setNegativeButton("Cancel", null);
            adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    locationList.remove(ltagPositionDel);
                    location.notifyDataSetChanged();

                    SharedPreferences mPrefs = getPreferences(MODE_PRIVATE);
                    SharedPreferences.Editor prefsEditor = mPrefs.edit();
                    Gson gson = new Gson();
                    String json = gson.toJson(albumList);
                    prefsEditor.putString("albumList", json);
                    prefsEditor.commit();
                    create(OpenPhoto.this, "storage.json", json);
                }
            });
            adb.show();
        }

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
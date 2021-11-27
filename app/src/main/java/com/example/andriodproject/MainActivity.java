package com.example.andriodproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Album> albumList;
    ListView albums;
    AlbumsAdapter adapter;
    Gson gson;

    private ArrayAdapter<String> autoComAdapter;
    private ArrayList<String> autoCom = new ArrayList<String>();
    private AutoCompleteTextView tagValue;
    private RecyclerView recAlbumList;
    private PhotoAdapter photoAdapter;
    private RecyclerView searchList;


    Type listType = new TypeToken<ArrayList<Album>>() {
    }.getType();

    public static final String ALBUM_NAMES = "album_names";
    public static final String ALBUM_POS = "album_pos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (isFilePresent(this, "storage.json")) {
            String albumsJson = read(this, "storage.json");
            albumList = new Gson().fromJson(albumsJson, listType);
        } else {
            albumList = new ArrayList<Album>();
        }

        recAlbumList = (RecyclerView) findViewById(R.id.albumRecList);
        adapter = new AlbumsAdapter(albumList);
        recAlbumList.setAdapter(adapter);
        recAlbumList.setLayoutManager(new LinearLayoutManager(this));

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int position = viewHolder.getAdapterPosition();
                albumList.remove(position);
                adapter.notifyDataSetChanged();
                SharedPreferences mPrefs = getPreferences(MODE_PRIVATE);
                SharedPreferences.Editor prefsEditor = mPrefs.edit();
                Gson gson = new Gson();
                String json = gson.toJson(albumList);
                prefsEditor.putString("albumList", json);
                prefsEditor.commit();
                create(MainActivity.this, "storage.json", json);
            }
        });

        itemTouchHelper.attachToRecyclerView(recAlbumList);


        albumList.get(0).getPhotos().get(0).addPTag("Bola");
        albumList.get(1).getPhotos().get(0).addPTag("Test");

        albumList.get(0).getPhotos().get(0).addLTag("New York");
        albumList.get(1).getPhotos().get(0).addLTag("New Jersey");


//        for (int i = 0; i < albumList.size(); i++) {
//            for (int j = 0; j < albumList.get(i).getPhotos().size(); j++) {
//                autoCom.addAll(albumList.get(i).getPhotos().get(j).getlTag());
//                autoCom.addAll(albumList.get(i).getPhotos().get(j).getPTag());
//
//            }
//        }

//        View mView = getLayoutInflater().inflate(R.layout.search_alert, null);
//
//        AutoCompleteTextView tagValue = (AutoCompleteTextView) mView.findViewById(R.id.tagValue);
//
//
//        autoComAdapter = new ArrayAdapter<String>(this,
//                android.R.layout.simple_dropdown_item_1line, autoCom);
//
//
//        tagValue.setAdapter(autoComAdapter);

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.searchButton:
                search();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void search() {

        List<String> spinnerList = new ArrayList<String>();
        spinnerList.add("Person");
        spinnerList.add("Location");
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.search_alert, null);
        mBuilder.setTitle("Select Tag Type");
        tagValue = (AutoCompleteTextView) mView.findViewById(R.id.tagValue);
        Spinner mSpinner = (Spinner) mView.findViewById(R.id.tagType);
        ArrayAdapter adp = new ArrayAdapter(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, spinnerList);
        adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adp);

        autoComAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, autoCom);

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                Log.w("test", String.valueOf(position));
                autoComFill(position);


                tagValue.setAdapter(autoComAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        mBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                showSearchResults(tagValue.getText().toString(), mSpinner.getSelectedItem().toString());
            }
        });

        mBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Context context = getApplicationContext();
                CharSequence text = "The Photo will not be moved";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        });

        mBuilder.setView(mView);

        AlertDialog d = mBuilder.create();

        d.show();
    }

    public void showSearchResults(String val, String type) {
        recAlbumList.setVisibility(View.INVISIBLE);
        ArrayList<Photo> searchResults = new ArrayList<Photo>();
        if (type.equals("Person")) {
            for (int i = 0; i < albumList.size(); i++) {
                for (int j = 0; j < albumList.get(i).getPhotos().size(); j++) {
                    if (albumList.get(i).getPhotos().get(j).getPTag().contains(val)) {
                        searchResults.add(albumList.get(i).getPhotos().get(j));
                    }
                }
            }
        } else {
            for (int i = 0; i < albumList.size(); i++) {
                for (int j = 0; j < albumList.get(i).getPhotos().size(); j++) {
                    if (albumList.get(i).getPhotos().get(j).getlTag().contains(val)) {
                        searchResults.add(albumList.get(i).getPhotos().get(j));
                    }
                }
            }
        }
        if (searchResults.size() == 0) {
            Context context = getApplicationContext();
            CharSequence text = "Nothing matches the Search Results";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
        photoAdapter = new PhotoAdapter(searchResults);
        searchList = (RecyclerView) findViewById(R.id.searchList);
        searchList.setVisibility(View.VISIBLE);
        searchList.setAdapter(photoAdapter);
        searchList.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onBackPressed() {
        if (recAlbumList.getVisibility() == View.INVISIBLE) {
            recAlbumList.setVisibility(View.VISIBLE);
            searchList.setVisibility(View.INVISIBLE);
        } else {
            Intent setIntent = new Intent(Intent.ACTION_MAIN);
            setIntent.addCategory(Intent.CATEGORY_HOME);
            setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(setIntent);
        }
    }

    public void autoComFill(int position) {
        autoCom.clear();
        Log.w("test", String.valueOf(position));
        if (position == 0) {
            for (int i = 0; i < albumList.size(); i++) {
                for (int j = 0; j < albumList.get(i).getPhotos().size(); j++) {
                    autoCom.addAll(albumList.get(i).getPhotos().get(j).getPTag());
                }
            }
        } else {
            for (int i = 0; i < albumList.size(); i++) {
                for (int j = 0; j < albumList.get(i).getPhotos().size(); j++) {
                    autoCom.addAll(albumList.get(i).getPhotos().get(j).getlTag());
                }
            }
        }
        autoComAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, autoCom);

    }

    public void addButtonClick(View v) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText albumName = new EditText(this);
        albumName.setHint("Album Name");
        alert.setTitle("Enter The Album Name");

        alert.setView(albumName);

        alert.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                String name = albumName.getText().toString();
                Album newAlbum = new Album(name);
                albumList.add(newAlbum);
                adapter.notifyDataSetChanged();

                SharedPreferences mPrefs = getPreferences(MODE_PRIVATE);
                SharedPreferences.Editor prefsEditor = mPrefs.edit();
                Gson gson = new Gson();
                String json = gson.toJson(albumList);
                prefsEditor.putString("albumList", json);
                prefsEditor.commit();
                create(MainActivity.this, "storage.json", json);
            }
        });

        alert.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                Context context = getApplicationContext();
                CharSequence text = "The Album will not be added";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        });

        alert.show();
    }

    private void openAlbum(int position) {
        Intent intent = new Intent(this, OpenAlbum.class);
        String albumsJson = read(this, "storage.json");
        Bundle bundle = new Bundle();
        bundle.putString(ALBUM_POS, String.valueOf(position));

        intent.putExtras(bundle);
        startActivity(intent);
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

    public boolean isFilePresent(Context context, String fileName) {
        String path = context.getFilesDir().getAbsolutePath() + "/" + fileName;
        File file = new File(path);
        return file.exists();
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


    public class AlbumsAdapter extends
            RecyclerView.Adapter<AlbumsAdapter.ViewHolder> {


        // Provide a direct reference to each of the views within a data item
        // Used to cache the views within the item layout for fast access
        public class ViewHolder extends RecyclerView.ViewHolder {
            // Your holder should contain a member variable
            // for any view that will be set as you render a row
            public TextView nameTextView;
            public Button deletebtn;
            public Button editbtn;

            // We also create a constructor that accepts the entire item row
            // and does the view lookups to find each subview
            public ViewHolder(View itemView) {
                // Stores the itemView in a public final member variable that can be used
                // to access the context from any ViewHolder instance.
                super(itemView);

                nameTextView = (TextView) itemView.findViewById(R.id.photoName);
                deletebtn = (Button) itemView.findViewById(R.id.deletePhoto);
                editbtn = (Button) itemView.findViewById(R.id.editBtn);


            }

        }

        private List<Album> albumsList;

        // Pass in the contact array into the constructor
        public AlbumsAdapter(List<Album> albumsList) {
            this.albumsList = albumsList;
        }

        @Override
        public AlbumsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);

            // Inflate the custom layout
            View contactView = inflater.inflate(R.layout.album_list_view, parent, false);

            // Return a new holder instance
            ViewHolder viewHolder = new ViewHolder(contactView);
            return viewHolder;
        }

        // Involves populating data into the item through holder
        @Override
        public void onBindViewHolder(AlbumsAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
            // Get the data model based on position
            Album album = albumsList.get(position);

            // Set item views based on your views and data model
            TextView textView = holder.nameTextView;
            textView.setText(album.getName());
            Button deletebtn = holder.deletebtn;
            Button editbtn = holder.editbtn;

            holder.itemView.setOnClickListener(view -> openAlbum(position));

            deletebtn.setOnClickListener(view -> {
                albumsList.remove(albumsList.get(position));
                notifyDataSetChanged();

                SharedPreferences mPrefs = getPreferences(MODE_PRIVATE);
                SharedPreferences.Editor prefsEditor = mPrefs.edit();
                Gson gson = new Gson();
                String json = gson.toJson(albumList);
                prefsEditor.putString("albumList", json);
                prefsEditor.commit();
                create(MainActivity.this, "storage.json", json);
            });


            editbtn.setOnClickListener(view -> {

                AlertDialog.Builder editAlert = new AlertDialog.Builder(MainActivity.this);
                final EditText newAlbumName = new EditText(MainActivity.this);
                newAlbumName.setHint("Album Name");
                editAlert.setTitle("Enter The New Album Name");
                editAlert.setView(newAlbumName);

                editAlert.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        String name = newAlbumName.getText().toString();
                        Album editAlbum = albumList.get(position);
                        editAlbum.setName(name);
                        notifyDataSetChanged();

                        SharedPreferences mPrefs = getPreferences(MODE_PRIVATE);
                        SharedPreferences.Editor prefsEditor = mPrefs.edit();
                        Gson gson = new Gson();
                        String json = gson.toJson(albumList);
                        prefsEditor.putString("albumList", json);
                        prefsEditor.commit();
                        create(MainActivity.this, "storage.json", json);
                    }
                });

                editAlert.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        Context context = getApplicationContext();
                        CharSequence text = "The Album name will not be changed";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                    }
                });
                editAlert.show();
            });
        }

        // Returns the total count of items in the list
        @Override
        public int getItemCount() {
            return albumsList.size();
        }


    }


    public class PhotoAdapter extends
            RecyclerView.Adapter<MainActivity.PhotoAdapter.ViewHolder> {
        public class ViewHolder extends RecyclerView.ViewHolder {

            public TextView nameTextView;
            public ImageView photoView;
            public Button deletebtn;
            public TextView buttonViewOption;

            public ViewHolder(View itemView) {
                super(itemView);
                nameTextView = itemView.findViewById(R.id.photoName);
                photoView = itemView.findViewById(R.id.imageView);
                deletebtn = itemView.findViewById(R.id.deletePhoto);
                buttonViewOption = (TextView) itemView.findViewById(R.id.textViewOptions);
            }

        }

        private List<Photo> PhotoList;

        public PhotoAdapter(List<Photo> PhotoList) {
            if (PhotoList == null) {
                PhotoList = new ArrayList<Photo>();
                this.PhotoList = PhotoList;
            }
            this.PhotoList = PhotoList;

        }

        @Override
        public MainActivity.PhotoAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            View contactView = inflater.inflate(R.layout.album_photos_list_view, parent, false);
            MainActivity.PhotoAdapter.ViewHolder viewHolder = new ViewHolder(contactView);
            return viewHolder;
        }

        // Involves populating data into the item through holder
        @Override
        public void onBindViewHolder(MainActivity.PhotoAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
            Photo photo = PhotoList.get(position);
            Bitmap img = StringToBitMap(photo.getPhotoString());

            TextView textView = holder.nameTextView;
            ImageView photoView = holder.photoView;
            Button deleteBtn = holder.deletebtn;
            TextView options = holder.buttonViewOption;

            deleteBtn.setVisibility(View.INVISIBLE);
            options.setVisibility(View.INVISIBLE);

            photoView.setImageBitmap(img);
            textView.setText(photo.getName());

        }

        @Override
        public int getItemCount() {
            return PhotoList.size();
        }


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


}
package com.example.andriodproject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
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
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.navigation.ui.AppBarConfiguration;
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
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.Inflater;

public class OpenAlbum extends AppCompatActivity {
    private static final int GET_FROM_GALLERY = 1;
    private ArrayList<Photo> photosList = new ArrayList<Photo>();
    private PhotoAdapter adapter;
    private ArrayList<Album> albumList;
    private int pos;
    Type listType = new TypeToken<ArrayList<Album>>() {
    }.getType();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_album);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView photoList = findViewById(R.id.photoList);


        Bundle bundle = getIntent().getExtras();
        pos = Integer.parseInt(bundle.getString(MainActivity.ALBUM_POS));
        String albumsJson = read(this, "storage.json");
        albumList = new Gson().fromJson(albumsJson, listType);
        setTitle(albumList.get(pos).getName());

        if (photosList == null) {
            photosList = new ArrayList<Photo>();
            photosList = albumList.get(pos).getPhotos();
        } else {
            photosList = albumList.get(pos).getPhotos();
        }
        adapter = new PhotoAdapter(photosList);
        photoList.setAdapter(adapter);
        photoList.setLayoutManager(new LinearLayoutManager(this));


        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int position = viewHolder.getAdapterPosition();
                albumList.get(pos).getPhotos().remove(position);
                adapter.notifyDataSetChanged();
                SharedPreferences mPrefs = getPreferences(MODE_PRIVATE);
                SharedPreferences.Editor prefsEditor = mPrefs.edit();
                Gson gson = new Gson();
                String json = gson.toJson(albumList);
                prefsEditor.putString("albumList", json);
                prefsEditor.commit();
                create(OpenAlbum.this, "storage.json", json);
            }
        });

        itemTouchHelper.attachToRecyclerView(photoList);

    }

    public void openPhoto(int position) {
        Log.w("test", String.valueOf(position));
    }

    public void uploadPhoto(View v) {
        startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        String[] photoName = new String[1];
        String photoString = null;
        //Detects request codes
        if (requestCode == GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            Bitmap bitmap = null;


            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                photoString = BitMapToString(bitmap);
                createPhoto(photoString);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }


    }

    public void createPhoto(String photoString) {
        AlertDialog.Builder editAlert = new AlertDialog.Builder(OpenAlbum.this);
        final EditText newPhotoName = new EditText(OpenAlbum.this);
        newPhotoName.setHint("Photo Name");
        editAlert.setTitle("Please Enter The New Photo Name");
        editAlert.setView(newPhotoName);

        editAlert.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String photoName = newPhotoName.getText().toString();
                Photo photo = new Photo(photoName, photoString);
                albumList.get(pos).addPhoto(photo);
                adapter.notifyDataSetChanged();
                SharedPreferences mPrefs = getPreferences(MODE_PRIVATE);
                SharedPreferences.Editor prefsEditor = mPrefs.edit();
                Gson gson = new Gson();
                String json = gson.toJson(albumList);
                prefsEditor.putString("albumList", json);
                prefsEditor.commit();
                create(OpenAlbum.this, "storage.json", json);
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

    public class PhotoAdapter extends
            RecyclerView.Adapter<OpenAlbum.PhotoAdapter.ViewHolder> {
        public class ViewHolder extends RecyclerView.ViewHolder {

            public TextView nameTextView;
            public Button deletebtn;
            public ImageView photoView;
            public TextView buttonViewOption;

            public ViewHolder(View itemView) {
                super(itemView);
                nameTextView = itemView.findViewById(R.id.photoName);
                deletebtn = itemView.findViewById(R.id.deletePhoto);
                photoView = itemView.findViewById(R.id.imageView);
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
        public OpenAlbum.PhotoAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            View contactView = inflater.inflate(R.layout.album_photos_list_view, parent, false);
            OpenAlbum.PhotoAdapter.ViewHolder viewHolder = new OpenAlbum.PhotoAdapter.ViewHolder(contactView);
            return viewHolder;
        }

        // Involves populating data into the item through holder
        @Override
        public void onBindViewHolder(OpenAlbum.PhotoAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
            Photo photo = PhotoList.get(position);
            Bitmap img = StringToBitMap(photo.getPhotoString());

            TextView textView = holder.nameTextView;
            Button deletebtn = holder.deletebtn;
            ImageView photoView = holder.photoView;


            photoView.setImageBitmap(img);
            textView.setText(photo.getName());

            deletebtn.setOnClickListener(view -> {
                albumList.get(pos).getPhotos().remove(position);
                notifyDataSetChanged();

                SharedPreferences mPrefs = getPreferences(MODE_PRIVATE);
                SharedPreferences.Editor prefsEditor = mPrefs.edit();
                Gson gson = new Gson();
                String json = gson.toJson(albumList);
                prefsEditor.putString("albumList", json);
                prefsEditor.commit();
                create(OpenAlbum.this, "storage.json", json);
            });


            holder.buttonViewOption.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //creating a popup menu
                    PopupMenu popup = new PopupMenu(OpenAlbum.this, holder.buttonViewOption);
                    //inflating menu from xml resource
                    popup.inflate(R.menu.options_menu);
                    //adding click listener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.move:
                                    movePhoto(position);
                                    break;
                                case R.id.edit:
                                    editName(position);
                                    break;
                            }
                            return false;
                        }
                    });
                    //displaying the popup
                    popup.show();

                }
            });

            holder.itemView.setOnClickListener(view -> openPhoto(position));
        }

        @Override
        public int getItemCount() {
            return PhotoList.size();
        }


    }

    public void editName(int photoPosition) {

        AlertDialog.Builder editAlert = new AlertDialog.Builder(OpenAlbum.this);
        final EditText newPhotoName = new EditText(OpenAlbum.this);
        newPhotoName.setHint("Photo Name");
        editAlert.setTitle("Please Enter The New Photo Name");
        editAlert.setView(newPhotoName);

        editAlert.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                albumList.get(pos).getPhotos().get(photoPosition).setName(newPhotoName.getText().toString());
                adapter.notifyDataSetChanged();

                SharedPreferences mPrefs = getPreferences(MODE_PRIVATE);
                SharedPreferences.Editor prefsEditor = mPrefs.edit();
                Gson gson = new Gson();
                String json = gson.toJson(albumList);
                prefsEditor.putString("albumList", json);
                prefsEditor.commit();
                create(OpenAlbum.this, "storage.json", json);
            }
        });

        editAlert.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                Context context = getApplicationContext();
                CharSequence text = "The Photo name will not be changed";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        });
        editAlert.show();


    }

    public void movePhoto(int photoPostion) {


        List<Album> spinnerList = albumList.stream()
                .filter(album -> !album.getName().equals(albumList.get(pos).getName()))
                .collect(Collectors.toList());


        AlertDialog.Builder mBuilder = new AlertDialog.Builder(OpenAlbum.this);
        View mView = getLayoutInflater().inflate(R.layout.dialog_spinner, null);
        mBuilder.setTitle("Select an Album");
        Spinner mSpinner = (Spinner) mView.findViewById(R.id.spinner);
        ArrayAdapter adp = new ArrayAdapter(OpenAlbum.this, android.R.layout.simple_spinner_dropdown_item, spinnerList);
        adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adp);


        mBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Album album = (Album) mSpinner.getSelectedItem();
                album.addPhoto(albumList.get(pos).getPhotos().get(photoPostion));
                albumList.get(pos).getPhotos().remove(photoPostion);
                adapter.notifyDataSetChanged();

                SharedPreferences mPrefs = getPreferences(MODE_PRIVATE);
                SharedPreferences.Editor prefsEditor = mPrefs.edit();
                Gson gson = new Gson();
                String json = gson.toJson(albumList);
                prefsEditor.putString("albumList", json);
                prefsEditor.commit();
                create(OpenAlbum.this, "storage.json", json);
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

    public boolean isFilePresent(Context context, String fileName) {
        String path = context.getFilesDir().getAbsolutePath() + "/" + fileName;
        File file = new File(path);
        return file.exists();
    }

}
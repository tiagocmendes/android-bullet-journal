package ua.deti.bulletjounal;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MenuItem;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class CollectionsHub extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "CollectionsHub";
    private RecyclerView collectionsRecyclerView;
    private HubAdapter collectionsAdapter;
    private RecyclerView.LayoutManager collectionsLayoutManager;
    private ArrayList<HubItem> collections;

    private ImageView saveBtn;
    private ImageView cancelBtn;
    private ImageView deleteBtn;
    private EditText editText;
    private String inputText;

    private View thisView;
    private Context thisContext;
    private TextView show;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collections_hub);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Collections Hub");
        show=(TextView)findViewById(R.id.textView7) ;


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(4).setChecked(true);

        ImageView addCollectionBtn = findViewById(R.id.addCollectionBtn);

        thisContext = getBaseContext();
        thisView = getCurrentFocus();
        addCollectionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                thisView = view;
                callAndDialog(view);
            }
        });

        // recycle view
        createCollectionsList();
        buildRecyclerView();

        // load collections
        loadCollections();

        if(collections.size()==0){
            show.setText("Empty! Add something :)");
        }
        else
            show.setText("");

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.collections_hub, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();



        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Intent myIntent = null;
        if (id == R.id.dailyIcon) {
            myIntent = new Intent(this, Daily_Log_Hub.class);;
        } else if (id == R.id.monthlyIcon) {
            myIntent = new Intent(this, Monthly_Log_Hub.class);
        } else if (id == R.id.yearlyIcon) {
            myIntent = new Intent(this, Yearly_Log_Hub.class);
        } else if (id == R.id.collectionIcon) {
            myIntent = new Intent(this, CollectionsHub.class);
        } else if(id == R.id.homeIcon) {
            myIntent = new Intent(this, MainActivity.class);
        } else if(id == R.id.helpIcon) {
            myIntent = new Intent(this, Intro.class);

        }
        if(id != R.id.collectionIcon)
            startActivity(myIntent);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void callAndDialog(View view)
    {
        final Dialog addDialog = new Dialog(this);
        thisContext = this;
        addDialog.setContentView(R.layout.pop_window_add_collection);
        addDialog.setCancelable(true);
        addDialog.setTitle("New collection");

        saveBtn = addDialog.findViewById(R.id.saveBtn);
        cancelBtn = addDialog.findViewById(R.id.cancelBtn);

        // dismiss modal
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDialog.dismiss();
            }
        });

        // save
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText = addDialog.findViewById(R.id.editText2);
                inputText = editText.getText().toString();
                if(inputText.length() == 0)
                {
                    Toast.makeText(addDialog.getContext(), "Please, add a name to the collection.", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    insertItem(inputText);
                    saveCollections(-1);
                    addDialog.dismiss();
                }

            }
        });

        addDialog.show();
    }

    public void createCollectionsList()
    {
        collections = new ArrayList<>();
    }

    public void  buildRecyclerView()
    {
        collectionsRecyclerView = findViewById(R.id.collectionsRecyclerView);
        collectionsRecyclerView.setHasFixedSize(true);
        collectionsLayoutManager = new GridLayoutManager(this, 2);
        collectionsAdapter = new HubAdapter(collections);
        collectionsRecyclerView.setLayoutManager(collectionsLayoutManager);
        collectionsRecyclerView.setAdapter(collectionsAdapter);

        collectionsAdapter.setOnItemClickListener(new HubAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Intent intent = new Intent(getBaseContext(), CollectionPage.class);
                intent.putExtra("CollectionName", collections.get(position).getItemName());
                startActivity(intent);

            }
        });

        collectionsAdapter.setOnItemLongClickListener(new HubAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(int position) {
                deleteDialog(position);
                //removeItem(position);
                //saveCollections(position);
            }
        });
    }

    public void insertItem(String inputText)
    {
        HubItem itemToInsert = new HubItem(inputText);
        for(HubItem hi : collections)
        {
            if(hi.getItemName().equals(inputText))
            {
                Toast.makeText(getBaseContext(), "There is already a collection named '" + inputText + "'", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        collections.add(new HubItem(inputText));
        collectionsAdapter.notifyItemInserted(collections.size()-1);

        if(collections.size()==0){
            show.setText("Empty! Add something :)");
        }
        else
            show.setText("");
    }

    public void removeItem(int position)
    {
        collections.remove(position);
        collectionsAdapter.notifyItemRemoved(position);

        if(collections.size()==0){
            show.setText("Empty! Add something :)");
        }
        else
            show.setText("");
    }



    public void loadCollections()
    {
        File myDir = getApplicationContext().getFilesDir();
        File documentsFolder = new File(myDir, "Collections");
        if(!documentsFolder.exists())
            documentsFolder.mkdirs();
        File[] files = documentsFolder.listFiles();
        if (files == null)
        {
            Toast.makeText(getBaseContext(),"There are no collections", Toast.LENGTH_SHORT).show();
        }
        else {
            for (File inFile : files) {
                String[] name = inFile.getName().split("\\.");
                insertItem(name[0]);
            }
        }
    }

    public void saveCollections(int mode)
    {
        File myDir = getApplicationContext().getFilesDir();
        File documentsFolder = new File(myDir,"Collections");
        File[] files = documentsFolder.listFiles();
        if (files == null)
        {
            Toast.makeText(getBaseContext(),"There are no collections", Toast.LENGTH_SHORT).show();
        }
        if (mode == -1)
        {
            for(HubItem hi : collections)
            {
                File myFile = new File(documentsFolder, hi.getItemName() + ".txt");
                if(!myFile.exists())
                    try
                    {
                        myFile.createNewFile();
                    } catch (IOException e){}
            }
        }
        else
        {
            files[mode].delete();
        }
    }

    public void deleteDialog(int position)
    {
        final Dialog deleteDialog = new Dialog(this);
        deleteDialog.setContentView(R.layout.pop_window_delete_collection);
        deleteDialog.setCancelable(true);
        deleteDialog.setTitle("Delete collection");

        TextView question = deleteDialog.findViewById(R.id.deleteCollection);
        question.setText("Do you want to delete \n'" + collections.get(position).getItemName() + "'?");

        ImageView noButton = deleteDialog.findViewById(R.id.noDeleteCollection);
        ImageView yesButton = deleteDialog.findViewById(R.id.yesDeleteCollection);

        // dismiss modal
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteDialog.dismiss();
            }
        });

        final int p = position;
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeItem(p);
                saveCollections(p);
                deleteDialog.dismiss();
            }
        });
        deleteDialog.show();
    }



}

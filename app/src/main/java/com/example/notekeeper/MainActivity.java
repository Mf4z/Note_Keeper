package com.example.notekeeper;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notekeeper.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.example.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import static com.example.notekeeper.NoteKeeperProviderContract.*;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor> {
    public static final int LOADER_NOTES = 0;
    private NoteRecyclerAdapter mNoteRecyclerAdapter;
    private RecyclerView mRecyclerItems;
    private LinearLayoutManager mNotesLayoutManager;
    private CourseRecyclerAdapter mCourseRecyclerAdapter;
    private GridLayoutManager mCoursesLayoutManager;
    private NoteKeeperOpenHelper mDbOpenHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDbOpenHelper = new NoteKeeperOpenHelper(this);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,NoteActivity.class));
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        initializeDisplayContent();
    }


    @Override
    protected void onDestroy() {
        mDbOpenHelper.close();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(LOADER_NOTES, null, this);
        updateNavHeader();
    }

    private void loadNotes() {
        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();

        String[] noteColumns = {
                NoteInfoEntry.COLUMN_NOTE_TITLE,
                NoteInfoEntry.COLUMN_COURSE_ID,
                NoteInfoEntry._ID};

        String noteOrderBy = NoteInfoEntry.COLUMN_COURSE_ID + "," + NoteInfoEntry.COLUMN_NOTE_TITLE;
        final Cursor noteCursor = db.query(NoteInfoEntry.TABLE_NAME, noteColumns,
                null, null, null, null,
                noteOrderBy);

        mNoteRecyclerAdapter.changeCursor(noteCursor);
    }

    private void updateNavHeader() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView textUsername = (TextView) headerView.findViewById(R.id.text_user_name);
        TextView textEmailAddress = (TextView) headerView.findViewById(R.id.text_email_address);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String username = pref.getString("user_display_name","");
        String emailAddress = pref.getString("user_email_address","");

        textUsername.setText(username);
        textEmailAddress.setText(emailAddress);



    }

    private void initializeDisplayContent() {

        DataManager.loadFromDatabase(mDbOpenHelper);
        mRecyclerItems = (RecyclerView) findViewById(R.id.list_items);
        mNotesLayoutManager = new LinearLayoutManager(this);
        mCoursesLayoutManager = new GridLayoutManager(this,getResources().getInteger(R.integer.course_grid_span));


        mNoteRecyclerAdapter = new NoteRecyclerAdapter(this,null);

        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        mCourseRecyclerAdapter = new CourseRecyclerAdapter(this,courses);

        displayNotes();
    }

    private void displayNotes() {
        mRecyclerItems.setAdapter(mNoteRecyclerAdapter);
        mRecyclerItems.setLayoutManager(mNotesLayoutManager);

        selectNavigationMenuItem(R.id.nav_notes);
    }

    private void selectNavigationMenuItem(int id) {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();
        menu.findItem(id).setChecked(true);
    }

    private void displayCourses(){
        mRecyclerItems.setLayoutManager(mCoursesLayoutManager);
        mRecyclerItems.setAdapter(mCourseRecyclerAdapter);

        selectNavigationMenuItem(R.id.nav_courses);
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
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this,SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_notes) {
            displayNotes();
        } else if (id == R.id.nav_courses) {
            displayCourses();
        } else if (id == R.id.nav_share) {
           // handleSelection(R.string.nav_share_message);
            handleShare();
        } else if (id == R.id.nav_send) {
            handleSelection(R.string.nav_send_message);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void handleShare() {

        View view = findViewById(R.id.list_items);
        Snackbar.make(view,"Share to - " +
                PreferenceManager.getDefaultSharedPreferences(this).getString("reply",""),Snackbar.LENGTH_LONG).show();
    }

    private void handleSelection(int message_id) {

        View view = findViewById(R.id.list_items);
        Snackbar.make(view,message_id,Snackbar.LENGTH_LONG).show();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        CursorLoader loader = null;
        if(id == LOADER_NOTES) {


                    final String[] noteColumns = {
                            Notes._ID,
                            Notes.COLUMN_NOTE_TITLE,
                            Notes.COLUMN_COURSE_TITLE
                    };

                    final String noteOrderBy = Notes.COLUMN_COURSE_TITLE +
                            "," + Notes.COLUMN_NOTE_TITLE;

                    loader = new CursorLoader(this, Notes.CONTENT_EXPANDED_URI,noteColumns,
                            null,null,noteOrderBy);
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(loader.getId() == LOADER_NOTES)  {
            mNoteRecyclerAdapter.changeCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if(loader.getId() == LOADER_NOTES)  {
            mNoteRecyclerAdapter.changeCursor(null);
        }
    }
}



package com.example.notekeeper;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


import android.os.StrictMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.example.notekeeper.NoteKeeperProviderContract.Courses;
import com.example.notekeeper.NoteKeeperProviderContract.Notes;

import static com.example.notekeeper.NoteKeeperDatabaseContract.*;

public class NoteActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int LOADER_NOTES = 0;
    public static final int LOADER_COURSES = 1;
    private final String TAG = getClass().getSimpleName();
    public static final String NOTE_ID = "com.example.notekeeper.NOTE_ID";
    public static final String ORIGINAL_NOTE_COURSE_ID = "com.example.notekeeper.ORIGINAL_NOTE_COURSE_ID";
    public static final String ORIGINAL_NOTE_TITLE = "com.example.notekeeper.ORIGINAL_NOTE_TITLE";
    public static final String ORIGINAL_NOTE_TEXT = "com.example.notekeeper.ORIGINAL_NOTE_TEXT";
    public static final int ID_NOT_SET = -1;
    private NoteInfo mNote = new NoteInfo(DataManager.getInstance().getCourses().get(0), "", "");
    private boolean mIsNewNote;
    private Spinner mSpinnerCourses;
    private EditText mTextNoteTitle;
    private EditText mTextNoteText;
    private int mNotePosition;
    private boolean mIsCanceling;
    private String mOriginalNoteCourseID;
    private String mOriginalNoteTitle;
    private String mOriginalNoteText;
    private NoteKeeperOpenHelper mDbOpenHelper;
    private Cursor mNoteCursor;
    private int mCourseIdPos;
    private int mNoteTitlePos;
    private int mNoteTextPos;
    private int mNoteId;
    private SimpleCursorAdapter mAdapterCourses;
    private boolean mCoursesQueryFinished;
    private boolean mNotesQueryFinished;
    private Uri mNoteUri;

    @Override
    protected void onDestroy() {
        mDbOpenHelper.close();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        enableStrictMode();

        mDbOpenHelper = new NoteKeeperOpenHelper(this);
        mSpinnerCourses = (Spinner) findViewById(R.id.spinner_courses);


        mAdapterCourses = new SimpleCursorAdapter(this,android.R.layout.simple_list_item_1, null,
                new String[] {CourseInfoEntry.COLUMN_COURSE_TITLE},
                new int[] {android.R.id.text1}, 0);

        mAdapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //Setting adapter to spinner
        mSpinnerCourses.setAdapter(mAdapterCourses);

        //Load Courses from db
        //loadCourseData();
        getLoaderManager().initLoader(LOADER_COURSES,null,this);

        //Method to display values gotten from intent
        readDisplayStateValues();

        if(savedInstanceState == null){
        //Method retrive the original values
        saveOriginalNoteValues();
        }
        else {
            restoreOriginalNoteValues(savedInstanceState);
        }


        mTextNoteTitle = (EditText) findViewById(R.id.editText_note_title);
        mTextNoteText = (EditText) findViewById(R.id.editText_note_text);

        if(!mIsNewNote)
            getLoaderManager().initLoader(LOADER_NOTES,null,this);


    }

    private void enableStrictMode() {
        if (BuildConfig.DEBUG){
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build();

            StrictMode.setThreadPolicy(policy);
        }
    }

    private void loadCourseData() {
        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();
        String[] courseColumns = {
                CourseInfoEntry.COLUMN_COURSE_TITLE,
                CourseInfoEntry.COLUMN_COURSE_ID,
                CourseInfoEntry._ID};

        Cursor cursor = db.query(CourseInfoEntry.TABLE_NAME,courseColumns,
                null,null,null,
                null,CourseInfoEntry.COLUMN_COURSE_TITLE);

        mAdapterCourses.changeCursor(cursor);
    }

    private void loadNoteData() { //Querying info for a particular note
        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();

        String courseId = "android_intents";
        String titleStart = "dynamic";

        String selection = NoteInfoEntry._ID + " = ?";
        String[] selectionArgs = {Integer.toString(mNoteId)};

        String[] noteColumns = {
          NoteInfoEntry.COLUMN_COURSE_ID,
          NoteInfoEntry.COLUMN_NOTE_TITLE,
          NoteInfoEntry.COLUMN_NOTE_TEXT
        };

        mNoteCursor = db.query(NoteInfoEntry.TABLE_NAME,noteColumns,selection,selectionArgs,
                null,null,null);

        mCourseIdPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        mNoteTitlePos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        mNoteTextPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);
        mNoteCursor.moveToNext();

        displayNote();
    }

    private void restoreOriginalNoteValues(Bundle savedInstanceState) {
        mOriginalNoteCourseID = savedInstanceState.getString(ORIGINAL_NOTE_COURSE_ID);
        mOriginalNoteTitle = savedInstanceState.getString(ORIGINAL_NOTE_TITLE);
        mOriginalNoteText = savedInstanceState.getString(ORIGINAL_NOTE_TEXT);
    }

    private void saveOriginalNoteValues() {
        if(mIsNewNote)
            return;

        mOriginalNoteCourseID = mNote.getCourse().getCourseId();
        mOriginalNoteTitle = mNote.getTitle();
        mOriginalNoteText = mNote.getText();
    }

    private void createNewNote() {
        final ContentValues values = new ContentValues();
        values.put(Notes.COLUMN_COURSE_ID,"");
        values.put(Notes.COLUMN_NOTE_TITLE,"");
        values.put(Notes.COLUMN_NOTE_TEXT,"");

        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {

                mNoteUri = getContentResolver().insert(Notes.CONTENT_URI,values);
                return null;
            }
        };

        task.execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mIsCanceling){
            if(mIsNewNote){
                deleteNoteFromDatabase();
            }
            else {
                storePreviousNoteValues();
            }
        }
        else {
        saveNote();
        }
    }

    private void deleteNoteFromDatabase() {
        final String selection = NoteInfoEntry._ID + " = ?";
        final String[] selectionArgs = {Integer.toString(mNoteId)};

        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {

                SQLiteDatabase  db = mDbOpenHelper.getWritableDatabase();
                db.delete(NoteInfoEntry.TABLE_NAME,selection,selectionArgs);
                return null;
            }
        };

        task.execute();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(ORIGINAL_NOTE_COURSE_ID,mOriginalNoteCourseID);
        outState.putString(ORIGINAL_NOTE_TITLE,mOriginalNoteTitle);
        outState.putString(ORIGINAL_NOTE_TEXT,mOriginalNoteText);
    }

    private void storePreviousNoteValues() {
        CourseInfo course = DataManager.getInstance().getCourse(mOriginalNoteCourseID);
        mNote.setCourse(course);
        mNote.setTitle(mOriginalNoteTitle);
        mNote.setText(mOriginalNoteText);
    }

    private void saveNote() {
        String courseId = selectedCourseId();
        String noteTitle = mTextNoteTitle.getText().toString();
        String noteText = mTextNoteText.getText().toString();
        saveNoteToDatabase(courseId,noteTitle,noteText);
    }

    private String selectedCourseId() {
        int selectedPosition = mSpinnerCourses.getSelectedItemPosition();
        Cursor cursor = mAdapterCourses.getCursor();
        cursor.moveToPosition(selectedPosition);
        int courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);
        String courseId = cursor.getString(courseIdPos);
        return courseId;
    }

    private void saveNoteToDatabase (final String courseId,final String noteTitle,final String noteText){

        final String selection = NoteInfoEntry._ID + " = ?";
        final String[] selectionArgs = {Integer.toString(mNoteId)};

        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                ContentValues values = new ContentValues();
                values.put(NoteInfoEntry.COLUMN_COURSE_ID,courseId);
                values.put(NoteInfoEntry.COLUMN_NOTE_TITLE,noteTitle);
                values.put(NoteInfoEntry.COLUMN_NOTE_TEXT,noteText);

                SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
                db.update(NoteInfoEntry.TABLE_NAME,values,selection,selectionArgs);

                return null;
            }
        };
        task.execute();
    }

    private void displayNote() {
        String courseId = mNoteCursor.getString(mCourseIdPos);
        String noteTitle = mNoteCursor.getString(mNoteTitlePos);
        String noteText = mNoteCursor.getString(mNoteTextPos);

        int courseIndex = getIndexOfCourseId(courseId);
        mSpinnerCourses.setSelection(courseIndex);
        mTextNoteTitle.setText(noteTitle);
        mTextNoteText.setText(noteText);

    }

    private int getIndexOfCourseId(String courseId) {
        Cursor cursor = mAdapterCourses.getCursor();
        int courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);
        int courseRowIndex = 0;

        boolean more = cursor.moveToFirst();
        while (more){
            String coursorCourseId = cursor.getString(courseIdPos);
            if(courseId.equals(coursorCourseId))
                break;

            courseRowIndex++;
            more = cursor.moveToNext();
        }

        return courseRowIndex;
    }

    private void readDisplayStateValues() {
        Intent intent = getIntent();
        mNoteId = intent.getIntExtra(NOTE_ID, ID_NOT_SET);

        mIsNewNote = mNoteId == ID_NOT_SET;

        if(mIsNewNote){
            createNewNote();
        }
        /*if(!mIsNewNote)
            mNote = DataManager.getInstance().getNotes().get(mNoteId);*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send_mail) {

            sendEmail();
            return true;
        }

        else if (id == R.id.action_cancel) {
            mIsCanceling = true;
            finish(); //It means end activity. This is a
        }
        else if (id == R.id.action_next) {
            moveNext();

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_next);
        int lastNoteIndex = DataManager.getInstance().getNotes().size()-1;
        item.setEnabled(mNotePosition < lastNoteIndex);
        return super.onPrepareOptionsMenu(menu);
    }

    private void moveNext() {
        saveNote();

        ++mNotePosition;
        mNote = DataManager.getInstance().getNotes().get(mNotePosition);

        saveOriginalNoteValues();
        displayNote();
        invalidateOptionsMenu();
    }

    private void sendEmail() {

        CourseInfo course = (CourseInfo) mSpinnerCourses.getSelectedItem();
        String subject = mTextNoteTitle.getText().toString();
        String text = "Check out what I learnt in the Pluralsight Course \"" +course.getTitle()+"" +
                "\"\n"+ mTextNoteText.getText().toString();

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc2822");
        intent.putExtra(Intent.EXTRA_SUBJECT,subject);
        intent.putExtra(Intent.EXTRA_TEXT,text);
        startActivity(intent);

    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = null;
        if (id == LOADER_NOTES)
            loader = createLoaderNotes();
        else if (id == LOADER_COURSES)
            loader = createLoaderCourses();
        return loader;
    }

    private CursorLoader createLoaderCourses() {
        mCoursesQueryFinished = false;

        Uri uri = Courses.CONTENT_URI;
        String[] courseColumns = {
                Courses.COLUMN_COURSE_TITLE,
                Courses.COLUMN_COURSE_ID,
                Courses._ID
        };

        return new CursorLoader(this,uri,courseColumns,null,null,Courses.COLUMN_COURSE_TITLE);

    }

    private CursorLoader createLoaderNotes() {
        mNotesQueryFinished = false;

        String[] noteColumns = {
                        Notes.COLUMN_COURSE_ID,
                        Notes.COLUMN_NOTE_TITLE,
                        Notes.COLUMN_NOTE_TEXT
                };

     mNoteUri =  ContentUris.withAppendedId(Notes.CONTENT_URI,mNoteId);

     return new CursorLoader(this,mNoteUri,noteColumns,null,null,null);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if(loader.getId() == LOADER_NOTES) {
            loadFinishedNotes(data);
        }
        else if (loader.getId() == LOADER_COURSES){
            mAdapterCourses.changeCursor(data);
            mCoursesQueryFinished = true;
            displayNoteWhenQueriesFinished();
        }

    }

    private void loadFinishedNotes(Cursor data) {
        mNoteCursor = data;

        mCourseIdPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        mNoteTitlePos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        mNoteTextPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);
        mNoteCursor.moveToNext();
        mNotesQueryFinished = true;
        //displayNote();
        displayNoteWhenQueriesFinished();
    }

    private void displayNoteWhenQueriesFinished() {
        if(mNotesQueryFinished && mCoursesQueryFinished)
            displayNote();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == LOADER_NOTES){
            if(mNoteCursor != null)
                mNoteCursor.close();
        }else if (loader.getId() == LOADER_COURSES){
            mAdapterCourses.changeCursor(null);
        }


    }
}

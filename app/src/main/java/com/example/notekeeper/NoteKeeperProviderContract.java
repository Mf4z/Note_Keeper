package com.example.notekeeper;

import android.net.Uri;
import android.os.StrictMode;
import android.provider.BaseColumns;

public final class NoteKeeperProviderContract {

    private NoteKeeperProviderContract(){}

    public static final String AUTHORITY = "com.example.notekeeper.provider";
    public static final Uri AUTHORITY_URI = Uri.parse("content://"+ AUTHORITY) ;

    protected interface CoursesIdColumns{ //for columns appearing in two tables

        public static final String COLUMN_COURSE_ID = "course_id";

    }
    protected interface CoursesColumns{

        public static final String COLUMN_COURSE_TITLE = "course_title  ";
    }

    protected interface NotesColumns {

        public static final String COLUMN_NOTE_TITLE = "note_title  ";
        public static final String COLUMN_NOTE_TEXT = "note_text  ";


    }


    public static final class Courses implements BaseColumns,CoursesColumns,CoursesIdColumns {
        public static final String PATH = "courses";
        //content://com.example.notekeeper.provider/courses

        //can be used to access content provider courses table
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI,PATH);
    }

    public static final class Notes implements BaseColumns,NotesColumns,CoursesIdColumns{
        public static final String PATH = "notes";

        //can be used to access content provider notes table
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI,PATH);
    }
}

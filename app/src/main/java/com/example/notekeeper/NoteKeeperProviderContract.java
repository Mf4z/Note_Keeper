package com.example.notekeeper;

import android.net.Uri;
import android.os.StrictMode;

public final class NoteKeeperProviderContract {

    private NoteKeeperProviderContract(){}

    public static final String AUTHORITY = "com.example.notekeeper.provider";
    public static final Uri AUTHORITY_URI = Uri.parse("content://"+ AUTHORITY) ;

    public static final class Courses {
        public static final String PATH = "courses";
        //content://com.example.notekeeper.provider/courses

        //can be used to access content provider courses table
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI,PATH);
    }

    public static final class Notes {
        public static final String PATH = "notes";

        //can be used to access content provider notes table
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI,PATH);
    }
}

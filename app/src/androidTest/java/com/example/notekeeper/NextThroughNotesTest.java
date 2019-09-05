package com.example.notekeeper;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.*;
import android.content.Intent;
import android.os.Bundle;
import android.service.autofill.FieldClassification;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.test.espresso.contrib.DrawerActions;
import android.support.test.espresso.contrib.NavigationViewActions;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;

import java.util.List;
import java.util.regex.Matcher;

public class NextThroughNotesTest   {

    @Rule
   public ActivityTestRule <MainActivity> mActivityTestRule = new ActivityTestRule(MainActivity.class);

    @Test
    public void NextThroughNotes(){
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_notes));

        onView(withId(R.id.list_items)).perform(actionOnItemAtPosition(0,click()));

        List<NoteInfo> notes = DataManager.getInstance().getNotes();
        int index = 0;
        NoteInfo note = notes.get(index);

        onView(withId(R.id.spinner_courses)).check(
                matches(withSpinnerText(note.getCourse().getTitle())));
        onView(withId(R.id.editText_note_title)).check(matches(withText(note.getTitle())));
        onView(withId(R.id.editText_note_text)).check(matches(withText(note.getText())));
    }

}
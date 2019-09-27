package com.example.notekeeper;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class NoteRecyclerAdapter extends RecyclerView.Adapter<NoteRecyclerAdapter.ViewHolder>{

    private final Context mContext;
    private final LayoutInflater mLayoutInflater;
    private final List<NoteInfo> mNotes;

    public NoteRecyclerAdapter(Context mContext, List<NoteInfo> mNotes) {
        this.mContext = mContext;
        this.mNotes = mNotes;
        //Creating a ayout using layout inflater
        mLayoutInflater = LayoutInflater.from(mContext);

    }

    @NonNull
    @Override  //What recycler view uses to create pool of views.
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = mLayoutInflater.inflate(R.layout.item_note_list,viewGroup,false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        NoteInfo note = mNotes.get(position);
        viewHolder.mTextCourse.setText(note.getCourse().getTitle());
        viewHolder.mTextTitle.setText(note.getTitle());
        viewHolder.mId = note.getId();


    }

    @Override
    public int getItemCount() {
        //To know how much data there is
        return mNotes.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public final TextView mTextCourse;
        public final TextView mTextTitle;
        public int mId;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mTextCourse = (TextView) itemView.findViewById(R.id.text_course);
            mTextTitle = (TextView) itemView.findViewById(R.id.text_title);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext,NoteActivity.class);
                    intent.putExtra(NoteActivity.NOTE_ID, mId);
                    mContext.startActivity(intent);
                }
            });

        }
    }
}

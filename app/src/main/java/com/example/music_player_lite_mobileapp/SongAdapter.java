package com.example.music_player_lite_mobileapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {
    private Context context;
    private ArrayList<Song> songList;
    private OnItemClickListener listener;

    public interface OnItemClickListener { void onItemClick(int position); }
    public void setOnItemClickListener(OnItemClickListener listener) { this.listener = listener; }

    public SongAdapter(Context context, ArrayList<Song> songList) {
        this.context = context;
        this.songList = songList;
    }

    @NonNull @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_song, parent, false);
        return new SongViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songList.get(position);
        holder.txtTitle.setText(song.getTitle());
        holder.txtArtist.setText(song.getArtist());
    }

    @Override public int getItemCount() { return songList.size(); }

    public static class SongViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtArtist;
        public SongViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtArtist = itemView.findViewById(R.id.txtArtist);
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(getAdapterPosition());
            });
        }
    }
}
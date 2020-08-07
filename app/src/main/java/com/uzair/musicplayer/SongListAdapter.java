package com.uzair.musicplayer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class SongListAdapter extends BaseAdapter
{
   private List<SongModel> songModelList;

    public SongListAdapter(List<SongModel> songModelList) {
        this.songModelList = songModelList;
    }

    @Override
    public int getCount() {
        return songModelList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent)
    {
        View myView = LayoutInflater.from(parent.getContext()).inflate(R.layout.play_list_item, null);

        TextView songTitle = myView.findViewById(R.id.songTitle);
        TextView songArtist = myView.findViewById(R.id.songArtist);
        TextView songAlbum= myView.findViewById(R.id.songAlbum);
        SongModel  model = songModelList.get(position);

        songTitle.setText(model.getaName());
        songAlbum.setText(model.getaAlbum());
        songArtist.setText(model.getaArtist());


        return myView;
    }
}

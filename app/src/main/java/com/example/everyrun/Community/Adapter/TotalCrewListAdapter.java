package com.example.everyrun.Community.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.everyrun.R;
import com.example.everyrun.RetrofitData.CrewData;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class TotalCrewListAdapter extends BaseAdapter {

    Context mContext = null;
    LayoutInflater layoutInflater =null;
    ArrayList<CrewData> crewDataArrayList;

    public TotalCrewListAdapter(Context mContext, ArrayList<CrewData> crewDataArrayList) {
        this.mContext = mContext;
        this.crewDataArrayList = crewDataArrayList;
    }

    @Override
    public int getCount() {
        return crewDataArrayList.size();
    }

    @Override
    public CrewData getItem(int position) {
        return crewDataArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int i, View converView, ViewGroup viewGroup) {
        View view = layoutInflater.inflate(R.layout.item_mycrewdetail, null);

        TextView title = (TextView) view.findViewById(R.id.title1);
        TextView area = (TextView) view.findViewById(R.id.area1);
        TextView current = (TextView) view.findViewById(R.id.current1);
        TextView tx_reader = (TextView) view.findViewById(R.id.tx_reader);
        ImageView img = (ImageView) view.findViewById(R.id.img1);

        final String[] spinnerArea = mContext.getResources().getStringArray(R.array.area_list);


        title.setText(crewDataArrayList.get(i).getTitle());
        area.setText(spinnerArea[crewDataArrayList.get(i).getArea()]);
        current.setText(String.valueOf(crewDataArrayList.get(i).getCurrent()));

        if(crewDataArrayList.get(i).getBanner().equals("basic"))
        {
            img.setImageResource(R.drawable.img1);
        }else{
            String url = "http://3.36.174.137/CrewImg/" + crewDataArrayList.get(i).getBanner();

            Glide.with(mContext).load(url).apply(new RequestOptions().centerCrop()).into(img);
        }

        tx_reader.setVisibility(View.GONE);

        return null;
    }
}

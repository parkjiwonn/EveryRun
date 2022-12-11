package com.example.everyrun.Community.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.everyrun.Community.Crew_CRUD.ReadCrewActivity;
import com.example.everyrun.R;
import com.example.everyrun.RetrofitData.CrewData;

import java.util.ArrayList;

// 내 크루 더보기 했을 때 grid 형식으로 내 크루 자세히 모아볼 수 있는 리사이클러뷰 어댑터
public class MyCrewDetailAdapter extends RecyclerView.Adapter<MyCrewDetailAdapter.ViewHolder>{

    private final ArrayList<CrewData> crewDataArrayList;
    private Context context;
    int pos;
    private final String TAG = this.getClass().getSimpleName();

    public MyCrewDetailAdapter(ArrayList<CrewData> crewDataArrayList, Context context) {
        this.crewDataArrayList = crewDataArrayList;
        this.context = context;
    }


    @NonNull
    @Override
    public MyCrewDetailAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "태그 onCreateViewHolder 들어옴");

        Context hcontext = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) hcontext.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
        // layoutinflater는 xml에 미리 정의해둔 툴을 실제 메모리에 올려준다. inflater는 부풀리다라는 의미

        View view = inflater.inflate (R.layout.item_mycrewdetail, parent, false);
        // Xml에 덩의된 resource를 view 객체로 반환해주는 역할을 한다.
        // 레이아웃정의부에서 실행되고 메모리 로딩이 돼 화면에 띄워주는 역할.


        //false는 바로 인플레이션 하지 x, true는 바로 인플에이션 한다.
        MyCrewDetailAdapter.ViewHolder hvh = new MyCrewDetailAdapter.ViewHolder(view);
        // 뷰홀더 생성 , 리턴값이 뷰홀더이다.
        return hvh;
    }

    @Override
    public void onBindViewHolder(@NonNull MyCrewDetailAdapter.ViewHolder holder, int position) {
        CrewData crewData = crewDataArrayList.get(position);

        String banner = crewData.getBanner();
        String title = crewData.getTitle();
        int current = crewData.getCurrent();
        int area = crewData.getArea();
        String now_user_email = crewData.getMessage(); // 현재 로그인한 유저 이메일
        String reader = crewData.getReader(); // 크루 리더

        final String[] spinnerArea = context.getResources().getStringArray(R.array.area_list);

        if(banner.equals("basic"))
        {
            holder.img.setImageResource(R.drawable.img1); // 옆에다가 주석처리.
        }
        else{

            String url = "http://3.36.174.137/CrewImg/" + banner;

            Glide.with(holder.itemView.getContext()).load(url).apply(new RequestOptions().centerCrop()).into(holder.img);
        }

        holder.title.setText(title);
        holder.area.setText(spinnerArea[area]);
        holder.current.setText(String.valueOf(current)+"명");

        if(now_user_email.equals(reader))
        {
            holder.tx_reader.setVisibility(View.VISIBLE);
        }else{
            holder.tx_reader.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return crewDataArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView title, area, current,tx_reader;
        public ImageView img;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            this.title = itemView.findViewById(R.id.title1);
            this.area = itemView.findViewById(R.id.area1);
            this.current = itemView.findViewById(R.id.current1);
            this.img = itemView.findViewById(R.id.img1);
            this.tx_reader = itemView.findViewById(R.id.tx_reader);

            // 아이템 클릭 리스너
            itemView.setClickable(true);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    pos = getAdapterPosition();

                    if(pos != RecyclerView.NO_POSITION){
                        CrewData crewData = crewDataArrayList.get(pos);
                        int crew_id = crewData.getCrew_id();
                        context.startActivity(new Intent(context, ReadCrewActivity.class).putExtra("crew_id", crew_id).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    }
                }
            });

        }
    }
}

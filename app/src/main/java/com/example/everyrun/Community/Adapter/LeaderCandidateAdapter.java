package com.example.everyrun.Community.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.everyrun.R;
import com.example.everyrun.RetrofitData.UserInfoData;

import java.util.ArrayList;

public class LeaderCandidateAdapter extends RecyclerView.Adapter<LeaderCandidateAdapter.ViewHolder> {

    private final ArrayList<UserInfoData> userInfoDataArrayList;
    private Context context;
    int pos;

    private final String TAG = this.getClass().getSimpleName();

    public LeaderCandidateAdapter(ArrayList<UserInfoData> userInfoDataArrayList, Context context) {
        this.userInfoDataArrayList = userInfoDataArrayList;
        this.context = context;
    }

    //-----------------------------------Custom Click Listener-----------------------
    public interface OnItemClickListener{
        void onItemClick(View v, int pos); // 멤버 선택(체크 버튼) 클릭 리스터
    }
    // 커스텀 리스너 인터페이스 정의

    private LeaderCandidateAdapter.OnItemClickListener cListener = null;
    // 전달된 객체를 저장할 변수 cListener 추가

    public void setOnItemClickListener(LeaderCandidateAdapter.OnItemClickListener listener){
        this.cListener = listener;
    }
    // 커스텀 리스너 객체를 전달하는 메서드

    //-----------------------------------Custom Click Listener-----------------------

    @NonNull
    @Override
    public LeaderCandidateAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "태그 onCreateViewHolder 들어옴");

        Context hcontext = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) hcontext.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
        // layoutinflater는 xml에 미리 정의해둔 툴을 실제 메모리에 올려준다. inflater는 부풀리다라는 의미

        View view = inflater.inflate (R.layout.item_reader_candidate, parent, false);
        // Xml에 덩의된 resource를 view 객체로 반환해주는 역할을 한다.
        // 레이아웃정의부에서 실행되고 메모리 로딩이 돼 화면에 띄워주는 역할.


        //false는 바로 인플레이션 하지 x, true는 바로 인플에이션 한다.
        LeaderCandidateAdapter.ViewHolder hvh = new LeaderCandidateAdapter.ViewHolder(view);
        // 뷰홀더 생성 , 리턴값이 뷰홀더이다.
        return hvh;
    }

    @Override
    public void onBindViewHolder(@NonNull LeaderCandidateAdapter.ViewHolder holder, int position) {

        UserInfoData userInfoData = userInfoDataArrayList.get(position);

        String photo = userInfoData.getUser_photo();
        String name = userInfoData.getUser_name();
        String message = userInfoData.getMessage(); // 체크 아이콘 클릭 여부 확인하기위한 변수

        holder.tx_name.setText(name); // 유저 이름 세팅하기

        if(photo.equals("basic"))
        {
            // 유저 프로필이 기본이미지라는 뜻
            holder.user_img.setImageResource(R.drawable.user_img);
        }else{
            // 유저 프로필이 사용자가 직접 설정한 사진이라는 뜻
            String url = "http://3.36.174.137/UserProfileImg/" + photo;

            Glide.with(holder.itemView.getContext()).load(url).apply(new RequestOptions().centerCrop()).into(holder.user_img);

        }

        if(message.equals("false"))
        {
            holder.btn_check.setImageResource(R.drawable.check_inactive);
        }else{
            holder.btn_check.setImageResource(R.drawable.check_active);
        }

    }

    @Override
    public int getItemCount() {
        return userInfoDataArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView btn_check;
        public ImageView user_img;
        public TextView tx_name;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            this.btn_check = itemView.findViewById(R.id.btn_check);
            this.user_img = itemView.findViewById(R.id.user_img);
            this.tx_name = itemView.findViewById(R.id.tx_name);

            itemView.setClickable(true);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = getAdapterPosition();

                    if(pos != RecyclerView.NO_POSITION)
                    {
                        if(cListener != null)
                        {
                            cListener.onItemClick(view, pos);
                        }
                    }
                }
            });


        }
    }
}

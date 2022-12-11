package com.example.everyrun.Community.Adapter;

import android.content.Context;
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
import com.example.everyrun.R;
import com.example.everyrun.RetrofitData.UserInfoData;

import java.util.ArrayList;

public class MemberListAdapter  extends RecyclerView.Adapter<MemberListAdapter.ViewHolder> {

    private final ArrayList<UserInfoData> userInfoDataArrayList;
    private Context context;
    int pos;

    private final String TAG = this.getClass().getSimpleName();

    public MemberListAdapter(ArrayList<UserInfoData> userInfoDataArrayList, Context context) {
        this.userInfoDataArrayList = userInfoDataArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public MemberListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "태그 onCreateViewHolder 들어옴");

        Context hcontext = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) hcontext.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
        // layoutinflater는 xml에 미리 정의해둔 툴을 실제 메모리에 올려준다. inflater는 부풀리다라는 의미

        View view = inflater.inflate (R.layout.item_memberlist, parent, false);
        // Xml에 덩의된 resource를 view 객체로 반환해주는 역할을 한다.
        // 레이아웃정의부에서 실행되고 메모리 로딩이 돼 화면에 띄워주는 역할.


        //false는 바로 인플레이션 하지 x, true는 바로 인플에이션 한다.
        MemberListAdapter.ViewHolder hvh = new MemberListAdapter.ViewHolder(view);
        // 뷰홀더 생성 , 리턴값이 뷰홀더이다.
        return hvh;
    }

    @Override
    public void onBindViewHolder(@NonNull MemberListAdapter.ViewHolder holder, int position) {

        UserInfoData userInfoData = userInfoDataArrayList.get(position);

        String photo = userInfoData.getUser_photo(); // 유저 프로필 사진
        String name = userInfoData.getUser_name(); // 유저 닉네임
        String message = userInfoData.getMessage(); // 리더 이름이 message에 들어올것.
        String user_email = userInfoData.getUser_email(); // 유저 이메일
        Log.d(TAG, "onBindViewHolder: photo =" + photo);
        Log.d(TAG, "onBindViewHolder: name =" + name);
        Log.d(TAG, "onBindViewHolder: message =" + message);
        Log.d(TAG, "onBindViewHolder: user_email =" + user_email);

        holder.tx_name.setText(name); // 유저 이름 세팅하기

        if(photo.equals("basic"))
        {
            // 유저 프로필이 기본이미지라는 뜻
            holder.img.setImageResource(R.drawable.user_img);
        }else{
            // 유저 프로필이 사용자가 직접 설정한 사진이라는 뜻
            String url = "http://3.36.174.137/UserProfileImg/" + photo;

            Glide.with(holder.itemView.getContext()).load(url).apply(new RequestOptions().centerCrop()).into(holder.img);

        }

        // 현재 유저가 리더인지 아닌지 구분하기
        // 리더를 제일 위 상단에 세팅해야 한다.
        if(message.equals(user_email)){
            // 만약 해당 유저가 리더라면
            Log.d(TAG, "onBindViewHolder: 리더임");
            holder.crown.setVisibility(View.VISIBLE);
        }else{
            // 만약 해당 유저가 리더가 아니라면
            Log.d(TAG, "onBindViewHolder: 리더 아님");
            holder.crown.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return userInfoDataArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView img, crown;
        public TextView tx_name;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            this.img = itemView.findViewById(R.id.img);
            this.crown = itemView.findViewById(R.id.crown);
            this.tx_name = itemView.findViewById(R.id.tx_name);
        }
    }
}

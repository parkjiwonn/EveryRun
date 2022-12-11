package com.example.everyrun.Community.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.everyrun.R;
import com.example.everyrun.RetrofitData.JoinData;
import com.example.everyrun.RetrofitData.UserInfoData;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class JoinListAdapter  extends RecyclerView.Adapter<JoinListAdapter.ViewHolder>{

    private final ArrayList<JoinData> joinDataArrayList;
    private Context context;
    int pos;

    private final String TAG = this.getClass().getSimpleName();

    public JoinListAdapter(ArrayList<JoinData> joinDataArrayList, Context context) {
        this.joinDataArrayList = joinDataArrayList;
        this.context = context;
    }

    //-----------------------------------Custom Click Listener-----------------------
    public interface OnItemClickListener{
        void onAcceptClick(View v, int pos); // 수락 버튼 클릭 리스너
        void onDenyClick(View v, int pos); // 거절 버튼 클릭 리스너
    }
    // 커스텀 리스너 인터페이스 정의

    private JoinListAdapter.OnItemClickListener cListener = null;
    // 전달된 객체를 저장할 변수 cListener 추가

    public void setOnItemClickListener(OnItemClickListener listener){
        this.cListener = listener;
    }
    // 커스텀 리스너 객체를 전달하는 메서드

    //-----------------------------------Custom Click Listener-----------------------

    @NonNull
    @Override
    public JoinListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "태그 onCreateViewHolder 들어옴");

        Context hcontext = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) hcontext.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
        // layoutinflater는 xml에 미리 정의해둔 툴을 실제 메모리에 올려준다. inflater는 부풀리다라는 의미

        View view = inflater.inflate (R.layout.item_joinlist, parent, false);
        // Xml에 덩의된 resource를 view 객체로 반환해주는 역할을 한다.
        // 레이아웃정의부에서 실행되고 메모리 로딩이 돼 화면에 띄워주는 역할.


        //false는 바로 인플레이션 하지 x, true는 바로 인플에이션 한다.
        JoinListAdapter.ViewHolder hvh = new JoinListAdapter.ViewHolder(view);
        // 뷰홀더 생성 , 리턴값이 뷰홀더이다.
        return hvh;
    }

    @Override
    public void onBindViewHolder(@NonNull JoinListAdapter.ViewHolder holder, int position) {

        JoinData joinData = joinDataArrayList.get(position);

        String photo = joinData.getUser_photo();
        String name = joinData.getUser_name();
        String join_date = joinData.getJoin_date();

        if(photo.equals("basic")) {
            // 유저 이미지가 기본 이미지 일 때
            holder.img.setImageResource(R.drawable.user_img);
        }else{
            // 유저 프로필이 사용자가 직접 설정한 사진이라는 뜻
            String url = "http://3.36.174.137/UserProfileImg/" + photo;

            Glide.with(holder.itemView.getContext()).load(url).apply(new RequestOptions().centerCrop()).into(holder.img);
        }

        holder.tx_name.setText(name);
        holder.tx_date.setText(join_date);

    }

    @Override
    public int getItemCount() {
        return joinDataArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView img;
        public TextView tx_name, tx_date;
        public Button btn_accept, btn_deny;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            this.img = itemView.findViewById(R.id.img);
            this.tx_name = itemView.findViewById(R.id.tx_name);
            this.tx_date = itemView.findViewById(R.id.tx_date);
            this.btn_accept = itemView.findViewById(R.id.btn_accept);
            this.btn_deny = itemView.findViewById(R.id.btn_deny);

            // 가입 신청 수락 버튼 클릭리스너
            btn_accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = getAdapterPosition();

                    if(pos != RecyclerView.NO_POSITION)
                    {
                        if(cListener != null)
                        {
                            cListener.onAcceptClick(view, pos);
                        }
                    }
                }
            });

            // 가입 신청 거절 버튼 클릭리스너
            btn_deny.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = getAdapterPosition();

                    if(pos != RecyclerView.NO_POSITION)
                    {
                        if(cListener != null)
                        {
                            cListener.onDenyClick(view, pos);
                        }
                    }
                }
            });
        }
    }
}

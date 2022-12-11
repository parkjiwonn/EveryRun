package com.example.everyrun.Community;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.everyrun.R;
import com.example.everyrun.UserProfile.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class CrewBottomSheetDialog extends BottomSheetDialogFragment implements View.OnClickListener{

    // 초기변수 설정
    private View view;
    // 인터페이스 변수
    private CrewBottomSheetDialog.BottomSheetListener mListener;
    // 바텀시트 숨기기 버튼
    private TextView tx_camera, tx_gallery, tx_basic, tx_cancel;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.crew_bottom_sheet_dialog, container, false);

        mListener = (CrewBottomSheetDialog.BottomSheetListener) getContext();

        tx_camera = view.findViewById(R.id.tx_camara);
        tx_gallery = view.findViewById(R.id.tx_gallery);
        tx_basic = view.findViewById(R.id.tx_basic);
        tx_cancel = view.findViewById(R.id.tx_cancel);

        tx_camera.setOnClickListener(this);
        tx_gallery.setOnClickListener(this);
        tx_basic.setOnClickListener(this);
        tx_cancel.setOnClickListener(this);


        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tx_camara:
                mListener.onButtonClicked(1);
                dismiss();
                break;

            case R.id.tx_gallery:
                mListener.onButtonClicked(2);
                dismiss();
                break;

            case R.id.tx_basic:
                mListener.onButtonClicked(3);
                dismiss();
                break;

            case R.id.tx_cancel:
                mListener.onButtonClicked(4);
                dismiss();
                break;
        }
    }

    // 부모 액티비티와 연결하기위한 인터페이스
    public interface BottomSheetListener {
        void onButtonClicked(int option);
    }

}

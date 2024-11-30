package com.example.doorlock.visitor;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.doorlock.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

//출입기록 리스트
public class VisitorListAdapter extends ArrayAdapter<Visitors> {
    private static final int LAYOUT_RESOURCE_ID = R.layout.list_visitors; //list_visitors.xml과 연결
    private Context mContext; //선택한 Activity에 대한 Context를 가져오기 위한 변수
    private List<Visitors> mItemList; //Visitors.java 객체의 정보를 가져와 리스트 생성

    /* 출입 기록 리스트 어댑터에 연결 */
    public VisitorListAdapter(Context a_context) {
        super(a_context, LAYOUT_RESOURCE_ID);

        mContext = a_context;
        mItemList = new ArrayList<>();
    }

    /*리스트에 아이템 추가*/
    public void setListItem(List<Visitors> a_itemList) {
        mItemList.addAll(a_itemList);
    }

    /*리스트 position 반환*/
    @Override
    public long getItemId(int position) {
        return position;
    }

    /*리스트 사이즈 반환*/
    @Override
    public int getCount() {
        return mItemList.size();
    }

    /*Visitors.java의 Item GET*/
    @Nullable
    @Override
    public Visitors getItem(int position) {
        return mItemList.get(position);
    }

    /*출입 기록 각각의 아이템 매칭 함수*/
    public View getView(int a_position, View a_convertView, ViewGroup a_parent) {
        GridItemViewHolder viewHolder;
        if (a_convertView == null) {
            a_convertView = LayoutInflater.from(mContext).inflate(LAYOUT_RESOURCE_ID, a_parent, false);

            viewHolder = new GridItemViewHolder(a_convertView);
            a_convertView.setTag(viewHolder);
        } else {
            viewHolder = (GridItemViewHolder) a_convertView.getTag();
        }
        final Visitors countryItem = mItemList.get(a_position);
        //Firebase 이미지 로딩
        Glide.with(mContext)
                .load(countryItem.getImgUri())
                .into(viewHolder.ivIcon);

        /*데이터 베이스에서 받아온 시간을 "yyyy-MM-dd HH:mm:ss" 형태로 출력*/
        try {
            Date date = new SimpleDateFormat("yyyyMMddHHmmss").parse(countryItem.getmStrName());
            String resultTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
            // 시간 text 설정
            viewHolder.tvName.setText(resultTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        /*등록된 사용자 구분*/
        //등록된 사용자가 아닐 경우
        if (countryItem.getmStrFy().equals("false")) {
            //시간 text 빨간색
            viewHolder.tvName.setTextColor(Color.parseColor("#ff0000"));
        } else { //등록된 사용자일 경우
            //시간 text 검정색
            viewHolder.tvName.setTextColor(Color.parseColor("#000000"));
        }

        return a_convertView;
    }

    /*GridView 정렬*/
    //출입 기록을 내림차순으로 정렬
    public void setSortASC() {
        //오름차순 정렬
        Collections.sort(mItemList, new Comparator<Visitors>() {
            //출입 기록 시간인 "yyyyMMddHHmmss"을 long형 숫자로 비교하여 내림차순 정렬
            @Override
            public int compare(Visitors visitors, Visitors visitors2) {
                if (Long.parseLong(visitors.getmStrName()) > Long.parseLong(visitors2.getmStrName())) {
                    return 1;
                } else if (Long.parseLong(visitors.getmStrName()) < Long.parseLong(visitors2.getmStrName())) {
                    return -1;
                }
                return 0;
            }
        }.reversed()); //오름차순 정렬 반대로 => 내림차순
    }

    /*출입기록 리스트가 어댑터에 연결된 후, ViewHolder 생성*/
    class GridItemViewHolder {
        public ImageView ivIcon; //list_visitors의 이미지
        public TextView tvName; //list_visitors의 텍스트

        //list_visitors.xml의 이미지, 텍스트 변수 설정
        public GridItemViewHolder(View a_convertView) {
            ivIcon = a_convertView.findViewById(R.id.iv_icon);
            tvName = a_convertView.findViewById(R.id.tv_name);
        }
    }

}
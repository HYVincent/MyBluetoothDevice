package com.example.vincent.mybluetoothdevice;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.vincent.mybluetoothdevice.utils.DateUtils;

import java.util.List;

/**
 * @author Vincent QQ:1032006226
 * @version v1.0
 * @name MyBluetoothDevice
 * @page com.example.vincent.mybluetoothdevice
 * @class describe
 * @date 2018/1/23 18:05
 */

public class MsgAdapter extends RecyclerView.Adapter<MsgAdapter.MsgViewHolder> {

    private Context mContext;
    private List<DataEntity> data;


    public MsgAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void setData(List<DataEntity> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @Override
    public MsgViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(mContext == null){
            mContext = parent.getContext();
        }
        return new MsgViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_layout_data,parent,false));
    }

    @Override
    public void onBindViewHolder(MsgViewHolder holder, int position) {
        if(position < data.size()){
            DataEntity dataEntity = data.get(position);
            if(dataEntity == null){
                return;
            }
            switch (dataEntity.getType()){
                case 0:
                    holder.tvType.setText("收到:");
                    holder.tvType.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimaryDark));
                    holder.tvMsg.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimaryDark));
                    break;
                case 1:
                    holder.tvType.setText("发送:");
                    holder.tvType.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimaryDark));
                    holder.tvMsg.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimaryDark));
                    break;
                case 2:
                    holder.tvType.setText("状态:");
                    holder.tvType.setTextColor(ContextCompat.getColor(mContext,R.color.colorAccent));
                    holder.tvMsg.setTextColor(ContextCompat.getColor(mContext,R.color.colorAccent));
                    break;
                default:
                    holder.tvType.setText("其他:");
                    holder.tvMsg.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimaryDark));
                    break;
            }
            holder.tvTime.setText(DateUtils.getDateString(DateUtils.DATE_FORMAT_ALL2,dataEntity.getTime()));
            holder.tvMsg.setText(dataEntity.getMsg());
        }
    }

    @Override
    public int getItemCount() {
        return data == null?0:data.size();
    }

    class MsgViewHolder extends RecyclerView.ViewHolder{

        private TextView tvType,tvTime,tvMsg;

        public MsgViewHolder(View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.data_tv_type);
            tvTime = itemView.findViewById(R.id.data_tv_time);
            tvMsg = itemView.findViewById(R.id.data_tv_conent);
        }
    }
}

package com.example.vincent.mybluetoothdevice;

import android.content.Context;
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
        DataEntity dataEntity = data.get(position);
        if(dataEntity.getType() == 0){
            holder.tvType.setText("收:");
        }else {
            holder.tvType.setText("发:");
        }
        holder.tvTime.setText(DateUtils.getDateString(DateUtils.DATE_FORMAT_ALL2,dataEntity.getTime()));
        holder.tvMsg.setText(dataEntity.getMsg());
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

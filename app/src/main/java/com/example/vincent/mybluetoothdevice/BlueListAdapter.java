package com.example.vincent.mybluetoothdevice;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.vincent.mybluetoothdevice.bluetooth.BluetoothEntity;
import com.example.vincent.mybluetoothdevice.bluetooth.BluetoothUtils;

import java.util.List;

/**
 * @author Vincent QQ:1032006226
 * @version v1.0
 * @name MyBluetoothDevice
 * @page com.example.vincent.mybluetoothdevice
 * @class describe
 * @date 2018/1/22 10:14
 */

public class BlueListAdapter extends RecyclerView.Adapter<BlueListAdapter.BlueListViewHolder> {

    private Context mContext;
    private List<BluetoothEntity> datas;
    private BlueListItemOnClickListener listItemOnClickListener;
    private ProgressDialog dialog;

    public BlueListAdapter(Context mContext) {
        this.mContext = mContext;
        dialog = new ProgressDialog(mContext);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置进度条的形式为圆形转动的进度条
        dialog.setCancelable(true);// 设置是否可以通过点击Back键取消
        dialog.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条
    }

    public void setDatas(List<BluetoothEntity> datas) {
        this.datas = datas;
        notifyDataSetChanged();
    }

    public void setListItemOnClickListener(BlueListItemOnClickListener listItemOnClickListener) {
        this.listItemOnClickListener = listItemOnClickListener;
    }

    @Override
    public BlueListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(mContext == null){
            mContext = parent.getContext();
        }
        return new BlueListViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_layout_bluethooth,parent,false));
    }

    @Override
    public void onBindViewHolder(BlueListViewHolder holder, final int position) {
        BluetoothEntity device = datas.get(position);
        holder.tvName.setText(device.getName());
        holder.tvAddress.setText(device.getAddress());
        switch (device.getStatus()){
            case 0:
                holder.tvStatus.setText("");
                break;
            case BluetoothUtils.BLUETOOTH_STATUS_CONNECTING:
                holder.tvStatus.setText("正在连接蓝牙..");
                dialog.setMessage("正在连接蓝牙..");
                dialog.show();
                break;
            case BluetoothUtils.BLUETOOTH_STATUS_CONNECT_SUCCESS:
                holder.tvStatus.setText("蓝牙连接成功");
                dialog.dismiss();
                break;
            case BluetoothUtils.BLUETOOTH_STATUS_CONNECT_FAIL:
                dialog.dismiss();
                holder.tvStatus.setText("蓝牙连接失败");
                break;
            default:
                dialog.dismiss();
                break;
        }
        if(datas.size() -1 == position){
            holder.line.setVisibility(View.GONE);
        }else {
            holder.line.setVisibility(View.VISIBLE);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listItemOnClickListener.onItemClick(view,position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return datas == null ?0:datas.size();
    }

    class BlueListViewHolder extends RecyclerView.ViewHolder{

        private TextView tvName,tvAddress,tvStatus;
        private View line;

        public BlueListViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.item_bluetooth_name);
            tvAddress = itemView.findViewById(R.id.item_bluetooth_address);
            line = itemView.findViewById(R.id.item_bluetooth_line);
            tvStatus =itemView.findViewById(R.id.item_bluetooth_status);
        }
    }

    public interface BlueListItemOnClickListener{
        void onItemClick(View view,int position);
    }

}

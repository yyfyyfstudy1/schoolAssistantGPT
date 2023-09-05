package comp5216.sydney.edu.au.learn.recyclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import comp5216.sydney.edu.au.learn.R;

public class waterFallAdapter extends RecyclerView.Adapter<waterFallAdapter.LinearViewHolder> {

    private Context mcontext;
    private OnItemClickListener mlistener;

    public waterFallAdapter(Context context, OnItemClickListener listener){
        this.mcontext = context;
        this.mlistener = listener;
    }
    @NonNull
    @Override
    public waterFallAdapter.LinearViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        return new LinearViewHolder(LayoutInflater.from(mcontext).inflate(R.layout.layout_water_recycler_item, parent, false));

    }

    @Override
    public void onBindViewHolder(@NonNull LinearViewHolder holder, int position) {




        if(position % 2 !=0){
            String imageUrl = "https://img.freepik.com/free-vector/isolated-rose-flower-line-art-with-leaf-clipart_41066-2958.jpg?size=626&ext=jpg"; // 替换为您想要加载的网络图片URL

            Picasso.get().load(imageUrl).into(holder.imageView);
        }else {

            String imageUrl = "https://photo.16pic.com/00/13/89/16pic_1389821_b.jpg"; // 替换为您想要加载的网络图片URL
            Picasso.get().load(imageUrl).into(holder.imageView);


        }
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               mlistener.onClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return 30;
    }

    class LinearViewHolder extends RecyclerView.ViewHolder{

        private ImageView imageView;

        public LinearViewHolder(View itemView){
            super(itemView);
            imageView = itemView.findViewById(R.id.iv);
        }
    }

    public interface OnItemClickListener{
        void onClick(int pos);
    }
}

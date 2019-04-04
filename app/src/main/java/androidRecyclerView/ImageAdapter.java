package androidRecyclerView;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.List;

import john.example.bluetoothproject.R;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder>{

    private List<Image> imageList;

    public static final int SENDER = 0;
    public static final int RECIPIENT = 1;

    public ImageAdapter(Context context, List<Image> images) {
        imageList = images;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView mImageView;

        public ViewHolder(LinearLayout v) {
            super(v);
            mImageView = v.findViewById(R.id.image);
        }
    }

    @Override
    public ImageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 1) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_item_purple, parent, false);
            ImageAdapter.ViewHolder vh = new ImageAdapter.ViewHolder((LinearLayout) v);
            return vh;
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_item_green, parent, false);
            ImageAdapter.ViewHolder vh = new ImageAdapter.ViewHolder((LinearLayout) v);
            return vh;
        }
    }

    public void remove(int pos) {
        imageList.remove(pos);
        notifyItemRemoved(pos);
        notifyItemRangeChanged(pos, imageList.size());

    }

    @Override
    public void onBindViewHolder(ImageAdapter.ViewHolder holder, final int position) {
        holder.mImageView.setImageBitmap(imageList.get(position).getBitmap());
        holder.mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                remove(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        Image image = imageList.get(position);

        if (image.getSenderName().equals("Me")) {
            return SENDER;
        } else {
            return RECIPIENT;
        }

    }

}

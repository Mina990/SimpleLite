package com.creativetrends.simple.app.adapters;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.creativetrends.simple.app.lite.R;
import com.creativetrends.simple.app.utils.FileTypeUtils;
import com.creativetrends.simple.app.utils.FileUtils;
import com.creativetrends.simple.app.utils.ThemeUtils;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class AdapterDownloads extends RecyclerView.Adapter<AdapterDownloads.ViewHolder> {
    private final Context context;
    private final Activity activity;
    private final ArrayList<UserFiles> filesList;

    public AdapterDownloads(Activity activity, Context context, ArrayList<UserFiles> filesList) {
        this.activity = activity;
        this.context = context;
        this.filesList = filesList;
    }

    @NonNull
    @Override
    public AdapterDownloads.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_downloads, parent, false);
        return new AdapterDownloads.ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull AdapterDownloads.ViewHolder holder, final int position) {
        final UserFiles files = filesList.get(position);
        final Uri uri = Uri.parse(files.getUri().toString());
        final File file = new File(Objects.requireNonNull(uri.getPath()));
        FileTypeUtils.FileType fileType = FileTypeUtils.getFileType(file);
        holder.mVid.setImageResource(fileType.getIcon());
        Glide.with(context)
                .load(files.getUri())
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .thumbnail(0.5f)
                .centerCrop()
                .error(fileType.getIcon())
                .into(holder.mFileImage);
        holder.mFileTitle.setText(file.getName());
        holder.mSize.setText(context.getResources().getString(R.string.sizer_size, FileUtils.getReadableFileSize(file.length()), readableFileSize()));
        Locale locale = Locale.getDefault();
        holder.mFileSubtitle.setText(new SimpleDateFormat("MMM d, yyyy, h:mm a", Locale.forLanguageTag(locale.toLanguageTag())).format(new Date(file.lastModified())));
        try {
            ContentResolver cR = context.getContentResolver();
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            String type = mime.getExtensionFromMimeType(cR.getType(uri));
            holder.mFileType.setText(type);
        }catch (NullPointerException p){
            p.printStackTrace();
        }
        holder.mCard.setCardBackgroundColor(ThemeUtils.getMenu(context));

        holder.mFile_Holder.setOnClickListener(v -> handleFileClicked(file));

        holder.share.setOnClickListener(view -> {
            final String path = filesList.get(position).getPath();
            final File file1 = new File(path);
            Uri files1 = FileProvider.getUriForFile(context, context.getString(R.string.auth), file1);
            Intent share = new Intent(Intent.ACTION_SEND);
            share.putExtra(Intent.EXTRA_TEXT, "Share");
            share.putExtra(Intent.EXTRA_STREAM, files1);
            share.setDataAndType(files1, getMimeType(files1));
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(share);
        });
        holder.trash.setOnClickListener(view -> {
            final String pathz = filesList.get(position).getPath();
            final File file1z = new File(pathz);
            MaterialAlertDialogBuilder delete = new MaterialAlertDialogBuilder(activity);
            delete.setTitle(context.getString(R.string.delete));
            delete.setMessage(context.getString(R.string.delete_message, file.getName()));
            delete.setNegativeButton(context.getString(R.string.cancel), null);
            delete.setPositiveButton(context.getString(R.string.ok), (dialog, which) -> {
                try {
                    if (file1z.exists()) {
                        boolean del = file1z.delete();
                        filesList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, filesList.size());
                        notifyDataSetChanged();
                        Toast.makeText(context, context.getString(R.string.removed_from_downloads, file.getName()), Toast.LENGTH_SHORT).show();
                        if (del) {
                            MediaScannerConnection.scanFile(context, new String[]{pathz, pathz}, null, (path1, uri1) -> {
                            });
                        }
                    }
                    dialog.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            delete.show();
        });

    }


    private void handleFileClicked(final File clickedFile) {
        try {
            Uri files = FileProvider.getUriForFile(context, context.getString(R.string.auth), clickedFile);
            Intent newIntent = new Intent(Intent.ACTION_VIEW);
            newIntent.setDataAndType(files, getMimeType(files));
            newIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(newIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, context.getResources().getString(R.string.error)+" "+System.currentTimeMillis(), Toast.LENGTH_SHORT).show();
        } catch (Exception p) {
            p.printStackTrace();
            Toast.makeText(context, context.getResources().getString(R.string.error)+" "+System.currentTimeMillis(), Toast.LENGTH_SHORT).show();
        }
    }

    private String getMimeType(Uri uri) {
        String mimeType;
        if (Objects.equals(uri.getScheme(), ContentResolver.SCHEME_CONTENT)) {
            ContentResolver cr = context.getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType;
    }


    @Override
    public int getItemCount() {
        return filesList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView mFileImage;
        private final ImageView mVid;
        private final TextView mFileTitle;
        private final TextView mFileSubtitle;
        private final TextView mSize;
        private final TextView mFileType;
        private final RelativeLayout mFile_Holder;
        private final MaterialCardView mCard;
        private final ImageView share;
        private final ImageView trash;

        ViewHolder(View itemView) {
            super(itemView);
            mFileImage = itemView.findViewById(R.id.item_file_image);
            mFileTitle = itemView.findViewById(R.id.item_file_title);
            mFileSubtitle = itemView.findViewById(R.id.item_file_subtitle);
            mFileType = itemView.findViewById(R.id.item_file_type);
            mSize = itemView.findViewById(R.id.item_file_size);
            mFile_Holder = itemView.findViewById(R.id.file_holder);
            mCard = itemView.findViewById(R.id.download_card);
            mVid = itemView.findViewById(R.id.video_play);
            share = itemView.findViewById(R.id.share_down);
            trash = itemView.findViewById(R.id.trash_down);
        }
    }



    public static String readableFileSize() {
        long availableSpace;
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        availableSpace = stat.getBlockSizeLong() * stat.getBlockCountLong();
        if (availableSpace <= 0) return "0";
        final String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(availableSpace) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(availableSpace / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }


}

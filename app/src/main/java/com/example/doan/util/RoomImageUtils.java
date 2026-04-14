package com.example.doan.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.util.Size;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.doan.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

/**
 * Ảnh phòng: tên drawable trong project, đường dẫn file nội bộ, hoặc content:/ (tạm).
 */
public final class RoomImageUtils {

    private RoomImageUtils() {}

    public static String copyUriToRoomImagesDir(Context context, Uri uri) throws IOException {
        return copyUriToSubdir(context, uri, "room_images", "room_");
    }

    /** Ảnh nền trang chủ — đường dẫn tuyệt đối lưu trong app. */
    public static String copyUriToHomeHeroDir(Context context, Uri uri) throws IOException {
        return copyUriToSubdir(context, uri, "home_hero", "hero_");
    }

    /** Ảnh đại diện người dùng (Cài đặt). */
    public static String copyUriToProfileImagesDir(Context context, Uri uri) throws IOException {
        return copyUriToSubdir(context, uri, "profile_images", "profile_");
    }

    /** Ảnh nền vùng nội dung app (cấu hình trong Trang chủ — cài đặt). */
    public static String copyUriToAppBgDir(Context context, Uri uri) throws IOException {
        return copyUriToSubdir(context, uri, "app_bg", "bg_");
    }

    /**
     * Drawable cho {@code View#setBackground}: tên drawable, đường dẫn file, hoặc {@code content:/}.
     */
    @Nullable
    public static Drawable loadDrawableForBackground(Context ctx, String ref) {
        if (ref == null || ref.trim().isEmpty()) {
            return null;
        }
        String s = ref.trim();
        try {
            if (s.startsWith("content:")) {
                Uri u = Uri.parse(s);
                try (InputStream in = ctx.getContentResolver().openInputStream(u)) {
                    if (in != null) {
                        Bitmap bm = BitmapFactory.decodeStream(in);
                        if (bm != null) {
                            return new BitmapDrawable(ctx.getResources(), bm);
                        }
                    }
                }
                return null;
            }
            if (s.startsWith("file:")) {
                String path = Uri.parse(s).getPath();
                if (path != null) {
                    File f = new File(path);
                    if (f.isFile()) {
                        return decodeSampledBitmapDrawable(ctx, f.getAbsolutePath());
                    }
                }
                return null;
            }
            File f = new File(s);
            if (f.isFile()) {
                return decodeSampledBitmapDrawable(ctx, f.getAbsolutePath());
            }
            int res = ctx.getResources().getIdentifier(s, "drawable", ctx.getPackageName());
            if (res != 0) {
                return ContextCompat.getDrawable(ctx, res);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    @Nullable
    private static BitmapDrawable decodeSampledBitmapDrawable(Context ctx, String path) {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, o);
        if (o.outWidth <= 0 || o.outHeight <= 0) {
            return decodeBitmapDrawableFromFileImageDecoder(ctx, path);
        }
        int maxDim = 2048;
        int sample = 1;
        while (Math.max(o.outWidth, o.outHeight) / sample > maxDim) {
            sample *= 2;
        }
        o.inJustDecodeBounds = false;
        o.inSampleSize = sample;
        Bitmap bm = BitmapFactory.decodeFile(path, o);
        if (bm == null) {
            return decodeBitmapDrawableFromFileImageDecoder(ctx, path);
        }
        return new BitmapDrawable(ctx.getResources(), bm);
    }

    /** HEIF/WEBP và một số định dạng — BitmapFactory không đo được bounds. */
    @Nullable
    private static BitmapDrawable decodeBitmapDrawableFromFileImageDecoder(Context ctx, String path) {
        Bitmap bm = decodeBitmapFileImageDecoder(ctx, path, null);
        if (bm == null) {
            return null;
        }
        return new BitmapDrawable(ctx.getResources(), bm);
    }

    private static final int DECODE_MAX_SIDE = 2048;

    @Nullable
    private static Bitmap decodeBitmapFileImageDecoder(Context ctx, String path, @Nullable ImageView imageView) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            return null;
        }
        try {
            ImageDecoder.Source src = ImageDecoder.createSource(new File(path));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                return ImageDecoder.decodeBitmap(src, (decoder, info, source) -> {
                    Size sz = info.getSize();
                    int w = sz.getWidth();
                    int h = sz.getHeight();
                    int max = DECODE_MAX_SIDE;
                    if (imageView != null && imageView.getWidth() > 0 && imageView.getHeight() > 0) {
                        max = Math.min(DECODE_MAX_SIDE, Math.max(imageView.getWidth(), imageView.getHeight()) * 2);
                    }
                    if (w > max || h > max) {
                        float scale = Math.min((float) max / w, (float) max / h);
                        decoder.setTargetSize(Math.round(w * scale), Math.round(h * scale));
                    }
                });
            }
            return ImageDecoder.decodeBitmap(src);
        } catch (Exception e) {
            return null;
        }
    }

    private static String copyUriToSubdir(Context context, Uri uri, String subdir, String prefix) throws IOException {
        File dir = new File(context.getFilesDir(), subdir);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Không tạo được thư mục ảnh");
        }
        String name = prefix + System.currentTimeMillis() + ".jpg";
        File out = new File(dir, name);
        boolean written = false;
        Bitmap bm = null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.Source src = ImageDecoder.createSource(context.getContentResolver(), uri);
                bm = ImageDecoder.decodeBitmap(src);
            } else {
                try (InputStream in = context.getContentResolver().openInputStream(uri)) {
                    if (in != null) {
                        bm = BitmapFactory.decodeStream(in);
                    }
                }
            }
        } catch (Exception ignored) {
            bm = null;
        }
        if (bm != null) {
            try (OutputStream os = new FileOutputStream(out)) {
                written = bm.compress(Bitmap.CompressFormat.JPEG, 92, os);
                os.flush();
            } finally {
                bm.recycle();
            }
        }
        if (!written) {
            try (InputStream in = context.getContentResolver().openInputStream(uri);
                 OutputStream os = new FileOutputStream(out)) {
                if (in == null) {
                    throw new IOException("Không đọc được ảnh");
                }
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) != -1) {
                    os.write(buf, 0, n);
                }
            }
        }
        if (!out.exists() || out.length() == 0L) {
            //noinspection ResultOfMethodCallIgnored
            out.delete();
            throw new IOException("Sao chép ảnh thất bại (0 byte). Hãy thử chọn ảnh khác hoặc cấp quyền truy cập ảnh.");
        }
        return out.getAbsolutePath();
    }

    /**
     * Xóa file ảnh đã lưu trong thư mục app (khi chọn ảnh mới thay thế).
     * Chỉ xóa nếu đường dẫn nằm trong {@code filesDir/home_hero}, {@code app_bg}, {@code room_images}, {@code profile_images}.
     */
    public static void deleteAppStoredImageIfReplaced(Context ctx, @Nullable String oldPath, @Nullable String newPath) {
        if (oldPath == null || oldPath.trim().isEmpty()) {
            return;
        }
        String o = oldPath.trim();
        if (newPath != null && o.equals(newPath.trim())) {
            return;
        }
        if (!isAppManagedImageFile(ctx, o)) {
            return;
        }
        File f = new File(o);
        if (f.isFile()) {
            //noinspection ResultOfMethodCallIgnored
            f.delete();
        }
    }

    private static boolean isAppManagedImageFile(Context ctx, String path) {
        String[] subs = {"home_hero", "app_bg", "room_images", "profile_images"};
        for (String sub : subs) {
            if (isUnderFilesSubdir(ctx, path, sub)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isUnderFilesSubdir(Context ctx, String path, String subdir) {
        try {
            File base = new File(ctx.getFilesDir(), subdir);
            File f = new File(path);
            if (!f.isAbsolute()) {
                return false;
            }
            String p = f.getCanonicalPath();
            String b = base.getCanonicalPath();
            return p.startsWith(b + File.separator);
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Giải mã bitmap nhỏ để lấy màu (Palette) — không dùng cho hiển thị full quality.
     */
    @Nullable
    public static Bitmap decodeBitmapForPalette(Context context, String ref, int maxSide) {
        if (ref == null || ref.trim().isEmpty()) {
            return null;
        }
        String s = ref.trim();
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        decodeBoundsOnly(context, s, opts);
        if (opts.outWidth <= 0 || opts.outHeight <= 0) {
            return null;
        }
        int maxDim = Math.max(opts.outWidth, opts.outHeight);
        int sample = 1;
        while (maxDim / sample > maxSide) {
            sample *= 2;
        }
        opts = new BitmapFactory.Options();
        opts.inSampleSize = sample;
        return decodeFull(context, s, opts);
    }

    private static void decodeBoundsOnly(Context ctx, String s, BitmapFactory.Options opts) {
        try {
            if (s.startsWith("content:")) {
                Uri u = Uri.parse(s);
                try (java.io.InputStream in = ctx.getContentResolver().openInputStream(u)) {
                    if (in != null) {
                        BitmapFactory.decodeStream(in, null, opts);
                    }
                }
                return;
            }
            if (s.startsWith("file:")) {
                String path = Uri.parse(s).getPath();
                if (path != null) {
                    BitmapFactory.decodeFile(path, opts);
                }
                return;
            }
            File f = new File(s);
            if (f.isFile()) {
                BitmapFactory.decodeFile(f.getAbsolutePath(), opts);
                return;
            }
            int res = ctx.getResources().getIdentifier(s, "drawable", ctx.getPackageName());
            if (res != 0) {
                BitmapFactory.decodeResource(ctx.getResources(), res, opts);
            }
        } catch (Exception ignored) {
        }
    }

    @Nullable
    private static Bitmap decodeFull(Context ctx, String s, BitmapFactory.Options opts) {
        try {
            if (s.startsWith("content:")) {
                Uri u = Uri.parse(s);
                try (java.io.InputStream in = ctx.getContentResolver().openInputStream(u)) {
                    if (in != null) {
                        return BitmapFactory.decodeStream(in, null, opts);
                    }
                }
                return null;
            }
            if (s.startsWith("file:")) {
                String path = Uri.parse(s).getPath();
                if (path != null) {
                    return BitmapFactory.decodeFile(path, opts);
                }
                return null;
            }
            File f = new File(s);
            if (f.isFile()) {
                return BitmapFactory.decodeFile(f.getAbsolutePath(), opts);
            }
            int res = ctx.getResources().getIdentifier(s, "drawable", ctx.getPackageName());
            if (res != 0) {
                return BitmapFactory.decodeResource(ctx.getResources(), res, opts);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /** Hiển thị ảnh: drawable tên, file tuyệt đối, hoặc content:/ */
    public static void loadInto(ImageView imageView, String ref) {
        if (ref == null || ref.trim().isEmpty()) {
            imageView.setImageResource(R.drawable.bg_guest_hero);
            return;
        }
        String s = ref.trim();
        Context ctx = imageView.getContext();
        if (s.startsWith("content:")) {
            try {
                Uri u = Uri.parse(s);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    try {
                        ImageDecoder.Source src = ImageDecoder.createSource(ctx.getContentResolver(), u);
                        Bitmap bm = ImageDecoder.decodeBitmap(src, (decoder, info, decSource) -> {
                            Size sz = info.getSize();
                            int w = sz.getWidth();
                            int h = sz.getHeight();
                            int max = DECODE_MAX_SIDE;
                            if (imageView.getWidth() > 0 && imageView.getHeight() > 0) {
                                max = Math.min(DECODE_MAX_SIDE, Math.max(imageView.getWidth(), imageView.getHeight()) * 2);
                            }
                            if (w > max || h > max) {
                                float scale = Math.min((float) max / w, (float) max / h);
                                decoder.setTargetSize(Math.round(w * scale), Math.round(h * scale));
                            }
                        });
                        if (bm != null) {
                            imageView.setImageBitmap(bm);
                            return;
                        }
                    } catch (Exception ignored) {
                    }
                }
                try (InputStream in = ctx.getContentResolver().openInputStream(u)) {
                    if (in != null) {
                        Bitmap bm = BitmapFactory.decodeStream(in);
                        if (bm != null) {
                            imageView.setImageBitmap(bm);
                            return;
                        }
                    }
                }
            } catch (Exception ignored) {
            }
            imageView.setImageResource(R.drawable.bg_guest_hero);
            return;
        }
        if (s.startsWith("file:")) {
            String path = Uri.parse(s).getPath();
            if (path != null) {
                File f = new File(path);
                if (f.isFile()) {
                    Bitmap bm = decodeBitmapForDisplay(ctx, f.getAbsolutePath(), imageView);
                    if (bm != null) {
                        imageView.setImageBitmap(bm);
                        return;
                    }
                    try {
                        imageView.setImageURI(Uri.fromFile(f));
                        if (imageView.getDrawable() != null) {
                            return;
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
            imageView.setImageResource(R.drawable.bg_guest_hero);
            return;
        }
        File f = new File(s);
        if (f.isFile()) {
            Bitmap bm = decodeBitmapForDisplay(ctx, f.getAbsolutePath(), imageView);
            if (bm != null) {
                imageView.setImageBitmap(bm);
                return;
            }
            try {
                imageView.setImageURI(Uri.fromFile(f));
                if (imageView.getDrawable() != null) {
                    return;
                }
            } catch (Exception ignored) {
            }
            imageView.setImageResource(R.drawable.bg_guest_hero);
            return;
        }
        int resId = resolveDrawableId(ctx, s);
        if (resId != 0) {
            imageView.setImageResource(resId);
        } else {
            imageView.setImageResource(R.drawable.bg_guest_hero);
        }
    }

    private static int resolveDrawableId(Context ctx, String s) {
        int res = ctx.getResources().getIdentifier(s.toLowerCase(Locale.ROOT), "drawable", ctx.getPackageName());
        if (res != 0) {
            return res;
        }
        if (!s.contains("/") && !s.contains("\\") && s.contains(".")) {
            String base = s.substring(0, s.lastIndexOf('.'));
            return ctx.getResources().getIdentifier(base.toLowerCase(Locale.ROOT), "drawable", ctx.getPackageName());
        }
        return 0;
    }

    /**
     * Giải mã ảnh hiển thị (giới hạn kích thước tránh OOM). Nếu view chưa đo, dùng cạnh tối đa 2048.
     */
    @Nullable
    private static Bitmap decodeBitmapForDisplay(Context ctx, String path, ImageView imageView) {
        int w = imageView.getWidth();
        int h = imageView.getHeight();
        int maxSide = 2048;
        if (w > 0 && h > 0) {
            maxSide = Math.min(2048, Math.max(w, h) * 2);
        }
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, o);
        if (o.outWidth <= 0 || o.outHeight <= 0) {
            return decodeBitmapFileImageDecoder(ctx, path, imageView);
        }
        int dim = Math.max(o.outWidth, o.outHeight);
        int sample = 1;
        while (dim / sample > maxSide) {
            sample *= 2;
        }
        o = new BitmapFactory.Options();
        o.inSampleSize = sample;
        try {
            Bitmap bm = BitmapFactory.decodeFile(path, o);
            if (bm == null) {
                return decodeBitmapFileImageDecoder(ctx, path, imageView);
            }
            return bm;
        } catch (OutOfMemoryError e) {
            o.inSampleSize *= 2;
            try {
                return BitmapFactory.decodeFile(path, o);
            } catch (OutOfMemoryError e2) {
                return decodeBitmapFileImageDecoder(ctx, path, imageView);
            }
        }
    }
}
